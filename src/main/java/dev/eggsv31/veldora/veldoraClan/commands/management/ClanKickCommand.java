package dev.eggsv31.veldora.veldoraClan.commands.management;

import dev.eggsv31.veldora.veldoraClan.managers.clan.ClanMemberManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public class ClanKickCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage IClanDataStorage;
    private final ClanMemberManager clanMemberManager;
    private final YamlLangManager langManager;

    public ClanKickCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage, YamlLangManager langManager, ClanMemberManager clanMemberManager) {
        this.plugin = plugin;
        this.IClanDataStorage = IClanDataStorage;
        this.clanMemberManager = clanMemberManager;
        this.langManager = langManager;
    }

    public void handleClanKick(Player player, String[] args, boolean permissionEnabled) {
        if (permissionEnabled && !player.hasPermission("veldora.clan.kick")) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-permission", "Bunu yapmaya yetkiniz yok."),
                    getPrefix()));
            return;
        }
        if (args.length <= 1) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-player-specified", "Lütfen bir oyuncu ismi girin."),
                    getPrefix()));
            return;
        }
        String targetName = args[1];
        handleKick(player, targetName);
    }

    private void handleKick(Player player, String targetName) {
        String clanName = IClanDataStorage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."),
                    getPrefix()));
            return;
        }
        if (!IClanDataStorage.isClanLeader(player)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.not-leader", "Bu komutu yalnızca klan liderleri kullanabilir."),
                    getPrefix()));
            return;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target != null && clanName.equalsIgnoreCase(IClanDataStorage.getPlayerClan(target))) {
            clanMemberManager.removePlayerFromClan(target);
            IClanDataStorage.incrementTotalKicked(clanName);
            List<String> kickedUUIDs = IClanDataStorage.getKickedUUIDs(clanName);
            kickedUUIDs.add(target.getUniqueId().toString());
            FileConfiguration cfg = IClanDataStorage.getClanConfig(clanName);
            cfg.set("kicked-uuids", kickedUUIDs);
            IClanDataStorage.saveClanConfig(cfg, clanName);

            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.kick-success", "%player% adlı oyuncu klandan atıldı.")
                            .replace("%player%", target.getName()),
                    getPrefix()));
            target.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.kicked", "Klandan atıldınız."),
                    getPrefix()));
        } else {
            handleOfflinePlayerKick(player, targetName, clanName);
        }
    }

    private void handleOfflinePlayerKick(Player player, String targetName, String clanName) {
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
        if (offlineTarget.hasPlayedBefore()
                && clanName.equalsIgnoreCase(IClanDataStorage.getPlayerClan(offlineTarget))) {
            clanMemberManager.removeOfflinePlayerFromClan(offlineTarget);
            IClanDataStorage.incrementTotalKicked(clanName);
            List<String> kickedUUIDs = IClanDataStorage.getKickedUUIDs(clanName);
            if (offlineTarget.getUniqueId() != null) {
                kickedUUIDs.add(offlineTarget.getUniqueId().toString());
            }
            FileConfiguration cfg = IClanDataStorage.getClanConfig(clanName);
            cfg.set("kicked-uuids", kickedUUIDs);
            IClanDataStorage.saveClanConfig(cfg, clanName);

            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.kick-success", "%player% adlı oyuncu klandan atıldı.")
                            .replace("%player%", Objects.requireNonNull(offlineTarget.getName())),
                    getPrefix()));
        } else {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.player-not-in-clan", "Bu oyuncu klanınızda değil."),
                    getPrefix()));
        }
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
