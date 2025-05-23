package dev.eggsv31.veldora.veldoraClan.managers.ally;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class AllyManager {
    private final IClanDataStorage IClanDataStorage;

    public AllyManager(IClanDataStorage IClanDataStorage) {
        this.IClanDataStorage = IClanDataStorage;
    }

    public void addClanAlly(String clanName, String allyName, String playerName) {
        String dbType = IClanDataStorage.getDbType();

        if (dbType.equalsIgnoreCase("YAML")) {
            FileConfiguration clanConfig = IClanDataStorage.getClanConfig(clanName);
            FileConfiguration allyConfig = IClanDataStorage.getClanConfig(allyName);

            if (clanConfig != null && allyConfig != null) {
                List<String> allies = clanConfig.getStringList("allies");
                if (!allies.contains(allyName)) {
                    allies.add(allyName);
                    clanConfig.set("allies", allies);
                    clanConfig.set("ally-count", allies.size());
                    IClanDataStorage.saveClanConfig(clanConfig, clanName);
                }

                List<String> allyAllies = allyConfig.getStringList("allies");
                if (!allyAllies.contains(clanName)) {
                    allyAllies.add(clanName);
                    allyConfig.set("allies", allyAllies);
                    allyConfig.set("ally-count", allyAllies.size());
                    IClanDataStorage.saveClanConfig(allyConfig, allyName);
                }
            }
        }
    }

    public void removeClanAlly(String clanName, String allyName) {
        String dbType = IClanDataStorage.getDbType();

        if (dbType.equalsIgnoreCase("YAML")) {
            FileConfiguration clanConfig = IClanDataStorage.getClanConfig(clanName);
            FileConfiguration allyConfig = IClanDataStorage.getClanConfig(allyName);
            if (clanConfig != null && allyConfig != null) {
                List<String> allies = clanConfig.getStringList("allies");
                allies.remove(allyName);
                clanConfig.set("allies", allies);
                clanConfig.set("ally-count", allies.size());
                IClanDataStorage.saveClanConfig(clanConfig, clanName);

                List<String> allyAllies = allyConfig.getStringList("allies");
                allyAllies.remove(clanName);
                allyConfig.set("allies", allyAllies);
                allyConfig.set("ally-count", allyAllies.size());
                IClanDataStorage.saveClanConfig(allyConfig, allyName);
            }
        }
    }

    public boolean isClanAlly(String clanName, String targetClanName) {
        String dbType = IClanDataStorage.getDbType();

        if (dbType.equalsIgnoreCase("YAML")) {
            FileConfiguration clanConfig = IClanDataStorage.getClanConfig(clanName);
            if (clanConfig == null) return false;
            List<String> allies = clanConfig.getStringList("allies");
            return allies.contains(targetClanName);
        }
        return false;
    }

    public int getAllyCount(String clanName) {
        String dbType = IClanDataStorage.getDbType();

        if (dbType.equalsIgnoreCase("YAML")) {
            FileConfiguration config = IClanDataStorage.getClanConfig(clanName);
            if (config == null) return 0;

            List<String> allies = config.getStringList("allies");
            int count = (allies != null) ? allies.size() : 0;

            int currentAllyCount = config.getInt("ally-count", -1);
            if (currentAllyCount != count) {
                config.set("ally-count", count);
                IClanDataStorage.saveClanConfig(config, clanName);
            }
            return count;
        }
        return 0;
    }
}
