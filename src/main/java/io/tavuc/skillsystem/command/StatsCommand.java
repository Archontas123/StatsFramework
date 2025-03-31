package io.tavuc.skillsystem.command;

import io.tavuc.skillsystem.gui.StatsGUI;
import io.tavuc.skillsystem.manager.StatManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * Command executor for opening the stats GUI.
 */
public class StatsCommand implements CommandExecutor{

    private final StatManager statManager;
    private final StatsGUI statsGUI;

    /**
     * Constructs the stats command.
     *
     * @param statsGUI2 The stat manager instance.
     */
    public StatsCommand(StatsGUI statsGUI2, StatManager statManager2) {
        this.statManager = statManager2;
        this.statsGUI = new StatsGUI(statManager2);
    }

    public StatsCommand(StatsGUI statsGUI) {
        this.statsGUI = statsGUI;
        this.statManager = null; 
    }

    public StatsCommand(StatManager statManager2) {
        this.statManager = statManager2; 
        this.statsGUI = null; 
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
