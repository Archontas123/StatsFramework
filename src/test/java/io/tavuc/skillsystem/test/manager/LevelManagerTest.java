package io.tavuc.skillsystem.test.manager;

import io.tavuc.skillsystem.api.events.PlayerLevelUpEvent;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.config.ConfigManager;
import io.tavuc.skillsystem.manager.LevelManager;
import io.tavuc.skillsystem.test.UnitTest;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LevelManagerTest extends UnitTest {
    
    @Mock
    private Plugin plugin;
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private Player player;
    
    @Mock
    private PluginManager pluginManager;
    
    private LevelManager levelManager;
    private PlayerStats playerStats;
    
    @BeforeEach
    public void setUp() {
        when(plugin.getServer()).thenReturn(mock(org.bukkit.Server.class));
        when(plugin.getServer().getPluginManager()).thenReturn(pluginManager);
        
        when(configManager.getInt("leveling.base-exp-requirement", 100)).thenReturn(100);
        when(configManager.getInt("leveling.linear-increase", 50)).thenReturn(50);
        when(configManager.getString("leveling.exp-formula", "linear")).thenReturn("linear");
        when(configManager.getInt("leveling.max-level", 100)).thenReturn(100);
        when(configManager.getInt("leveling.default-stat-points", 5)).thenReturn(5);
        when(configManager.getBoolean("leveling.effects.enabled", true)).thenReturn(false);
        
        levelManager = new LevelManager(plugin, configManager);
        playerStats = new PlayerStats();
    }
    
    @Test
    public void testRequiredExpForNextLevel() {
        assertEquals(100, levelManager.getRequiredExpForNextLevel(1));
        assertEquals(150, levelManager.getRequiredExpForNextLevel(2));
        assertEquals(200, levelManager.getRequiredExpForNextLevel(3));
    }
    
    @Test
    public void testGetStatPointsForLevel() {
        when(plugin.getConfig()).thenReturn(mock(org.bukkit.configuration.file.FileConfiguration.class));
        when(plugin.getConfig().getConfigurationSection("leveling.stat-points-per-level")).thenReturn(null);
        
        assertEquals(5, levelManager.getStatPointsForLevel(1));
    }
    
    @Test
    public void testAddExperienceNoLevelUp() {
        playerStats.setLevel(1);
        playerStats.setExperience(50);
        
        levelManager.addExperience(player, playerStats, 20);
        
        assertEquals(1, playerStats.getLevel());
        assertEquals(70, playerStats.getExperience());
        
        verify(pluginManager, never()).callEvent(any(PlayerLevelUpEvent.class));
    }
    
    @Test
    public void testAddExperienceWithLevelUp() {
        playerStats.setLevel(1);
        playerStats.setExperience(90);
        
        levelManager.addExperience(player, playerStats, 20);
        
        assertEquals(2, playerStats.getLevel());
        assertEquals(10, playerStats.getExperience());
        
        verify(pluginManager).callEvent(any(PlayerLevelUpEvent.class));
    }
    
    @Test
    public void testAddExperienceWithMultipleLevelUps() {
        playerStats.setLevel(1);
        playerStats.setExperience(90);
        
        levelManager.addExperience(player, playerStats, 170);
        
        assertEquals(3, playerStats.getLevel());
        assertEquals(10, playerStats.getExperience());
        
        verify(pluginManager, times(2)).callEvent(any(PlayerLevelUpEvent.class));
    }
    
    @Test
    public void testAddExperienceAtMaxLevel() {
        when(configManager.getInt("leveling.max-level", 100)).thenReturn(5);
        
        playerStats.setLevel(5);
        playerStats.setExperience(50);
        
        levelManager.addExperience(player, playerStats, 100);
        
        assertEquals(5, playerStats.getLevel());
        assertEquals(50, playerStats.getExperience());
        
        verify(pluginManager, never()).callEvent(any(PlayerLevelUpEvent.class));
    }
    
    @Test
    public void testGetLevelProgress() {
        playerStats.setLevel(1);
        playerStats.setExperience(75);
        
        assertEquals(75.0, levelManager.getLevelProgress(playerStats), 0.01);
    }
    
    @Test
    public void testGetTotalExpToLevel() {
        assertEquals(0, levelManager.getTotalExpToLevel(1));
        assertEquals(100, levelManager.getTotalExpToLevel(2));
        assertEquals(250, levelManager.getTotalExpToLevel(3));
        assertEquals(450, levelManager.getTotalExpToLevel(4));
    }
    
    @ParameterizedTest
    @CsvSource({
        "1, 50, 70, 1, 70",
        "1, 90, 20, 2, 10",
        "2, 140, 20, 3, 10",
        "1, 50, 250, 3, 0"
    })
    public void testVariousExperienceScenarios(int startLevel, int startExp, int addExp,
                                              int expectedLevel, int expectedExp) {
        playerStats.setLevel(startLevel);
        playerStats.setExperience(startExp);
        
        levelManager.addExperience(player, playerStats, addExp);
        
        assertEquals(expectedLevel, playerStats.getLevel());
        assertEquals(expectedExp, playerStats.getExperience());
    }
}