package io.tavuc.skillsystem.test.listener;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.data.DataManager;
import io.tavuc.skillsystem.listener.PlayerListener;
import io.tavuc.skillsystem.manager.StatManager;
import io.tavuc.skillsystem.test.MockBukkitTest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class PlayerListenerTest extends MockBukkitTest {
    
    @Mock
    private StatManager statManager;
    
    @Mock
    private DataManager dataManager;
    
    @Mock
    private PlayerStats playerStats;
    
    @Mock
    private FileConfiguration config;
    
    private PlayerListener playerListener;
    private PlayerMock player;
    
    @BeforeEach
    public void setUp() {
        super.baseSetUp();
        MockitoAnnotations.openMocks(this);
        
        when(plugin.getConfigManager()).thenReturn(mock(io.tavuc.skillsystem.config.ConfigManager.class));
        when(plugin.getConfigManager().getBoolean(anyString(), anyBoolean())).thenReturn(false);
        when(plugin.getConfigManager().getInt(anyString(), anyInt())).thenReturn(5);
        
        when(statManager.getPlayerStats(any())).thenReturn(playerStats);
        
        playerListener = new PlayerListener(plugin, statManager, dataManager);
        player = server.addPlayer();
    }
    
    @Test
    public void testPlayerJoinLoadsData() {
        when(dataManager.hasPlayerData(player.getUniqueId())).thenReturn(true);
        
        PlayerJoinEvent event = new PlayerJoinEvent(player, "Player joined");
        playerListener.onPlayerJoin(event);
        
        verify(dataManager).loadPlayer(player.getUniqueId());
    }
    
    @Test
    public void testPlayerJoinInitializesNewPlayer() {
        when(dataManager.hasPlayerData(player.getUniqueId())).thenReturn(false);
        when(plugin.getConfigManager().getInt("new-player.starting-points", 0)).thenReturn(5);
        
        PlayerJoinEvent event = new PlayerJoinEvent(player, "Player joined");
        playerListener.onPlayerJoin(event);
        
        verify(dataManager, never()).loadPlayer(player.getUniqueId());
        verify(playerStats).addUnspentPoints(5);
    }
    
    @Test
    public void testPlayerQuitSavesData() {
        PlayerQuitEvent event = new PlayerQuitEvent(player, "Player quit");
        playerListener.onPlayerQuit(event);
        
        verify(dataManager).savePlayer(player.getUniqueId());
    }
    
    @Test
    public void testDebugLoggingEnabled() {
        when(plugin.getConfigManager().getBoolean("debug.player-data", false)).thenReturn(true);
        
        // Create a new listener with debug enabled
        playerListener = new PlayerListener(plugin, statManager, dataManager);
        
        PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, "Player joined");
        playerListener.onPlayerJoin(joinEvent);
        
        PlayerQuitEvent quitEvent = new PlayerQuitEvent(player, "Player quit");
        playerListener.onPlayerQuit(quitEvent);
        
        // Debug logging should still save/load data as normal
        verify(dataManager).savePlayer(player.getUniqueId());
    }
    
    @Test
    public void testMultiplePlayerJoins() {
        when(dataManager.hasPlayerData(any())).thenReturn(false);
        
        // First player
        PlayerMock player1 = server.addPlayer("player1");
        PlayerJoinEvent event1 = new PlayerJoinEvent(player1, "Player1 joined");
        playerListener.onPlayerJoin(event1);
        
        // Second player
        PlayerMock player2 = server.addPlayer("player2");
        PlayerJoinEvent event2 = new PlayerJoinEvent(player2, "Player2 joined");
        playerListener.onPlayerJoin(event2);
        
        // Both should initialize stats
        verify(statManager, times(2)).getPlayerStats(any());
        verify(playerStats, times(2)).addUnspentPoints(anyInt());
    }
}