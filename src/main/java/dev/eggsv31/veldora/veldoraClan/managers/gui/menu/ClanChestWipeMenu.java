package dev.eggsv31.veldora.veldoraClan.managers.gui.menu;

import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.gui.menu.holders.WipeMenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import java.util.List;

public class ClanChestWipeMenu {

    public static void openChestWipeConfirmation(Player player, int pageIndex, GuiLangManager guiLangManager, Plugin plugin) {
        String defaultTitle = "&c&lSandık Boşaltma Onayı";
        String title = guiLangManager.getMessage("clan.menu.wipe.title", defaultTitle);
        Inventory inv = Bukkit.createInventory(new WipeMenuHolder(pageIndex), 27, ChatColor.translateAlternateColorCodes('&', title));
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, createGuiItem(plugin, Material.RED_STAINED_GLASS_PANE, " ", false, null, null));
        }
        List<String> confirmLore = guiLangManager.getStringList("clan.menu.wipe.confirm_lore");
        String confirmName = guiLangManager.getMessage("clan.menu.wipe.confirm", "&a&lSandığı Boşalt");
        for (int slot = 10; slot <= 12; slot++) {
            inv.setItem(slot, createGuiItem(plugin, Material.TNT, confirmName, true, "confirm", confirmLore));
        }
        List<String> cancelLore = guiLangManager.getStringList("clan.menu.wipe.cancel_lore");
        String cancelName = guiLangManager.getMessage("clan.menu.wipe.cancel", "&c&lİptal Et");
        for (int slot = 14; slot <= 16; slot++) {
            inv.setItem(slot, createGuiItem(plugin, Material.REDSTONE_BLOCK, cancelName, true, "cancel", cancelLore));
        }
        player.openInventory(inv);
    }

    private static ItemStack createGuiItem(Plugin plugin, Material material, String displayName, boolean glint, String buttonType, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            if (lore != null && !lore.isEmpty()) {
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
                meta.setLore(lore);
            }
            if (glint) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            if (buttonType != null) {
                NamespacedKey key = new NamespacedKey(plugin, "chest_button");
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, buttonType);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
