package dev.eggsv31.veldora.veldoraClan.managers.gui.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ClanGuiUtils {

    private static final boolean IS_LEGACY_VERSION = checkLegacyVersion();

    private static boolean checkLegacyVersion() {
        String version = Bukkit.getServer().getBukkitVersion();
        return version.contains("1.16") || version.contains("1.17") || version.contains("1.18");
    }

    public static void fillDecorativeSlots(Inventory inv) {
        ItemStack filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    public static ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        List<String> loreList = new ArrayList<>();
        Collections.addAll(loreList, lore);
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createEnchantedItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createEnchantedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createCustomHead(String base64, String displayName, String... lore) {
        if (base64 == null || base64.isEmpty()) {
            System.err.println("[VeldoraClan] HATA: createCustomHead için geçersiz base64!");
            return new ItemStack(Material.PLAYER_HEAD);
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta == null) return head;

        try {
            Method setOwningPlayerMethod = SkullMeta.class.getMethod("setOwningPlayer", OfflinePlayer.class);
            if (setOwningPlayerMethod != null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.randomUUID());
                skullMeta.setOwningPlayer(offlinePlayer);
            }
        } catch (NoSuchMethodException e) {
            try {
                Field profileField = skullMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                GameProfile profile = new GameProfile(UUID.randomUUID(), "Steve");
                profile.getProperties().put("textures", new Property("textures", base64));
                profileField.set(skullMeta, profile);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }

        skullMeta.setDisplayName(displayName);
        if (lore != null && lore.length > 0) {
            skullMeta.setLore(List.of(lore));
        }
        head.setItemMeta(skullMeta);
        return head;
    }

    public static ItemStack createCustomHead(String base64, String displayName, List<String> lore) {
        ItemStack head = createCustomHead(base64, displayName);
        if (head.getItemMeta() != null && lore != null) {
            ItemMeta meta = head.getItemMeta();
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    public static ItemStack createPlayerHead(Player player, String displayName, String... lore) {
        if (player == null) {
            System.err.println("[VeldoraClan] HATA: createPlayerHead için geçersiz Player!");
            return new ItemStack(Material.PLAYER_HEAD);
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta == null) return head;
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName(displayName);
        if (lore != null && lore.length > 0) {
            skullMeta.setLore(List.of(lore));
        }
        head.setItemMeta(skullMeta);
        return head;
    }

    public static ItemStack createPlayerHead(Player player, String displayName, List<String> lore) {
        ItemStack head = createPlayerHead(player, displayName);
        if (head.getItemMeta() != null && lore != null) {
            ItemMeta meta = head.getItemMeta();
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }
}