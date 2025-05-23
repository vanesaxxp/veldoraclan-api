package dev.eggsv31.veldora.veldoraClan.commands.utilities;

import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Material.BLACK_WOOL;
import static org.bukkit.Material.BLUE_WOOL;

public class ClanSetColorCommand implements Listener {
    private final JavaPlugin plugin;
    private final IClanDataStorage clanDataStorage;
    private final YamlLangManager langManager;
    private final GuiLangManager guiLangManager;
    public ClanSetColorCommand(JavaPlugin plugin, IClanDataStorage clanDataStorage, YamlLangManager langManager, GuiLangManager guiLangManager) {
        this.plugin = plugin;
        this.clanDataStorage = clanDataStorage;
        this.langManager = langManager;
        this.guiLangManager = guiLangManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    public void handleClanSetColor(Player player) {
        String clanName = clanDataStorage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("commands.no-clan", "No estás en un clan."), getPrefix()));
            return;
        }
        if (clanDataStorage.isClanLeader(player)) {
            openColorSelectionGUI(player, clanName);
        } else {
            openColorInfoGUI(player, clanName);
        }
    }
    private void openColorSelectionGUI(Player player, String clanName) {
        String title = guiLangManager.getMessage("ColorSelection.title", "&6Selecciona el color de tu clan");
        Inventory colorSelectionGUI = Bukkit.createInventory(new ColorSelectionGUIHolder(clanName), 54, title);
        setupGUI(colorSelectionGUI, clanName);
        addInfoItem(colorSelectionGUI, clanName);
        player.openInventory(colorSelectionGUI);
    }
    private void openColorInfoGUI(Player player, String clanName) {
        String title = guiLangManager.getMessage("ColorSelection.title", "&6Selecciona el color de tu clan");
        Inventory infoGUI = Bukkit.createInventory(new ColorSelectionGUIHolder(clanName), 9, title);
        addInfoItemOnly(infoGUI, clanName);
        player.openInventory(infoGUI);
    }
    private void setupGUI(Inventory colorSelectionGUI, String clanName) {
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        for (int i = 0; i < colorSelectionGUI.getSize(); i++) {
            colorSelectionGUI.setItem(i, glassPane);
        }
        ChatColor currentColor = clanDataStorage.getClanColor(clanName);
        int slotIndex = 10;
        for (ChatColor color : ChatColor.values()) {
            if (!color.isFormat()) {
                ItemStack wool = createWoolItem(color, currentColor);
                colorSelectionGUI.setItem(slotIndex, wool);
                slotIndex++;
                if (slotIndex % 9 == 8) slotIndex += 2;
            }
        }
    }
    private ItemStack createWoolItem(ChatColor color, ChatColor currentColor) {
        Material woolMaterial = getWoolMaterialByChatColor(color);
        ItemStack wool = new ItemStack(woolMaterial);
        ItemMeta meta = wool.getItemMeta();
        if (meta != null) {
            String colorNameKey = color.name();
            String displayName = guiLangManager.getMessage("ColorSelection.woolitems." + colorNameKey + ".display_name", color + colorNameKey);
            meta.setDisplayName(displayName);
            List<String> loreList = new ArrayList<>();
            String loreString = guiLangManager.getMessage("ColorSelection.woolitems." + colorNameKey + ".lore", "&7Haz clic para seleccionar este color " + colorNameKey.toLowerCase() + ".");
            loreList.add(loreString);
            if (color == currentColor) {
                meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.setLore(loreList);
            wool.setItemMeta(meta);
        }
        return wool;
    }
    private void addInfoItem(Inventory colorSelectionGUI, String clanName) {
        ChatColor currentColor = clanDataStorage.getClanColor(clanName);
        long cooldownMillis = plugin.getConfig().getInt("color.cooldown", 24) * 3600000L;
        long timeSinceLastChange = System.currentTimeMillis() - clanDataStorage.getLastColorChangeTime(clanName);
        if (cooldownMillis > 0 && timeSinceLastChange < cooldownMillis) {
            addCooldownInfoItem(colorSelectionGUI, currentColor, cooldownMillis - timeSinceLastChange);
        } else {
            addChangeableInfoItem(colorSelectionGUI, currentColor);
        }
    }
    private void addInfoItemOnly(Inventory infoGUI, String clanName) {
        ChatColor currentColor = clanDataStorage.getClanColor(clanName);
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            String displayName = guiLangManager.getMessage("ColorSelection.InfoItem.display_name", "&6Información del color del clan");
            infoMeta.setDisplayName(displayName);
            String loreString = guiLangManager.getMessage("ColorSelection.InfoItem.lore", "&eColor actual: %current_color%\n&cEl color se puede cambiar en: %hours% horas %minutes% minutos");
            String currentColorName = currentColor != null ? currentColor + currentColor.name() : "&eNinguno";
            loreString = loreString.replace("%current_color%", currentColorName);
            long cooldownMillis = plugin.getConfig().getInt("color.cooldown", 24) * 3600000L;
            long timeSinceLastChange = System.currentTimeMillis() - clanDataStorage.getLastColorChangeTime(clanName);
            long remainingMillis = cooldownMillis - timeSinceLastChange;
            loreString = loreString.replace("%hours%", String.valueOf(remainingMillis / 3600000L))
                    .replace("%minutes%", String.valueOf((remainingMillis % 3600000L) / 60000L));
            String[] loreLines = loreString.split("\\n");
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(line);
            }
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        infoGUI.setItem(4, infoItem);
    }
    private void addChangeableInfoItem(Inventory colorSelectionGUI, ChatColor currentColor) {
        ItemStack changeItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta changeMeta = changeItem.getItemMeta();
        if (changeMeta != null) {
            String itemName = guiLangManager.getMessage("ColorSelection.change.display_name", "&aCambiable");
            changeMeta.setDisplayName(itemName);
            String loreStr = guiLangManager.getMessage("ColorSelection.change.lore", "&e¡El color se puede cambiar!");
            List<String> lore = new ArrayList<>();
            lore.add(loreStr);
            changeMeta.setLore(lore);
            changeItem.setItemMeta(changeMeta);
        }
        colorSelectionGUI.setItem(49, changeItem);
    }
    private void addCooldownInfoItem(Inventory colorSelectionGUI, ChatColor currentColor, long remainingMillis) {
        ItemStack cooldownItem = new ItemStack(Material.CLOCK);
        ItemMeta cooldownMeta = cooldownItem.getItemMeta();
        if (cooldownMeta != null) {
            String itemName = guiLangManager.getMessage("ColorSelection.cooldown.display_name", "&cEnfriamiento");
            cooldownMeta.setDisplayName(itemName);
            String currentColorName = currentColor != null ? currentColor + currentColor.name() : "&eNinguno";
            String loreString = guiLangManager.getMessage("ColorSelection.cooldown.lore", "&eColor actual: %current_color%|&cPuedes cambiar el color en: %hours% horas %minutes% minutos")
                    .replace("%current_color%", currentColorName)
                    .replace("%hours%", String.valueOf(remainingMillis / 3600000L))
                    .replace("%minutes%", String.valueOf((remainingMillis % 3600000L) / 60000L));
            String[] lines = loreString.split("\\|");
            List<String> lore = new ArrayList<>();
            for (String l : lines) {
                lore.add(l);
            }
            cooldownMeta.setLore(lore);
            cooldownItem.setItemMeta(cooldownMeta);
        }
        colorSelectionGUI.setItem(49, cooldownItem);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof ColorSelectionGUIHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE || clickedItem.getType() == Material.BOOK) {
            return;
        }
        String clanName = holder.getClanName();
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("commands.no-clan", "No estás en un clan."), getPrefix()));
            player.closeInventory();
            return;
        }
        if (!clanDataStorage.isClanLeader(player)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("commands.not-leader", "Este comando solo lo puede usar el líder del clan."), getPrefix()));
            player.closeInventory();
            return;
        }
        long cooldownMillis = plugin.getConfig().getInt("color.cooldown", 24) * 3600000L;
        long timeSinceLastChange = System.currentTimeMillis() - clanDataStorage.getLastColorChangeTime(clanName);
        if (cooldownMillis > 0 && timeSinceLastChange < cooldownMillis) {
            long remainingMillis = cooldownMillis - timeSinceLastChange;
            String waitMessage = langManager.getMessage("commands.color-wait", "Espera %hours% horas %minutes% minutos para cambiar el color de nuevo.")
                    .replace("%hours%", String.valueOf(remainingMillis / 3600000L))
                    .replace("%minutes%", String.valueOf((remainingMillis % 3600000L) / 60000L));
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(waitMessage, getPrefix()));
            player.closeInventory();
            return;
        }
        ChatColor chosenColor = getColorFromItem(clickedItem.getType());
        clanDataStorage.setClanColor(clanName, chosenColor);
        if (cooldownMillis > 0) {
            clanDataStorage.setLastColorChangeTime(clanName, System.currentTimeMillis());
        }
        String successMessage = langManager.getMessage("commands.color-set", "El color de tu clan se ha establecido correctamente a %color%.")
                .replace("%color%", chosenColor + chosenColor.name());
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(successMessage, getPrefix()));
        player.closeInventory();
    }
    private ChatColor getColorFromItem(Material material) {
        return switch (material) {
            case BLACK_WOOL -> ChatColor.BLACK;
            case BLUE_WOOL -> ChatColor.BLUE;
            case GREEN_WOOL -> ChatColor.GREEN;
            case CYAN_WOOL -> ChatColor.AQUA;
            case RED_WOOL -> ChatColor.RED;
            case PURPLE_WOOL -> ChatColor.DARK_PURPLE;
            case ORANGE_WOOL -> ChatColor.GOLD;
            case LIGHT_GRAY_WOOL -> ChatColor.GRAY;
            case GRAY_WOOL -> ChatColor.DARK_GRAY;
            case WHITE_WOOL -> ChatColor.WHITE;
            case YELLOW_WOOL -> ChatColor.YELLOW;
            case PINK_WOOL -> ChatColor.LIGHT_PURPLE;
            case BLUE_TERRACOTTA -> ChatColor.DARK_BLUE;
            case GREEN_TERRACOTTA -> ChatColor.DARK_GREEN;
            case CYAN_TERRACOTTA -> ChatColor.DARK_AQUA;
            case RED_TERRACOTTA -> ChatColor.DARK_RED;
            default -> ChatColor.WHITE;
        };
    }
    private Material getWoolMaterialByChatColor(ChatColor color) {
        return switch (color) {
            case BLACK -> BLACK_WOOL;
            case DARK_BLUE -> Material.BLUE_TERRACOTTA;
            case DARK_GREEN -> Material.GREEN_TERRACOTTA;
            case DARK_AQUA -> Material.CYAN_TERRACOTTA;
            case DARK_RED -> Material.RED_TERRACOTTA;
            case DARK_PURPLE -> Material.PURPLE_WOOL;
            case GOLD -> Material.ORANGE_WOOL;
            case GRAY -> Material.LIGHT_GRAY_WOOL;
            case DARK_GRAY -> Material.GRAY_WOOL;
            case BLUE -> BLUE_WOOL;
            case GREEN -> Material.GREEN_WOOL;
            case AQUA -> Material.CYAN_WOOL;
            case RED -> Material.RED_WOOL;
            case LIGHT_PURPLE -> Material.PINK_WOOL;
            case YELLOW -> Material.YELLOW_WOOL;
            case WHITE -> Material.WHITE_WOOL;
            default -> Material.BARRIER;
        };
    }
    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
    private static class ColorSelectionGUIHolder implements InventoryHolder {
        private final String clanName;
        public ColorSelectionGUIHolder(String clanName) {
            this.clanName = clanName;
        }
        public String getClanName() {
            return clanName;
        }
        public Inventory getInventory() {
            return null;
        }
    }
}
