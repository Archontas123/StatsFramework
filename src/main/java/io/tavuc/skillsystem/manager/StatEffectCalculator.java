package io.tavuc.skillsystem.manager;

import io.tavuc.skillsystem.config.ConfigManager;
import io.tavuc.skillsystem.config.FormulaConfig;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.Stat;
import io.tavuc.skillsystem.api.model.StatType;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Calculates and applies stat-based effects using configurable formulas.
 */
public class StatEffectCalculator implements Listener {

    private final StatManager statManager;
    private final ConfigManager configManager;
    private final Logger logger;
    private final Random random = ThreadLocalRandom.current();
    
    // Cache for storing applying effects to avoid frequent calculations
    private final Map<UUID, Map<StatType, Double>> playerEffectCache = new HashMap<>();
    
    // Scheduled task ID for periodic updates
    private int updateTaskId = -1;
    
    /**
     * Constructs the stat effect calculator.
     *
     * @param plugin        The plugin instance
     * @param statManager   The stat manager
     * @param configManager The config manager
     */
    public StatEffectCalculator(Plugin plugin, StatManager statManager, ConfigManager configManager) {
        this.statManager = statManager;
        this.configManager = configManager;
        this.logger = plugin.getLogger();
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Start periodic update task (every 10 ticks)
        updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateEffects, 10L, 10L);
    }
    
    /**
     * Cleans up resources used by the calculator.
     */
    public void cleanup() {
        if (updateTaskId != -1) {
            Bukkit.getScheduler().cancelTask(updateTaskId);
            updateTaskId = -1;
        }
        playerEffectCache.clear();
    }
    
    /**
     * Updates all stat effects.
     */
    public void updateEffects() {
        // Update all online players' stat effects
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            
            PlayerStats stats = statManager.getPlayerStats(playerId);
            if (stats != null) {
                stats.updateEffects();
                
                // Update attributes that need real-time updates
                updatePlayerAttributes(player, stats);
                
                // Clear and rebuild effect cache
                Map<StatType, Double> effectCache = playerEffectCache.computeIfAbsent(
                        playerId, k -> new EnumMap<>(StatType.class));
                effectCache.clear();
            }
        }
    }
    
    /**
     * Updates a player's Bukkit attributes based on their stats.
     *
     * @param player The player
     * @param stats  The player's stats
     */
    public void updatePlayerAttributes(Player player, PlayerStats stats) {
        // Apply VITALITY to max health
        int vitality = stats.getStat(StatType.VITALITY).getValue();
        double baseHealth = 20.0; // Default Minecraft health
        
        FormulaConfig healthFormula = configManager.getFormulaConfig("vitality_health");
        if (healthFormula != null) {
            double maxHealth = healthFormula.calculate(vitality);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        } else {
            // Fallback if formula not found
            double bonus = vitality * 0.5;
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHealth + bonus);
        }
        
        // Apply AGILITY to movement speed
        int agility = stats.getStat(StatType.AGILITY).getValue();
        FormulaConfig speedFormula = configManager.getFormulaConfig("agility_speed");
        if (speedFormula != null && agility > 0) {
            double speedMultiplier = speedFormula.calculate(agility);
            // Default walk speed is 0.2
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2 * speedMultiplier);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());
        
        // Apply initial attributes
        updatePlayerAttributes(player, stats);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up cache when player leaves
        playerEffectCache.remove(event.getPlayer().getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());
            
            // Apply STRENGTH to damage
            int strength = stats.getStat(StatType.STRENGTH).getValue();
            FormulaConfig damageFormula = configManager.getFormulaConfig("strength_damage");
            if (damageFormula != null) {
                double damageMultiplier = damageFormula.calculate(strength);
                event.setDamage(event.getDamage() * damageMultiplier);
                
                if (configManager.getBoolean("debug.stats", false)) {
                    logger.info("STRENGTH applied to " + player.getName() + " => Damage x" + damageMultiplier);
                }
            }
            
            // Apply FEROCITY for critical hit chance
            int ferocity = stats.getStat(StatType.FEROCITY).getValue();
            FormulaConfig critChanceFormula = configManager.getFormulaConfig("ferocity_crit_chance");
            FormulaConfig critDamageFormula = configManager.getFormulaConfig("ferocity_crit_damage");
            
            if (critChanceFormula != null && critDamageFormula != null) {
                double critChance = critChanceFormula.calculate(ferocity);
                if (random.nextDouble() * 100 < critChance) {
                    double critMultiplier = critDamageFormula.calculate(ferocity);
                    event.setDamage(event.getDamage() * critMultiplier);
                    
                    if (configManager.getBoolean("debug.stats", false)) {
                        logger.info("FEROCITY triggered! Critical Hit for " + player.getName() + 
                                " => Damage x" + critMultiplier);
                    }
                    
                    // Send critical hit message to player if enabled
                    if (configManager.getBoolean("messages.show-crit-hit", true)) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("damage", String.format("%.1f", event.getFinalDamage()));
                        player.sendMessage(configManager.getMessage("critical-hit", placeholders));
                    }
                }
            }
            
            // Apply AGILITY for multi-hit chance
            int agility = stats.getStat(StatType.AGILITY).getValue();
            FormulaConfig multiHitChanceFormula = configManager.getFormulaConfig("agility_multi_hit_chance");
            
            if (multiHitChanceFormula != null) {
                double multiHitChance = multiHitChanceFormula.calculate(agility);
                if (random.nextDouble() * 100 < multiHitChance) {
                    // Schedule an extra hit in the next tick
                    Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugins()[0], () -> {
                        if (event.getEntity().isValid() && !event.getEntity().isDead()) {
                            // Apply a second hit with 50% damage
                            if (event.getEntity() instanceof LivingEntity target) {
                                target.damage(event.getFinalDamage() * 0.5, player);
                                
                                if (configManager.getBoolean("debug.stats", false)) {
                                    logger.info("AGILITY triggered! Multi-Hit for " + player.getName());
                                }
                                
                                // Send multi-hit message to player if enabled
                                if (configManager.getBoolean("messages.show-multi-hit", true)) {
                                    Map<String, String> placeholders = new HashMap<>();
                                    placeholders.put("damage", String.format("%.1f", event.getFinalDamage() * 0.5));
                                    player.sendMessage(configManager.getMessage("multi-hit", placeholders));
                                }
                            }
                        }
                    }, 1L);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageTaken(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());
            
            // Apply DEFENSE for damage reduction
            int defense = stats.getStat(StatType.DEFENSE).getValue();
            FormulaConfig defenseFormula = configManager.getFormulaConfig("defense_reduction");
            
            if (defenseFormula != null) {
                double reduction = defenseFormula.calculate(defense) / 100.0; // Convert to percentage
                reduction = Math.min(reduction, 0.80); // Cap at 80% damage reduction
                
                double originalDamage = event.getDamage();
                double newDamage = originalDamage * (1.0 - reduction);
                event.setDamage(newDamage);
                
                if (configManager.getBoolean("debug.stats", false)) {
                    logger.info("DEFENSE applied to " + player.getName() + 
                            " => Damage reduced by " + (reduction * 100) + "% (" + 
                            originalDamage + " -> " + newDamage + ")");
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityKill(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player instanceof Player) {
            PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());
            
            // Get base EXP from config
            int baseExp = configManager.getInt("experience.base-kill-exp", 10);
            
            // Apply INTELLIGENCE for bonus experience
            int intelligence = stats.getStat(StatType.INTELLIGENCE).getValue();
            FormulaConfig expBonusFormula = configManager.getFormulaConfig("intelligence_exp_bonus");
            
            if (expBonusFormula != null) {
                // Calculate bonus exp multiplier
                double expMultiplier = expBonusFormula.calculate(intelligence);
                int bonus = (int) (baseExp * (expMultiplier - 1.0));
                int total = baseExp + bonus;
                
                statManager.getLevelManager().addExperience(player, stats, total);
                
                if (configManager.getBoolean("debug.stats", false)) {
                    logger.info(player.getName() + " gained " + total + " EXP (+" + 
                            bonus + " from INTELLIGENCE)");
                }
            } else {
                // Fallback if formula not found
                int bonus = (int) (baseExp * (intelligence * 0.01));
                int total = baseExp + bonus;
                
                statManager.getLevelManager().addExperience(player, stats, total);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityHeal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player player && 
                event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            
            PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());
            
            // Apply VITALITY for bonus natural regeneration
            int vitality = stats.getStat(StatType.VITALITY).getValue();
            FormulaConfig regenFormula = configManager.getFormulaConfig("vitality_regen");
            
            if (regenFormula != null && vitality > 0) {
                double regenMultiplier = regenFormula.calculate(vitality);
                event.setAmount(event.getAmount() * regenMultiplier);
                
                if (configManager.getBoolean("debug.stats", false)) {
                    logger.info("VITALITY applied to " + player.getName() + 
                            " => Healing x" + regenMultiplier);
                }
            }
        }
    }
}