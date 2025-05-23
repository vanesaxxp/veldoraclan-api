package dev.eggsv31.veldora.veldoraClan.api;

import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import dev.eggsv31.veldora.veldoraClan.VeldoraClan;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.managers.clan.ClanStatsManager;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.storage.DataStorage;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VeldoraClanAPIImpl implements VeldoraClanAPI {
    private final IClanDataStorage storage;
    private final YamlLangManager guiLang;
    private final VeldoraClan plugin;

    public VeldoraClanAPIImpl(VeldoraClan plugin, IClanDataStorage clanDataStorage, GuiLangManager guiLangManager) {
        this.plugin = plugin;
        this.storage = plugin.getClanDataStorage();
        this.guiLang = plugin.getLangManager();
    }

    @Override
    public IClanDataStorage getClanDataStorage() {
        return storage;
    }

    @Override public JavaPlugin getPlugin()                   { return storage.getPlugin(); }
    @Override public File getClanFolder()                     { return storage.getClanFolder(); }
    @Override public DataStorage getDataStorage()             { return storage.getDataStorage(); }
    @Override public ClanStatsManager getClanStatsManager()   { return storage.getClanStatsManager(); }
    @Override public YamlLangManager getLangManager()         { return guiLang; }

    @Override public boolean isMemberStorageAccessAllowed(String clanName) { return storage.isMemberStorageAccessAllowed(clanName); }
    @Override public void setMemberStorageAccess(String clanName, boolean allowed) { storage.setMemberStorageAccess(clanName, allowed); }

    @Override public void saveAllClansToDisk()                { storage.saveAllClansToDisk(); }

    @Override public List<String> getAllClans()               { return storage.getAllClans(); }
    @Override public FileConfiguration getClanConfig(String clanName) { return storage.getClanConfig(clanName); }
    @Override public void saveClanConfig(FileConfiguration config, String clanName) { storage.saveClanConfig(config, clanName); }

    @Override public String getDbType()                       { return storage.getDbType(); }

    @Override public String getPlayerClan(OfflinePlayer p)    { return storage.getPlayerClan(p); }
    @Override public boolean isClanLeader(Player player)      { return storage.isClanLeader(player); }

    @Override public void createClan(String clanName, Player player)   { storage.createClan(clanName, player); }
    @Override public void deleteClan(String clanName, Player player)   { storage.deleteClan(clanName, player); }

    @Override public int getTotalClans()                     { return storage.getTotalClans(); }

    @Override public void showClanInfo(Player player, String clanName) { storage.showClanInfo(player, clanName); }

    @Override public int getClanRanking()                    { return storage.getClanRanking(); }

    @Override public List<String> getPlayerNames(List<String> uuids) { return storage.getPlayerNames(uuids); }
    @Override public List<String> getClanMembers(String name) { return storage.getClanMembers(name); }

    @Override public void setClanLeader(Player newLeader, String clanName) { storage.setClanLeader(newLeader, clanName); }
    @Override public String getClanLeader(String clanName)    { return storage.getClanLeader(clanName); }

    @Override public void setClanColor(String clanName, ChatColor color) { storage.setClanColor(clanName, color); }
    @Override public ChatColor getClanColor(String clanName)  { return storage.getClanColor(clanName); }

    @Override public long getLastColorChangeTime(String clanName) { return storage.getLastColorChangeTime(clanName); }
    @Override public void setLastColorChangeTime(String clanName, long t) { storage.setLastColorChangeTime(clanName, t); }

    @Override public List<Player> getOnlineClanMembers(String clanName) { return storage.getOnlineClanMembers(clanName); }

    @Override public int getClanKills(String clanName)       { return storage.getClanKills(clanName); }
    @Override public int getClanDeaths(String clanName)      { return storage.getClanDeaths(clanName); }

    @Override public long getLastRenameTime(String clanName) { return storage.getLastRenameTime(clanName); }
    @Override public void setLastRenameTime(String clanName, long t) { storage.setLastRenameTime(clanName, t); }
    @Override public void renameClan(String oldName, String newName) { storage.renameClan(oldName, newName); }

    @Override public List<String> getClanAllies(String clanName) { return storage.getClanAllies(clanName); }

    @Override public void fixClanFiles()                     { storage.fixClanFiles(); }

    @Override public boolean clanExists(String clanName)     { return storage.clanExists(clanName); }
    @Override public boolean clanExistsIgnoreCase(String clanName) { return storage.clanExistsIgnoreCase(clanName); }

    @Override public int getTotalJoined(String clanName)     { return storage.getTotalJoined(clanName); }
    @Override public void incrementTotalJoined(String clanName){ storage.incrementTotalJoined(clanName); }

    @Override public int getTotalLeft(String clanName)       { return storage.getTotalLeft(clanName); }
    @Override public void incrementTotalLeft(String clanName) { storage.incrementTotalLeft(clanName); }

    @Override public int getTotalKicked(String clanName)     { return storage.getTotalKicked(clanName); }
    @Override public void incrementTotalKicked(String clanName){ storage.incrementTotalKicked(clanName); }

    @Override public List<String> getJoinedUUIDs(String clanName) { return storage.getJoinedUUIDs(clanName); }
    @Override public List<String> getLeftUUIDs(String clanName)   { return storage.getLeftUUIDs(clanName); }
    @Override public List<String> getKickedUUIDs(String clanName) { return storage.getKickedUUIDs(clanName); }

    @Override public long getLeaderLastLogin(String clanName) { return storage.getLeaderLastLogin(clanName); }
    @Override public void setLeaderLastLogin(String clanName, long ts) { storage.setLeaderLastLogin(clanName, ts); }

    @Override public int getInviteSentCount(String clanName)  { return storage.getInviteSentCount(clanName); }
    @Override public void incrementInviteSentCount(String clanName) { storage.incrementInviteSentCount(clanName); }

    @Override public int getInviteAcceptedCount(String clanName) { return storage.getInviteAcceptedCount(clanName); }
    @Override public void incrementInviteAcceptedCount(String clanName) { storage.incrementInviteAcceptedCount(clanName); }

    @Override public int getInviteRejectedCount(String clanName) { return storage.getInviteRejectedCount(clanName); }
    @Override public void incrementInviteRejectedCount(String clanName) { storage.incrementInviteRejectedCount(clanName); }

    @Override public void setMemberJoinDate(String clanName, UUID uuid, long date) { storage.setMemberJoinDate(clanName, uuid, date); }
    @Override public long getMemberJoinDate(String clanName, UUID uuid) { return storage.getMemberJoinDate(clanName, uuid); }

    @Override public void setMemberLastLogin(String clanName, UUID uuid, long ts) { storage.setMemberLastLogin(clanName, uuid, ts); }
    @Override public long getMemberLastLogin(String clanName, UUID uuid) { return storage.getMemberLastLogin(clanName, uuid); }

    @Override public void addAllyInviteLog(String from, String to, long ts, String status) { storage.addAllyInviteLog(from, to, ts, status); }
    @Override public List<Map<String, Object>> getAllyInviteLogs(String clanName) { return storage.getAllyInviteLogs(clanName); }

    @Override public void addRivalInviteLog(String from, String to, long ts, String status) { storage.addRivalInviteLog(from, to, ts, status); }
    @Override public List<Map<String, Object>> getRivalInviteLogs(String clanName) { return storage.getRivalInviteLogs(clanName); }
}
