package dev.eggsv31.veldora.veldoraClan.storage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStorage {
    private final JavaPlugin plugin;
    private final File clanFolder;
    private final Map<String, FileConfiguration> clanCache = new ConcurrentHashMap<>();

    public DataStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.clanFolder = new File(plugin.getDataFolder(), "Storage/YAML");
        if (!clanFolder.exists()) {
            clanFolder.mkdirs();
        }
        loadAllClans();
    }

    public void loadAllClans() {
        clanCache.clear();
        File[] clanFiles = clanFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (clanFiles == null) return;
        for (File file : clanFiles) {
            String clanName = file.getName().replace(".yml", "");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            clanCache.put(clanName, config);
        }
    }

    public FileConfiguration getClanConfig(String clanName) {
        return clanCache.get(clanName);
    }

    public void saveClanConfig(String clanName) {
        FileConfiguration config = clanCache.get(clanName);
        if (config == null) return;
        File file = new File(clanFolder, clanName + ".yml");
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save clan file: " + clanName + ".yml");
            e.printStackTrace();
        }
    }

    public void saveAllClans() {
        for (String clanName : clanCache.keySet()) {
            saveClanConfig(clanName);
        }
    }

    public void addClan(String clanName, FileConfiguration config) {
        clanCache.put(clanName, config);
        saveClanConfig(clanName);
    }

    public void removeClan(String clanName) {
        clanCache.remove(clanName);
        File file = new File(clanFolder, clanName + ".yml");
        if (file.exists() && !file.delete()) {
            plugin.getLogger().severe("Failed to delete clan file: " + clanName + ".yml");
        }
    }

    public boolean clanExistsIgnoreCase(String clanName) {
        return clanCache.keySet().stream()
                .anyMatch(existing -> existing.equalsIgnoreCase(clanName));
    }

    public Map<String, FileConfiguration> getAllCachedClans() {
        return clanCache;
    }

    public List<String> getPlayerNamesFromUUIDs(List<String> uuids) {
        List<String> playerNames = new ArrayList<>();
        for (String uuidStr : uuids) {
            if (uuidStr == null || uuidStr.isBlank()) continue;
            try {
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));
                playerNames.add(player.getName() != null ? player.getName() : "Unknown");
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid UUID in getPlayerNamesFromUUIDs: " + uuidStr);
            }
        }
        return playerNames;
    }
}
