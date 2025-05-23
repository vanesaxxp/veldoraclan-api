package dev.eggsv31.veldora.veldoraClan.managers.clan;

import dev.eggsv31.veldora.veldoraClan.events.player.models.PlayerStats;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;;

public class ClanStatsManager {
    private final IClanDataStorage storage;

    public ClanStatsManager(IClanDataStorage storage) {
        this.storage = storage;
    }

    public boolean isFounder(String clanName, UUID playerUuid) {
        String leader = storage.getClanConfig(clanName).getString("leader");
        return leader != null && leader.equals(playerUuid.toString());
    }

    public boolean isModerator(String clanName, UUID playerUuid) {
        List<String> mods = storage.getClanConfig(clanName).getStringList("mods");
        return mods.contains(playerUuid.toString());
    }

    public boolean addModerator(String clanName, UUID playerUuid) {
        FileConfiguration cfg = storage.getClanConfig(clanName);
        List<String> mods = new ArrayList<>(cfg.getStringList("mods"));
        String asString = playerUuid.toString();
        if (mods.contains(asString)) return false;
        mods.add(asString);
        cfg.set("mods", mods);
        storage.saveClanConfig(cfg, clanName);
        return true;
    }

    public boolean removeModerator(String clanName, UUID playerUuid) {
        FileConfiguration cfg = storage.getClanConfig(clanName);
        List<String> mods = new ArrayList<>(cfg.getStringList("mods"));
        String asString = playerUuid.toString();
        if (!mods.remove(asString)) return false;
        cfg.set("mods", mods);
        storage.saveClanConfig(cfg, clanName);
        return true;
    }

    public List<UUID> getModerators(String clanName) {
        List<UUID> out = new ArrayList<>();
        for (String s : storage.getClanConfig(clanName).getStringList("mods")) {
            try {
                out.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return out;
    }

    public int getClanKills(FileConfiguration clanConfig) {
        return clanConfig != null ? clanConfig.getInt("total-kills", 0) : 0;
    }

    public int getClanDeaths(FileConfiguration clanConfig) {
        return clanConfig != null ? clanConfig.getInt("total-deaths", 0) : 0;
    }

    public double getClanKDR(FileConfiguration clanConfig) {
        int kills = getClanKills(clanConfig);
        int deaths = getClanDeaths(clanConfig);
        return deaths > 0 ? (double) kills / deaths : (double) kills;
    }

    public void updateClanStatsOnPlayerJoin(FileConfiguration clanConfig, OfflinePlayer player, boolean includeStats) {
        if (clanConfig == null || !includeStats)
            return;

        String playerUUID = player.getUniqueId().toString();
        if (clanConfig.contains("members." + playerUUID))
            return;

        PlayerStats stats = new PlayerStats(player);
        int kills = clanConfig.getInt("total-kills", 0) + stats.getKills();
        int deaths = clanConfig.getInt("total-deaths", 0) + stats.getDeaths();

        clanConfig.set("total-kills", kills);
        clanConfig.set("total-deaths", deaths);
        clanConfig.set("members." + playerUUID + ".kills", stats.getKills());
        clanConfig.set("members." + playerUUID + ".deaths", stats.getDeaths());
    }

    public void updateClanStatsOnPlayerLeave(FileConfiguration clanConfig, OfflinePlayer player, boolean includeStats) {
        if (clanConfig == null || !includeStats)
            return;

        String playerUUID = player.getUniqueId().toString();
        int storedKills = clanConfig.getInt("members." + playerUUID + ".kills", 0);
        int storedDeaths = clanConfig.getInt("members." + playerUUID + ".deaths", 0);

        int kills = Math.max(clanConfig.getInt("total-kills", 0) - storedKills, 0);
        int deaths = Math.max(clanConfig.getInt("total-deaths", 0) - storedDeaths, 0);

        clanConfig.set("total-kills", kills);
        clanConfig.set("total-deaths", deaths);
        clanConfig.set("members." + playerUUID, null);
    }
}