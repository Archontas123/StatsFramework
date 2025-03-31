package io.tavuc.skillsystem.test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.tavuc.skillsystem.api.SkillAPI;
import io.tavuc.skillsystem.api.model.ModifierType;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.StatType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

/**
 * End-to-end integration tests for the Skills Framework.
 * Tests complete user scenarios across multiple components.
 */
public class SkillSystemIntegrationTest extends MockBukkitTest {
    
    /**
     * Test the full cycle of a player:
     * - Joining the server
     * - Gaining experience
     * - Leveling up
     * - Assigning stat points
     * - Using abilities with stats
     * - Opening the stats GUI
     */
    @Test
    public void testCompletePlayerProgression() {
        // Add a player to the server
        PlayerMock player = server.addPlayer();
        
        // Get the player's stats through the API
        PlayerStats stats = SkillAPI.getStats(player);
        assertNotNull(stats);
        assertEquals(1, stats.getLevel());
        
        // Mock an attribute for health modifications
        AttributeInstance maxHealthAttribute = mock(AttributeInstance.class);
        when(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).thenReturn(maxHealthAttribute);
        
        // Level up the player
        plugin.getLevelManager().addExperience(player, stats, 500);
        
        // Player should now be at a higher level
        assertTrue(stats.getLevel() > 1);
        assertTrue(stats.getUnspentPoints() > 0);
        
        // Assign stat points
        int initialStrength = stats.getStat(StatType.STRENGTH).getBaseValue();
        player.performCommand("assign strength 3");
        
        // Verify stats were increased
        assertEquals(initialStrength + 3, stats.getStat(StatType.STRENGTH).getBaseValue());
        
        // Test combat mechanics with a mock target
        LivingEntity target = mock(LivingEntity.class);
        when(target.isValid()).thenReturn(true);
        when(target.isDead()).thenReturn(false);
        
        double baseDamage = 10.0;
        EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(
                player, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, baseDamage);
        
        // Process the damage event
        server.getPluginManager().callEvent(damageEvent);
        
        // Damage should be increased due to strength
        assertTrue(damageEvent.getDamage() > baseDamage);
        
        // Open stats GUI
        player.performCommand("stats");
        
        // Verify inventory was opened
        assertNotNull(player.getOpenInventory());
        
        // Test adding a temporary buff
        UUID effectId = stats.addStatEffect(StatType.AGILITY, 20.0f, ModifierType.ADDITIVE, 100, "test");
        assertNotNull(effectId);
        
        // Verify effect was applied
        assertTrue(stats.getStat(StatType.AGILITY).getValue() > stats.getStat(StatType.AGILITY).getBaseValue());
        
        // Update effects to simulate ticks passing
        for (int i = 0; i < 101; i++) {
            stats.updateEffects();
        }
        
        // Effect should have expired
        assertEquals(stats.getStat(StatType.AGILITY).getBaseValue(), stats.getStat(StatType.AGILITY).getValue());
        
        // Simulate player quitting and rejoining to test data persistence
        player.disconnect();
        
        // Re-add the player
        player = server.addPlayer(player.getName());
        
        // Get stats again - they should match what we had before
        PlayerStats newStats = SkillAPI.getStats(player);
        assertEquals(stats.getLevel(), newStats.getLevel());
        assertEquals(stats.getStat(StatType.STRENGTH).getBaseValue(), 
                newStats.getStat(StatType.STRENGTH).getBaseValue());
    }
    
    /**
     * Test admin commands for managing player stats.
     */
    @Test
    public void testAdminCommandFlow() {
        // Add an admin player
        PlayerMock admin = server.addPlayer("admin");
        admin.setOp(true);
        admin.addAttachment(plugin, "skillsystem.admin", true);
        
        // Add a target player
        PlayerMock target = server.addPlayer("target");
        
        // Set target's level using admin command
        admin.performCommand("skillsadmin setlevel target 10");
        
        // Verify level was set
        PlayerStats targetStats = SkillAPI.getStats(target);
        assertEquals(10, targetStats.getLevel());
        
        // Add experience using admin command
        admin.performCommand("skillsadmin addexp target 500");
        
        // Check target's stats
        int expBefore = targetStats.getExperience();
        assertTrue(targetStats.getExperience() > expBefore);
        
        // Set a stat directly
        admin.performCommand("skillsadmin setstat target strength 50");
        assertEquals(50, targetStats.getStat(StatType.STRENGTH).getBaseValue());
        
        // Add a temporary effect
        admin.performCommand("skillsadmin addeffect target agility 20 ADDITIVE 100 test");
        assertTrue(targetStats.getStat(StatType.AGILITY).getValue() > 
                targetStats.getStat(StatType.AGILITY).getBaseValue());
        
        // Remove the effect
        admin.performCommand("skillsadmin removeeffect target test");
        assertEquals(targetStats.getStat(StatType.AGILITY).getBaseValue(), 
                targetStats.getStat(StatType.AGILITY).getValue());
        
        // Reset player stats
        admin.performCommand("skillsadmin reset target all");
        assertEquals(1, targetStats.getLevel());
        assertEquals(0, targetStats.getExperience());
        assertEquals(0, targetStats.getStat(StatType.STRENGTH).getBaseValue());
    }
    
    /**
     * Test the performance of the plugin with multiple players.
     */
    @Test
    public void testMultiPlayerPerformance() {
        final int PLAYER_COUNT = 10;
        
        // Add multiple players
        for (int i = 0; i < PLAYER_COUNT; i++) {
            server.addPlayer("player" + i);
        }
        
        // Verify all players have stats
        for (PlayerMock player : server.getOnlinePlayers()) {
            assertNotNull(SkillAPI.getStats(player));
        }
        
        // Add effects to all players
        for (PlayerMock player : server.getOnlinePlayers()) {
            PlayerStats stats = SkillAPI.getStats(player);
            stats.addStatEffect(StatType.STRENGTH, 10.0f, ModifierType.ADDITIVE, 100, "test");
        }
        
        // Simulate ticks - shouldn't cause any errors
        for (int i = 0; i < 101; i++) {
            for (PlayerMock player : server.getOnlinePlayers()) {
                PlayerStats stats = SkillAPI.getStats(player);
                stats.updateEffects();
            }
        }
        
        // Clean version
        server.getScheduler().performTicks(120);
    }
}