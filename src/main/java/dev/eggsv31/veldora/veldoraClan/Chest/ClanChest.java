package dev.eggsv31.veldora.veldoraClan.Chest;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.gui.menu.holders.ClanChestHolder;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class ClanChest {

    private final IClanDataStorage clanDataStorage;
    private final YamlLangManager langManager;
    private final GuiLangManager guiLangManager;
    private final Map<String, Inventory> clanChests = new HashMap<>();
    private final File clanChestFolder;
    private final FileConfiguration config;
    private final JavaPlugin plugin;
    private final Economy economy;

    public ClanChest(IClanDataStorage c,
                     YamlLangManager l,
                     GuiLangManager g,
                     File dataFolder,
                     FileConfiguration conf,
                     JavaPlugin pl,
                     Economy eco) {
        this.clanDataStorage = c;
        this.langManager = l;
        this.guiLangManager = g;
        this.config = conf;
        this.plugin = pl;
        this.economy = eco;
        this.clanChestFolder = new File(dataFolder, "ClanChest");
        if (!clanChestFolder.exists()) {
            clanChestFolder.mkdirs();
        }
    }

    public void openClanChest(Player player, int pageIndex) {
        String clanName = clanDataStorage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."),
                    getPrefix()));
            return;
        }
        FileConfiguration clanConfig = clanDataStorage.getClanConfig(clanName);
        if (clanConfig == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."),
                    getPrefix()));
            return;
        }
        boolean vaultEnabled = config.getBoolean("VaultSupport.enabled", false);
        double chestCost = config.getDouble("VaultSupport.chest", 100.0);
        boolean chestPaid = clanConfig.getBoolean("chest-paid", false);
        if (vaultEnabled && !chestPaid) {
            if (economy.has(player, chestCost)) {
                economy.withdrawPlayer(player, chestCost);
                clanConfig.set("chest-paid", true);
                clanDataStorage.saveClanConfig(clanConfig, clanName);
                String paidMessage = langManager.getMessage("chest.paid", "&aKlan sandığını açmak için %amount% ödediniz! Bir dahaki sefere ücretsiz.");
                paidMessage = paidMessage.replace("%amount%", String.valueOf(chestCost));
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(paidMessage, getPrefix()));
            } else {
                String insufficientMessage = langManager.getMessage("chest.insufficient", "&cYeterli bakiyeniz yok! Gerekli miktar: &b%amount%");
                insufficientMessage = insufficientMessage.replace("%amount%", String.valueOf(chestCost));
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(insufficientMessage, getPrefix()));
                return;
            }
        }
        int maxChestLimit = getChestLimit(player);
        if (pageIndex > maxChestLimit) {
            String limitMessage = langManager.getMessage("chest.action-no-access", "Bu sandık sayfasına erişim izniniz yok! Maksimum erişilebilir sayfa: %limit%");
            limitMessage = limitMessage.replace("%limit%", String.valueOf(maxChestLimit));
            player.sendMessage(ChatColor.RED + ChatUtils.formatMessage(limitMessage));
            return;
        }
        Inventory inv = getClanChest(clanName, pageIndex);
        inv.setItem(2, createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ", false));
        setupLockedSlots(inv, player);
        player.openInventory(inv);
    }

    public Inventory getClanChest(String clanName, int pageIndex) {
        String cacheKey = clanName + "-" + pageIndex;
        if (clanChests.containsKey(cacheKey)) {
            return clanChests.get(cacheKey);
        }
        File file = new File(clanChestFolder, clanName + ".yml");
        migrateOldSingleFormatIfNeeded(file);
        migrateSeparateFilesIfAny(clanName);
        FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(file);
        int size = config.getInt("Chest.slots", 54);
        String defaultTitle = "&2%clan% Sandığı (Sayfa %page%)";
        String chestTitle = guiLangManager.getMessage("clanChest.title", defaultTitle);
        chestTitle = chestTitle.replace("%clan%", clanName)
                .replace("%page%", String.valueOf(pageIndex));
        chestTitle = ChatColor.translateAlternateColorCodes('&', chestTitle);
        Inventory inv = Bukkit.createInventory(new ClanChestHolder(clanName, pageIndex), size, chestTitle);
        String itemsPath = "page." + pageIndex + ".items";
        if (chestConfig.contains(itemsPath)) {
            ConfigurationSection section = chestConfig.getConfigurationSection(itemsPath);
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(key);
                        ItemStack item = chestConfig.getItemStack(itemsPath + "." + key);
                        inv.setItem(slot, item);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid slot number in chest config for " + clanName + ": " + key);
                    }
                }
            }
        }
        clanChests.put(cacheKey, inv);
        return inv;
    }

    public void saveClanChest(String clanName, int pageIndex) {
        String cacheKey = clanName + "-" + pageIndex;
        Inventory inv = clanChests.get(cacheKey);
        if (inv == null) return;
        File file = new File(clanChestFolder, clanName + ".yml");
        FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(file);
        String itemsPath = "page." + pageIndex + ".items";
        chestConfig.set(itemsPath, null);
        for (int i = 9; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                chestConfig.set(itemsPath + "." + i, item);
            }
        }
        try {
            chestConfig.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save clan chest for " + clanName);
        }
    }

    public void saveAllClanChests() {
        for (String key : clanChests.keySet()) {
            String[] parts = key.split("-");
            if (parts.length != 2) continue;
            String cName = parts[0];
            int pIndex;
            try {
                pIndex = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                continue;
            }
            saveClanChest(cName, pIndex);
        }
    }

    public void openClanChest(Player p) {
        openClanChest(p, 1);
    }

    public void wipeChestInventory(String clanName, int pageIndex) {
        String cacheKey = clanName + "-" + pageIndex;
        Inventory inv = clanChests.get(cacheKey);
        if (inv != null) {
            for (int i = 9; i < inv.getSize(); i++) {
                inv.setItem(i, null);
            }
        }
        File file = new File(clanChestFolder, clanName + ".yml");
        if (!file.exists()) return;
        FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(file);
        String itemsPath = "page." + pageIndex + ".items";
        ConfigurationSection sec = chestConfig.getConfigurationSection(itemsPath);
        if (sec != null) {
            for (String s : sec.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(s);
                    if (slot >= 9) {
                        chestConfig.set(itemsPath + "." + s, null);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        try {
            chestConfig.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not wipe clan chest data for " + clanName, e);
        }
    }

    private void migrateSeparateFilesIfAny(String clanName) {
        File[] oldFiles = clanChestFolder.listFiles((dir, name) ->
                name.startsWith(clanName + "-") && name.endsWith(".yml")
        );
        if (oldFiles == null || oldFiles.length == 0) return;
        File targetFile = new File(clanChestFolder, clanName + ".yml");
        FileConfiguration targetConfig = YamlConfiguration.loadConfiguration(targetFile);
        for (File oldFile : oldFiles) {
            String oldName = oldFile.getName();
            int dashIndex = oldName.lastIndexOf('-');
            int dotIndex = oldName.lastIndexOf('.');
            if (dashIndex == -1 || dotIndex == -1 || dashIndex >= dotIndex) continue;
            String pageStr = oldName.substring(dashIndex + 1, dotIndex);
            int pageIndex;
            try {
                pageIndex = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                continue;
            }
            FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
            String itemsPath = "page." + pageIndex + ".items";
            for (String slotStr : oldConfig.getKeys(false)) {
                ItemStack is = oldConfig.getItemStack(slotStr);
                if (is != null) {
                    targetConfig.set(itemsPath + "." + slotStr, is);
                }
            }
            try {
                targetConfig.save(targetFile);
                if (!oldFile.delete()) {
                    plugin.getLogger().warning("Could not delete old file: " + oldFile.getName());
                } else {
                    plugin.getLogger().info("Merged old file " + oldName + " into " + clanName + ".yml");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Error merging " + oldName + " into " + clanName + ".yml", e);
            }
        }
    }

    private void migrateOldSingleFormatIfNeeded(File clanChestFile) {
        if (!clanChestFile.exists()) return;
        FileConfiguration conf = YamlConfiguration.loadConfiguration(clanChestFile);
        if (conf.isConfigurationSection("page")) return;
        Set<String> rootKeys = conf.getKeys(false);
        if (rootKeys.isEmpty()) return;
        boolean migrated = false;
        String itemsPath = "page.1.items";
        for (String key : rootKeys) {
            if ("page".equalsIgnoreCase(key)) continue;
            try {
                Integer.parseInt(key);
            } catch (NumberFormatException ex) {
                continue;
            }
            ItemStack oldItem = conf.getItemStack(key);
            if (oldItem != null) {
                conf.set(itemsPath + "." + key, oldItem);
                conf.set(key, null);
                migrated = true;
            }
        }
        if (migrated) {
            try {
                conf.save(clanChestFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not migrate old single-format data!", e);
            }
        }
    }

    private void setupLockedSlots(Inventory gui, Player player) {
        gui.setItem(0, createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ", false));
        gui.setItem(1, createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ", false));
        gui.setItem(3, createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ", false));
        String transferTitle = guiLangManager.getMessage("clanChest.transfer.title", ChatColor.LIGHT_PURPLE + "Transfer");
        List<String> transferLore = guiLangManager.getList("clanChest.transfer.lore",
                Arrays.asList(ChatColor.GRAY + "Click to transfer items."));
        gui.setItem(4, createGuiItemWithLore(Material.HOPPER, transferTitle, transferLore, true));
        String slimeTitle = guiLangManager.getMessage("clanChest.slimeBall.title", ChatColor.GREEN + "Büyülü Balçık (%page%)");
        slimeTitle = slimeTitle.replace("%page%", String.valueOf(getChestLimit(player)));
        List<String> slimeLore = guiLangManager.getList("clanChest.slimeBall.lore",
                Arrays.asList(ChatColor.GRAY + "Click to confirm transfer."));
        gui.setItem(5, createEnchantedItemWithLore(Material.SLIME_BALL, slimeTitle, slimeLore));
        gui.setItem(6, createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ", false));
        gui.setItem(7, createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ", false));
        String nextTitle = guiLangManager.getMessage("clanChest.next.title", ChatColor.RED + "İleri");
        List<String> nextLore = guiLangManager.getList("clanChest.next.lore",
                Arrays.asList(ChatColor.GRAY + "Click to go to the next page."));
        gui.setItem(8, createGuiItemWithLore(Material.ARROW, nextTitle, nextLore, false));
    }

    private ItemStack createGuiItem(Material material, String displayName, boolean glint) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (glint) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItemWithLore(Material material, String displayName, List<String> lore, boolean glint) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            if (glint) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createEnchantedItemWithLore(Material material, String displayName, List<String> lore) {
        return createGuiItemWithLore(material, displayName, lore, true);
    }

    public int getChestLimit(Player p) {
        if (p.hasPermission("veldora.clan.chest.limit.0")) {
            return 0;
        }
        int max = 1;
        for (int i = 1; i <= 100; i++) {
            if (p.hasPermission("veldora.clan.chest.limit." + i)) {
                max = i;
            }
        }
        return Math.max(max, 1);
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}