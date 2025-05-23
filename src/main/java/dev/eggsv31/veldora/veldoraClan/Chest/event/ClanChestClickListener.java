package dev.eggsv31.veldora.veldoraClan.Chest.event;

import dev.eggsv31.veldora.veldoraClan.Chest.ClanChest;
import dev.eggsv31.veldora.veldoraClan.managers.gui.menu.holders.ClanChestHolder;
import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.managers.gui.menu.ClanChestWipeMenu;
import dev.eggsv31.veldora.veldoraClan.managers.gui.menu.holders.WipeMenuHolder;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Event.Result;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ClanChestClickListener implements Listener {

    private final ClanChest clanChest;
    private final IClanDataStorage clanDataStorage;
    private final YamlLangManager yamlLangManager;
    private final GuiLangManager guiLangManager;
    private final Plugin plugin;

    public ClanChestClickListener(ClanChest clanChest, IClanDataStorage clanDataStorage, GuiLangManager guiLangManager, YamlLangManager yamlLangManager, Plugin plugin) {
        this.clanChest = clanChest;
        this.clanDataStorage = clanDataStorage;
        this.guiLangManager = guiLangManager;
        this.yamlLangManager = yamlLangManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        String clanName = clanDataStorage.getPlayerClan(player);
        if (clanName == null) return;
        if (e.getInventory().getHolder() instanceof WipeMenuHolder holder) {
            e.setCancelled(true);
            e.setResult(Result.DENY);
            if (!clickedItem.hasItemMeta()) return;
            NamespacedKey chestButtonKey = new NamespacedKey(plugin, "chest_button");
            String buttonType = clickedItem.getItemMeta().getPersistentDataContainer().get(chestButtonKey, PersistentDataType.STRING);
            if (buttonType == null) return;
            int pageIndex = holder.getPageIndex();
            String wipeDone = ChatUtils.formatMessageWithOptionalPrefix(
                    yamlLangManager.getMessage("chest.wipe-done", "%prefix% &aThe ballot box has been emptied successfully!"),
                    getPrefix()
            );
            if (buttonType.equalsIgnoreCase("confirm")) {
                clanChest.wipeChestInventory(clanName, pageIndex);
                player.sendMessage(wipeDone);
                player.closeInventory();
            } else if (buttonType.equalsIgnoreCase("cancel")) {
                player.closeInventory();
            }
            return;
        }
        if (!(e.getInventory().getHolder() instanceof ClanChestHolder)) return;
        ClanChestHolder holder = (ClanChestHolder) e.getInventory().getHolder();
        if (!holder.getClanName().equalsIgnoreCase(clanName)) return;
        String onlyLeader = ChatUtils.formatMessageWithOptionalPrefix(
                yamlLangManager.getMessage("commands.not-leader", "&cSadece lider sandığı boşaltabilir!"),
                getPrefix()
        );
        int slot = e.getSlot();
        int topSize = e.getView().getTopInventory().getSize();
        boolean clickedTop = e.getRawSlot() < topSize;
        if (clickedTop && slot < 9) {
            e.setCancelled(true);
            e.setResult(Result.DENY);
            if (slot == 4 && clickedItem.getType() == Material.HOPPER) {
                if (!clanDataStorage.isClanLeader(player)) {
                    player.sendMessage(onlyLeader);
                    player.closeInventory();
                } else {
                    int pageIndex = holder.getPageIndex();
                    ClanChestWipeMenu.openChestWipeConfirmation(player, pageIndex, guiLangManager, plugin);
                }
            } else if (slot == 8 && clickedItem.getType() == Material.ARROW) {
                int currentPage = holder.getPageIndex();
                int maxPages = clanChest.getChestLimit(player);
                if (currentPage + 1 > maxPages) {
                    String noAccessMsg = ChatUtils.formatMessageWithOptionalPrefix(
                            yamlLangManager.getMessage("chest.action-no-access", "&cBu sandık sayfasına erişim izniniz yok! Maksimum erişilebilir sayfa: %max%")
                                    .replace("%max%", String.valueOf(maxPages)),
                            getPrefix()
                    );
                    player.sendMessage(noAccessMsg);
                } else {
                    clanChest.openClanChest(player, currentPage + 1);
                }
            }
        }
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
