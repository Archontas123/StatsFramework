package io.tavuc.skillsystem.manager;

import io.tavuc.skillsystem.api.SkillAPI;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.StatType;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Calculates and applies stat-based effects and handles default EXP gain.
 */
public class StatEffectCalculator implements Listener {

    private final Map<StatType, BiConsumer<Player, Integer>> handlers = new EnumMap<>(StatType.class);
    private final Random random = new Random();
    private final StatManager statManager;
    private final Logger logger;

    /**
     * Constructs the stat effect calculator.
     *
     * @param statManager The stat manager.
     */
    public StatEffectCalculator(StatManager statManager) {
        this.statManager = statManager;
        this.logger = Bukkit.getLogger();
        registerDefaults();
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]);
    }

    /**
     * Registers a handler for a stat.
     *
     * @param statType The stat type.
     * @param handler  The handler logic.
     */
    public void registerHandler(StatType statType, BiConsumer<Player, Integer> handler) {
        handlers.put(statType, handler);
    }

    /**
     * Applies a stat's effect.
     *
     * @param player   The player.
     * @param statType The stat type.
     * @param value    The stat value.
     */
    public void apply(Player player, StatType statType, int value) {
        if (handlers.containsKey(statType)) {
            handlers.get(statType).accept(player, value);
        }
    }

    /**
     * Clears all registered handlers.
     */
    public void clearHandlers() {
        handlers.clear();
    }

    /**
     * Returns the registered handlers.
     *
     * @return The registered handlers.
     */
    public Map<StatType, BiConsumer<Player, Integer>> getHandlers() {
        return handlers;
    }

    private void registerDefaults() {
        registerHandler(StatType.VITALITY, this::applyVitality);
    }

    private void applyVitality(Player player, Integer value) {
        double baseHealth = 20.0;
        double bonus = value * 0.5;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHealth + bonus);
        logger.info("VITALITY applied to " + player.getName() + " => MaxHealth: " + (baseHealth + bonus));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());

            int strength = stats.getStat(StatType.STRENGTH).getValue();
            double bonus = 1.0 + (strength * 0.01);
            event.setDamage(event.getDamage() * bonus);
            logger.info("STRENGTH applied to " + player.getName() + " => Damage x" + bonus);

            int agility = stats.getStat(StatType.AGILITY).getValue();
            if (random.nextDouble() < agility * 0.005) {
                event.setDamage(event.getDamage() * 2.0);
                logger.info("AGILITY triggered! Multi-Hit for " + player.getName());
            }

            int ferocity = stats.getStat(StatType.FEROCITY).getValue();
            if (random.nextDouble() < ferocity * 0.01) {
                event.setDamage(event.getDamage() * 1.5);
                logger.info("FEROCITY triggered! Critical Hit for " + player.getName());
            }
        }
    }

    @EventHandler
    public void onEntityDamageTaken(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            int defense = statManager.getPlayerStats(player.getUniqueId()).getStat(StatType.DEFENSE).getValue();
            double reduction = defense * 0.01;
            event.setDamage(event.getDamage() * (1.0 - reduction));
            logger.info("DEFENSE applied to " + player.getName() + " => Damage reduced by " + (reduction * 100) + "%");
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = (Player) event.getEntity().getKiller();
            PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());
            int base = 10;
            int intelligence = stats.getStat(StatType.INTELLIGENCE).getValue();
            int bonus = (int) (base * (intelligence * 0.01));
            int total = base + bonus;

            statManager.getLevelManager().addExperience(player, stats, total);
            logger.info(player.getName() + " gained " + total + " EXP (+" + bonus + " from INTELLIGENCE)");
        }
    }
}
