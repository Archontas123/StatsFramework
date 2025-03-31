package io.tavuc.skillsystem.manager;

import io.tavuc.skillsystem.api.events.PlayerLevelUpEvent;
import io.tavuc.skillsystem.api.model.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Handles player leveling and experience progression.
 */
public class LevelManager {

    private int baseExpRequirement = 100;
    private int expIncreasePerLevel = 50;
    private int statPointsPerLevel = 5;

    /**
     * Sets the base experience requirement for level 1.
     *
     * @param baseExpRequirement The base requirement.
     */
    public void setBaseExpRequirement(int baseExpRequirement) {
        this.baseExpRequirement = baseExpRequirement;
    }

    /**
     * Sets the experience increase per level.
     *
     * @param expIncreasePerLevel The experience increase.
     */
    public void setExpIncreasePerLevel(int expIncreasePerLevel) {
        this.expIncreasePerLevel = expIncreasePerLevel;
    }

    /**
     * Sets the stat points rewarded per level.
     *
     * @param statPointsPerLevel The stat points.
     */
    public void setStatPointsPerLevel(int statPointsPerLevel) {
        this.statPointsPerLevel = statPointsPerLevel;
    }

    /**
     * Adds experience to a player and handles level-up if applicable.
     *
     * @param player     The player.
     * @param playerStats The player's stats.
     * @param amount     The amount of experience to add.
     */
    public void addExperience(Player player, PlayerStats playerStats, int amount) {
        playerStats.setExperience(playerStats.getExperience() + amount);
        while (playerStats.getExperience() >= getRequiredExpForNextLevel(playerStats.getLevel())) {
            playerStats.setExperience(playerStats.getExperience() - getRequiredExpForNextLevel(playerStats.getLevel()));
            playerStats.setLevel(playerStats.getLevel() + 1);
            playerStats.addUnspentPoints(statPointsPerLevel);
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(player, playerStats.getLevel()));
        }
    }

    /**
     * Returns the required experience for the next level.
     *
     * @param level The current level.
     * @return The required experience.
     */
    public int getRequiredExpForNextLevel(int level) {
        return baseExpRequirement + (expIncreasePerLevel * (level - 1));
    }
}
