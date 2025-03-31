package io.tavuc.skillsystem.test.manager;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.tavuc.skillsystem.api.SkillAPI;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.Stat;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.test.MockBukkitTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for StatManager that uses the actual plugin instance.
 */
public class StatManagerIntegrationTest extends MockBukkitTest {
    
    @Test
    public void testGetPlayerStats() {
        // Add a player
        PlayerMock player = server.addPlayer();
        
        // Get stats via API
        PlayerStats stats = SkillAPI.getStats(player);
        
        // Test that stats are initialized correctly
        assertNotNull(stats);
        assertEquals(1, stats.getLevel());
        assertEquals(0, stats.getExperience());
        assertEquals(0, stats.getUnspentPoints());
        
        // Test that all stat types are initialized
        for (StatType type : StatType.values()) {
            Stat stat = stats.getStat(type);
            assertNotNull(stat);
            assertEquals(type, stat.getType());
            assertEquals(0, stat.getBaseValue());
        }
    }
    
    @Test
    public void testStatIsPersistent() {
        // Add a player
        PlayerMock player = server.addPlayer();
        
        // Get stats and modify
        PlayerStats stats = SkillAPI.getStats(player);
        stats.getStat(StatType.STRENGTH).setBaseValue(10);
        
        // Get stats again via manager
        PlayerStats sameStats = plugin.getStatManager().getPlayerStats(player.getUniqueId());
        
        // Confirm changes persisted
        assertEquals(10, sameStats.getStat(StatType.STRENGTH).getBaseValue());
    }
    
    @Test
    public void testRegisterCustomStat() {
        // Register a custom stat
        plugin.getStatManager().registerStat("custom_power", StatType.STRENGTH);
        
        // Add a player
        PlayerMock player = server.addPlayer();
        
        // Try to modify the stat using the custom name
        player.performCommand("assign custom_power 5");
        
        // Check if the stat was modified
        PlayerStats stats = SkillAPI.getStats(player);
        assertEquals(5, stats.getStat(StatType.STRENGTH).getBaseValue());
    }
    
    @Test
    public void testMultiplePlayersHaveSeparateStats() {
        // Add two players
        PlayerMock player1 = server.addPlayer("player1");
        PlayerMock player2 = server.addPlayer("player2");
        
        // Get and modify stats for player1
        PlayerStats stats1 = SkillAPI.getStats(player1);
        stats1.getStat(StatType.STRENGTH).setBaseValue(10);
        stats1.setLevel(5);
        
        // Get and modify stats for player2
        PlayerStats stats2 = SkillAPI.getStats(player2);
        stats2.getStat(StatType.DEFENSE).setBaseValue(15);
        stats2.setLevel(3);
        
        // Get stats again via manager
        PlayerStats newStats1 = plugin.getStatManager().getPlayerStats(player1.getUniqueId());
        PlayerStats newStats2 = plugin.getStatManager().getPlayerStats(player2.getUniqueId());
        
        // Confirm player1 stats
        assertEquals(10, newStats1.getStat(StatType.STRENGTH).getBaseValue());
        assertEquals(0, newStats1.getStat(StatType.DEFENSE).getBaseValue());
        assertEquals(5, newStats1.getLevel());
        
        // Confirm player2 stats
        assertEquals(0, newStats2.getStat(StatType.STRENGTH).getBaseValue());
        assertEquals(15, newStats2.getStat(StatType.DEFENSE).getBaseValue());
        assertEquals(3, newStats2.getLevel());
    }
    
    @Test
    public void testGetRegisteredStats() {
        // Get registered stats
        Map<String, StatType> stats = plugin.getStatManager().getRegisteredStats();
        
        // Should contain all enum values
        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        
        // Check each enum value exists
        for (StatType type : StatType.values()) {
            String key = type.name().toLowerCase();
            assertTrue(stats.containsKey(key));
            assertEquals(type, stats.get(key));
        }
    }
    
    @Test
    public void testStatTypeRegistration() {
        // Register a custom stat
        String customName = "awesome_power";
        StatType type = StatType.FEROCITY;
        plugin.getStatManager().registerStat(customName, type);
        
        // Check that the custom name is registered
        assertTrue(plugin.getStatManager().isRegistered(customName));
        assertEquals(type, plugin.getStatManager().getStatType(customName));
        
        // Check case insensitivity
        assertTrue(plugin.getStatManager().isRegistered("AWESOME_power"));
        assertEquals(type, plugin.getStatManager().getStatType("AWESOME_power"));
    }
}