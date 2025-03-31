package io.tavuc.skillsystem.api;

import io.tavuc.skillsystem.manager.StatManager;
import org.bukkit.entity.Player;

/**
 * Static API access point for the Skill Framework.
 */
public class SkillAPI {

    private static StatManager skillManager;

    /**
     * Registers the skill manager for API usage.
     *
     * @param manager The skill manager instance to register.
     */
    public static void register(StatManager manager) {
        if (skillManager != null) {
            throw new IllegalStateException("SkillAPI is already registered.");
        }
        skillManager = manager;
    }

    /**
     * Unregisters the skill manager.
     */
    public static void unregister() {
        skillManager = null;
    }

    /**
     * Returns the registered SkillManager.
     *
     * @return The StatManager instance.
     */
    public static StatManager getStatManager() {
        if (skillManager == null) {
            throw new IllegalStateException("SkillAPI is not registered yet.");
        }
        return skillManager;
    }

    /**
     * Returns whether the SkillAPI is registered.
     *
     * @return True if registered, false otherwise.
     */
    public static boolean isRegistered() {
        return skillManager != null;
    }

    /**
     * Shortcut to get a player's stats.
     *
     * @param player The player.
     * @return The player's stats.
     */
    public static io.tavuc.skillsystem.api.model.PlayerStats getStats(Player player) {
        return getStatManager().getPlayerStats(player.getUniqueId());
    }
}
