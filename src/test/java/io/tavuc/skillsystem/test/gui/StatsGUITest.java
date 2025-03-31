package io.tavuc.skillsystem.test.gui;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.Stat;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.config.ConfigManager;
import io.tavuc.skillsystem.config.StatConfig;
import io.tavuc.skillsystem.gui.StatsGUI;
import io.tavuc.skillsystem.manager.StatManager;
import io.tavuc.skillsystem.test.MockBukkitTest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StatsGUITest extends MockBukkitTest {
    
    @Mock
    private StatManager statManager;
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private PlayerStats playerStats;
    
    @Mock
    private Stat strengthStat;
    
    @Mock
    private Stat defenseStat;
    
    private StatsGUI statsGUI;
    private PlayerMock player;
    
    @BeforeEach
    public void setUp() {
        super.baseSetUp();
        MockitoAnnotations.openMocks(this);
        
        // Setup config messages
        when(configManager.getMessage("stats-gui-title")).thenReturn(ChatColor.GOLD + "Your Stats");
        when(configManager.getMessage("unspent-points")).thenReturn(ChatColor.GREEN + "Unspent Points: {points}");
        Map<String, String> placeholders = Map.of("points", "5");
        when(configManager.getMessage(eq("unspent-points"), anyMap())).thenReturn(ChatColor.GREEN + "Unspent Points: 5");
        when(configManager.getMessage("close-button")).thenReturn(ChatColor.RED + "Close");
        when(configManager.getMessage("stat-increased")).thenReturn(ChatColor.GREEN + "Increased {stat} by 1.");
        when(configManager.getMessage(eq("stat-increased"), anyMap())).thenReturn(ChatColor.GREEN + "Increased STRENGTH by 1.");
        
        // Setup stat configs
        StatConfig strengthConfig = new StatConfig(StatType.STRENGTH, "Strength", "Increases damage", Material.IRON_SWORD, 1.0, false);
        StatConfig defenseConfig = new StatConfig(StatType.DEFENSE, "Defense", "Reduces damage", Material.SHIELD, 1.0, false);
        when(configManager.getStatConfig(StatType.STRENGTH)).thenReturn(strengthConfig);
        when(configManager.getStatConfig(StatType.DEFENSE)).thenReturn(defenseConfig);
        
        // Setup stats
        when(strengthStat.getType()).thenReturn(StatType.STRENGTH);
        when(strengthStat.getValue()).thenReturn(10);
        when(strengthStat.getBaseValue()).thenReturn(10);
        
        when(defenseStat.getType()).thenReturn(StatType.DEFENSE);
        when(defenseStat.getValue()).thenReturn(5);
        when(defenseStat.getBaseValue()).thenReturn(5);
        
        Map<StatType, Stat> statsMap = new EnumMap<>(StatType.class);
        statsMap.put(StatType.STRENGTH, strengthStat);
        statsMap.put(StatType.DEFENSE, defenseStat);
        
        when(playerStats.getAllStats()).thenReturn(statsMap);
        when(playerStats.getStat(StatType.STRENGTH)).thenReturn(strengthStat);
        when(playerStats.getStat(StatType.DEFENSE)).thenReturn(defenseStat);
        when(playerStats.getUnspentPoints()).thenReturn(5);
        
        // Setup player
        player = server.addPlayer();
        when(statManager.getPlayerStats(player.getUniqueId())).thenReturn(playerStats);
        
        // Create StatsGUI
        statsGUI = new StatsGUI(statManager);
    }
    
    @Test
    public void testOpenGUI() {
        // Open the GUI
        statsGUI.open(player);
        
        // Verify inventory was opened
        Inventory openInv = player.getOpenInventory().getTopInventory();
        assertNotNull(openInv);
        assertEquals(ChatColor.GOLD + "Your Stats", openInv.getType());
        
        // Verify items were created
        assertNotNull(openInv.getItem(22)); // Unspent points
        assertEquals(Material.EXPERIENCE_BOTTLE, openInv.getItem(22).getType());
    }
    
    @Test
    public void testClickStatItem() {
        // Open the GUI
        statsGUI.open(player);
        
        // Create a click event for the strength item
        Inventory openInv = player.getOpenInventory().getTopInventory();
        
        // Create mock item for STRENGTH stat
        ItemStack strengthItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = strengthItem.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "STRENGTH");
        strengthItem.setItemMeta(meta);
        
        // Place it in the inventory
        openInv.setItem(2, strengthItem);
        
        // Create and process the click event
        InventoryClickEvent event = new InventoryClickEvent(
                player.getOpenInventory(),
                org.bukkit.event.inventory.InventoryType.SlotType.CONTAINER,
                2,
                org.bukkit.event.inventory.ClickType.LEFT,
                org.bukkit.event.inventory.InventoryAction.PICKUP_ALL);
        
        // Process the event manually
        statsGUI.onClick(event);
        
        // Verify event was cancelled
        assertTrue(event.isCancelled());
        
        // Verify point was assigned
        verify(playerStats).removeUnspentPoints(1);
        verify(strengthStat).add(1);
        
        // Verify message was sent
        assertTrue(player.nextMessage().contains("Increased STRENGTH by 1"));
    }
    
    @Test
    public void testClickCloseButton() {
        // Open the GUI
        statsGUI.open(player);
        
        // Create a click event for the close button
        Inventory openInv = player.getOpenInventory().getTopInventory();
        
        // Create mock item for close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = closeItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Close");
        closeItem.setItemMeta(meta);
        
        // Place it in the inventory
        openInv.setItem(26, closeItem);
        
        // Create and process the click event
        InventoryClickEvent event = new InventoryClickEvent(
                player.getOpenInventory(),
                org.bukkit.event.inventory.InventoryType.SlotType.CONTAINER,
                26,
                org.bukkit.event.inventory.ClickType.LEFT,
                org.bukkit.event.inventory.InventoryAction.PICKUP_ALL);
        
        // Process the event manually
        statsGUI.onClick(event);
        
        // Verify event was cancelled
        assertTrue(event.isCancelled());
         
    }
    
    @Test
    public void testClickGlassPane() {
        // Open the GUI
        statsGUI.open(player);
        
        // Create a click event for a glass pane
        Inventory openInv = player.getOpenInventory().getTopInventory();
        
        // Create mock item for glass pane
        ItemStack glassItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glassItem.getItemMeta();
        meta.setDisplayName(" ");
        glassItem.setItemMeta(meta);
        
        // Place it in the inventory
        openInv.setItem(0, glassItem);
        
        // Create and process the click event
        InventoryClickEvent event = new InventoryClickEvent(
                player.getOpenInventory(),
                org.bukkit.event.inventory.InventoryType.SlotType.CONTAINER,
                0,
                org.bukkit.event.inventory.ClickType.LEFT,
                org.bukkit.event.inventory.InventoryAction.PICKUP_ALL);
        
        // Process the event manually
        statsGUI.onClick(event);
        
        // Verify event was cancelled
        assertTrue(event.isCancelled());
        
        // Nothing should change
        verify(playerStats, never()).removeUnspentPoints(anyInt());
        verify(strengthStat, never()).add(anyInt());
    }
    
    @Test
    public void testNotEnoughPoints() {
        // Set unspent points to 0
        when(playerStats.getUnspentPoints()).thenReturn(0);
        
        // Open the GUI
        statsGUI.open(player);
        
        // Create a click event for the strength item
        Inventory openInv = player.getOpenInventory().getTopInventory();
        
        // Create mock item for STRENGTH stat
        ItemStack strengthItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = strengthItem.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "STRENGTH");
        strengthItem.setItemMeta(meta);
        
        // Place it in the inventory
        openInv.setItem(2, strengthItem);
        
        // Create and process the click event
        InventoryClickEvent event = new InventoryClickEvent(
                player.getOpenInventory(),
                org.bukkit.event.inventory.InventoryType.SlotType.CONTAINER,
                2,
                org.bukkit.event.inventory.ClickType.LEFT,
                org.bukkit.event.inventory.InventoryAction.PICKUP_ALL);
        
        // Process the event manually
        statsGUI.onClick(event);
        
        // Verify event was cancelled
        assertTrue(event.isCancelled());
        
        // No points should be assigned
        verify(playerStats, never()).removeUnspentPoints(anyInt());
        verify(strengthStat, never()).add(anyInt());
        
        // Verify error message
        assertTrue(player.nextMessage().contains("don't have any unspent points"));
    }
}