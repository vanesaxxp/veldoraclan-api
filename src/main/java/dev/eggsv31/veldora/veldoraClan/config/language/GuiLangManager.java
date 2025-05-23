package dev.eggsv31.veldora.veldoraClan.config.language;

import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GuiLangManager {

    private final JavaPlugin plugin;
    private FileConfiguration guiLangConfig;
    private File guiLangFile;

    private final String[][] defaultENG = {
            {"clan.menu.wipe.title", "&c&lSandık Boşaltma Onayı"},
            {"clan.menu.wipe.confirm", "&a&lSandığı Boşalt"},
            {"clan.menu.wipe.cancel", "&c&lİptal Et"},
            {"clan.menu.wipe.page", "&7Sayfa: "},
            {"chest.action.cancel-button", "&c&lİptal Et"},
            {"chest.action.wipe-button", "&a&lSandığı Boşalt"},
            {"chest.action.wipe-done", "&aSandık başarıyla boşaltıldı!"},
            {"chest.action.leader-only", "&cSadece lider sandığı boşaltabilir!"},
            {"chest.action.no-access", "&cBu sandık sayfasına erişim izniniz yok! Maksimum erişilebilir sayfa: %max%"}
    };

    private final String[][] defaultTR = {
            {"chest.title.locked", "&cKlan Sandığı Kilitli!"},
            {"chest.title.unlocked", "&aKlan Sandığı Kilidi Açık!"},
            {"chest.action.wipe-confirm", "&aBu sayfayı temizlemek istediğinize emin misiniz?"},
            {"chest.action.wipe-done", "&aBütün eşyalar silindi!"},
            {"chest.action.cancel", "&cİptal"}
    };

    public GuiLangManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadGuiLanguageFile();
    }

    public void loadGuiLanguageFile() {
        String lang = plugin.getConfig().getString("language.gui", "ENG");
        File langFolder = new File(plugin.getDataFolder(), "Language/GUI");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        this.guiLangFile = new File(langFolder, lang + ".yml");
        if (!this.guiLangFile.exists()) {
            plugin.saveResource("Language/GUI/" + lang + ".yml", false);
        }
        this.guiLangConfig = YamlConfiguration.loadConfiguration(guiLangFile);
        switch (lang.toUpperCase()) {
            case "TR":
                break;
            case "ENG":
                break;
            default:
                break;
        }
    }

    public List<String> getMessageList(String key, List<String> def) {
        List<String> list = guiLangConfig.getStringList(key);
        if (list == null || list.isEmpty()) {
            list = def;
        }
        List<String> formattedList = new ArrayList<>();
        for (String line : list) {
            formattedList.add(ChatUtils.formatMessage(line));
        }
        return formattedList;
    }

    public String getMessage(String key, String def) {
        String msg = guiLangConfig.getString(key, def);
        return ChatUtils.formatMessage(msg);
    }

    public List<String> getLore(String path) {
        List<String> loreList = guiLangConfig.getStringList(path);
        if (loreList == null || loreList.isEmpty()) {
            return List.of("");
        }
        List<String> formattedLore = new ArrayList<>();
        for (String line : loreList) {
            formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return formattedLore;
    }

    public List<String> getStringList(String key) {
        List<String> list = guiLangConfig.getStringList(key);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> formatted = new ArrayList<>();
        for (String line : list) {
            formatted.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return formatted;
    }

    public List<String> getList(String key, List<String> def) {
        List<String> list = guiLangConfig.getStringList(key);
        if (list == null || list.isEmpty()) {
            list = def;
        }
        List<String> formattedList = new ArrayList<>();
        for (String line : list) {
            formattedList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return formattedList;
    }

    public void reloadGuiLang() {
        loadGuiLanguageFile();
    }
}
