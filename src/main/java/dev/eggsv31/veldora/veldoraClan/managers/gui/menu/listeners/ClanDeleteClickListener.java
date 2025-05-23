package dev.eggsv31.veldora.veldoraClan.managers.gui.menu.listeners;

import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.managers.gui.menu.holders.ClanDeleteMenuHolder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ClanDeleteClickListener implements Listener {

    private final IClanDataStorage clanDataStorage;
    private final GuiLangManager guiLangManager;

    public ClanDeleteClickListener(IClanDataStorage clanDataStorage, GuiLangManager guiLangManager, Plugin plugin) {
        this.clanDataStorage = clanDataStorage;
        this.guiLangManager = guiLangManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof ClanDeleteMenuHolder)) return;

        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        String confirmDisplay = ChatColor.stripColor(guiLangManager.getMessage("clan.menu.delete.confirm", "&a&lKlanı Sil"));
        String cancelDisplay = ChatColor.stripColor(guiLangManager.getMessage("clan.menu.delete.cancel", "&c&lİptal Et"));

        if (displayName.equalsIgnoreCase(confirmDisplay)) {
            String clanName = clanDataStorage.getPlayerClan(player);
            if (clanName != null) {
                clanDataStorage.deleteClan(clanName, player);
            }
            player.closeInventory();
        } else if (displayName.equalsIgnoreCase(cancelDisplay)) {
            player.closeInventory();
        }
    }
}
