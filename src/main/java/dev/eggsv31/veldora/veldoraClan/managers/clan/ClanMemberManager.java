package dev.eggsv31.veldora.veldoraClan.managers.clan;

import dev.eggsv31.veldora.veldoraClan.events.clan.ClanJoinListener;
import dev.eggsv31.veldora.veldoraClan.events.clan.ClanLeaveListener;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.events.combat.managers.CombatManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class ClanMemberManager {
    private final IClanDataStorage clanDataStorage;
    private final CombatManager combatManager;

    public ClanMemberManager(IClanDataStorage clanDataStorage, CombatManager combatManager) {
        this.clanDataStorage = clanDataStorage;
        this.combatManager   = combatManager;
    }

    public void addPlayerToClan(Player player, String clanName) {
        FileConfiguration config = clanDataStorage.getClanConfig(clanName);
        if (config == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    clanDataStorage.getPlugin().getConfig().getString("messages.no-clan", "&cBu isimde bir klan bulunamadı!"),
                    clanDataStorage.getPlugin().getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")
            ));
            return;
        }

        List<String> members = config.getStringList("clan-members");
        String playerUUID = player.getUniqueId().toString();
        if (!members.contains(playerUUID)) {
            members.add(playerUUID);
            config.set("clan-members", members);
            config.set("player-count", members.size());

            if (clanDataStorage.getPlugin().getConfig().getBoolean("stats.includeStats", true)) {
                clanDataStorage.getClanStatsManager().updateClanStatsOnPlayerJoin(config, player, true);
            }

            clanDataStorage.saveClanConfig(config, clanName);

            if (clanDataStorage.getPlugin().getConfig().getBoolean("broadcasts.join", false)) {
                String prefix = clanDataStorage.getPlugin().getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
                String msg = clanDataStorage.getPlugin().getConfig()
                        .getString("BroadCastMessages.clan-join-player-messages",
                                "%prefix% &7| &a%player% &ehas joined the clan &b%clan%")
                        .replace("%prefix%", prefix)
                        .replace("%player%", player.getName())
                        .replace("%clan%", clanName);
                Bukkit.getServer().broadcastMessage("\n" + ChatUtils.formatMessage(msg) + "\n");
            }

            new ClanJoinListener(clanDataStorage.getConfigManager())
                    .sendDiscordWebhookOnJoin(clanName, player.getName());
        }
    }

    public void removePlayerFromClan(Player player) {
        String clanName = clanDataStorage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    clanDataStorage.getPlugin().getConfig().getString("messages.no-clan", "&cKlan bulunamadı!"),
                    clanDataStorage.getPlugin().getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")
            ));
            return;
        }

        FileConfiguration config = clanDataStorage.getClanConfig(clanName);
        if (config == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    clanDataStorage.getPlugin().getConfig().getString("messages.no-clan", "&cBu isimde bir klan bulunamadı!"),
                    clanDataStorage.getPlugin().getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")
            ));
            return;
        }

        List<String> members = config.getStringList("clan-members");
        String playerUUID = player.getUniqueId().toString();
        if (members.remove(playerUUID)) {
            config.set("clan-members", members);
            config.set("player-count", members.size());

            if (clanDataStorage.getPlugin().getConfig().getBoolean("stats.includeStats", true)) {
                clanDataStorage.getClanStatsManager().updateClanStatsOnPlayerLeave(config, player, false);
            }

            clanDataStorage.saveClanConfig(config, clanName);

            if (clanDataStorage.getPlugin().getConfig().getBoolean("broadcasts.leave", false)) {
                String msg = clanDataStorage.getPlugin().getConfig()
                        .getString("BroadCastMessages.clan-leave-player-messages",
                                "%prefix% &c%player% adlı oyuncu %clan% adlı clandan ayrıldı")
                        .replace("%prefix%", clanDataStorage.getPlugin().getConfig().getString("prefix", "&7[&bVeldoraClan&7] "))
                        .replace("%player%", player.getName())
                        .replace("%clan%", clanName);
                Bukkit.getServer().broadcastMessage("\n" + ChatUtils.formatMessage(msg) + "\n");
            }

            new ClanLeaveListener(clanDataStorage.getConfigManager())
                    .sendDiscordWebhookOnLeave(clanName, player.getName());

            combatManager.removePlayerFromCache(player);
        }
    }

    public void removeOfflinePlayerFromClan(OfflinePlayer offlinePlayer) {
        String clanName = clanDataStorage.getPlayerClan(offlinePlayer);
        if (clanName == null) return;

        FileConfiguration config = clanDataStorage.getClanConfig(clanName);
        if (config == null) return;

        List<String> members = config.getStringList("clan-members");
        String playerUUID = offlinePlayer.getUniqueId().toString();
        if (members.remove(playerUUID)) {
            config.set("clan-members", members);
            config.set("player-count", members.size());

            if (clanDataStorage.getPlugin().getConfig().getBoolean("stats.includeStats", true)) {
                clanDataStorage.getClanStatsManager().updateClanStatsOnPlayerLeave(config, offlinePlayer, false);
            }

            clanDataStorage.saveClanConfig(config, clanName);

            new ClanLeaveListener(clanDataStorage.getConfigManager())
                    .sendDiscordWebhookOnLeave(clanName,
                            offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown");

            if (clanDataStorage.getPlugin().getConfig().getBoolean("broadcasts.leave", false)) {
                String msg = clanDataStorage.getPlugin().getConfig()
                        .getString("BroadCastMessages.clan-leave-player-messages",
                                "%prefix% &c%player% adlı oyuncu %clan% adlı clandan ayrıldı")
                        .replace("%prefix%", clanDataStorage.getPlugin().getConfig().getString("prefix", "&7[&bVeldoraClan&7] "))
                        .replace("%player%", offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown")
                        .replace("%clan%", clanName);
                Bukkit.getServer().broadcastMessage("\n" + ChatUtils.formatMessage(msg) + "\n");
            }
        }
    }
}