package io.tavuc.skillsystem.listener;

import io.tavuc.skillsystem.Main;
import io.tavuc.skillsystem.data.DataManager;
import io.tavuc.skillsystem.manager.StatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player-related events.
 */
public class PlayerListener implements Listener {

    private final Main plugin;
    private final StatManager statManager;
    private final DataManager dataManager;
    
    /**
     * Constructs the player listener.
     *
     * @param plugin      The plugin instance
     * @param statManager The stat manager
     * @param dataManager The data manager
     */
    public PlayerListener(Main plugin, StatManager statManager, DataManager dataManager) {
        this.plugin = plugin;
        this.statManager = statManager;
        this.dataManager = dataManager;
    }
    
    /**
     * Handles player join event.
     *
     * @param event The event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load player data if it exists
        if (dataManager.hasPlayerData(player.getUniqueId())) {
            dataManager.loadPlayer(player.getUniqueId());
            
            if (plugin.getConfigManager().getBoolean("debug.player-data", false)) {
                plugin.getLogger().info("Loaded data for player: " + player.getName());
            }
        } else {
            // Initialize with starting stats if specified
            int startingPoints = plugin.getConfigManager().getInt("new-player.starting-points", 0);
            if (startingPoints > 0) {
                statManager.getPlayerStats(player.getUniqueId()).addUnspentPoints(startingPoints);
                
                if (plugin.getConfigManager().getBoolean("debug.player-data", false)) {
                    plugin.getLogger().info("Initialized new player with " + startingPoints + 
                            " starting points: " + player.getName());
                }
            }
        }
    }
    
    /**
     * Handles player quit event.
     *
     * @param event The event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Save player data
        dataManager.savePlayer(player.getUniqueId());
        
        if (plugin.getConfigManager().getBoolean("debug.player-data", false)) {
            plugin.getLogger().info("Saved data for player: " + player.getName());
        }
    }
}