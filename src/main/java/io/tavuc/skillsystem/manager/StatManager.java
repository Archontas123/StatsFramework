package io.tavuc.skillsystem.manager;

import io.tavuc.skillsystem.Main;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.StatType;
import java.util.*;

/**
 * Manages all player stats and registered stat types.
 */
public class StatManager {

    private final Main plugin;
    private LevelManager levelManager;
    private final Map<UUID, PlayerStats> playerStats = new HashMap<>();
    private final Map<String, StatType> registeredStats = new HashMap<>();

    /**
     * Constructs the skill manager.
     *
     * @param plugin            The main plugin instance.
     * @param levelManager      The level manager.
     */
    public StatManager(Main plugin, LevelManager levelManager) {
        this.plugin = plugin;
        this.levelManager = levelManager;
        for (StatType type : StatType.values()) {
            registeredStats.put(type.name().toLowerCase(), type);
        }
    }
    



    /**
     * Returns the level manager.
     *
     * @return The level manager.
     */
    public LevelManager getLevelManager() {
        return levelManager;
    }

    /**
     * Returns the stats for a player, creating them if necessary.
     *
     * @param uuid The player UUID.
     * @return The player stats.
     */
    public PlayerStats getPlayerStats(UUID uuid) {
        return playerStats.computeIfAbsent(uuid, u -> new PlayerStats());
    }

    /**
     * Registers a custom stat.
     *
     * @param name The stat name.
     * @param type The stat type.
     */
    public void registerStat(String name, StatType type) {
        registeredStats.put(name.toLowerCase(), type);
    }

    /**
     * Returns all registered stat types.
     *
     * @return A map of stat names to types.
     */
    public Map<String, StatType> getRegisteredStats() {
        return Collections.unmodifiableMap(registeredStats);
    }

    /**
     * Checks if a stat is registered.
     *
     * @param name The stat name.
     * @return True if registered, otherwise false.
     */
    public boolean isRegistered(String name) {
        return registeredStats.containsKey(name.toLowerCase());
    }

    /**
     * Returns the StatType for a given name.
     *
     * @param name The stat name.
     * @return The StatType or null.
     */
    public StatType getStatType(String name) {
        return registeredStats.get(name.toLowerCase());
    }




    public void setLevelManager(LevelManager levelManager2) {
        levelManager = levelManager2;
    }
}
