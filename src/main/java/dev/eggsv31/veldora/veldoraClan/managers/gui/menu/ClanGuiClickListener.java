package dev.eggsv31.veldora.veldoraClan.managers.gui.menu;

import dev.eggsv31.veldora.veldoraClan.Chest.ClanChest;
import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.managers.gui.ClanMainMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ClanGuiClickListener implements Listener {

    private final IClanDataStorage clanDataStorage;
    private final ClanSettingsMenu clanSettingsMenu;
    private final ClanChest clanChestManager;
    private final GuiLangManager guiLangManager;

    public ClanGuiClickListener(IClanDataStorage clanDataStorage, Plugin plugin, ClanChest clanChestManager, GuiLangManager guiLangManager) {
        this.clanDataStorage = clanDataStorage;
        this.clanSettingsMenu = new ClanSettingsMenu(plugin);
        this.clanChestManager = clanChestManager;
        this.guiLangManager = guiLangManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;

        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        String mainMenuTitle = guiLangManager.getMessage("clan.menu.main.title", ClanMainMenu.MAIN_MENU_DEFAULT_TITLE);

        if (ChatColor.stripColor(e.getView().getTitle()).equals(ChatColor.stripColor(mainMenuTitle))) {
            e.setCancelled(true);

            if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

            String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (displayName.equalsIgnoreCase(ChatColor.stripColor(guiLangManager.getMessage("clan.menu.settings", "&c§lKlan Ayarları")))) {
                clanSettingsMenu.openSettingsMenu(player);
                player.sendMessage(ChatColor.GREEN + "Klan ayarları açıldı!");
            }
            else if (displayName.equalsIgnoreCase(ChatColor.stripColor(guiLangManager.getMessage("clan.menu.chest", "&6§lKlan Deposu")))) {
                clanChestManager.openClanChest(player);
                player.sendMessage(ChatColor.YELLOW + "Klan Deposuna erişim sağlanıyor...");
            }
            return;
        }
    }
}
