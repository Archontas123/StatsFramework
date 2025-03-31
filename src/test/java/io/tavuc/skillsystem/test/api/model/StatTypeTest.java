package io.tavuc.skillsystem.test.api.model;

import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.test.UnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StatTypeTest extends UnitTest {
    
    @Test
    public void testEnumValues() {
        // Test that all expected enum values exist
        StatType[] types = StatType.values();
        
        // Should have 6 values (STRENGTH, DEFENSE, VITALITY, FEROCITY, AGILITY, INTELLIGENCE)
        assertEquals(6, types.length);
        
        // Verify expected enum values exist
        assertTrue(containsStatType(types, "STRENGTH"));
        assertTrue(containsStatType(types, "DEFENSE"));
        assertTrue(containsStatType(types, "VITALITY"));
        assertTrue(containsStatType(types, "FEROCITY"));
        assertTrue(containsStatType(types, "AGILITY"));
        assertTrue(containsStatType(types, "INTELLIGENCE"));
    }
    
    @Test
    public void testValueOf() {
        // Test that valueOf works correctly for each type
        assertEquals(StatType.STRENGTH, StatType.valueOf("STRENGTH"));
        assertEquals(StatType.DEFENSE, StatType.valueOf("DEFENSE"));
        assertEquals(StatType.VITALITY, StatType.valueOf("VITALITY"));
        assertEquals(StatType.FEROCITY, StatType.valueOf("FEROCITY"));
        assertEquals(StatType.AGILITY, StatType.valueOf("AGILITY"));
        assertEquals(StatType.INTELLIGENCE, StatType.valueOf("INTELLIGENCE"));
    }
    
    @Test
    public void testValueOfInvalidValue() {
        // Test that valueOf throws exception for invalid values
        assertThrows(IllegalArgumentException.class, () -> StatType.valueOf("INVALID"));
    }
    
    @Test
    public void testEnumOrdinal() {
        // Test that ordinals are as expected
        // Note: Ordinals can change if enum order changes, so this might need updating
        assertEquals(0, StatType.STRENGTH.ordinal());
        assertEquals(1, StatType.DEFENSE.ordinal());
        assertEquals(2, StatType.VITALITY.ordinal());
        assertEquals(3, StatType.FEROCITY.ordinal());
        assertEquals(4, StatType.AGILITY.ordinal());
        assertEquals(5, StatType.INTELLIGENCE.ordinal());
    }
    
    @Test
    public void testEnumName() {
        // Test name() method
        assertEquals("STRENGTH", StatType.STRENGTH.name());
        assertEquals("DEFENSE", StatType.DEFENSE.name());
        assertEquals("VITALITY", StatType.VITALITY.name());
        assertEquals("FEROCITY", StatType.FEROCITY.name());
        assertEquals("AGILITY", StatType.AGILITY.name());
        assertEquals("INTELLIGENCE", StatType.INTELLIGENCE.name());
    }
    
    @Test
    public void testEnumToString() {
        // Test toString() method
        assertEquals("STRENGTH", StatType.STRENGTH.toString());
        assertEquals("DEFENSE", StatType.DEFENSE.toString());
        assertEquals("VITALITY", StatType.VITALITY.toString());
        assertEquals("FEROCITY", StatType.FEROCITY.toString());
        assertEquals("AGILITY", StatType.AGILITY.toString());
        assertEquals("INTELLIGENCE", StatType.INTELLIGENCE.toString());
    }
    
    @Test
    public void testEnumEquals() {
        // Test equals() method
        assertEquals(StatType.STRENGTH, StatType.STRENGTH);
        assertNotEquals(StatType.STRENGTH, StatType.DEFENSE);
        assertNotEquals(StatType.STRENGTH, null);
        assertNotEquals(StatType.STRENGTH, "STRENGTH");
    }
    
    @Test
    public void testEnumHashCode() {
        // Test that hashCode is consistent
        assertEquals(StatType.STRENGTH.hashCode(), StatType.STRENGTH.hashCode());
        
        // Different enum values should have different hash codes
        assertNotEquals(StatType.STRENGTH.hashCode(), StatType.DEFENSE.hashCode());
    }
    
    @Test
    public void testCompareTo() {
        // Test compareTo method
        assertTrue(StatType.STRENGTH.compareTo(StatType.DEFENSE) < 0);
        assertTrue(StatType.DEFENSE.compareTo(StatType.STRENGTH) > 0);
        assertEquals(0, StatType.STRENGTH.compareTo(StatType.STRENGTH));
    }
    
    // Helper method to check if an array of StatType contains a stat with the given name
    private boolean containsStatType(StatType[] types, String name) {
        for (StatType type : types) {
            if (type.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}