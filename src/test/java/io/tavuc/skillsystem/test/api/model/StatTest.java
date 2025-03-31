package io.tavuc.skillsystem.test.api.model;

import io.tavuc.skillsystem.api.model.Stat;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.api.model.ModifierType;
import io.tavuc.skillsystem.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StatTest extends UnitTest {
    
    private Stat stat;
    
    @BeforeEach
    public void setUp() {
        stat = new Stat(StatType.STRENGTH, 50, 100);
    }
    
    @Test
    public void testBaseValueRespectsMaximum() {
        stat.setBaseValue(150);
        assertEquals(100, stat.getBaseValue());
    }
    
    @Test
    public void testBaseValueRespectsMinimum() {
        stat.setBaseValue(-10);
        assertEquals(0, stat.getBaseValue());
    }
    
    @Test
    public void testAddToBaseValue() {
        stat.add(20);
        assertEquals(70, stat.getBaseValue());
    }
    
    @Test
    public void testRemoveFromBaseValue() {
        stat.remove(20);
        assertEquals(30, stat.getBaseValue());
    }
    
    @Test
    public void testAdditiveModifier() {
        stat.addModifier(10.0f, ModifierType.ADDITIVE, -1, "test");
        assertEquals(60, stat.getValue());
    }
    
    @Test
    public void testMultiplicativeModifier() {
        stat.addModifier(50.0f, ModifierType.MULTIPLICATIVE, -1, "test");
        assertEquals(75, stat.getValue());
    }
    
    @Test
    public void testCombinedModifiers() {
        stat.addModifier(10.0f, ModifierType.ADDITIVE, -1, "test");
        stat.addModifier(50.0f, ModifierType.MULTIPLICATIVE, -1, "test");
        assertEquals(90, stat.getValue());
    }
    
    @Test
    public void testTemporaryModifierExpires() {
        stat.addModifier(10.0f, ModifierType.ADDITIVE, 2, "test");
        assertEquals(60, stat.getValue());
        
        stat.updateModifiers();
        assertEquals(60, stat.getValue());
        
        stat.updateModifiers();
        assertEquals(50, stat.getValue());
    }
    
    @Test
    public void testRemoveModifierById() {
        UUID id = stat.addModifier(10.0f, ModifierType.ADDITIVE, -1, "test");
        assertEquals(60, stat.getValue());
        
        assertTrue(stat.removeModifier(id));
        assertEquals(50, stat.getValue());
    }
    
    @Test
    public void testRemoveModifiersBySource() {
        stat.addModifier(10.0f, ModifierType.ADDITIVE, -1, "source1");
        stat.addModifier(20.0f, ModifierType.ADDITIVE, -1, "source2");
        assertEquals(80, stat.getValue());
        
        int count = stat.removeModifiersFromSource("source1");
        assertEquals(1, count);
        assertEquals(70, stat.getValue());
    }
    
    @ParameterizedTest
    @CsvSource({
        "ADDITIVE, 10, 60",
        "MULTIPLICATIVE, 20, 60",
        "ADDITIVE, -10, 40",
        "MULTIPLICATIVE, -20, 40"
    })
    public void testDifferentModifiers(ModifierType type, float value, int expected) {
        stat.addModifier(value, type, -1, "test");
        assertEquals(expected, stat.getValue());
    }
}