package dev.eggsv31.veldora.veldoraClan.commands.Rival;

import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.managers.rival.RivalManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class RivalDenyCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage storage;
    private final YamlLangManager langManager;
    private final Map<String, List<String>> pendingRivalInvites;
    private final RivalManager rivalManager;
    public RivalDenyCommand(JavaPlugin plugin, IClanDataStorage storage, YamlLangManager langManager, Map<String, List<String>> pendingRivalInvites, RivalManager rivalManager) {
        this.plugin = plugin;
        this.storage = storage;
        this.langManager = langManager;
        this.pendingRivalInvites = pendingRivalInvites;
        this.rivalManager = rivalManager;
    }
    public void handleRivalDeny(Player player) {
        String targetClan = storage.getPlayerClan(player);
        if(targetClan == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("rival.no-clan", "Klanınız yok."), plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        List<String> invites = pendingRivalInvites.get(targetClan);
        if(invites == null || invites.isEmpty()) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("rival.no-invites", "Reddedecek düşman daveti bulunamadı."), plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        String fromClan = invites.remove(0);
        rivalManager.denyRivalInvite(fromClan, targetClan);
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("rival.invite-denied", "Düşman daveti reddedildi."), plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
    }
}
