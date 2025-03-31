package io.tavuc.skillsystem.test.data;

import io.tavuc.skillsystem.api.model.ModifierType;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.Stat;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.config.ConfigManager;
import io.tavuc.skillsystem.data.DataManager;
import io.tavuc.skillsystem.manager.StatManager;
import io.tavuc.skillsystem.test.UnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DataManagerTest extends UnitTest {
    
    @TempDir
    Path tempDir;
    
    @Mock
    private Plugin plugin;
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private StatManager statManager;
    
    @Mock
    private Server server;
    
    @Mock
    private Player player1;
    
    @Mock
    private Player player2;
    
    @Mock
    private PlayerStats playerStats1;
    
    @Mock
    private PlayerStats playerStats2;
    
    @Mock
    private Stat strengthStat;
    
    private DataManager dataManager;
    private File dataFolder;
    private UUID player1Id = UUID.randomUUID();
    private UUID player2Id = UUID.randomUUID();
    
    @BeforeEach
    public void setUp() {
        // Setup plugin mock
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("Test"));
        
        // Create data folder
        dataFolder = new File(tempDir.toFile(), "playerdata");
        dataFolder.mkdirs();
        
        // Setup server and players
        mockStatic(Bukkit.class);
        when(Bukkit.getServer()).thenReturn(server);
        
        when(player1.getUniqueId()).thenReturn(player1Id);
        when(player1.getName()).thenReturn("Player1");
        
        when(player2.getUniqueId()).thenReturn(player2Id);
        when(player2.getName()).thenReturn("Player2");
        
        
        // Setup stats
        when(statManager.getPlayerStats(player1Id)).thenReturn(playerStats1);
        when(statManager.getPlayerStats(player2Id)).thenReturn(playerStats2);
        
        // Setup strength stat
        when(strengthStat.getType()).thenReturn(StatType.STRENGTH);
        when(strengthStat.getBaseValue()).thenReturn(10);
        when(strengthStat.getMaxValue()).thenReturn(100);
        
        // Setup player stats
        when(playerStats1.getLevel()).thenReturn(5);
        when(playerStats1.getExperience()).thenReturn(50);
        when(playerStats1.getUnspentPoints()).thenReturn(10);
        when(playerStats1.getStat(StatType.STRENGTH)).thenReturn(strengthStat);
        when(playerStats1.getAllStats()).thenReturn(Collections.singletonMap(StatType.STRENGTH, strengthStat));
        
        // Create DataManager
        dataManager = new DataManager(plugin, configManager, statManager);
    }
    
    @Test
    public void testSavePlayer() {
        dataManager.savePlayer(player1Id);
        
        File playerFile = new File(dataFolder, player1Id.toString() + ".yml");
        assertTrue(playerFile.exists());
        
        // Load the file and verify contents
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        assertEquals(5, config.getInt("level"));
        assertEquals(50, config.getInt("experience"));
        assertEquals(10, config.getInt("unspent-points"));
        
        // Verify stats were saved
        assertEquals(10, config.getInt("stats.strength.base-value"));
    }
    
    @Test
    public void testLoadPlayer() throws IOException {
        // Create a player data file
        File playerFile = new File(dataFolder, player1Id.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        config.set("level", 7);
        config.set("experience", 75);
        config.set("unspent-points", 15);
        config.set("stats.strength.base-value", 20);
        config.save(playerFile);
        
        // Load the player
        dataManager.loadPlayer(player1Id);
        
        // Verify stats were loaded
        verify(playerStats1).setLevel(7);
        verify(playerStats1).setExperience(75);
        verify(playerStats1).setUnspentPoints(15);
        verify(strengthStat).setBaseValue(20);
    }
    
    @Test
    public void testLoadPlayerWithModifiers() throws IOException {
        // Create a player data file with modifiers
        File playerFile = new File(dataFolder, player1Id.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        
        UUID modifierId = UUID.randomUUID();
        
        config.set("level", 7);
        config.set("experience", 75);
        config.set("unspent-points", 15);
        config.set("stats.strength.base-value", 20);
        config.set("stats.strength.modifiers." + modifierId + ".value", 10.0);
        config.set("stats.strength.modifiers." + modifierId + ".type", "ADDITIVE");
        config.set("stats.strength.modifiers." + modifierId + ".duration", -1);
        config.set("stats.strength.modifiers." + modifierId + ".source", "equipment");
        
        config.save(playerFile);
        
        // Mock the addModifier method
        when(strengthStat.addModifier(any(UUID.class), anyFloat(), any(ModifierType.class), anyInt(), anyString()))
                .thenReturn(mock(io.tavuc.skillsystem.api.model.StatModifier.class));
        
        // Load the player
        dataManager.loadPlayer(player1Id);
        
        // Verify stats and modifiers were loaded
        verify(playerStats1).setLevel(7);
        verify(playerStats1).setExperience(75);
        verify(playerStats1).setUnspentPoints(15);
        verify(strengthStat).setBaseValue(20);
        verify(strengthStat).addModifier(eq(modifierId), eq(10.0f), eq(ModifierType.ADDITIVE), eq(-1), eq("equipment"));
    }
    
    @Test
    public void testHasPlayerData() {
        // Create a player data file
        File playerFile = new File(dataFolder, player1Id.toString() + ".yml");
        try {
            playerFile.createNewFile();
        } catch (IOException e) {
            fail("Could not create test file");
        }
        
        assertTrue(dataManager.hasPlayerData(player1Id));
        assertFalse(dataManager.hasPlayerData(UUID.randomUUID()));
    }
    
    @Test
    public void testLoadOnlinePlayers() {
        // Create player data files
        File player1File = new File(dataFolder, player1Id.toString() + ".yml");
        File player2File = new File(dataFolder, player2Id.toString() + ".yml");
        
        try {
            player1File.createNewFile();
            player2File.createNewFile();
        } catch (IOException e) {
            fail("Could not create test files");
        }
        
        dataManager.loadOnlinePlayers();
        
        verify(statManager).getPlayerStats(player1Id);
        verify(statManager).getPlayerStats(player2Id);
    }
    
    @Test
    public void testSaveAllPlayers() {
        dataManager.saveAllPlayers();
        
        verify(statManager).getPlayerStats(player1Id);
        verify(statManager).getPlayerStats(player2Id);
        
        File player1File = new File(dataFolder, player1Id.toString() + ".yml");
        File player2File = new File(dataFolder, player2Id.toString() + ".yml");
        
        assertTrue(player1File.exists());
        assertTrue(player2File.exists());
    }
    
    @Test
    public void testSetStatManager() {
        // Create new DataManager without StatManager
        DataManager newDataManager = new DataManager(plugin, configManager);
        
        // Set StatManager
        newDataManager.setStatManager(mock(StatManager.class));
        
        // Test that it works
        assertDoesNotThrow(() -> newDataManager.savePlayer(player1Id));
    }
}