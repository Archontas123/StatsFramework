package io.tavuc.skillsystem.test.api.model;

import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.api.model.ModifierType;
import io.tavuc.skillsystem.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerStatsTest extends UnitTest {
    
    private PlayerStats playerStats;
    
    @BeforeEach
    public void setUp() {
        playerStats = new PlayerStats();
    }
    
    @Test
    public void testInitialValues() {
        assertEquals(1, playerStats.getLevel());
        assertEquals(0, playerStats.getExperience());
        assertEquals(0, playerStats.getUnspentPoints());
        
        for (StatType type : StatType.values()) {
            assertNotNull(playerStats.getStat(type));
            assertEquals(0, playerStats.getStat(type).getBaseValue());
        }
    }
    
    @Test
    public void testSetLevel() {
        playerStats.setLevel(10);
        assertEquals(10, playerStats.getLevel());
    }
    
    @Test
    public void testSetExperience() {
        playerStats.setExperience(500);
        assertEquals(500, playerStats.getExperience());
    }
    
    @Test
    public void testUnspentPoints() {
        playerStats.addUnspentPoints(5);
        assertEquals(5, playerStats.getUnspentPoints());
        
        playerStats.removeUnspentPoints(3);
        assertEquals(2, playerStats.getUnspentPoints());
        
        playerStats.removeUnspentPoints(5);
        assertEquals(0, playerStats.getUnspentPoints());
    }
    
    @Test
    public void testGetAllStats() {
        assertEquals(StatType.values().length, playerStats.getAllStats().size());
    }
    
    @Test
    public void testAddStatEffect() {
        UUID effectId = playerStats.addStatEffect(StatType.STRENGTH, 10.0f, ModifierType.ADDITIVE, -1, "test");
        
        assertNotNull(effectId);
        assertEquals(10, playerStats.getStat(StatType.STRENGTH).getValue());
    }
    
    @Test
    public void testRemoveEffect() {
        UUID effectId = playerStats.addStatEffect(StatType.STRENGTH, 10.0f, ModifierType.ADDITIVE, -1, "test");
        assertTrue(playerStats.removeEffect(effectId));
        assertEquals(0, playerStats.getStat(StatType.STRENGTH).getValue());
    }
    
    @Test
    public void testRemoveEffectsFromSource() {
        playerStats.addStatEffect(StatType.STRENGTH, 10.0f, ModifierType.ADDITIVE, -1, "source1");
        playerStats.addStatEffect(StatType.DEFENSE, 20.0f, ModifierType.ADDITIVE, -1, "source1");
        playerStats.addStatEffect(StatType.VITALITY, 30.0f, ModifierType.ADDITIVE, -1, "source2");
        
        int count = playerStats.removeEffectsFromSource("source1");
        assertEquals(2, count);
        
        assertEquals(0, playerStats.getStat(StatType.STRENGTH).getValue());
        assertEquals(0, playerStats.getStat(StatType.DEFENSE).getValue());
        assertEquals(30, playerStats.getStat(StatType.VITALITY).getValue());
    }
    
    @Test
    public void testUpdateEffects() {
        playerStats.addStatEffect(StatType.STRENGTH, 10.0f, ModifierType.ADDITIVE, 2, "test");
        assertEquals(10, playerStats.getStat(StatType.STRENGTH).getValue());
        
        playerStats.updateEffects();
        assertEquals(10, playerStats.getStat(StatType.STRENGTH).getValue());
        
        playerStats.updateEffects();
        assertEquals(0, playerStats.getStat(StatType.STRENGTH).getValue());
    }
    
    @Test
    public void testMaxStatValueForLevel() {
        assertEquals(50, playerStats.getMaxStatValueForLevel(1));
        assertEquals(50, playerStats.getMaxStatValueForLevel(9));
        assertEquals(100, playerStats.getMaxStatValueForLevel(10));
        assertEquals(200, playerStats.getMaxStatValueForLevel(25));
        assertEquals(350, playerStats.getMaxStatValueForLevel(50));
        assertEquals(500, playerStats.getMaxStatValueForLevel(75));
    }
}