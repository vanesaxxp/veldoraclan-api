package dev.eggsv31.veldora.veldoraClan.storage.save;

import dev.eggsv31.veldora.veldoraClan.Chest.ClanChest;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ClanChestSaveEvent implements Listener {
    private final IClanDataStorage clanDataStorage;
    private final ClanChest clanChestManager;

    public ClanChestSaveEvent(IClanDataStorage clanDataStorage, ClanChest clanChestManager) {
        this.clanDataStorage = clanDataStorage;
        this.clanChestManager = clanChestManager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        String title = event.getView().getTitle();
        String strippedTitle = ChatColor.stripColor(title);

        if (!strippedTitle.contains(" Sandığı (Sayfa ")) return;

        int index = strippedTitle.indexOf(" Sandığı (Sayfa ");
        if (index <= 0) return;

        String clanName = strippedTitle.substring(0, index).trim();

        int pageStart = strippedTitle.indexOf("Sayfa ") + "Sayfa ".length();
        int pageEnd = strippedTitle.indexOf(")", pageStart);
        int pageIndex = 1;
        try {
            pageIndex = Integer.parseInt(strippedTitle.substring(pageStart, pageEnd));
        } catch (Exception e) {
        }

        clanChestManager.saveClanChest(clanName, pageIndex);
    }
}
