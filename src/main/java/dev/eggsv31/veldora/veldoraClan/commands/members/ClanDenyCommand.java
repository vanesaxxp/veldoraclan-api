package dev.eggsv31.veldora.veldoraClan.commands.members;

import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanDenyCommand {
    private final JavaPlugin plugin;
    private final ClanInviteCommand clanInviteCommand;
    private final YamlLangManager langManager;

    public ClanDenyCommand(JavaPlugin plugin, ClanInviteCommand clanInviteCommand, YamlLangManager langManager) {
        this.plugin = plugin;
        this.clanInviteCommand = clanInviteCommand;
        this.langManager = langManager;
    }

    public void handleClanDeny(Player player, String clanName) {
        if (clanName == null || clanName.isEmpty()) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan-specified-deny", "Lütfen reddetmek için bir klan adı belirtin."),
                    getPrefix()));
            return;
        }
        if (clanInviteCommand.hasPendingInvite(player, clanName)) {
            clanInviteCommand.removePendingInvite(player, clanName);
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.invite-denied", "Klan davetini reddettiniz."), getPrefix()));
            clanInviteCommand.cancelInviteBossBar(player);
        } else {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-invite", "Belirtilen klandan gelen davetiniz yok."), getPrefix()));
        }
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
