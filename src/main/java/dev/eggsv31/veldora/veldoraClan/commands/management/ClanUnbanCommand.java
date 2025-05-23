package dev.eggsv31.veldora.veldoraClan.commands.management;

import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.Map;

public class ClanUnbanCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage dataStorage;
    private final YamlLangManager langManager;

    public ClanUnbanCommand(JavaPlugin plugin, IClanDataStorage dataStorage, YamlLangManager langManager) {
        this.plugin = plugin;
        this.dataStorage = dataStorage;
        this.langManager = langManager;
    }

    public void handleClanUnban(Player player, String[] args) {
        if (args.length < 2) {
            return;
        }
        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
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
        if (bannedList == null || bannedList.isEmpty()) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("commands.not-banned", "Bu oyuncu banlı değil."), getPrefix()));
            return;
        }
        boolean found = false;
        for (Map<String, Object> entry : bannedList) {
            if (entry.get("uuid").equals(target.getUniqueId().toString()) && Boolean.TRUE.equals(entry.get("active"))) {
                entry.put("active", false);
                found = true;
                break;
            }
        }
        if (!found) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("commands.not-banned", "Bu oyuncu banlı değil."), getPrefix()));
            return;
        }
        cfg.set("banned", bannedList);
        dataStorage.saveClanConfig(cfg, clanName);
        String successMessage = langManager.getMessage("commands.unban-success", "%player% adlı oyuncunun banı kaldırıldı.");
        successMessage = successMessage.replace("%player%", target.getName() == null ? targetName : target.getName());
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(successMessage, getPrefix()));
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
