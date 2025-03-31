package io.tavuc.skillsystem.test.api;

import io.tavuc.skillsystem.api.SkillAPI;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.manager.StatManager;
import io.tavuc.skillsystem.test.UnitTest;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SkillAPITest extends UnitTest {
    
    @Mock
    private StatManager statManager;
    
    @Mock
    private Player player;
    
    @Mock
    private PlayerStats playerStats;
    
    private UUID playerId = UUID.randomUUID();
    
    @BeforeEach
    public void setUp() {
        when(player.getUniqueId()).thenReturn(playerId);
        when(statManager.getPlayerStats(playerId)).thenReturn(playerStats);
    }
    
    @AfterEach
    public void tearDown() {
        // Unregister after each test to avoid affecting other tests
        try {
            SkillAPI.unregister();
        } catch (IllegalStateException e) {
            // Ignore if already unregistered
        }
    }
    
    @Test
    public void testRegister() {
        // Register the stat manager
        SkillAPI.register(statManager);
        
        // Verify it was registered
        assertTrue(SkillAPI.isRegistered());
        assertSame(statManager, SkillAPI.getStatManager());
    }
    
    @Test
    public void testRegisterTwice() {
        // Register the stat manager
        SkillAPI.register(statManager);
        
        // Trying to register again should throw an exception
        assertThrows(IllegalStateException.class, () -> SkillAPI.register(statManager));
    }
    
    @Test
    public void testUnregister() {
        // Register then unregister
        SkillAPI.register(statManager);
        SkillAPI.unregister();
        
        // Should no longer be registered
        assertFalse(SkillAPI.isRegistered());
    }
    
    @Test
    public void testGetStatManagerWhenNotRegistered() {
        // Accessing the stat manager when not registered should throw an exception
        assertThrows(IllegalStateException.class, () -> SkillAPI.getStatManager());
    }
    
    @Test
    public void testGetStats() {
        // Register the stat manager
        SkillAPI.register(statManager);
        
        // Get the player's stats
        PlayerStats stats = SkillAPI.getStats(player);
        
        // Verify the right stats were returned
        assertSame(playerStats, stats);
        verify(statManager).getPlayerStats(playerId);
    }
    
    @Test
    public void testGetStatsWhenNotRegistered() {
        // Accessing stats when not registered should throw an exception
        assertThrows(IllegalStateException.class, () -> SkillAPI.getStats(player));
    }
    
    @Test
    public void testIsRegistered() {
        // Initially not registered
        assertFalse(SkillAPI.isRegistered());
        
        // Register
        SkillAPI.register(statManager);
        assertTrue(SkillAPI.isRegistered());
        
        // Unregister
        SkillAPI.unregister();
        assertFalse(SkillAPI.isRegistered());
    }
}