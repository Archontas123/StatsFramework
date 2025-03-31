package io.tavuc.skillsystem.test.config;

import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.config.StatConfig;
import io.tavuc.skillsystem.test.UnitTest;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class StatConfigTest extends UnitTest {
    
    @Test
    public void testDefaultConstructor() {
        StatConfig config = new StatConfig(StatType.STRENGTH);
        
        assertEquals(StatType.STRENGTH, config.getType());
        assertEquals("STRENGTH", config.getDisplayName());
        assertEquals("", config.getDescription());
        assertEquals(Material.BOOK, config.getIcon());
        assertEquals(1.0, config.getBaseScale(), 0.001);
        assertFalse(config.isHidden());
    }
    
    @Test
    public void testFullConstructor() {
        StatConfig config = new StatConfig(
                StatType.STRENGTH,
                "Super Strength",
                "Makes you strong",
                Material.DIAMOND_SWORD,
                2.5,
                true
        );
        
        assertEquals(StatType.STRENGTH, config.getType());
        assertEquals("Super Strength", config.getDisplayName());
        assertEquals("Makes you strong", config.getDescription());
        assertEquals(Material.DIAMOND_SWORD, config.getIcon());
        assertEquals(2.5, config.getBaseScale(), 0.001);
        assertTrue(config.isHidden());
    }
    
    @ParameterizedTest
    @CsvSource({
        "STRENGTH, Strength, Increases damage, IRON_SWORD, 1.0, false",
        "DEFENSE, Defense, Reduces damage, SHIELD, 1.5, false",
        "VITALITY, Vitality, Increases health, GOLDEN_APPLE, 2.0, false",
        "FEROCITY, Ferocity, Critical hits, NETHERITE_SWORD, 0.5, true"
    })
    public void testMultipleConfigs(
            StatType type, 
            String displayName, 
            String description, 
            String iconName, 
            double baseScale, 
            boolean hidden) {
        
        Material icon = Material.valueOf(iconName);
        
        StatConfig config = new StatConfig(type, displayName, description, icon, baseScale, hidden);
        
        assertEquals(type, config.getType());
        assertEquals(displayName, config.getDisplayName());
        assertEquals(description, config.getDescription());
        assertEquals(icon, config.getIcon());
        assertEquals(baseScale, config.getBaseScale(), 0.001);
        assertEquals(hidden, config.isHidden());
    }
    
    @Test
    public void testGetters() {
        StatConfig config = new StatConfig(
                StatType.STRENGTH,
                "Super Strength",
                "Makes you strong",
                Material.DIAMOND_SWORD,
                2.5,
                true
        );
        
        // Call all getters for code coverage
        assertNotNull(config.getType());
        assertNotNull(config.getDisplayName());
        assertNotNull(config.getDescription());
        assertNotNull(config.getIcon());
        assertTrue(config.getBaseScale() > 0);
        assertTrue(config.isHidden());
    }
    
    @Test
    public void testEquality() {
        StatConfig config1 = new StatConfig(StatType.STRENGTH);
        StatConfig config2 = new StatConfig(StatType.STRENGTH);
        StatConfig config3 = new StatConfig(StatType.DEFENSE);
        
        // Stats with same type should not be equal (they're different objects)
        assertNotSame(config1, config2);
        
        // Stats with different types should not be equal
        assertNotEquals(config1, config3);
        
        // A stat is not equal to null or other types
        assertNotEquals(config1, null);
        assertNotEquals(config1, "not a stat");
    }
}