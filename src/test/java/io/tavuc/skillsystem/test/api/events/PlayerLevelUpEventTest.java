package io.tavuc.skillsystem.test.api.events;

import io.tavuc.skillsystem.api.events.PlayerLevelUpEvent;
import io.tavuc.skillsystem.test.UnitTest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlayerLevelUpEventTest extends UnitTest {
    
    @Mock
    private Player player;
    
    @Test
    public void testConstructor() {
        int newLevel = 10;
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, newLevel);
        
        // Verify fields were properly set
        assertEquals(player, event.getPlayer());
        assertEquals(newLevel, event.getNewLevel());
    }
    
    @Test
    public void testGetPlayer() {
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, 5);
        
        assertSame(player, event.getPlayer());
    }
    
    @Test
    public void testGetNewLevel() {
        int newLevel = 15;
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, newLevel);
        
        assertEquals(newLevel, event.getNewLevel());
    }
    
    @Test
    public void testGetHandlers() {
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, 3);
        
        HandlerList handlers = event.getHandlers();
        assertNotNull(handlers);
    }
    
    @Test
    public void testGetHandlerList() {
        HandlerList handlers = PlayerLevelUpEvent.getHandlerList();
        
        assertNotNull(handlers);
    }
    
    @Test
    public void testEventIsCancellable() {
        // Verify the event is not cancellable (inherits from Event, not Cancellable)
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, 7);
        
        assertFalse(event instanceof org.bukkit.event.Cancellable);
    }
    
    @Test
    public void testEventIsCorrectType() {
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, 8);
        
        // Should be a Bukkit Event
        assertTrue(event instanceof Event);
    }
    
    @Test
    public void testMultipleEvents() {
        // Create multiple events and verify they have different parameters
        PlayerLevelUpEvent event1 = new PlayerLevelUpEvent(player, 10);
        
        Player player2 = mock(Player.class);
        PlayerLevelUpEvent event2 = new PlayerLevelUpEvent(player2, 20);
        
        assertEquals(player, event1.getPlayer());
        assertEquals(10, event1.getNewLevel());
        
        assertEquals(player2, event2.getPlayer());
        assertEquals(20, event2.getNewLevel());
    }
    
    @Test
    public void testHandlerListIsSingleton() {
        // Verify that getHandlerList() returns the same instance each time
        HandlerList handlers1 = PlayerLevelUpEvent.getHandlerList();
        HandlerList handlers2 = PlayerLevelUpEvent.getHandlerList();
        
        assertSame(handlers1, handlers2);
    }
    
    @Test
    public void testInvalidNewLevel() {
        // Test with negative level (should still work, validation should be elsewhere)
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, -5);
        
        assertEquals(-5, event.getNewLevel());
    }
    
    @Test
    public void testNullPlayer() {
        // Event should accept null player (though this should never happen in practice)
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(null, 10);
        
        assertNull(event.getPlayer());
        assertEquals(10, event.getNewLevel());
    }
}