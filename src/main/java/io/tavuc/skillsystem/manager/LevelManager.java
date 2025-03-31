package io.tavuc.skillsystem.manager;

import io.tavuc.skillsystem.api.events.PlayerLevelUpEvent;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles player leveling and experience progression with configurable formulas.
 */
public class LevelManager {

    private final ConfigManager configManager;
    private final Plugin plugin;
    
    // Cache for exp requirements by level
    private final Map<Integer, Integer> expRequirementCache = new HashMap<>();
    
    /**
     * Constructs the level manager.
     *
     * @param plugin        The plugin instance
     * @param configManager The config manager
     */
    public LevelManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        loadExpCache();
    }
    
    /**
     * Preloads experience requirements for faster lookup.
     */
    private void loadExpCache() {
        expRequirementCache.clear();
        
        int baseRequirement = configManager.getInt("leveling.base-exp-requirement", 100);
        int maxLevel = configManager.getInt("leveling.max-level", 100);
        
        for (int level = 1; level <= maxLevel; level++) {
            expRequirementCache.put(level, calculateExpForLevel(level, baseRequirement));
        }
    }
    
    /**
     * Calculates the experience required for a specific level.
     *
     * @param level           The level
     * @param baseRequirement The base experience requirement
     * @return The experience required
     */
    private int calculateExpForLevel(int level, int baseRequirement) {
        String formula = configManager.getString("leveling.exp-formula", "linear");
        
        switch (formula.toLowerCase()) {
            case "exponential":
                double expBase = configManager.getDouble("leveling.exponential-base", 1.5);
                return (int) (baseRequirement * Math.pow(expBase, level - 1));
            
            case "quadratic":
                double quadFactor = configManager.getDouble("leveling.quadratic-factor", 0.5);
                return (int) (baseRequirement * (1 + quadFactor * (level - 1) * (level - 1)));
            
            case "linear":
            default:
                int increase = configManager.getInt("leveling.linear-increase", 50);
                return baseRequirement + (increase * (level - 1));
        }
    }
    
    /**
     * Returns the required experience for the next level.
     *
     * @param level The current level.
     * @return The required experience.
     */
    public int getRequiredExpForNextLevel(int level) {
        // Check cache first
        if (expRequirementCache.containsKey(level)) {
            return expRequirementCache.get(level);
        }
        
        // Calculate if not in cache
        int baseRequirement = configManager.getInt("leveling.base-exp-requirement", 100);
        return calculateExpForLevel(level, baseRequirement);
    }
    
    /**
     * Returns the number of stat points awarded per level.
     *
     * @param level The level reached.
     * @return The stat points.
     */
    public int getStatPointsForLevel(int level) {
        ConfigurationSection statPointsSection = 
                plugin.getConfig().getConfigurationSection("leveling.stat-points-per-level");
        
        if (statPointsSection != null) {
            // Look for specific tier ranges that include this level
            for (String key : statPointsSection.getKeys(false)) {
                if (key.contains("-")) {
                    String[] range = key.split("-");
                    int min = Integer.parseInt(range[0]);
                    int max = Integer.parseInt(range[1]);
                    
                    if (level >= min && level <= max) {
                        return statPointsSection.getInt(key, 5);
                    }
                }
            }
            
            // If no specific range matches, check for a default
            if (statPointsSection.contains("default")) {
                return statPointsSection.getInt("default", 5);
            }
        }
        
        // Fallback to global config
        return configManager.getInt("leveling.default-stat-points", 5);
    }
    
    /**
     * Adds experience to a player and handles level-up if applicable.
     *
     * @param player      The player.
     * @param playerStats The player's stats.
     * @param amount      The amount of experience to add.
     */
    public void addExperience(Player player, PlayerStats playerStats, int amount) {
        // Get the player's current level and experience
        int currentLevel = playerStats.getLevel();
        int currentExp = playerStats.getExperience();
        int maxLevel = configManager.getInt("leveling.max-level", 100);
        
        // Check if player is already at max level
        if (currentLevel >= maxLevel) {
            // Don't add experience if at max level
            return;
        }
        
        // Add the experience
        playerStats.setExperience(currentExp + amount);
        
        // Check for level up
        boolean leveledUp = false;
        while (playerStats.getExperience() >= getRequiredExpForNextLevel(playerStats.getLevel())) {
            // Reduce experience by the requirement
            playerStats.setExperience(playerStats.getExperience() - getRequiredExpForNextLevel(playerStats.getLevel()));
            
            // Increase level
            int newLevel = playerStats.getLevel() + 1;
            playerStats.setLevel(newLevel);
            leveledUp = true;
            
            // Award stat points
            int statPoints = getStatPointsForLevel(newLevel);
            playerStats.addUnspentPoints(statPoints);
            
            // Check if max level is reached
            if (newLevel >= maxLevel) {
                // Set exp to 0 at max level
                playerStats.setExperience(0);
                break;
            }
        }
        
        // If the player leveled up, fire event and play effects
        if (leveledUp) {
            int newLevel = playerStats.getLevel();
            
            // Fire the level up event
            PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, newLevel);
            Bukkit.getPluginManager().callEvent(event);
            
            // Play level up effects
            if (configManager.getBoolean("leveling.effects.enabled", true)) {
                // Send level up message
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("level", String.valueOf(newLevel));
                placeholders.put("points", String.valueOf(getStatPointsForLevel(newLevel)));
                
                String levelUpMessage = configManager.getMessage("level-up", placeholders);
                player.sendMessage(levelUpMessage);
                
                // Play sound
                String soundName = configManager.getString("leveling.effects.sound", "ENTITY_PLAYER_LEVELUP");
                try {
                    Sound sound = Sound.valueOf(soundName);
                    float volume = (float) configManager.getDouble("leveling.effects.volume", 1.0);
                    float pitch = (float) configManager.getDouble("leveling.effects.pitch", 1.0);
                    
                    player.playSound(player.getLocation(), sound, volume, pitch);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound name in config: " + soundName);
                }
                
                // Title message
                if (configManager.getBoolean("leveling.effects.title.enabled", true)) {
                    String title = configManager.getMessage("level-up-title", placeholders);
                    String subtitle = configManager.getMessage("level-up-subtitle", placeholders);
                    
                    int fadeIn = configManager.getInt("leveling.effects.title.fade-in", 10);
                    int stay = configManager.getInt("leveling.effects.title.stay", 70);
                    int fadeOut = configManager.getInt("leveling.effects.title.fade-out", 20);
                    
                    player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
                }
            }
        }
    }
    
    /**
     * Gets the player's progress to the next level as a percentage.
     *
     * @param stats The player stats
     * @return Progress percentage (0-100)
     */
    public double getLevelProgress(PlayerStats stats) {
        int requiredExp = getRequiredExpForNextLevel(stats.getLevel());
        int currentExp = stats.getExperience();
        
        return (double) currentExp / requiredExp * 100;
    }
    
    /**
     * Gets the total experience required to reach a specific level from level 1.
     *
     * @param targetLevel The target level
     * @return The total experience required
     */
    public int getTotalExpToLevel(int targetLevel) {
        int total = 0;
        for (int level = 1; level < targetLevel; level++) {
            total += getRequiredExpForNextLevel(level);
        }
        return total;
    }
}