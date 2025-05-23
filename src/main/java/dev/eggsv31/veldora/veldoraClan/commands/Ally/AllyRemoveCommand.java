package dev.eggsv31.veldora.veldoraClan.commands.Ally;

import dev.eggsv31.veldora.veldoraClan.managers.ally.AllyManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AllyRemoveCommand {
    private final AllyManager allyManager;
    private final IClanDataStorage IClanDataStorage;
    private final YamlLangManager yamlLangManager;
    private final JavaPlugin plugin;

    public AllyRemoveCommand(JavaPlugin plugin, AllyManager allyManager,
                             IClanDataStorage IClanDataStorage, YamlLangManager yamlLangManager) {
        this.plugin = plugin;
        this.allyManager = allyManager;
        this.IClanDataStorage = IClanDataStorage;
        this.yamlLangManager = yamlLangManager;
    }

    public void handleAllyRemove(Player player, String targetClanName) {
        String playerClan = IClanDataStorage.getPlayerClan(player);
        if (playerClan == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    yamlLangManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."),
                    getPrefix()));
            return;
        }

        if (!IClanDataStorage.isClanLeader(player)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    yamlLangManager.getMessage("commands.not-leader", "Bu komutu yalnızca klan liderleri kullanabilir."),
                    getPrefix()));
            return;
        }

        if (!allyManager.isClanAlly(playerClan, targetClanName)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    yamlLangManager.getMessage("ally.commands.not-ally", "Bu klandan müttefiklik bulunamadı."),
                    getPrefix()));
            return;
        }

        allyManager.removeClanAlly(playerClan, targetClanName);
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                yamlLangManager.getMessage("ally.commands.ally-removed", "Müttefikliği kaldırdınız: %clan%")
                        .replace("%clan%", targetClanName),
                getPrefix()));

        Player targetLeader = getClanLeader(targetClanName);
        if (targetLeader != null) {
            targetLeader.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    yamlLangManager.getMessage("ally.commands.ally-removed-notification",
                                    "%clan% klanı müttefikliğinizi sonlandırdı.")
                            .replace("%clan%", playerClan),
                    getPrefix()));
        }

        long now = System.currentTimeMillis();
        this.IClanDataStorage.addAllyInviteLog(playerClan, targetClanName, now, "ally-removed");
        this.IClanDataStorage.addAllyInviteLog(targetClanName, playerClan, now, "ally-removed");
    }

    private Player getClanLeader(String clanName) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (IClanDataStorage.isClanLeader(onlinePlayer)
                    && clanName.equalsIgnoreCase(IClanDataStorage.getPlayerClan(onlinePlayer))) {
                return onlinePlayer;
            }
        }
        return null;
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
