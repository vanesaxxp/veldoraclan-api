package dev.eggsv31.veldora.veldoraClan.managers.gui.menu;

import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.gui.menu.holders.ClanDeleteMenuHolder;
import dev.eggsv31.veldora.veldoraClan.managers.gui.utils.ClanGuiUtils;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.List;

public class ClanDeleteMenu {

    public static final String DELETE_MENU_BASE_TITLE = ChatColor.RED + "§lKlan Silme Onayı";

    public static void openClanDeleteConfirmation(Player player, GuiLangManager guiLangManager) {
        String rawTitle = guiLangManager.getMessage("clan.menu.delete.title", DELETE_MENU_BASE_TITLE);
        String finalTitle = ChatUtils.formatMessage(rawTitle);

        Inventory inv = Bukkit.createInventory(new ClanDeleteMenuHolder(), 27, finalTitle);

        ClanGuiUtils.fillDecorativeSlots(inv);

        String confirmMessage = guiLangManager.getMessage("clan.menu.delete.confirm", "&a&lKlanı Sil");
        List<String> confirmLore = guiLangManager.getMessageList("clan.menu.delete.confirm_lore", Arrays.asList("", "<yellow>Click to accept."));
        ItemStack diamond = ClanGuiUtils.createEnchantedItem(Material.DIAMOND_BLOCK, ChatUtils.formatMessage(confirmMessage), confirmLore);
        inv.setItem(10, diamond);
        inv.setItem(11, diamond);
        inv.setItem(12, diamond);

        String cancelMessage = guiLangManager.getMessage("clan.menu.delete.cancel", "&c&lİptal Et");
        List<String> cancelLore = guiLangManager.getMessageList("clan.menu.delete.cancel_lore", Arrays.asList("", "<yellow>Click to cancel."));
        ItemStack redstone = ClanGuiUtils.createEnchantedItem(Material.REDSTONE_BLOCK, ChatUtils.formatMessage(cancelMessage), cancelLore);
        inv.setItem(14, redstone);
        inv.setItem(15, redstone);
        inv.setItem(16, redstone);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1f, 1f);
    }
}
