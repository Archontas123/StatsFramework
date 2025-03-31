package io.tavuc.skillsystem.test.api.events;

import io.tavuc.skillsystem.api.events.PlayerAssignStatEvent;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.test.UnitTest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlayerAssignStatEventTest extends UnitTest {
    
    @Mock
    private Player player;
    
    @Test
    public void testConstructor() {
        int pointsAssigned = 5;
        PlayerAssignStatEvent event = new PlayerAssignStatEvent(player, StatType.STRENGTH, pointsAssigned);
        
        // Verify fields were properly set
        assertEquals(player, event.getPlayer());
        assertEquals(StatType.STRENGTH, event.getStatType());
        assertEquals(pointsAssigned, event.getPointsAssigned());
    }
    
    @Test
    public void testGetPlayer() {
        PlayerAssignStatEvent event = new PlayerAssignStatEvent(player, StatType.DEFENSE, 3);
        
        assertSame(player, event.getPlayer());
    }
    
    @Test
    public void testGetStatType() {
        PlayerAssignStatEvent event = new PlayerAssignStatEvent(player, StatType.VITALITY, 2);
        
        assertEquals(StatType.VITALITY, event.getStatType());
    }
    
    @Test
    public void testGetPointsAssigned() {
        int pointsAssigned = 10;
        PlayerAssignStatEvent event = new PlayerAssignStatEvent(player, StatType.FEROCITY, pointsAssigned);
        
        assertEquals(pointsAssigned, event.getPointsAssigned());
    }
    
    @Test
    public void testGetHandlers() {
        PlayerAssignStatEvent event = new PlayerAssignStatEvent(player, StatType.AGILITY, 1);
        
        HandlerList handlers = event.getHandlers();
        assertNotNull(handlers);
    }
    
    @Test
    public void testGetHandlerList() {
        HandlerList handlers = PlayerAssignStatEvent.getHandlerList();
        
        assertNotNull(handlers);
    }
    
    @Test
    public void testEventIsCancellable() {
        // Verify the event is not cancellable (inherits from Event, not Cancellable)
        PlayerAssignStatEvent event = new PlayerAssignStatEvent(player, StatType.INTELLIGENCE, 3);
        
        assertFalse(event instanceof org.bukkit.event.Cancellable);
    }
    
    @Test
    public void testEventIsCorrectType() {
        PlayerAssignStatEvent event = new PlayerAssignStatEvent(player, StatType.STRENGTH, 1);
        
        // Should be a Bukkit Event
        assertTrue(event instanceof Event);
    }
    
    @Test
    public void testMultipleEvents() {
        // Create multiple events and verify they have different parameters
        PlayerAssignStatEvent event1 = new PlayerAssignStatEvent(player, StatType.STRENGTH, 1);
        PlayerAssignStatEvent event2 = new PlayerAssignStatEvent(player, StatType.DEFENSE, 2);
        
        assertEquals(StatType.STRENGTH, event1.getStatType());
        assertEquals(1, event1.getPointsAssigned());
        
        assertEquals(StatType.DEFENSE, event2.getStatType());
        assertEquals(2, event2.getPointsAssigned());
    }
    
    @Test
    public void testHandlerListIsSingleton() {
        // Verify that getHandlerList() returns the same instance each time
        HandlerList handlers1 = PlayerAssignStatEvent.getHandlerList();
        HandlerList handlers2 = PlayerAssignStatEvent.getHandlerList();
        
        assertSame(handlers1, handlers2);
    }
}