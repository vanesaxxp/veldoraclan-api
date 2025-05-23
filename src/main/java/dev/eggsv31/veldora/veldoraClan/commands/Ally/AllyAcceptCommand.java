package dev.eggsv31.veldora.veldoraClan.commands.Ally;

import dev.eggsv31.veldora.veldoraClan.managers.ally.AllyManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class AllyAcceptCommand {
    private final IClanDataStorage IClanDataStorage;
    private final YamlLangManager yamlLangManager;
    private final AllyManager allyManager;
    private final Map<Player, String> allyInvitePending;
    private final JavaPlugin plugin;
    private final AllyAddCommand allyAddCommand;

    public AllyAcceptCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage,
                             YamlLangManager yamlLangManager, Map<Player, String> allyInvitePending,
                             AllyManager allyManager, AllyAddCommand allyAddCommand) {
        this.plugin = plugin;
        this.allyManager = allyManager;
        this.IClanDataStorage = IClanDataStorage;
        this.yamlLangManager = yamlLangManager;
        this.allyInvitePending = allyInvitePending;
        this.allyAddCommand = allyAddCommand;
    }

    public void handleAllyAccept(Player player) {
        if (this.allyInvitePending.containsKey(player)) {
            String invitingClan = this.allyInvitePending.get(player);
            String playerClan = this.IClanDataStorage.getPlayerClan(player);
            if (playerClan == null) {
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                        this.yamlLangManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."),
                        this.getPrefix()));
                this.allyInvitePending.remove(player);
                allyAddCommand.cancelInviteBossBar(player);
                return;
            }
            this.allyInvitePending.remove(player);
            allyAddCommand.cancelInviteBossBar(player);
            this.allyManager.addClanAlly(playerClan, invitingClan, player.getName());
            long now = System.currentTimeMillis();
            this.IClanDataStorage.addAllyInviteLog(invitingClan, playerClan, now, "accepted");
            this.IClanDataStorage.addAllyInviteLog(playerClan, invitingClan, now, "accepted");
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("ally.commands.ally-accepted", "Müttefikliği kabul ettiniz."),
                    this.getPrefix()));
        } else {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("ally.commands.no-pending-invite", "Bekleyen bir müttefik davetiniz yok."),
                    this.getPrefix()));
        }
    }

    private String getPrefix() {
        return this.plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
