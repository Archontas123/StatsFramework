package io.tavuc.skillsystem.test.command;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.tavuc.skillsystem.api.events.PlayerAssignStatEvent;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.Stat;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.command.AssignStatCommand;
import io.tavuc.skillsystem.config.ConfigManager;
import io.tavuc.skillsystem.manager.StatManager;
import io.tavuc.skillsystem.test.MockBukkitTest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AssignStatCommandTest extends MockBukkitTest {
    
    @Mock
    private StatManager statManager;
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private PlayerStats playerStats;
    
    @Mock
    private Stat stat;
    
    private AssignStatCommand assignStatCommand;
    private PlayerMock player;
    
    @BeforeEach
    public void setUp() {
        super.baseSetUp();
        MockitoAnnotations.openMocks(this);
        
        assignStatCommand = new AssignStatCommand(statManager, configManager);
        player = server.addPlayer();
        
        when(statManager.getPlayerStats(player.getUniqueId())).thenReturn(playerStats);
        when(playerStats.getStat(any(StatType.class))).thenReturn(stat);
        
        setupConfigMessages();
    }
    
    private void setupConfigMessages() {
        Map<String, String> messages = new HashMap<>();
        messages.put("no-permission", "&cYou don't have permission to use this command.");
        messages.put("invalid-number", "&cAmount must be a positive integer.");
        messages.put("not-enough-points", "&cYou don't have enough unspent points.");
        messages.put("unknown-stat", "&cUnknown stat: {stat}");
        messages.put("stat-assigned", "&aAssigned {amount} points to {stat}.");
        
        // Setup configManager to return messages
        when(configManager.getMessage(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return ChatColor.translateAlternateColorCodes('&', messages.getOrDefault(key, key));
        });
        
        when(configManager.getMessage(anyString(), anyMap())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Map<String, String> placeholders = invocation.getArgument(1);
            String message = messages.getOrDefault(key, key);
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            return ChatColor.translateAlternateColorCodes('&', message);
        });
    }
    
    @Test
    public void testAssignCommandRequiresPlayer() {
        CommandSender consoleSender = server.getConsoleSender();
        Command command = mock(Command.class);
        
        boolean result = assignStatCommand.onCommand(consoleSender, command, "assign", new String[]{"strength", "5"});
        
        assertTrue(result);
        verify(playerStats, never()).removeUnspentPoints(anyInt());
    }
    
    @Test
    public void testAssignCommandRequiresArguments() {
        Command command = mock(Command.class);
        
        boolean result = assignStatCommand.onCommand(player, command, "assign", new String[0]);
        
        assertTrue(result);
        assertTrue(player.nextMessage().contains("Usage"));
    }
    
    @Test
    public void testAssignCommandValidatesStatType() {
        Command command = mock(Command.class);
        when(statManager.getStatType("invalidstat")).thenReturn(null);
        
        boolean result = assignStatCommand.onCommand(player, command, "assign", new String[]{"invalidstat", "5"});
        
        assertTrue(result);
        assertTrue(player.nextMessage().contains("Unknown stat"));
    }
    
    @Test
    public void testAssignCommandValidatesAmount() {
        Command command = mock(Command.class);
        when(statManager.getStatType("strength")).thenReturn(StatType.STRENGTH);
        
        boolean result = assignStatCommand.onCommand(player, command, "assign", new String[]{"strength", "invalid"});
        
        assertTrue(result);
        assertTrue(player.nextMessage().contains("Amount must be a positive integer"));
    }
    
    @Test
    public void testAssignCommandRequiresEnoughPoints() {
        Command command = mock(Command.class);
        when(statManager.getStatType("strength")).thenReturn(StatType.STRENGTH);
        when(playerStats.getUnspentPoints()).thenReturn(3);
        
        boolean result = assignStatCommand.onCommand(player, command, "assign", new String[]{"strength", "5"});
        
        assertTrue(result);
        assertTrue(player.nextMessage().contains("don't have enough unspent points"));
    }
    
    @Test
    public void testAssignCommandSuccessful() {
        Command command = mock(Command.class);
        PluginManager pluginManager = server.getPluginManager();
        
        when(statManager.getStatType("strength")).thenReturn(StatType.STRENGTH);
        when(playerStats.getUnspentPoints()).thenReturn(10);
        
        boolean result = assignStatCommand.onCommand(player, command, "assign", new String[]{"strength", "5"});
        
        assertTrue(result);
        verify(playerStats).removeUnspentPoints(5);
        verify(stat).add(5);
        assertTrue(player.nextMessage().contains("Assigned 5 points to STRENGTH"));
        
        // Verify that the event was fired
        verify(pluginManager).callEvent(any(PlayerAssignStatEvent.class));
    }
    

    
}