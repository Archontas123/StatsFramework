package io.tavuc.skillsystem.test.task;

import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.manager.StatManager;
import io.tavuc.skillsystem.task.EffectUpdateTask;
import io.tavuc.skillsystem.test.UnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class EffectUpdateTaskTest extends UnitTest {
    
    @Mock
    private Plugin plugin;
    
    @Mock
    private StatManager statManager;
    
    @Mock
    private Server server;
    
    @Mock
    private BukkitScheduler scheduler;
    
    @Mock
    private Player player1;
    
    @Mock
    private Player player2;
    
    @Mock
    private PlayerStats playerStats1;
    
    @Mock
    private PlayerStats playerStats2;
    
    private EffectUpdateTask effectUpdateTask;
    
    @BeforeEach
    public void setUp() {
        when(plugin.getLogger()).thenReturn(Logger.getLogger("Test"));
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        
        // Setup mock for scheduleSyncRepeatingTask
        when(scheduler.scheduleSyncRepeatingTask(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenReturn(1);
        
        // Setup mock players
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        
        when(player1.getUniqueId()).thenReturn(player1Id);
        when(player2.getUniqueId()).thenReturn(player2Id);
        
        // Setup mock for Bukkit.getOnlinePlayers()
        Player[] players = {player1, player2};
        mockStatic(Bukkit.class);
        
        // Setup mock for StatManager
        when(statManager.getPlayerStats(player1Id)).thenReturn(playerStats1);
        when(statManager.getPlayerStats(player2Id)).thenReturn(playerStats2);
        
        effectUpdateTask = new EffectUpdateTask(plugin, statManager);
    }
    
    @Test
    public void testStartTask() {
        effectUpdateTask.start();
        
        verify(scheduler).scheduleSyncRepeatingTask(eq(plugin), any(Runnable.class), eq(1L), eq(1L));
        verify(plugin).getLogger();
    }
    
    @Test
    public void testStopTask() {
        // Start the task first
        when(scheduler.scheduleSyncRepeatingTask(eq(plugin), any(Runnable.class), anyLong(), anyLong()))
                .thenReturn(123);
        
        effectUpdateTask.start();
        effectUpdateTask.stop();
        
        verify(scheduler).cancelTask(123);
        verify(plugin, times(2)).getLogger();
    }
    
    @Test
    public void testStartAlreadyRunning() {
        // Start the task first
        effectUpdateTask.start();
        
        // Try to start again
        effectUpdateTask.start();
        
        // Should only schedule once
        verify(scheduler, times(1)).scheduleSyncRepeatingTask(
                eq(plugin), any(Runnable.class), anyLong(), anyLong());
    }
    
    @Test
    public void testStopNotRunning() {
        effectUpdateTask.stop();
        
        // Should not try to cancel any task
        verify(scheduler, never()).cancelTask(anyInt());
    }
    
    @Test
    public void testUpdateMethod() throws Exception {
        // Use reflection to call the private update method
        java.lang.reflect.Method updateMethod = EffectUpdateTask.class.getDeclaredMethod("update");
        updateMethod.setAccessible(true);
        updateMethod.invoke(effectUpdateTask);
        
        // Verify that all player stats were updated
        verify(playerStats1).updateEffects();
        verify(playerStats2).updateEffects();
    }
}