package io.tavuc.skillsystem.command;

import io.tavuc.skillsystem.gui.StatsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final StatsGUI statsGUI;

    public StatsCommand(StatsGUI statsGUI) {
        this.statsGUI = statsGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        statsGUI.open(player);
        return true;
    }
}
