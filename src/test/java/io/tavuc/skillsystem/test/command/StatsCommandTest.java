package io.tavuc.skillsystem.test.command;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.tavuc.skillsystem.gui.StatsGUI;
import io.tavuc.skillsystem.test.MockBukkitTest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StatsCommandTest extends MockBukkitTest {
    
    @Mock
    private StatsGUI statsGUI;
    
    private io.tavuc.skillsystem.command.StatsCommand statsCommand;
    private PlayerMock player;
    
    @BeforeEach
    public void setUp() {
        super.baseSetUp();
        MockitoAnnotations.openMocks(this);
        
        statsCommand = new io.tavuc.skillsystem.command.StatsCommand(statsGUI);
        player = server.addPlayer();
    }
    
    @Test
    public void testStatsCommandRequiresPlayer() {
        CommandSender consoleSender = server.getConsoleSender();
        Command command = mock(Command.class);
        
        boolean result = statsCommand.onCommand(consoleSender, command, "stats", new String[0]);
        
        assertTrue(result);
        verify(statsGUI, never()).open(any());
    }
    
    @Test
    public void testStatsCommandOpensGUI() {
        Command command = mock(Command.class);
        
        boolean result = statsCommand.onCommand(player, command, "stats", new String[0]);
        
        assertTrue(result);
        verify(statsGUI).open(player);
    }

    
    @Test
    public void testStatsCommandWithPlayerPermission() {
        Command command = mock(Command.class);
        player.addAttachment(plugin, "skillsystem.stats", true);
        
        boolean result = statsCommand.onCommand(player, command, "stats", new String[0]);
        
        assertTrue(result);
        verify(statsGUI).open(player);
    }
    
    @Test
    public void testStatsCommandHandlesInvalidArgs() {
        Command command = mock(Command.class);
        
        boolean result = statsCommand.onCommand(player, command, "stats", new String[]{"invalid", "args"});
        
        assertTrue(result);
        verify(statsGUI).open(player);
    }
}