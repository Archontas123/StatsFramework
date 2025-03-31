package io.tavuc.skillsystem.command;

import io.tavuc.skillsystem.Main;
import io.tavuc.skillsystem.api.model.ModifierType;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.config.ConfigManager;
import io.tavuc.skillsystem.manager.LevelManager;
import io.tavuc.skillsystem.manager.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin command for the Skills Framework plugin.
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final StatManager statManager;
    private final LevelManager levelManager;
    private final ConfigManager configManager;
    
    /**
     * Constructs the admin command.
     *
     * @param plugin        The plugin instance
     * @param statManager   The stat manager
     * @param levelManager  The level manager
     * @param configManager The config manager
     */
    public AdminCommand(Main plugin, StatManager statManager, LevelManager levelManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.statManager = statManager;
        this.levelManager = levelManager;
        this.configManager = configManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skillsystem.admin")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
                
            case "setlevel":
                handleSetLevel(sender, args);
                break;
                
            case "addexp":
                handleAddExp(sender, args);
                break;
                
            case "setstat":
                handleSetStat(sender, args);
                break;
                
            case "reset":
                handleReset(sender, args);
                break;
                
            case "addeffect":
                handleAddEffect(sender, args);
                break;
                
            case "removeeffect":
                handleRemoveEffect(sender, args);
                break;
                
            case "info":
                handleInfo(sender, args);
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * Sends help message.
     *
     * @param sender The command sender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Skills Framework Admin Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin reload " + ChatColor.WHITE + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin setlevel <player> <level> " + ChatColor.WHITE + "- Set player level");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin addexp <player> <amount> " + ChatColor.WHITE + "- Add experience");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin setstat <player> <stat> <value> " + ChatColor.WHITE + "- Set stat value");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin reset <player> [stats|level|all] " + ChatColor.WHITE + "- Reset player data");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin addeffect <player> <stat> <value> <type> [duration] [source] " + ChatColor.WHITE + "- Add stat effect");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin removeeffect <player> <source> " + ChatColor.WHITE + "- Remove effects by source");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin info <player> " + ChatColor.WHITE + "- Show player stat info");
    }
    
    /**
     * Handles reload command.
     *
     * @param sender The command sender
     */
    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(configManager.getMessage("config-reloaded"));
    }
    
    /**
     * Handles setlevel command.
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private void handleSetLevel(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin setlevel <player> <level>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }
        
        int level;
        try {
            level = Integer.parseInt(args[2]);
            if (level < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-number"));
            return;
        }
        
        PlayerStats stats = statManager.getPlayerStats(target.getUniqueId());
        stats.setLevel(level);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        placeholders.put("level", String.valueOf(level));
        
        sender.sendMessage(configManager.getMessage("level-set", placeholders));
    }
    
    /**
     * Handles addexp command.
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private void handleAddExp(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin addexp <player> <amount>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-number"));
            return;
        }
        
        PlayerStats stats = statManager.getPlayerStats(target.getUniqueId());
        levelManager.addExperience(target, stats, amount);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        placeholders.put("amount", String.valueOf(amount));
        
        sender.sendMessage(configManager.getMessage("exp-added", placeholders));
    }
    
    /**
     * Handles setstat command.
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private void handleSetStat(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin setstat <player> <stat> <value>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }
        
        String statName = args[2];
        StatType statType = statManager.getStatType(statName);
        
        if (statType == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("stat", statName);
            sender.sendMessage(configManager.getMessage("unknown-stat", placeholders));
            return;
        }
        
        int value;
        try {
            value = Integer.parseInt(args[3]);
            if (value < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-number"));
            return;
        }
        
        PlayerStats stats = statManager.getPlayerStats(target.getUniqueId());
        stats.getStat(statType).setBaseValue(value);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        placeholders.put("stat", statType.name());
        placeholders.put("value", String.valueOf(value));
        
        sender.sendMessage(configManager.getMessage("stat-set", placeholders));
    }
    
    /**
     * Handles reset command.
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin reset <player> [stats|level|all]");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }
        
        String resetType = args.length > 2 ? args[2].toLowerCase() : "all";
        PlayerStats stats = statManager.getPlayerStats(target.getUniqueId());
        
        switch (resetType) {
            case "stats":
                for (StatType type : StatType.values()) {
                    stats.getStat(type).setBaseValue(0);
                }
                break;
                
            case "level":
                stats.setLevel(1);
                stats.setExperience(0);
                stats.setUnspentPoints(0);
                break;
                
            case "all":
            default:
                for (StatType type : StatType.values()) {
                    stats.getStat(type).setBaseValue(0);
                }
                stats.setLevel(1);
                stats.setExperience(0);
                stats.setUnspentPoints(0);
                break;
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        placeholders.put("type", resetType);
        
        sender.sendMessage(configManager.getMessage("reset-complete", placeholders));
    }
    
    /**
     * Handles addeffect command.
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private void handleAddEffect(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin addeffect <player> <stat> <value> <type> [duration] [source]");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }
        
        String statName = args[2];
        StatType statType = statManager.getStatType(statName);
        
        if (statType == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("stat", statName);
            sender.sendMessage(configManager.getMessage("unknown-stat", placeholders));
            return;
        }
        
        float value;
        try {
            value = Float.parseFloat(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-number"));
            return;
        }
        
        ModifierType modifierType;
        try {
            modifierType = ModifierType.valueOf(args[4].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid modifier type. Use ADDITIVE or MULTIPLICATIVE.");
            return;
        }
        
        int duration = -1;
        if (args.length > 5) {
            try {
                duration = Integer.parseInt(args[5]);
                if (duration < -1) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(configManager.getMessage("invalid-number"));
                return;
            }
        }
        
        String source = args.length > 6 ? args[6] : "admin";
        
        PlayerStats stats = statManager.getPlayerStats(target.getUniqueId());
        UUID effectId = stats.addStatEffect(statType, value, modifierType, duration, source);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        placeholders.put("stat", statType.name());
        placeholders.put("value", String.valueOf(value));
        placeholders.put("type", modifierType.name());
        placeholders.put("duration", duration == -1 ? "permanent" : String.valueOf(duration) + " ticks");
        
        sender.sendMessage(configManager.getMessage("effect-added", placeholders));
    }
    
    /**
     * Handles removeeffect command.
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private void handleRemoveEffect(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin removeeffect <player> <source>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }
        
        String source = args[2];
        PlayerStats stats = statManager.getPlayerStats(target.getUniqueId());
        int count = stats.removeEffectsFromSource(source);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        placeholders.put("count", String.valueOf(count));
        placeholders.put("source", source);
        
        sender.sendMessage(configManager.getMessage("effects-removed", placeholders));
    }
    
    /**
     * Handles info command.
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin info <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }
        
        PlayerStats stats = statManager.getPlayerStats(target.getUniqueId());
        
        sender.sendMessage(ChatColor.GOLD + "=== Player Stats: " + target.getName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + stats.getLevel());
        sender.sendMessage(ChatColor.YELLOW + "Experience: " + ChatColor.WHITE + stats.getExperience() + 
                "/" + levelManager.getRequiredExpForNextLevel(stats.getLevel()));
        sender.sendMessage(ChatColor.YELLOW + "Unspent Points: " + ChatColor.WHITE + stats.getUnspentPoints());
        
        sender.sendMessage(ChatColor.GOLD + "Stats:");
        for (StatType type : StatType.values()) {
            sender.sendMessage(ChatColor.YELLOW + type.name() + ": " + ChatColor.WHITE + 
                    stats.getStat(type).getBaseValue() + ChatColor.GRAY + " (Effective: " + 
                    stats.getStat(type).getValue() + ")");
        }
        
        // Show active effects if any
        boolean hasEffects = false;
        for (StatType type : StatType.values()) {
            Map<UUID, io.tavuc.skillsystem.api.model.StatModifier> modifiers = 
                    stats.getStat(type).getModifiers();
            
            if (!modifiers.isEmpty()) {
                if (!hasEffects) {
                    sender.sendMessage(ChatColor.GOLD + "Active Effects:");
                    hasEffects = true;
                }
                
                for (Map.Entry<UUID, io.tavuc.skillsystem.api.model.StatModifier> entry : modifiers.entrySet()) {
                    io.tavuc.skillsystem.api.model.StatModifier modifier = entry.getValue();
                    sender.sendMessage(ChatColor.YELLOW + type.name() + ": " + 
                            (modifier.getValue() >= 0 ? "+" : "") + modifier.getValue() + 
                            (modifier.getType() == ModifierType.MULTIPLICATIVE ? "%" : "") + 
                            ChatColor.GRAY + " (" + modifier.getSource() + ", " + 
                            modifier.getFormattedDuration() + ")");
                }
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("skillsystem.admin")) {
            return Collections.emptyList();
        }
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "setlevel", "addexp", "setstat", 
                    "reset", "addeffect", "removeeffect", "info");
            
            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("setlevel") || subcommand.equals("addexp") || 
                    subcommand.equals("setstat") || subcommand.equals("reset") || 
                    subcommand.equals("addeffect") || subcommand.equals("removeeffect") || 
                    subcommand.equals("info")) {
                
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3) {
            String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("setstat") || subcommand.equals("addeffect")) {
                return Arrays.stream(StatType.values())
                        .map(StatType::name)
                        .map(String::toLowerCase)
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            
            if (subcommand.equals("reset")) {
                return Arrays.asList("stats", "level", "all").stream()
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        if (args.length == 5 && args[0].toLowerCase().equals("addeffect")) {
            return Arrays.asList("ADDITIVE", "MULTIPLICATIVE").stream()
                    .filter(s -> s.startsWith(args[4].toUpperCase()))
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }
}