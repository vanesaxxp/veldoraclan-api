package dev.eggsv31.veldora.veldoraClan.commands.members;

import dev.eggsv31.veldora.veldoraClan.managers.clan.ClanMemberManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ClanLeaveCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage IClanDataStorage;
    private final ClanMemberManager clanMemberManager;
    private final YamlLangManager langManager;

    public ClanLeaveCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage, YamlLangManager langManager, ClanMemberManager clanMemberManager) {
        this.plugin = plugin;
        this.IClanDataStorage = IClanDataStorage;
        this.clanMemberManager = clanMemberManager;
        this.langManager = langManager;
    }

    public void handleClanLeave(Player player) {
        String clanName = IClanDataStorage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."),
                    getPrefix()));
            return;
        }
        if (IClanDataStorage.isClanLeader(player)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.leader-cannot-leave", "Klan lideri olduğunuz için klanı terk edemezsiniz. Silmek için /clan delete kullanın."),
                    getPrefix()));
            return;
        }
        clanMemberManager.removePlayerFromClan(player);
        IClanDataStorage.incrementTotalLeft(clanName);
        List<String> leftUUIDs = IClanDataStorage.getLeftUUIDs(clanName);
        leftUUIDs.add(player.getUniqueId().toString());
        FileConfiguration cfg = IClanDataStorage.getClanConfig(clanName);
        cfg.set("left-uuids", leftUUIDs);
        IClanDataStorage.saveClanConfig(cfg, clanName);

        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("commands.clan-left", "Klandan ayrıldınız."),
                getPrefix()));
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
