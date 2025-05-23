package dev.eggsv31.veldora.veldoraClan.commands.management;

import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanBanCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage dataStorage;
    private final YamlLangManager langManager;

    public ClanBanCommand(JavaPlugin plugin, IClanDataStorage dataStorage, YamlLangManager langManager) {
        this.plugin = plugin;
        this.dataStorage = dataStorage;
        this.langManager = langManager;
    }

    public void handleClanBan(Player player, String[] args) {
        if (args.length < 2) {
            return;
        }
        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("commands.cannot-ban-self", "Kendinizi banlayamazsınız."), getPrefix()));
            return;
        }
        String clanName = dataStorage.getPlayerClan(player);
        if (clanName == null || clanName.isEmpty()) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("commands.no-clan", "Klanınız yok."), getPrefix()));
            return;
        }
        FileConfiguration cfg = dataStorage.getClanConfig(clanName);
        if (cfg == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("commands.no-clan", "Klanınız yok."), getPrefix()));
            return;
        }
        String leaderUUID = cfg.getString("leader", "");
        if (!player.getUniqueId().toString().equals(leaderUUID)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("commands.not-leader", "Sadece lider bu işlemi yapabilir."), getPrefix()));
            return;
        }
        List<Map<String, Object>> bannedList = (List<Map<String, Object>>) cfg.get("banned");
        if (bannedList == null) {
            bannedList = new ArrayList<>();
        }
        List<String> members = cfg.getStringList("clan-members");
        if (members.contains(target.getUniqueId().toString())) {
            members.remove(target.getUniqueId().toString());
            cfg.set("clan-members", members);
            cfg.set("player-count", members.size());
        }
        Map<String, Object> existingBanEntry = null;
        for (Map<String, Object> entry : bannedList) {
            if (entry.get("uuid").equals(target.getUniqueId().toString())) {
                existingBanEntry = entry;
                break;
            }
        }
        if (existingBanEntry != null) {
            existingBanEntry.put("banTime", System.currentTimeMillis());
            existingBanEntry.put("active", true);
            existingBanEntry.put("bannedBy", player.getName());
            existingBanEntry.put("bannedByUUID", player.getUniqueId().toString());
            existingBanEntry.put("name", target.getName());
        } else {
            Map<String, Object> banEntry = new HashMap<>();
            banEntry.put("uuid", target.getUniqueId().toString());
            banEntry.put("name", target.getName());
            banEntry.put("bannedBy", player.getName());
            banEntry.put("bannedByUUID", player.getUniqueId().toString());
            banEntry.put("banTime", System.currentTimeMillis());
            banEntry.put("active", true);
            bannedList.add(banEntry);
        }
        cfg.set("banned", bannedList);
        dataStorage.saveClanConfig(cfg, clanName);
        String banSuccess = langManager.getMessage("commands.ban-success", "%player% adlı oyuncu banlandı").replace("%player%", target.getName());
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(banSuccess, getPrefix()));
        if (target.isOnline()) {
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null) {
                String bannedNotification = langManager.getMessage("commands.banned-notification", "%clan% tarafından banlandınız").replace("%clan%", clanName);
                onlineTarget.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(bannedNotification, getPrefix()));
            }
        }
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
