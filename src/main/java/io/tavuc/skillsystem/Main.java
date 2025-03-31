package io.tavuc.skillsystem;

import io.tavuc.skillsystem.api.SkillAPI;
import io.tavuc.skillsystem.command.AssignStatCommand;
import io.tavuc.skillsystem.command.StatsCommand;
import io.tavuc.skillsystem.gui.StatsGUI;
import io.tavuc.skillsystem.manager.LevelManager;
import io.tavuc.skillsystem.manager.StatEffectCalculator;
import io.tavuc.skillsystem.manager.StatManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the Skill Framework plugin.
 */
public class Main extends JavaPlugin {

    private StatManager statManager;
    private LevelManager levelManager;
    private StatsGUI statsGUI;
    private StatEffectCalculator effectCalculator;

    @Override
    public void onEnable() {
        this.levelManager = new LevelManager();
        this.statManager = new StatManager(this, levelManager);
        this.effectCalculator = new StatEffectCalculator(statManager);
        this.statsGUI = new StatsGUI(statManager);

        SkillAPI.register(statManager);

        getCommand("stats").setExecutor(new StatsCommand(statsGUI));

        getCommand("assign").setExecutor(new AssignStatCommand(statManager));

        getLogger().info("Skill Framework Loaded ✅");
    }

    @Override
    public void onDisable() {
        SkillAPI.unregister();
        getLogger().info("Skill Framework Disabled ⛔");
    }

    public StatManager getStatManager() {
        return statManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public StatEffectCalculator getEffectCalculator() {
        return effectCalculator;
    }
}
