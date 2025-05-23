package dev.eggsv31.veldora.veldoraClan.managers.gui.menu.holders;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ClanChestHolder implements InventoryHolder {

    private final String clanName;
    private final int pageIndex;

    public ClanChestHolder(String clanName, int pageIndex) {
        this.clanName = clanName;
        this.pageIndex = pageIndex;
    }

    public String getClanName() {
        return clanName;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
