package dev.eggsv31.veldora.veldoraClan.managers.gui.menu;

import dev.eggsv31.veldora.veldoraClan.managers.gui.utils.ClanGuiUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ClanSettingsMenu implements Listener {

    public static final String SETTINGS_MENU_TITLE = ChatColor.RED + "§lKlan Ayarları";

    private final Plugin plugin;

    public ClanSettingsMenu(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openSettingsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, SETTINGS_MENU_TITLE);

        inv.setItem(11, ClanGuiUtils.createGuiItem(
                Material.DIAMOND_SWORD,
                ChatColor.RED + "§lPvP Ayarları",
                ChatColor.GRAY + "Klan içi PvP'yi etkinleştir veya devre dışı bırak."
        ));

        inv.setItem(13, ClanGuiUtils.createGuiItem(
                Material.PAPER,
                ChatColor.YELLOW + "§lDuyuru Ayarları",
                ChatColor.GRAY + "Klan duyurularını düzenleyin."
        ));

        inv.setItem(15, ClanGuiUtils.createGuiItem(
                Material.REDSTONE,
                ChatColor.AQUA + "§lKlan Yetkileri",
                ChatColor.GRAY + "Üyelerin yetkilerini düzenleyin."
        ));

        ClanGuiUtils.fillDecorativeSlots(inv);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;

        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        String title = e.getView().getTitle();

        if (!title.equals(SETTINGS_MENU_TITLE)) return;
        e.setCancelled(true);

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        switch (displayName) {
            case "PvP Ayarları":
                player.sendMessage(ChatColor.GREEN + "PvP ayarlarını değiştiriyorsunuz!");
                break;

            case "Duyuru Ayarları":
                player.sendMessage(ChatColor.GREEN + "Duyuru ayarlarını düzenliyorsunuz!");
                break;

            case "Klan Yetkileri":
                player.sendMessage(ChatColor.GREEN + "Klan yetkilerini değiştiriyorsunuz!");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Geçersiz seçim!");
                break;
        }
    }
}
