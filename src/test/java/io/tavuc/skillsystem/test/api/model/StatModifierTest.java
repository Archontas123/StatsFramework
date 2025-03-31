package io.tavuc.skillsystem.test.api.model;

import io.tavuc.skillsystem.api.model.ModifierType;
import io.tavuc.skillsystem.api.model.StatModifier;
import io.tavuc.skillsystem.test.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StatModifierTest extends UnitTest {
    
    @Test
    public void testConstructor() {
        UUID id = UUID.randomUUID();
        float value = 10.5f;
        ModifierType type = ModifierType.ADDITIVE;
        int duration = 100;
        String source = "test";
        
        StatModifier modifier = new StatModifier(id, value, type, duration, source);
        
        // Check all fields were set correctly
        assertEquals(id, modifier.getId());
        assertEquals(value, modifier.getValue(), 0.001);
        assertEquals(type, modifier.getType());
        assertEquals(duration, modifier.getDuration());
        assertEquals(source, modifier.getSource());
    }
    
    @Test
    public void testGetId() {
        UUID id = UUID.randomUUID();
        StatModifier modifier = new StatModifier(id, 10f, ModifierType.ADDITIVE, 100, "test");
        
        assertEquals(id, modifier.getId());
    }
    
    @Test
    public void testGetValue() {
        StatModifier modifier = new StatModifier(UUID.randomUUID(), 15.5f, ModifierType.ADDITIVE, 100, "test");
        
        assertEquals(15.5f, modifier.getValue(), 0.001);
    }
    
    @Test
    public void testGetType() {
        StatModifier additiveModifier = new StatModifier(
                UUID.randomUUID(), 10f, ModifierType.ADDITIVE, 100, "test");
        StatModifier multiplicativeModifier = new StatModifier(
                UUID.randomUUID(), 10f, ModifierType.MULTIPLICATIVE, 100, "test");
        
        assertEquals(ModifierType.ADDITIVE, additiveModifier.getType());
        assertEquals(ModifierType.MULTIPLICATIVE, multiplicativeModifier.getType());
    }
    
    @Test
    public void testGetDuration() {
        StatModifier modifier = new StatModifier(UUID.randomUUID(), 10f, ModifierType.ADDITIVE, 100, "test");
        
        assertEquals(100, modifier.getDuration());
    }
    
    @Test
    public void testSetDuration() {
        StatModifier modifier = new StatModifier(UUID.randomUUID(), 10f, ModifierType.ADDITIVE, 100, "test");
        
        modifier.setDuration(50);
        
        assertEquals(50, modifier.getDuration());
    }
    
    @Test
    public void testGetSource() {
        StatModifier modifier = new StatModifier(UUID.randomUUID(), 10f, ModifierType.ADDITIVE, 100, "potion");
        
        assertEquals("potion", modifier.getSource());
    }
    
    @Test
    public void testTickMethod() {
        // Test with temporary modifier
        StatModifier temporaryModifier = new StatModifier(
                UUID.randomUUID(), 10f, ModifierType.ADDITIVE, 3, "test");
        
        // Tick once
        assertTrue(temporaryModifier.tick());
        assertEquals(2, temporaryModifier.getDuration());
        
        // Tick twice
        assertTrue(temporaryModifier.tick());
        assertEquals(1, temporaryModifier.getDuration());
        
        // Tick three times - should expire
        assertFalse(temporaryModifier.tick());
        assertEquals(0, temporaryModifier.getDuration());
    }
    
    @Test
    public void testTickWithPermanentModifier() {
        StatModifier permanentModifier = new StatModifier(
                UUID.randomUUID(), 10f, ModifierType.ADDITIVE, -1, "test");
        
        // Permanent modifiers should always return true
        assertTrue(permanentModifier.tick());
        assertEquals(-1, permanentModifier.getDuration()); // Duration should stay -1
        
        // Tick again, still permanent
        assertTrue(permanentModifier.tick());
        assertEquals(-1, permanentModifier.getDuration());
    }
    
    @Test
    public void testGetFormattedDuration() {
        // Test permanent duration
        StatModifier permanentModifier = new StatModifier(
                UUID.randomUUID(), 10f, ModifierType.ADDITIVE, -1, "test");
        assertEquals("Permanent", permanentModifier.getFormattedDuration());
        
        // Test seconds
        StatModifier secondsModifier = new StatModifier(
                UUID.randomUUID(), 10f, ModifierType.ADDITIVE, 40, "test");
        assertEquals("2s", secondsModifier.getFormattedDuration());
        
        // Test minutes and seconds
        StatModifier minutesModifier = new StatModifier(
                UUID.randomUUID(), 10f, ModifierType.ADDITIVE, 1240, "test");
        assertEquals("1m 2s", minutesModifier.getFormattedDuration());
    }
    
    @ParameterizedTest
    @CsvSource({
        "20, 1s",
        "60, 3s",
        "100, 5s",
        "1200, 1m 0s",
        "1220, 1m 1s",
        "3600, 3m 0s"
    })
    public void testFormattedDurationFormatting(int ticks, String expected) {
        StatModifier modifier = new StatModifier(
                UUID.randomUUID(), 10f, ModifierType.ADDITIVE, ticks, "test");
        
        assertEquals(expected, modifier.getFormattedDuration());
    }
}