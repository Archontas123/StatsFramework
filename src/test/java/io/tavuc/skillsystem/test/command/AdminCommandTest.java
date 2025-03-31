package io.tavuc.skillsystem.test.command;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.tavuc.skillsystem.Main;
import io.tavuc.skillsystem.api.model.ModifierType;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.Stat;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.command.AdminCommand;
import io.tavuc.skillsystem.config.ConfigManager;
import io.tavuc.skillsystem.manager.LevelManager;
import io.tavuc.skillsystem.manager.StatManager;
import io.tavuc.skillsystem.test.MockBukkitTest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminCommandTest extends MockBukkitTest {
    
    @Mock
    private Main mainPlugin;
    
    @Mock
    private StatManager statManager;
    
    @Mock
    private LevelManager levelManager;
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private PlayerStats playerStats;
    
    @Mock
    private Stat stat;
    
    private AdminCommand adminCommand;
    private PlayerMock player;
    private PlayerMock targetPlayer;
    
    @BeforeEach
    public void setUp() {
        super.baseSetUp();
        MockitoAnnotations.openMocks(this);
        
        adminCommand = new AdminCommand(mainPlugin, statManager, levelManager, configManager);
        player = server.addPlayer("admin");
        targetPlayer = server.addPlayer("target");
        
        // Setup permissions
        player.addAttachment(plugin, "skillsystem.admin", true);
        
        // Setup stats
        when(statManager.getPlayerStats(targetPlayer.getUniqueId())).thenReturn(playerStats);
        when(playerStats.getStat(any(StatType.class))).thenReturn(stat);
        
        // Setup config messages
        when(configManager.getMessage(anyString())).thenReturn("Message");
        when(configManager.getMessage(anyString(), anyMap())).thenReturn("Message with placeholders");
    }
    
    @Test
    public void testAdminCommandRequiresPermission() {
        player.removeAttachment(player.getEffectivePermissions().stream()
                .filter(p -> p.getPermission().equals("skillsystem.admin"))
                .findFirst().get().getAttachment());
        
        Command command = mock(Command.class);
        
        boolean result = adminCommand.onCommand(player, command, "skillsadmin", new String[]{"reload"});
        
        assertTrue(result);
        verify(mainPlugin, never()).reload();
    }
    
    @Test
    public void testAdminCommandShowsHelp() {
        Command command = mock(Command.class);
        
        boolean result = adminCommand.onCommand(player, command, "skillsadmin", new String[0]);
        
        assertTrue(result);
        verify(mainPlugin, never()).reload();
        
        String message = player.nextMessage();
        assertTrue(message.contains("Skills Framework Admin"));
    }
    
    @Test
    public void testReloadCommand() {
        Command command = mock(Command.class);
        
        boolean result = adminCommand.onCommand(player, command, "skillsadmin", new String[]{"reload"});
        
        assertTrue(result);
        verify(mainPlugin).reload();
    }
    
    @Test
    public void testSetLevelCommand() {
        Command command = mock(Command.class);
        
        boolean result = adminCommand.onCommand(player, command, "skillsadmin",
                new String[]{"setlevel", "target", "10"});
        
        assertTrue(result);
        verify(playerStats).setLevel(10);
    }
    
    @Test
    public void testAddExpCommand() {
        Command command = mock(Command.class);
        
        boolean result = adminCommand.onCommand(player, command, "skillsadmin",
                new String[]{"addexp", "target", "100"});
        
        assertTrue(result);
        verify(levelManager).addExperience(eq(targetPlayer), eq(playerStats), eq(100));
    }
    
    @Test
    public void testSetStatCommand() {
        Command command = mock(Command.class);
        when(statManager.getStatType("strength")).thenReturn(StatType.STRENGTH);
        
        boolean result = adminCommand.onCommand(player, command, "skillsadmin",
                new String[]{"setstat", "target", "strength", "20"});
        
        assertTrue(result);
        verify(stat).setBaseValue(20);
    }
    
    @Test
    public void testResetCommand() {
        Command command = mock(Command.class);
        Map<StatType, Stat> statsMap = mock(Map.class);
        when(playerStats.getAllStats()).thenReturn(statsMap);
        when(statsMap.values()).thenReturn(List.of(stat, stat, stat));
        
        boolean result = adminCommand.onCommand(player, command, "skillsadmin",
                new String[]{"reset", "target", "all"});
        
        assertTrue(result);
        verify(playerStats).setLevel(1);
        verify(playerStats).setExperience(0);
        verify(playerStats).setUnspentPoints(0);
        verify(stat, atLeast(1)).setBaseValue(0);
    }
    
    @Test
    public void testAddEffectCommand() {
        Command command = mock(Command.class);
        when(statManager.getStatType("strength")).thenReturn(StatType.STRENGTH);
        UUID effectId = UUID.randomUUID();
        when(playerStats.addStatEffect(eq(StatType.STRENGTH), eq(10.0f), 
                eq(ModifierType.ADDITIVE), eq(100), eq("test"))).thenReturn(effectId);
        
        boolean result = adminCommand.onCommand(player, command, "skillsadmin",
                new String[]{"addeffect", "target", "strength", "10", "ADDITIVE", "100", "test"});
        
        assertTrue(result);
        verify(playerStats).addStatEffect(
                StatType.STRENGTH, 10.0f, ModifierType.ADDITIVE, 100, "test");
    }
    
    @Test
    public void testRemoveEffectCommand() {
        Command command = mock(Command.class);
        when(playerStats.removeEffectsFromSource("test")).thenReturn(3);
        
        boolean result = adminCommand.onCommand(player, command, "skillsadmin",
                new String[]{"removeeffect", "target", "test"});
        
        assertTrue(result);
        verify(playerStats).removeEffectsFromSource("test");
    }
    
    @Test
    public void testInfoCommand() {
        Command command = mock(Command.class);
        
        // Setup stats for realistic info display
        when(playerStats.getLevel()).thenReturn(10);
        when(playerStats.getExperience()).thenReturn(50);
        when(playerStats.getUnspentPoints()).thenReturn(5);
        
        when(levelManager.getRequiredExpForNextLevel(10)).thenReturn(200);
        
        Map<StatType, Stat> statsMap = mock(Map.class);
        when(playerStats.getAllStats()).thenReturn(statsMap);
        when(statsMap.entrySet()).thenReturn(
                StatType.values().length > 0 ? 
                Map.of(StatType.values()[0], stat).entrySet() : 
                Map.<StatType, Stat>of().entrySet()
        );
        
        boolean result = adminCommand.onCommand(player, command, "skillsadmin",
                new String[]{"info", "target"});
        
        assertTrue(result);
    }
    
    @Test
    public void testTabComplete() {
        Command command = mock(Command.class);
        
        List<String> completions = adminCommand.onTabComplete(player, command, "skillsadmin", new String[]{""});
        
        assertNotNull(completions);
        assertTrue(completions.contains("reload"));
        assertTrue(completions.contains("setlevel"));
        assertTrue(completions.contains("addexp"));
        assertTrue(completions.contains("info"));
    }
    
    @Test
    public void testTabCompletePlayerNames() {
        Command command = mock(Command.class);
        
        List<String> completions = adminCommand.onTabComplete(player, command, "skillsadmin", 
                new String[]{"setlevel", ""});
        
        assertNotNull(completions);
        assertTrue(completions.contains("admin"));
        assertTrue(completions.contains("target"));
    }
    
    @Test
    public void testTabCompleteStatTypes() {
        Command command = mock(Command.class);
        when(statManager.getRegisteredStats()).thenReturn(Map.of(
                "strength", StatType.STRENGTH,
                "vitality", StatType.VITALITY
        ));
        
        List<String> completions = adminCommand.onTabComplete(player, command, "skillsadmin", 
                new String[]{"setstat", "target", ""});
        
        assertNotNull(completions);
        assertTrue(completions.contains("strength"));
        assertTrue(completions.contains("vitality"));
    }
    
    @Test
    public void testTabCompleteResetOptions() {
        Command command = mock(Command.class);
        
        List<String> completions = adminCommand.onTabComplete(player, command, "skillsadmin", 
                new String[]{"reset", "target", ""});
        
        assertNotNull(completions);
        assertTrue(completions.contains("stats"));
        assertTrue(completions.contains("level"));
        assertTrue(completions.contains("all"));
    }
    
    @Test
    public void testTabCompleteModifierTypes() {
        Command command = mock(Command.class);
        
        List<String> completions = adminCommand.onTabComplete(player, command, "skillsadmin", 
                new String[]{"addeffect", "target", "strength", "10", ""});
        
        assertNotNull(completions);
        assertTrue(completions.contains("ADDITIVE"));
        assertTrue(completions.contains("MULTIPLICATIVE"));
    }
}