package io.tavuc.skillsystem.config;

import io.tavuc.skillsystem.Main;
import io.tavuc.skillsystem.api.model.StatType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages all configuration aspects of the plugin.
 */
public class ConfigManager {
    private final Main plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration stats;
    private FileConfiguration formulas;
    
    private final Map<StatType, StatConfig> statConfigs = new EnumMap<>(StatType.class);
    private final Map<String, String> messageCache = new HashMap<>();
    private final Map<String, FormulaConfig> formulaConfigs = new HashMap<>();
    
    /**
     * Constructs the config manager.
     *
     * @param plugin The main plugin instance.
     */
    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        createDefaultConfigs();
        loadConfigs();
    }
    
    
    /**
     * Creates the default configuration files if they don't exist.
     */
    private void createDefaultConfigs() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        File statsFile = new File(plugin.getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            plugin.saveResource("stats.yml", false);
        }
        
        File formulasFile = new File(plugin.getDataFolder(), "formulas.yml");
        if (!formulasFile.exists()) {
            plugin.saveResource("formulas.yml", false);
        }
    }
    
    /**
     * Loads all configuration files.
     */
    public void loadConfigs() {
        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        stats = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "stats.yml"));
        formulas = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "formulas.yml"));
        
        loadStatsConfig();
        loadFormulasConfig();
        messageCache.clear();
    }
    
    /**
     * Reloads all configuration files.
     */
    public void reloadConfigs() {
        createDefaultConfigs();
        loadConfigs();
    }
    
    /**
     * Loads the stat configurations.
     */
    private void loadStatsConfig() {
        statConfigs.clear();
        
        ConfigurationSection statsSection = stats.getConfigurationSection("stats");
        if (statsSection == null) {
            plugin.getLogger().severe("Failed to load stats configuration: 'stats' section missing");
            return;
        }
        
        for (StatType type : StatType.values()) {
            String typeName = type.name().toLowerCase();
            if (!statsSection.contains(typeName)) {
                plugin.getLogger().warning("No configuration found for stat: " + type.name());
                statConfigs.put(type, new StatConfig(type));
                continue;
            }
            
            ConfigurationSection statSection = statsSection.getConfigurationSection(typeName);
            if (statSection == null) {
                plugin.getLogger().warning("Invalid configuration for stat: " + type.name());
                statConfigs.put(type, new StatConfig(type));
                continue;
            }
            
            String displayName = statSection.getString("display-name", type.name());
            String description = statSection.getString("description", "");
            Material icon = Material.valueOf(statSection.getString("icon", "BOOK"));
            double baseScale = statSection.getDouble("base-scale", 1.0);
            boolean hidden = statSection.getBoolean("hidden", false);
            
            StatConfig statConfig = new StatConfig(type, displayName, description, icon, baseScale, hidden);
            statConfigs.put(type, statConfig);
        }
    }
    
    /**
     * Loads the formula configurations.
     */
    private void loadFormulasConfig() {
        formulaConfigs.clear();
        
        ConfigurationSection formulasSection = formulas.getConfigurationSection("formulas");
        if (formulasSection == null) {
            plugin.getLogger().severe("Failed to load formulas configuration: 'formulas' section missing");
            return;
        }
        
        for (String formulaKey : formulasSection.getKeys(false)) {
            ConfigurationSection formulaSection = formulasSection.getConfigurationSection(formulaKey);
            if (formulaSection == null) continue;
            
            String type = formulaSection.getString("type", "linear");
            double baseValue = formulaSection.getDouble("base-value", 0.0);
            double scaleValue = formulaSection.getDouble("scale-value", 1.0);
            double factor = formulaSection.getDouble("factor", 0.01);
            double step = formulaSection.getDouble("step", 1.0);
            
            FormulaConfig formulaConfig = new FormulaConfig(
                    formulaKey,
                    FormulaType.valueOf(type.toUpperCase()),
                    baseValue,
                    scaleValue,
                    factor,
                    step
            );
            
            formulaConfigs.put(formulaKey, formulaConfig);
        }
    }
    
    /**
     * Gets a configured message.
     *
     * @param key The message key.
     * @return The formatted message.
     */
    public String getMessage(String key) {
        if (messageCache.containsKey(key)) {
            return messageCache.get(key);
        }
        
        String message = messages.getString("messages." + key);
        if (message == null) {
            plugin.getLogger().warning("Missing message for key: " + key);
            message = "Missing message: " + key;
        }
        
        message = ChatColor.translateAlternateColorCodes('&', message);
        messageCache.put(key, message);
        
        return message;
    }
    
    /**
     * Gets a configured message with placeholders.
     *
     * @param key         The message key.
     * @param placeholders The placeholders to replace.
     * @return The formatted message.
     */
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return message;
    }
    
    /**
     * Gets the configuration for a stat.
     *
     * @param type The stat type.
     * @return The stat configuration.
     */
    public StatConfig getStatConfig(StatType type) {
        return statConfigs.get(type);
    }
    
    /**
     * Gets a formula configuration.
     *
     * @param key The formula key.
     * @return The formula configuration or null if not found.
     */
    public FormulaConfig getFormulaConfig(String key) {
        return formulaConfigs.get(key);
    }
    
    /**
     * Gets a configuration value.
     *
     * @param path The path to the value.
     * @param defaultValue The default value.
     * @return The configuration value.
     */
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }
    
    /**
     * Gets a configuration value.
     *
     * @param path The path to the value.
     * @param defaultValue The default value.
     * @return The configuration value.
     */
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    
    /**
     * Gets a configuration value.
     *
     * @param path The path to the value.
     * @param defaultValue The default value.
     * @return The configuration value.
     */
    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    /**
     * Gets a configuration value.
     *
     * @param path The path to the value.
     * @param defaultValue The default value.
     * @return The configuration value.
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
    
    /**
     * Saves a configuration file.
     *
     * @param config The configuration.
     * @param fileName The filename.
     */
    public void saveConfig(FileConfiguration config, String fileName) {
        try {
            config.save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + fileName, e);
        }
    }
}