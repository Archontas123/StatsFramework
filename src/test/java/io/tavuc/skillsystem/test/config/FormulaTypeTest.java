package io.tavuc.skillsystem.test.config;

import io.tavuc.skillsystem.config.FormulaType;
import io.tavuc.skillsystem.test.UnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FormulaTypeTest extends UnitTest {
    
    @Test
    public void testEnumValues() {
        // Test that all expected enum values exist
        FormulaType[] types = FormulaType.values();
        
        assertEquals(4, types.length);
        
        // Verify each enum value
        assertEquals(FormulaType.LINEAR, types[0]);
        assertEquals(FormulaType.DIMINISHING, types[1]);
        assertEquals(FormulaType.STEPPED, types[2]);
        assertEquals(FormulaType.CHANCE, types[3]);
    }
    
    @Test
    public void testValueOf() {
        // Test that valueOf works correctly
        assertEquals(FormulaType.LINEAR, FormulaType.valueOf("LINEAR"));
        assertEquals(FormulaType.DIMINISHING, FormulaType.valueOf("DIMINISHING"));
        assertEquals(FormulaType.STEPPED, FormulaType.valueOf("STEPPED"));
        assertEquals(FormulaType.CHANCE, FormulaType.valueOf("CHANCE"));
    }
    
    @Test
    public void testValueOfInvalidValue() {
        // Test that valueOf throws exception for invalid values
        assertThrows(IllegalArgumentException.class, () -> FormulaType.valueOf("INVALID"));
    }
    
    @Test
    public void testEnumOrdinal() {
        // Test ordinals
        assertEquals(0, FormulaType.LINEAR.ordinal());
        assertEquals(1, FormulaType.DIMINISHING.ordinal());
        assertEquals(2, FormulaType.STEPPED.ordinal());
        assertEquals(3, FormulaType.CHANCE.ordinal());
    }
    
    @Test
    public void testEnumToString() {
        // Test toString
        assertEquals("LINEAR", FormulaType.LINEAR.toString());
        assertEquals("DIMINISHING", FormulaType.DIMINISHING.toString());
        assertEquals("STEPPED", FormulaType.STEPPED.toString());
        assertEquals("CHANCE", FormulaType.CHANCE.toString());
    }
    
    @Test
    public void testEnumNameMethod() {
        // Test name()
        assertEquals("LINEAR", FormulaType.LINEAR.name());
        assertEquals("DIMINISHING", FormulaType.DIMINISHING.name());
        assertEquals("STEPPED", FormulaType.STEPPED.name());
        assertEquals("CHANCE", FormulaType.CHANCE.name());
    }
    
    @Test
    public void testEnumHashCode() {
        // Test that hashcode is consistent
        int hashCode1 = FormulaType.LINEAR.hashCode();
        int hashCode2 = FormulaType.LINEAR.hashCode();
        
        assertEquals(hashCode1, hashCode2);
    }
    
    @Test
    public void testEnumEquals() {
        // Test equals()
        assertEquals(FormulaType.LINEAR, FormulaType.LINEAR);
        assertNotEquals(FormulaType.LINEAR, FormulaType.DIMINISHING);
        assertNotEquals(FormulaType.LINEAR, null);
        assertNotEquals(FormulaType.LINEAR, "LINEAR");
    }
}