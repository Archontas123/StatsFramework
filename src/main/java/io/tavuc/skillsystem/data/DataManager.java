package io.tavuc.skillsystem.data;

import io.tavuc.skillsystem.api.model.ModifierType;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.Stat;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.config.ConfigManager;
import io.tavuc.skillsystem.manager.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Handles data storage and retrieval for player stats.
 */
public class DataManager {

    private final Plugin plugin;
    private StatManager statManager;
    private final ConfigManager configManager;
    private final File dataFolder;
    
    /**
     * Constructs the data manager.
     *
     * @param plugin        The plugin instance
     * @param configManager The config manager
     */
    public DataManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.statManager = null; // Will be set later to avoid circular dependency
        this.configManager = configManager;
        
        // Create data folder
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    /**
     * Constructs the data manager with a stat manager.
     *
     * @param plugin        The plugin instance
     * @param configManager The config manager
     * @param statManager   The stat manager
     */
    public DataManager(Plugin plugin, ConfigManager configManager, StatManager statManager) {
        this(plugin, configManager);
        this.statManager = statManager;
    }
    
    /**
     * Sets the stat manager after construction.
     *
     * @param statManager The stat manager
     */
    public void setStatManager(StatManager statManager) {
        // Only allow setting if it was null
        if (this.statManager == null) {
            // this.statManager = statManager;
        }
    }
    
    /**
     * Loads player data.
     *
     * @param uuid The player UUID
     */
    public void loadPlayer(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return; // No data to load
        }
        
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        PlayerStats stats = statManager.getPlayerStats(uuid);
        
        // Load level and experience
        stats.setLevel(playerConfig.getInt("level", 1));
        stats.setExperience(playerConfig.getInt("experience", 0));
        stats.setUnspentPoints(playerConfig.getInt("unspent-points", 0));
        
        // Load stats
        ConfigurationSection statsSection = playerConfig.getConfigurationSection("stats");
        if (statsSection != null) {
            for (String statName : statsSection.getKeys(false)) {
                try {
                    StatType type = StatType.valueOf(statName.toUpperCase());
                    Stat stat = stats.getStat(type);
                    
                    ConfigurationSection statSection = statsSection.getConfigurationSection(statName);
                    if (statSection != null) {
                        stat.setBaseValue(statSection.getInt("base-value", 0));
                        
                        // Load modifiers
                        ConfigurationSection modifiersSection = statSection.getConfigurationSection("modifiers");
                        if (modifiersSection != null) {
                            for (String modifierKey : modifiersSection.getKeys(false)) {
                                ConfigurationSection modifierSection = modifiersSection.getConfigurationSection(modifierKey);
                                if (modifierSection != null) {
                                    UUID modifierId = UUID.fromString(modifierKey);
                                    float value = (float) modifierSection.getDouble("value", 0);
                                    ModifierType modifierType = ModifierType.valueOf(
                                            modifierSection.getString("type", "ADDITIVE"));
                                    int duration = modifierSection.getInt("duration", -1);
                                    String source = modifierSection.getString("source", "unknown");
                                    
                                    stat.addModifier(modifierId, value, modifierType, duration, source);
                                }
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Unknown stat type in player data: " + statName);
                }
            }
        }
    }
    
    /**
     * Saves player data.
     *
     * @param uuid The player UUID
     */
    public void savePlayer(UUID uuid) {
        PlayerStats stats = statManager.getPlayerStats(uuid);
        if (stats == null) {
            return; // No stats to save
        }
        
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        FileConfiguration playerConfig = new YamlConfiguration();
        
        // Save level and experience
        playerConfig.set("level", stats.getLevel());
        playerConfig.set("experience", stats.getExperience());
        playerConfig.set("unspent-points", stats.getUnspentPoints());
        
        // Save stats
        for (Map.Entry<StatType, Stat> entry : stats.getAllStats().entrySet()) {
            StatType type = entry.getKey();
            Stat stat = entry.getValue();
            
            String path = "stats." + type.name().toLowerCase();
            playerConfig.set(path + ".base-value", stat.getBaseValue());
            
            // Save persistent modifiers (duration = -1)
            Map<UUID, io.tavuc.skillsystem.api.model.StatModifier> modifiers = stat.getModifiers();
            for (Map.Entry<UUID, io.tavuc.skillsystem.api.model.StatModifier> modEntry : modifiers.entrySet()) {
                UUID modifierId = modEntry.getKey();
                io.tavuc.skillsystem.api.model.StatModifier modifier = modEntry.getValue();
                
                // Only save persistent modifiers
                if (modifier.getDuration() == -1) {
                    String modPath = path + ".modifiers." + modifierId.toString();
                    playerConfig.set(modPath + ".value", modifier.getValue());
                    playerConfig.set(modPath + ".type", modifier.getType().name());
                    playerConfig.set(modPath + ".duration", -1);
                    playerConfig.set(modPath + ".source", modifier.getSource());
                }
            }
        }
        
        // Save the config
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + uuid, e);
        }
    }
    
    /**
     * Loads all online players' data.
     */
    public void loadOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayer(player.getUniqueId());
        }
    }
    
    /**
     * Saves all online players' data.
     */
    public void saveAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayer(player.getUniqueId());
        }
    }
    
    /**
     * Checks if player data exists.
     *
     * @param uuid The player UUID
     * @return true if data exists, false otherwise
     */
    public boolean hasPlayerData(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        return playerFile.exists();
    }
}