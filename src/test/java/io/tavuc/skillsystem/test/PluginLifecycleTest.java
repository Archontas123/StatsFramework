package io.tavuc.skillsystem.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.tavuc.skillsystem.Main;
import io.tavuc.skillsystem.api.SkillAPI;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.StatType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PluginLifecycleTest {
    
    private ServerMock server;
    private Main plugin;
    
    @BeforeEach
    public void setUp() {
        // Start the server and load the plugin
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Main.class);
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up after each test
        MockBukkit.unmock();
    }
    
    @Test
    public void testPluginEnables() {
        // Verify the plugin was enabled successfully
        assertTrue(plugin.isEnabled());
        
        // Verify the API was registered
        assertTrue(SkillAPI.isRegistered());
        assertNotNull(SkillAPI.getStatManager());
    }
    
    @Test
    public void testPluginDisables() {
        // Add a player and set some stats
        PlayerMock player = server.addPlayer();
        PlayerStats stats = plugin.getStatManager().getPlayerStats(player.getUniqueId());
        stats.setLevel(10);
        
        // Disable the plugin
        MockBukkit.unmock();
        
        // Verify the plugin was disabled
        assertFalse(plugin.isEnabled());
        
        // Verify the API was unregistered
        assertFalse(SkillAPI.isRegistered());
    }
    
    @Test
    public void testPluginReload() {
        // Add a player and set some stats
        PlayerMock player = server.addPlayer();
        PlayerStats stats = plugin.getStatManager().getPlayerStats(player.getUniqueId());
        stats.setLevel(10);
        stats.getStat(StatType.STRENGTH).setBaseValue(20);
        
        // Get references to managers before reload
        final var statManager = plugin.getStatManager();
        final var levelManager = plugin.getLevelManager();
        
        // Reload the plugin
        plugin.reload();
        
        // Verify the plugin is still enabled
        assertTrue(plugin.isEnabled());
        
        // Verify player data was preserved (in-memory test)
        PlayerStats statsAfterReload = plugin.getStatManager().getPlayerStats(player.getUniqueId());
        assertEquals(10, statsAfterReload.getLevel());
        assertEquals(20, statsAfterReload.getStat(StatType.STRENGTH).getBaseValue());
        
        // Verify level manager was re-initialized (due to config changes)
        assertNotSame(levelManager, plugin.getLevelManager());
        
        // Verify stat manager kept the same instance but updated its level manager
        assertSame(statManager, plugin.getStatManager());
    }
    
    @Test
    public void testCommandRegistration() {
        // Verify commands were registered
        assertNotNull(server.getPluginCommand("stats"));
        assertEquals(plugin, server.getPluginCommand("stats").getPlugin());
        
        assertNotNull(server.getPluginCommand("assign"));
        assertEquals(plugin, server.getPluginCommand("assign").getPlugin());
        
        assertNotNull(server.getPluginCommand("skillsadmin"));
        assertEquals(plugin, server.getPluginCommand("skillsadmin").getPlugin());
    }
    
    @Test
    public void testPluginStartsTasksCorrectly() {
        // Verify scheduled tasks were started
        assertTrue(server.getScheduler().getPendingTasks().stream()
                .anyMatch(task -> task.getOwner().equals(plugin)));
        
  
        // Verify default configs were created
        assertTrue(server.getPluginsFolder().toPath()
                .resolve(plugin.getDataFolder().toPath().getFileName())
                .resolve("config.yml").toFile().exists());
        
        assertTrue(server.getPluginsFolder().toPath()
                .resolve(plugin.getDataFolder().toPath().getFileName())
                .resolve("messages.yml").toFile().exists());
        
        assertTrue(server.getPluginsFolder().toPath()
                .resolve(plugin.getDataFolder().toPath().getFileName())
                .resolve("stats.yml").toFile().exists());
        
        assertTrue(server.getPluginsFolder().toPath()
                .resolve(plugin.getDataFolder().toPath().getFileName())
                .resolve("formulas.yml").toFile().exists());
    }
}