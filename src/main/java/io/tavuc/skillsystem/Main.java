package io.tavuc.skillsystem;

import io.tavuc.skillsystem.api.SkillAPI;
import io.tavuc.skillsystem.command.AdminCommand;
import io.tavuc.skillsystem.command.AssignStatCommand;
import io.tavuc.skillsystem.command.StatsCommand;
import io.tavuc.skillsystem.config.ConfigManager;
import io.tavuc.skillsystem.data.DataManager;
import io.tavuc.skillsystem.gui.StatsGUI;
import io.tavuc.skillsystem.listener.PlayerListener;
import io.tavuc.skillsystem.manager.LevelManager;
import io.tavuc.skillsystem.manager.StatEffectCalculator;
import io.tavuc.skillsystem.manager.StatManager;
import io.tavuc.skillsystem.task.EffectUpdateTask;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the Skills Framework plugin.
 */
public class Main extends JavaPlugin {

    private ConfigManager configManager;
    private DataManager dataManager;
    private LevelManager levelManager;
    private StatManager statManager;
    private StatEffectCalculator effectCalculator;
    private StatsGUI statsGUI;
    private EffectUpdateTask effectUpdateTask;
    
    @Override
    public void onEnable() {
        // Save default config files
        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("stats.yml", false);
        saveResource("formulas.yml", false);
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.levelManager = new LevelManager(this, configManager);
        this.statManager = new StatManager(this, levelManager);
        this.dataManager = new DataManager(this, configManager, statManager);
        this.effectCalculator = new StatEffectCalculator(this, statManager, configManager);
        this.statsGUI = new StatsGUI(statManager);
        
        // Start tasks
        this.effectUpdateTask = new EffectUpdateTask(this, statManager);
        effectUpdateTask.start();
        
        // Register API
        SkillAPI.register(statManager);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this, statManager, dataManager), this);
        
        // Load player data for online players (in case of reload)
        dataManager.loadOnlinePlayers();
        
        getLogger().info("Skills Framework " + getDescription().getVersion() + " enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save all player data
        if (dataManager != null) {
            dataManager.saveAllPlayers();
        }
        
        // Stop tasks
        if (effectUpdateTask != null) {
            effectUpdateTask.stop();
        }
        
        // Clean up resources
        if (effectCalculator != null) {
            effectCalculator.cleanup();
        }
        
        // Unregister API
        SkillAPI.unregister();
        
        getLogger().info("Skills Framework disabled!");
    }
    
    /**
     * Registers all commands.
     */
    private void registerCommands() {
        // Stats command
        PluginCommand statsCmd = getCommand("stats");
        if (statsCmd != null) {
            StatsCommand statsCommand = new StatsCommand(statManager);
            statsCmd.setExecutor(statsCommand);
        } else {
            getLogger().warning("Failed to register 'stats' command!");
        }
        
        // Assign command
        PluginCommand assignCmd = getCommand("assign");
        if (assignCmd != null) {
            AssignStatCommand assignCommand = new AssignStatCommand(statManager);
            assignCmd.setExecutor(assignCommand);
            assignCmd.setTabCompleter((TabCompleter) assignCommand);
        } else {
            getLogger().warning("Failed to register 'assign' command!");
        }
        
        // Admin command
        PluginCommand adminCmd = getCommand("skillsadmin");
        if (adminCmd != null) {
            AdminCommand adminCommand = new AdminCommand(this, statManager, levelManager, configManager);
            adminCmd.setExecutor(adminCommand);
            adminCmd.setTabCompleter(adminCommand);
        } else {
            getLogger().warning("Failed to register 'skillsadmin' command!");
        }
    }
    
    /**
     * Reloads the plugin configuration.
     */
    public void reload() {
        // Save players before reload
        dataManager.saveAllPlayers();
        
        // Reload config
        reloadConfig();
        configManager.reloadConfigs();
        
        // Reinitialize managers that need config updates
        levelManager = new LevelManager(this, configManager);
        statManager.setLevelManager(levelManager);
        
        // Update tasks
        effectUpdateTask.stop();
        effectUpdateTask = new EffectUpdateTask(this, statManager);
        effectUpdateTask.start();
        
        getLogger().info("Skills Framework reloaded!");
    }
    
    /**
     * Gets the config manager.
     *
     * @return The config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Gets the data manager.
     *
     * @return The data manager
     */
    public DataManager getDataManager() {
        return dataManager;
    }
    
    /**
     * Gets the level manager.
     *
     * @return The level manager
     */
    public LevelManager getLevelManager() {
        return levelManager;
    }
    
    /**
     * Gets the stat manager.
     *
     * @return The stat manager
     */
    public StatManager getStatManager() {
        return statManager;
    }
    
    /**
     * Gets the effect calculator.
     *
     * @return The effect calculator
     */
    public StatEffectCalculator getEffectCalculator() {
        return effectCalculator;
    }
    
    /**
     * Gets the stats GUI.
     *
     * @return The stats GUI
     */
    public StatsGUI getStatsGUI() {
        return statsGUI;
    }
}