package io.tavuc.skillsystem.gui;

import io.tavuc.skillsystem.api.SkillAPI;
import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.Stat;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.manager.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * GUI for assigning stat points.
 */
public class StatsGUI implements Listener {

    private final StatManager statManager;
    private final Map<String, Inventory> openInventories = new HashMap<>();

    /**
     * Constructs the stats GUI.
     *
     * @param statManager The stat manager.
     */
    public StatsGUI(StatManager statManager) {
        this.statManager = statManager;
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]);
    }

    /**
     * Opens the stats GUI for a player.
     *
     * @param player The player.
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Your Stats");
        PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, createGlassPane());
        }

        inv.setItem(2, createStatItem(StatType.STRENGTH, stats));
        inv.setItem(3, createStatItem(StatType.DEFENSE, stats));
        inv.setItem(4, createStatItem(StatType.VITALITY, stats));
        inv.setItem(11, createStatItem(StatType.FEROCITY, stats));
        inv.setItem(12, createStatItem(StatType.AGILITY, stats));
        inv.setItem(13, createStatItem(StatType.INTELLIGENCE, stats));

        inv.setItem(22, createUnspentPointsItem(stats.getUnspentPoints()));
        inv.setItem(26, createCloseButton());

        openInventories.put(player.getName(), inv);
        player.openInventory(inv);
    }

    private ItemStack createStatItem(StatType type, PlayerStats stats) {
        ItemStack item = new ItemStack(getIcon(type));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + type.name());
        meta.setLore(java.util.List.of(
                ChatColor.GRAY + "Current Value: " + ChatColor.WHITE + stats.getStat(type).getValue(),
                ChatColor.YELLOW + "Click to assign +1 point."
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createUnspentPointsItem(int points) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Unspent Points: " + points);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCloseButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Close");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private Material getIcon(StatType type) {
        return switch (type) {
            case STRENGTH -> Material.IRON_SWORD;
            case DEFENSE -> Material.SHIELD;
            case VITALITY -> Material.GOLDEN_APPLE;
            case FEROCITY -> Material.NETHERITE_SWORD;
            case AGILITY -> Material.FEATHER;
            case INTELLIGENCE -> Material.ENCHANTED_BOOK;
        };
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!openInventories.containsKey(player.getName())) return;
        if (event.getInventory() != openInventories.get(player.getName())) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (name.equalsIgnoreCase("Close")) {
            player.closeInventory();
            return;
        }

        for (StatType type : StatType.values()) {
            if (name.equalsIgnoreCase(type.name())) {
                PlayerStats stats = statManager.getPlayerStats(player.getUniqueId());
                if (stats.getUnspentPoints() <= 0) {
                    player.sendMessage(ChatColor.RED + "You don't have any unspent points.");
                    return;
                }
                Stat stat = stats.getStat(type);
                stat.add(1);
                stats.removeUnspentPoints(1);
                player.sendMessage(ChatColor.GREEN + "Assigned +1 point to " + type.name() + ".");
                open(player); // Refresh the GUI
                return;
            }
        }
    }
}
