package dev.eggsv31.veldora.veldoraClan.metrics;

import dev.eggsv31.veldora.veldoraClan.VeldoraClan;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.ConfigManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;

import java.util.HashMap;
import java.util.Map;

public class MetricsManager {
    private final Metrics metrics;
    private final ConfigManager configManager;

    public MetricsManager(VeldoraClan plugin) {
        this.configManager = plugin.getConfigManager();
        this.metrics = new Metrics(plugin, 23518);
        initializeMetrics(metrics);
    }

    private void initializeMetrics(Metrics metrics) {
        addCombinedSettingsPie(metrics);
    }

    private void addCombinedSettingsPie(Metrics metrics) {
        metrics.addCustomChart(new AdvancedPie("combined_settings", () -> {
            Map<String, Integer> data = new HashMap<>();
            data.put("Language: " + getFullLanguageName(getConfigString("language.messages", "ENG")), 1);
            data.put("Storage: " + getFullStorageType(getConfigString("Storage.type", "YAML")), 1);
            data.put("Permission: " + getConfigBoolean("Permission"), 1);
            data.put("VaultSupport: " + getConfigBoolean("VaultSupport.enabled"), 1);
            data.put("Chest: " + getConfigBoolean("Chest.enabled"), 1);
            data.put("Discord Clan Create: " + getDiscordBoolean("ClanCreate.enabled"), 1);
            data.put("Discord Clan Join: " + getDiscordBoolean("ClanJoin.enabled"), 1);
            data.put("Discord Clan Leave: " + getDiscordBoolean("ClanLeave.enabled"), 1);
            data.put("Discord Clan Ally Add: " + getDiscordBoolean("ClanAllyAdd.enabled"), 1);
            data.put("Discord Clan Ally Remove: " + getDiscordBoolean("ClanAllyRemove.enabled"), 1);
            return data;
        }));
    }

    private boolean getConfigBoolean(String path) {
        return configManager != null && configManager.getPlugin().getConfig().getBoolean(path, false);
    }

    private String getConfigString(String path, String defaultValue) {
        return configManager != null ? configManager.getPlugin().getConfig().getString(path, defaultValue) : defaultValue;
    }

    private boolean getDiscordBoolean(String path) {
        return configManager != null && configManager.getDiscordConfig().getBoolean(path, false);
    }

    private String getFullStorageType(String type) {
        switch (type.toUpperCase()) {
            case "YAML":
                return "YAML";
            default:
                return "Unknown";
        }
    }

    private String getFullLanguageName(String code) {
        Map<String, String> languageMap = new HashMap<>();
        languageMap.put("ENG", "English");
        languageMap.put("TR", "Turkish");
        languageMap.put("ES", "Spanish");
        languageMap.put("FR", "French");
        languageMap.put("RU", "Russian");
        languageMap.put("JP", "Japanese");

        return languageMap.getOrDefault(code.toUpperCase(), "Unknown");
    }
}
