package dev.eggsv31.veldora.veldoraClan.commands.members;

import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.clan.ClanMemberManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public class ClanAcceptCommand {
    private final JavaPlugin plugin;
    private final ClanMemberManager clanMemberManager;
    private final ClanInviteCommand clanInviteCommand;
    private final YamlLangManager langManager;

    public ClanAcceptCommand(JavaPlugin plugin, ClanMemberManager clanMemberManager,
                             ClanInviteCommand clanInviteCommand, YamlLangManager langManager) {
        this.plugin = plugin;
        this.clanMemberManager = clanMemberManager;
        this.clanInviteCommand = clanInviteCommand;
        this.langManager = langManager;
    }

    public void handleClanAccept(Player player, String clanName) {
        if (clanName == null || clanName.isEmpty()) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan-specified-accept", "Lütfen kabul etmek için bir klan adı belirtin."),
                    getPrefix()));
            return;
        }
        if (clanInviteCommand.hasPendingInvite(player, clanName)) {
            clanMemberManager.addPlayerToClan(player, clanName);
            clanInviteCommand.removePendingInvite(player, clanName);
            clanInviteCommand.getIClanDataStorage().incrementTotalJoined(clanName);
            clanInviteCommand.getIClanDataStorage().incrementInviteAcceptedCount(clanName);
            List<String> joinedUUIDs = clanInviteCommand.getIClanDataStorage().getJoinedUUIDs(clanName);
            joinedUUIDs.add(player.getUniqueId().toString());
            FileConfiguration cfg = clanInviteCommand.getIClanDataStorage().getClanConfig(clanName);
            cfg.set("joined-uuids", joinedUUIDs);
            clanInviteCommand.getIClanDataStorage().saveClanConfig(cfg, clanName);
            String acceptedMessage = langManager.getMessage("commands.invite-accepted", "Klan davetini kabul ettiniz.");
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(acceptedMessage, getPrefix()));
            clanInviteCommand.cancelInviteBossBar(player);
        } else {
            String msg = langManager.getMessage("commands.no-invite", "Belirtilen klandan gelen davetiniz yok.");
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
        }
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
