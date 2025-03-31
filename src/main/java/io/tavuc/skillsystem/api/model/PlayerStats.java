package io.tavuc.skillsystem.api.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents the stats and progression of a player.
 */
public class PlayerStats {

    private final Map<StatType, Stat> stats = new EnumMap<>(StatType.class);
    private int level;
    private int experience;
    private int unspentPoints;

    /**
     * Constructs a PlayerStats object with default values.
     */
    public PlayerStats() {
        for (StatType type : StatType.values()) {
            stats.put(type, new Stat(type, 0, 100));
        }
        this.level = 1;
        this.experience = 0;
        this.unspentPoints = 0;
    }

    /**
     * Returns the stat object for a given type.
     *
     * @param type The stat type.
     * @return The Stat object.
     */
    public Stat getStat(StatType type) {
        return stats.get(type);
    }

    /**
     * Returns all the player's stats.
     *
     * @return Map of StatType to Stat.
     */
    public Map<StatType, Stat> getAllStats() {
        return stats;
    }

    /**
     * Returns the player's level.
     *
     * @return The level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the player's level.
     *
     * @param level The new level.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns the player's experience.
     *
     * @return The experience.
     */
    public int getExperience() {
        return experience;
    }

    /**
     * Sets the player's experience.
     *
     * @param experience The new experience.
     */
    public void setExperience(int experience) {
        this.experience = experience;
    }

    /**
     * Returns the player's unspent stat points.
     *
     * @return The unspent points.
     */
    public int getUnspentPoints() {
        return unspentPoints;
    }

    /**
     * Sets the player's unspent stat points.
     *
     * @param unspentPoints The new amount of unspent points.
     */
    public void setUnspentPoints(int unspentPoints) {
        this.unspentPoints = unspentPoints;
    }

    /**
     * Increases unspent stat points.
     *
     * @param amount The amount to add.
     */
    public void addUnspentPoints(int amount) {
        this.unspentPoints += amount;
    }

    /**
     * Decreases unspent stat points.
     *
     * @param amount The amount to remove.
     */
    public void removeUnspentPoints(int amount) {
        this.unspentPoints = Math.max(0, this.unspentPoints - amount);
    }
}
