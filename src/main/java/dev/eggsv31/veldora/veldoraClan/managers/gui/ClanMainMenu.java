package dev.eggsv31.veldora.veldoraClan.managers.gui;

import dev.eggsv31.veldora.veldoraClan.Chest.ClanChest;
import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.managers.gui.utils.ClanGuiUtils;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class ClanMainMenu {

    private final Player player;
    private final IClanDataStorage clanDataStorage;
    private final ClanChest clanChestManager;
    private final GuiLangManager guiLangManager;

    public static final String MAIN_MENU_DEFAULT_TITLE = "&6&lKlan Menüsü";

    public ClanMainMenu(Player player,
                        IClanDataStorage clanDataStorage,
                        ClanChest clanChestManager,
                        GuiLangManager guiLangManager) {
        this.player = player;
        this.clanDataStorage = clanDataStorage;
        this.clanChestManager = clanChestManager;
        this.guiLangManager = guiLangManager;
    }

    public void openMainMenu() {
        String mainMenuTitle = guiLangManager.getMessage("clan.menu.main.title", MAIN_MENU_DEFAULT_TITLE);
        Inventory inv = Bukkit.createInventory(null, 45, ChatUtils.formatMessage(mainMenuTitle));

        String one = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWYzMDM0ZDI0YTg1ZGEzMWQ2NzkzMmMzM2U1ZjE4MjFlMjE5ZDVkY2Q5YzJiYTRmMjU1OWRmNDhkZWVhIn19fQ==";
        String world = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM4Y2YzZjhlNTRhZmMzYjNmOTFkMjBhNDlmMzI0ZGNhMTQ4NjAwN2ZlNTQ1Mzk5MDU1NTI0YzE3OTQxZjRkYyJ9fX0=";
        String exclamation = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZlNTIyZDkxODI1MjE0OWU2ZWRlMmVkZjNmZTBmMmMyYzU4ZmVlNmFjMTFjYjg4YzYxNzIwNzIxOGFlNDU5NSJ9fX0=";

        String logoTitle = guiLangManager.getMessage("clan.menu.main.logo.title", "&6§lKlan Logosu");
        List<String> logoLore = guiLangManager.getLore("clan.menu.main.logo.lore");

        String bannerTitle = guiLangManager.getMessage("clan.menu.main.banner.title", "&b§lKlan Bayrağı");
        List<String> bannerLore = guiLangManager.getLore("clan.menu.main.banner.lore");

        String announceTitle = guiLangManager.getMessage("clan.menu.main.announce.title", "&e§lKlan Duyurusu");
        List<String> announceLore = guiLangManager.getLore("clan.menu.main.announce.lore");

        String infoTitle = guiLangManager.getMessage("clan.menu.main.info.title", "&a§lKlan Bilgisi");
        List<String> infoLore = guiLangManager.getLore("clan.menu.main.info.lore");

        String armorGoldTitle = guiLangManager.getMessage("clan.menu.main.armor.gold.title", "&b§lKlan Zırhı (Altın)");
        List<String> armorGoldLore = guiLangManager.getLore("clan.menu.main.armor.gold.lore");

        String armorDiamondTitle = guiLangManager.getMessage("clan.menu.main.armor.diamond.title", "&b§lKlan Zırhı (Elmas)");
        List<String> armorDiamondLore = guiLangManager.getLore("clan.menu.main.armor.diamond.lore");

        String weaponTitle = guiLangManager.getMessage("clan.menu.main.weapon.title", "&c§lKlan Silahı");
        List<String> weaponLore = guiLangManager.getLore("clan.menu.main.weapon.lore");

        String arrowTitle = guiLangManager.getMessage("clan.menu.main.arrow.title", "&e§lKlan Oku");
        List<String> arrowLore = guiLangManager.getLore("clan.menu.main.arrow.lore");

        String flowerTitle = guiLangManager.getMessage("clan.menu.main.flower.title", "&d§lKlan Çiçeği");
        List<String> flowerLore = guiLangManager.getLore("clan.menu.main.flower.lore");

        String chestTitle = guiLangManager.getMessage("clan.menu.main.chest.title", "&6§lKlan Deposu");
        List<String> chestLore = guiLangManager.getLore("clan.menu.main.chest.lore");

        String settingsTitle = guiLangManager.getMessage("clan.menu.main.settings.title", "&c§lKlan Ayarları");
        List<String> settingsLore = guiLangManager.getLore("clan.menu.main.settings.lore");

        inv.setItem(2, ClanGuiUtils.createCustomHead(one, logoTitle, logoLore));
        inv.setItem(6, ClanGuiUtils.createCustomHead(world, logoTitle, logoLore));

        inv.setItem(4, ClanGuiUtils.createGuiItem(Material.WHITE_BANNER, bannerTitle, bannerLore));
        inv.setItem(8, ClanGuiUtils.createGuiItem(Material.PAPER, announceTitle, announceLore));

        inv.setItem(19, ClanGuiUtils.createPlayerHead(player, infoTitle, infoLore));
        inv.setItem(21, ClanGuiUtils.createPlayerHead(player, infoTitle, infoLore));

        inv.setItem(22, ClanGuiUtils.createEnchantedItem(Material.GOLDEN_HELMET, armorGoldTitle, armorGoldLore));
        inv.setItem(23, ClanGuiUtils.createEnchantedItem(Material.DIAMOND_HELMET, armorDiamondTitle, armorDiamondLore));

        inv.setItem(28, ClanGuiUtils.createEnchantedItem(Material.DIAMOND_SWORD, weaponTitle, weaponLore));

        inv.setItem(29, ClanGuiUtils.createCustomHead(exclamation, logoTitle, logoLore));

        inv.setItem(30, ClanGuiUtils.createGuiItem(Material.BOW, arrowTitle, arrowLore));
        inv.setItem(31, ClanGuiUtils.createGuiItem(Material.SUNFLOWER, flowerTitle, flowerLore));
        inv.setItem(32, ClanGuiUtils.createGuiItem(Material.CHEST, chestTitle, chestLore));

        inv.setItem(44, ClanGuiUtils.createEnchantedItem(Material.COMPARATOR, settingsTitle, settingsLore));

        ClanGuiUtils.fillDecorativeSlots(inv);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1f, 1.5f);
    }
}