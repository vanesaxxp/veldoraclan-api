package dev.eggsv31.veldora.veldoraClan.managers.configuration.datatype;

import dev.eggsv31.veldora.veldoraClan.events.clan.ClanCreateListener;
import dev.eggsv31.veldora.veldoraClan.managers.clan.ClanStatsManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.ConfigManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.storage.DataStorage;
import dev.eggsv31.veldora.veldoraClan.storage.tasks.AutoSaveTask;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClanDataStorageYAML implements IClanDataStorage {
    private final File clanFolder;
    private final JavaPlugin plugin;
    private YamlLangManager langManager;
    private final FileConfiguration config;
    private final ClanCreateListener clanCreateListener;
    private final ConfigManager configManager;
    private final ClanStatsManager clanStatsManager;
    private final DataStorage dataStorage;
    private final String dbType;
    private static final String DISCORD_SECTION = "discord-links";

    public ClanDataStorageYAML(JavaPlugin plugin, FileConfiguration config, YamlLangManager langManager, ConfigManager configManager, DataStorage dataStorage, IClanDataStorage storage) {
        this.plugin = plugin;
        this.config = config;
        this.langManager = langManager;
        this.configManager = configManager;
        this.clanFolder = new File(plugin.getDataFolder(), "Storage/YAML");
        this.clanCreateListener = new ClanCreateListener(configManager);
        this.clanStatsManager = new ClanStatsManager(storage);
        this.dataStorage = dataStorage;
        this.dbType = "YAML";
        File newClanFolder = new File(plugin.getDataFolder(), "Storage/YAML");
        if (!newClanFolder.exists()) {
            newClanFolder.mkdirs();
        }
        dataStorage.loadAllClans();
        startAutoSaveTask();
    }

    private void startAutoSaveTask() {
        new AutoSaveTask(this).runTaskTimer(plugin, 0L, 6000L);
    }

    @Override
    public void saveAllClansToDisk() {
        dataStorage.saveAllClans();
    }

    @Override
    public List<String> getAllClans() {
        return new ArrayList<>(dataStorage.getAllCachedClans().keySet());
    }

    @Override
    public FileConfiguration getClanConfig(String clanName) {
        return dataStorage.getClanConfig(clanName);
    }

    @Override
    public void saveClanConfig(FileConfiguration config, String clanName) {
        dataStorage.getAllCachedClans().put(clanName, config);
        dataStorage.saveClanConfig(clanName);
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public DataStorage getDataStorage() {
        return dataStorage;
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public ClanStatsManager getClanStatsManager() {
        return clanStatsManager;
    }

    @Override
    public String getDbType() {
        return dbType;
    }

    @Override
    public File getClanFolder() {
        return clanFolder;
    }

    @Override
    public void setLangManager(YamlLangManager langManager) {
        this.langManager = langManager;
    }

    @Override
    public String getPlayerClan(OfflinePlayer offlinePlayer) {
        UUID playerUUID = offlinePlayer.getUniqueId();
        for (Map.Entry<String, FileConfiguration> entry : dataStorage.getAllCachedClans().entrySet()) {
            FileConfiguration cfg = entry.getValue();
            List<String> members = cfg.getStringList("clan-members");
            String leaderUUID = cfg.getString("leader");
            if (members.contains(playerUUID.toString()) || playerUUID.toString().equals(leaderUUID)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void setDiscordLink(UUID playerUuid, long discordId) {
        String clanName = getPlayerClan(Bukkit.getOfflinePlayer(playerUuid));
        if (clanName == null) return;

        FileConfiguration cfg = getClanConfig(clanName);
        cfg.set(DISCORD_SECTION + ".members." + playerUuid.toString(), discordId);
        if (isClanLeader(Bukkit.getPlayer(playerUuid))) {
            cfg.set(DISCORD_SECTION + ".leader", discordId);
        }
        saveClanConfig(cfg, clanName);
        dataStorage.getAllCachedClans().put(clanName, cfg);
    }

    @Override
    public Long getDiscordId(UUID playerUuid) {
        String clanName = getPlayerClan(Bukkit.getOfflinePlayer(playerUuid));
        if (clanName == null) return null;
        return getClanConfig(clanName)
                .getLong(DISCORD_SECTION + ".members." + playerUuid.toString(), 0L);
    }

    @Override
    public UUID getPlayerUuidByDiscordId(long discordId) {
        for (String clan : getAllClans()) {
            FileConfiguration cfg = getClanConfig(clan);
            ConfigurationSection sec = cfg.getConfigurationSection(DISCORD_SECTION + ".members");
            if (sec == null) continue;
            for (String key : sec.getKeys(false)) {
                if (sec.getLong(key) == discordId) {
                    return UUID.fromString(key);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isClanLeader(Player player) {
        String clanName = getPlayerClan(player);
        if (clanName == null) return false;
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg == null) return false;
        String leaderUUID = cfg.getString("leader");
        if (leaderUUID != null && !leaderUUID.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(leaderUUID);
                return player.getUniqueId().equals(uuid);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void createClan(String clanName, Player player) {
        String existingClan = getPlayerClan(player);
        if (existingClan != null) {
            sendMessage(player, "commands.already-in-clan", "Zaten bir klana sahipsiniz ve başka bir klan kuramazsınız.");
            return;
        }
        String clanNamePattern = plugin.getConfig().getString("Clan.name.regex", "^[a-zA-Z0-9ığüşöçİĞÜŞÖÇ]+$");
        if (!clanName.matches(clanNamePattern)) {
            sendMessage(player, "commands.clan-name-banned", "Clan İsmi Yasaklı!");
            return;
        }
        if (clanExistsIgnoreCase(clanName)) {
            sendMessage(player, "commands.clan-exists", "Bu isimle bir klan zaten mevcut.");
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String creationDate = dateFormat.format(new Date());
        FileConfiguration clanConfig = new YamlConfiguration();
        clanConfig.set("clan-name", clanName);
        clanConfig.set("leader", player.getUniqueId().toString());
        clanConfig.set("creation-date", creationDate);
        clanConfig.set("chest-paid", false);
        clanConfig.set("color", null);
        clanConfig.set("lastColorChangeTime", 0L);
        clanConfig.set("lastRenameTime", 0L);
        clanConfig.set("allies", new ArrayList<>());
        if (plugin.getConfig().getBoolean("stats.includeStats", true)) {
            clanConfig.set("total-kills", player.getStatistic(Statistic.PLAYER_KILLS));
            clanConfig.set("total-deaths", player.getStatistic(Statistic.DEATHS));
        } else {
            clanConfig.set("total-kills", 0);
            clanConfig.set("total-deaths", 0);
        }
        clanConfig.set("player-count", 1);
        List<String> members = new ArrayList<>();
        members.add(player.getUniqueId().toString());
        clanConfig.set("clan-members", members);
        clanConfig.set("online_members", "");
        clanConfig.set("mods", new ArrayList<>());
        clanConfig.set("rival", new ArrayList<>());
        clanConfig.set("banned", new ArrayList<>());
        clanConfig.set("pvp", false);
        clanConfig.set("total-joined", 1);
        clanConfig.set("total-left", 0);
        clanConfig.set("total-kicked", 0);
        clanConfig.set("joined-uuids", new ArrayList<>());
        clanConfig.set("left-uuids", new ArrayList<>());
        clanConfig.set("kicked-uuids", new ArrayList<>());
        clanConfig.set("leader-last-login", 0L);
        clanConfig.set("invite-sent", 0);
        clanConfig.set("invite-accepted", 0);
        clanConfig.set("invite-rejected", 0);
        clanConfig.set("ally-logs", new ArrayList<>());
        clanConfig.set("rival-logs", new ArrayList<>());
        dataStorage.addClan(clanName, clanConfig);
        if (plugin.getConfig().getBoolean("broadcasts.create", false)) {
            String message = plugin.getConfig().getString("BroadCastMessages.clan-create-messages",
                            "%prefix% &a%player% adlı oyuncu %clan% adında bir clan oluşturdu")
                    .replace("%prefix%", plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] "))
                    .replace("%player%", player.getName())
                    .replace("%clan%", clanName);
            Bukkit.getServer().broadcastMessage("\n" + ChatUtils.formatMessage(message) + "\n");
        }
        int totalClans = getTotalClans();
        clanCreateListener.sendDiscordWebhookAsync(clanName, player.getName(), totalClans);
    }

    private void sendMessage(Player player, String key, String defaultMessage) {
        String message = langManager.getMessage(key, defaultMessage);
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(message, config.getString("prefix", "&7[&bVeldoraClan&7] ")));
    }

    @Override
    public int getTotalClans() {
        return dataStorage.getAllCachedClans().size();
    }

    @Override
    public void deleteClan(String clanName, Player player) {
        FileConfiguration clanConfig = getClanConfig(clanName);
        if (clanConfig == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan", "&cBu isimde bir klan bulunamadı!"),
                    config.getString("prefix", "&7[&bVeldoraClan&7] ")
            ));
            return;
        }
        if (plugin.getConfig().getBoolean("broadcasts.delete", false)) {
            String prefix = plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
            String messageKey = "BroadCastMessages.clan-delete-messages";
            String defaultMessage = "%prefix% &eThe clan %clan% has been deleted by %player%";
            String msg = plugin.getConfig().getString(messageKey, defaultMessage)
                    .replace("%prefix%", prefix)
                    .replace("%clan%", clanName)
                    .replace("%player%", player.getName());
            Bukkit.getServer().broadcastMessage("\n" + ChatUtils.formatMessage(msg) + "\n");
        }
        dataStorage.removeClan(clanName);
        File clanChestFile = new File(plugin.getDataFolder(), "ClanChest/" + clanName + ".yml");
        if (clanChestFile.exists()) {
            if (!clanChestFile.delete()) {
                plugin.getLogger().severe("Failed to delete clan chest file: " + clanChestFile.getPath());
            }
        }
    }

    @Override
    public void showClanInfo(Player player, String clanName) {
        FileConfiguration clanConfig = getClanConfig(clanName);
        if (clanConfig == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("commands.no-clan", "&cBu isimde bir klan bulunamadı!"), config.getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        String leaderUUID = clanConfig.getString("leader", "unknown");
        String leaderName = "unknown";
        try {
            if (!leaderUUID.equalsIgnoreCase("unknown")) {
                OfflinePlayer offlineLeader = Bukkit.getOfflinePlayer(UUID.fromString(leaderUUID));
                leaderName = offlineLeader.getName() != null ? offlineLeader.getName() : "unknown";
            }
        } catch (Exception ignored) {
        }
        int ranking = getClanRanking();
        int playerCount = clanConfig.getInt("player-count", 0);
        int totalKills = clanStatsManager.getClanKills(clanConfig);
        int totalDeaths = clanStatsManager.getClanDeaths(clanConfig);
        double kdr = clanStatsManager.getClanKDR(clanConfig);
        List<String> members = clanConfig.getStringList("clan-members");
        String memberList = members.isEmpty() ? "X" : String.join(", ", dataStorage.getPlayerNamesFromUUIDs(members));
        List<String> allies = clanConfig.getStringList("allies");
        String allyMessage = allies.isEmpty()
                ? langManager.getMessage("clan_list_messages.clan-list-no-allies", "&7Allies: &cX")
                : langManager.getMessage("clan_list_messages.clan-list-allies", "&7Allies: &a%allies%").replace("%allies%", String.join(", ", allies));
        String prefix = config.getString("prefix", "&7[&bVeldoraClan&7]");
        String[] messages = {
                langManager.getMessage("clan_list_messages.clan-list-title", "&6Clan Information:"),
                langManager.getMessage("clan_list_messages.clan-list-leader", "&eLeader: &f%leader%").replace("%leader%", leaderName),
                langManager.getMessage("clan_list_messages.clan-list-players", "&ePlayer Count: &f%player-count%").replace("%player-count%", String.valueOf(playerCount)),
                langManager.getMessage("clan_list_messages.clan-list-kills", "&eTotal Kills: &f%total-kills%").replace("%total-kills%", String.valueOf(totalKills)),
                langManager.getMessage("clan_list_messages.clan-list-deaths", "&eTotal Deaths: &f%total-deaths%").replace("%total-deaths%", String.valueOf(totalDeaths)),
                langManager.getMessage("clan_list_messages.clan-list-kdr", "&eKDR: &f%kdr%").replace("%kdr%", String.format("%.2f", kdr)),
                langManager.getMessage("clan_list_messages.clan-list-members", "&eMembers: &f%members%").replace("%members%", memberList),
                ChatColor.translateAlternateColorCodes('&', allyMessage),
                langManager.getMessage("clan_list_messages.clan-list-ranking", "&eRanking: &f%rank%").replace("%rank%", String.valueOf(ranking))
        };
        for (String message : messages) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(message, prefix));
        }
    }

    @Override
    public int getClanRanking() {
        Map<String, Integer> clanKills = new HashMap<>();
        for (Map.Entry<String, FileConfiguration> entry : dataStorage.getAllCachedClans().entrySet()) {
            int kills = clanStatsManager.getClanKills(entry.getValue());
            clanKills.put(entry.getKey(), kills);
        }
        return clanKills.values().stream().max(Comparator.naturalOrder()).orElse(-1);
    }

    @Override
    public List<String> getPlayerNames(List<String> uuids) {
        List<String> playerNames = new ArrayList<>();
        for (String uuidStr : uuids) {
            if (uuidStr == null || uuidStr.trim().isEmpty()) {
                continue;
            }
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    playerNames.add(p.getName());
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    if (offlinePlayer.hasPlayedBefore()) {
                        playerNames.add(offlinePlayer.getName());
                    } else {
                        playerNames.add("Unknown");
                    }
                }
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid UUID in getPlayerNames: " + uuidStr);
            }
        }
        return playerNames;
    }

    @Override
    public List<String> getClanMembers(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getStringList("clan-members") : Collections.emptyList();
    }

    @Override
    public void setClanLeader(Player newLeader, String clanName) {
        FileConfiguration clanConfig = getClanConfig(clanName);
        if (clanConfig != null) {
            clanConfig.set("leader", newLeader.getUniqueId().toString());
            saveClanConfig(clanConfig, clanName);
        }
    }

    @Override
    public void setClanColor(String clanName, ChatColor color) {
        FileConfiguration clanConfig = getClanConfig(clanName);
        if (clanConfig != null) {
            clanConfig.set("color", color != null ? color.name() : null);
            saveClanConfig(clanConfig, clanName);
        }
    }

    @Override
    public ChatColor getClanColor(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            String colorName = cfg.getString("color", null);
            if (colorName != null && !colorName.isEmpty()) {
                try {
                    return ChatColor.valueOf(colorName.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }

    @Override
    public long getLastColorChangeTime(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getLong("lastColorChangeTime", 0L) : 0L;
    }

    @Override
    public void setLastColorChangeTime(String clanName, long currentTime) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            cfg.set("lastColorChangeTime", currentTime);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public String getClanLeader(String clanName) {
        FileConfiguration clanConfig = getClanConfig(clanName);
        if (clanConfig == null) {
            return null;
        }
        String leaderUUID = clanConfig.getString("leader", "");
        if (leaderUUID.isEmpty()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(leaderUUID);
            OfflinePlayer leader = Bukkit.getOfflinePlayer(uuid);
            return leader.getName() != null ? leader.getName() : leaderUUID;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public List<Player> getOnlineClanMembers(String clanName) {
        List<Player> onlineClanMembers = new ArrayList<>();
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            List<String> members = cfg.getStringList("clan-members");
            String leaderUUID = cfg.getString("leader");
            for (Player pl : Bukkit.getOnlinePlayers()) {
                String playerUUID = pl.getUniqueId().toString();
                if (members.contains(playerUUID) || playerUUID.equals(leaderUUID)) {
                    onlineClanMembers.add(pl);
                }
            }
        }
        return onlineClanMembers;
    }

    @Override
    public int getClanKills(String clanName) {
        FileConfiguration clanConfig = getClanConfig(clanName);
        return clanConfig != null ? clanConfig.getInt("total-kills", 0) : 0;
    }

    @Override
    public int getClanDeaths(String clanName) {
        FileConfiguration clanConfig = getClanConfig(clanName);
        return clanConfig != null ? clanConfig.getInt("total-deaths", 0) : 0;
    }

    @Override
    public long getLastRenameTime(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getLong("lastRenameTime", 0L) : 0L;
    }

    @Override
    public void setLastRenameTime(String clanName, long currentTime) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            cfg.set("lastRenameTime", currentTime);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public void renameClan(String oldName, String newName) {
        File oldClanFile = new File(clanFolder, oldName + ".yml");
        if (!oldClanFile.exists()) {
            return;
        }
        File newClanFile = new File(clanFolder, newName + ".yml");
        if (newClanFile.exists()) {
            return;
        }
        try {
            Files.move(oldClanFile.toPath(), newClanFile.toPath());
            FileConfiguration newClanConfig = YamlConfiguration.loadConfiguration(newClanFile);
            newClanConfig.set("clan-name", newName);
            newClanConfig.set("lastRenameTime", System.currentTimeMillis());
            newClanConfig.save(newClanFile);
            dataStorage.getAllCachedClans().remove(oldName);
            dataStorage.getAllCachedClans().put(newName, newClanConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fixClanFiles() {
        if (dbType.equalsIgnoreCase("YAML")) {
        }
    }


    public boolean isMemberStorageAccessAllowed(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null && cfg.getBoolean("chest-access", true);
    }

    public void setMemberStorageAccess(String clanName, boolean allowed) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            cfg.set("chest-access", allowed);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public boolean clanExists(String clanName) {
        return dataStorage.clanExistsIgnoreCase(clanName);
    }

    @Override
    public boolean clanExistsIgnoreCase(String clanName) {
        return dataStorage.clanExistsIgnoreCase(clanName);
    }

    @Override
    public int getTotalJoined(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getInt("total-joined", 0) : 0;
    }

    @Override
    public void incrementTotalJoined(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            int current = cfg.getInt("total-joined", 0);
            cfg.set("total-joined", current + 1);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public int getTotalLeft(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getInt("total-left", 0) : 0;
    }

    @Override
    public void incrementTotalLeft(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            int current = cfg.getInt("total-left", 0);
            cfg.set("total-left", current + 1);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public int getTotalKicked(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getInt("total-kicked", 0) : 0;
    }

    @Override
    public void incrementTotalKicked(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            int current = cfg.getInt("total-kicked", 0);
            cfg.set("total-kicked", current + 1);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public List<String> getJoinedUUIDs(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getStringList("joined-uuids") : new ArrayList<>();
    }

    @Override
    public List<String> getLeftUUIDs(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getStringList("left-uuids") : new ArrayList<>();
    }

    @Override
    public List<String> getKickedUUIDs(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getStringList("kicked-uuids") : new ArrayList<>();
    }

    @Override
    public long getLeaderLastLogin(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getLong("leader-last-login", 0L) : 0L;
    }

    @Override
    public void setLeaderLastLogin(String clanName, long lastLogin) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            cfg.set("leader-last-login", lastLogin);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public int getInviteSentCount(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getInt("invite-sent", 0) : 0;
    }

    @Override
    public void incrementInviteSentCount(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            int val = cfg.getInt("invite-sent", 0);
            cfg.set("invite-sent", val + 1);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public int getInviteAcceptedCount(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getInt("invite-accepted", 0) : 0;
    }

    @Override
    public void incrementInviteAcceptedCount(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            int val = cfg.getInt("invite-accepted", 0);
            cfg.set("invite-accepted", val + 1);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public int getInviteRejectedCount(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return cfg != null ? cfg.getInt("invite-rejected", 0) : 0;
    }

    @Override
    public void incrementInviteRejectedCount(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            int val = cfg.getInt("invite-rejected", 0);
            cfg.set("invite-rejected", val + 1);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public void setMemberJoinDate(String clanName, UUID playerUUID, long joinDate) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            String key = "join-dates." + playerUUID;
            cfg.set(key, joinDate);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public long getMemberJoinDate(String clanName, UUID playerUUID) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            String key = "join-dates." + playerUUID;
            return cfg.getLong(key, 0L);
        }
        return 0L;
    }

    @Override
    public void setMemberLastLogin(String clanName, UUID playerUUID, long lastLogin) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            String key = "last-login." + playerUUID;
            cfg.set(key, lastLogin);
            saveClanConfig(cfg, clanName);
        }
    }

    @Override
    public long getMemberLastLogin(String clanName, UUID playerUUID) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg != null) {
            String key = "last-login." + playerUUID;
            return cfg.getLong(key, 0L);
        }
        return 0L;
    }

    @Override
    public void addAllyInviteLog(String fromClan, String toClan, long timestamp, String status) {
        FileConfiguration cfg = getClanConfig(fromClan);
        if (cfg == null) {
            return;
        }
        long acceptTimestamp = 0;
        if (status.equalsIgnoreCase("accepted")) {
            acceptTimestamp = System.currentTimeMillis();
        }
        List<Map<String, Object>> logs = (List<Map<String, Object>>) cfg.getList("ally-logs");
        if (logs == null) {
            logs = new ArrayList<>();
        }
        Map<String, Object> entry = new HashMap<>();
        entry.put("timestamp", timestamp);
        entry.put("toClan", toClan);
        entry.put("status", status);
        entry.put("accept_timestamp", acceptTimestamp);
        logs.add(entry);
        cfg.set("ally-logs", logs);
        saveClanConfig(cfg, fromClan);
    }

    @Override
    public List<Map<String, Object>> getAllyInviteLogs(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> logs = (List<Map<String, Object>>) cfg.getList("ally-logs");
        return logs != null ? logs : Collections.emptyList();
    }

    @Override
    public void addRivalInviteLog(String fromClan, String toClan, long timestamp, String status) {
        FileConfiguration cfg = getClanConfig(fromClan);
        if (cfg == null) {
            return;
        }
        long acceptTimestamp = 0;
        if (status.equalsIgnoreCase("accepted")) {
            acceptTimestamp = System.currentTimeMillis();
        }
        List<Map<String, Object>> logs = (List<Map<String, Object>>) cfg.getList("rival-logs");
        if (logs == null) {
            logs = new ArrayList<>();
        }
        Map<String, Object> entry = new HashMap<>();
        entry.put("timestamp", timestamp);
        entry.put("toClan", toClan);
        entry.put("status", status);
        entry.put("accept_timestamp", acceptTimestamp);
        logs.add(entry);
        cfg.set("rival-logs", logs);
        saveClanConfig(cfg, fromClan);
    }

    @Override
    public List<Map<String, Object>> getRivalInviteLogs(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        if (cfg == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> logs = (List<Map<String, Object>>) cfg.getList("rival-logs");
        return logs != null ? logs : Collections.emptyList();
    }

    @Override
    public List<String> getClanAllies(String clanName) {
        FileConfiguration cfg = getClanConfig(clanName);
        return (cfg != null) ? cfg.getStringList("allies") : Collections.emptyList();
    }
}
