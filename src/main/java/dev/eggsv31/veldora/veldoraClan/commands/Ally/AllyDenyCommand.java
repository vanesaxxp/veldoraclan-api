package dev.eggsv31.veldora.veldoraClan.commands.Ally;

import dev.eggsv31.veldora.veldoraClan.managers.ally.AllyManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class AllyDenyCommand {
    private final AllyManager allyManager;
    private final IClanDataStorage IClanDataStorage;
    private final YamlLangManager yamlLangManager;
    private final Map<Player, String> allyInvitePending;
    private final JavaPlugin plugin;
    private final AllyAddCommand allyAddCommand;

    public AllyDenyCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage,
                           YamlLangManager yamlLangManager, Map<Player, String> allyInvitePending,
                           AllyManager allyManager, AllyAddCommand allyAddCommand) {
        this.plugin = plugin;
        this.IClanDataStorage = IClanDataStorage;
        this.yamlLangManager = yamlLangManager;
        this.allyInvitePending = allyInvitePending;
        this.allyManager = allyManager;
        this.allyAddCommand = allyAddCommand;
    }

    public void handleAllyDeny(Player player) {
        if (this.allyInvitePending.containsKey(player)) {
            String invitingClan = this.allyInvitePending.get(player);
            String playerClan = this.IClanDataStorage.getPlayerClan(player);
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("ally.commands.ally-denied", "Müttefik davetini reddettiniz."),
                    this.getPrefix()));
            Player inviter = this.getClanLeader(invitingClan);
            if (inviter != null) {
                inviter.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                        this.yamlLangManager.getMessage("ally.commands.invite-denied",
                                "%clan% klanı müttefik davetinizi reddetti.").replace("%clan%", playerClan),
                        this.getPrefix()));
            }
            this.allyInvitePending.remove(player);
            allyAddCommand.cancelInviteBossBar(player);
            long now = System.currentTimeMillis();
            this.IClanDataStorage.addAllyInviteLog(invitingClan, playerClan, now, "denied");
            this.IClanDataStorage.addAllyInviteLog(playerClan, invitingClan, now, "denied");
        } else {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("ally.commands.no-pending-invite", "Bekleyen bir müttefik davetiniz yok."),
                    this.getPrefix()));
        }
    }

    private Player getClanLeader(String clanName) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (this.IClanDataStorage.isClanLeader(onlinePlayer)
                    && clanName.equalsIgnoreCase(this.IClanDataStorage.getPlayerClan(onlinePlayer))) {
                return onlinePlayer;
            }
        }
        return null;
    }

    private String getPrefix() {
        return this.plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
