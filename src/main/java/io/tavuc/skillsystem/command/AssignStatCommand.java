package io.tavuc.skillsystem.command;

import io.tavuc.skillsystem.api.events.PlayerAssignStatEvent;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.Stat;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.manager.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command for assigning stat points.
 */
public class AssignStatCommand implements CommandExecutor {

    private final StatManager statManager;

    /**
     * Constructs the assign stat command.
     *
     * @param statManager The stat manager.
     */
    public AssignStatCommand(StatManager statManager) {
        this.statManager = statManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /assign <stat> <amount>");
            return true;
        }

        Player player = (Player) sender;
        PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());
        String statName = args[0];
        StatType statType = statManager.getStatType(statName);

        if (statType == null) {
            player.sendMessage(ChatColor.RED + "Unknown stat: " + statName);
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Amount must be a positive integer.");
            return true;
        }

        if (stats.getUnspentPoints() < amount) {
            player.sendMessage(ChatColor.RED + "You don't have enough unspent points.");
            return true;
        }

        Stat stat = stats.getStat(statType);
        stats.removeUnspentPoints(amount);
        stat.add(amount);

        Bukkit.getPluginManager().callEvent(new PlayerAssignStatEvent(player, statType, amount));

        player.sendMessage(ChatColor.GREEN + "Assigned " + amount + " points to " + statType.name() + ".");
        return true;
    }
}
