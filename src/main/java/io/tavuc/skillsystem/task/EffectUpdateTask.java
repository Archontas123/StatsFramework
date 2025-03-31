package io.tavuc.skillsystem.task;

import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.manager.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Task for updating temporary stat effects.
 */
public class EffectUpdateTask {

    private final Plugin plugin;
    private final StatManager statManager;
    private int taskId = -1;
    
    /**
     * Constructs the effect update task.
     *
     * @param plugin      The plugin instance
     * @param statManager The stat manager
     */
    public EffectUpdateTask(Plugin plugin, StatManager statManager) {
        this.plugin = plugin;
        this.statManager = statManager;
    }
    
    /**
     * Starts the update task.
     */
    public void start() {
        // Only start if not already running
        if (taskId == -1) {
            // Update every tick (1/20th of a second)
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::update, 1L, 1L);
            plugin.getLogger().info("Effect update task started");
        }
    }
    
    /**
     * Stops the update task.
     */
    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
            plugin.getLogger().info("Effect update task stopped");
        }
    }
    
    /**
     * Updates all player effects.
     */
    private void update() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());
            if (stats != null) {
                stats.updateEffects();
            }
        }
    }
}