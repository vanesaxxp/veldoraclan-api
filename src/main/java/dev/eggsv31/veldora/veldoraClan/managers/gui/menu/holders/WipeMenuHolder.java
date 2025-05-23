package dev.eggsv31.veldora.veldoraClan.managers.gui.menu.holders;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class WipeMenuHolder implements InventoryHolder {
    private final int pageIndex;

    public WipeMenuHolder(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public int getPageIndex() {
        return pageIndex;
    }
}
