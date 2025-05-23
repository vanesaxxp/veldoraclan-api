package dev.eggsv31.veldora.veldoraClan.commands.Rival;

import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.managers.rival.RivalManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RivalAddCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage storage;
    private final YamlLangManager langManager;
    private final RivalManager rivalManager;
    private final Map<String, List<String>> pendingRivalInvites;

    public RivalAddCommand(JavaPlugin plugin, IClanDataStorage storage, YamlLangManager langManager, RivalManager rivalManager) {
        this.plugin = plugin;
        this.storage = storage;
        this.langManager = langManager;
        this.rivalManager = rivalManager;
        this.pendingRivalInvites = new HashMap<>();
    }

    public Map<String, List<String>> getRivalInvitePending() {
        return pendingRivalInvites;
    }

    public void handleRivalAdd(Player player, String targetClanName) {
        if (!storage.isClanLeader(player)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("rival.only-leader-can-add", "Sadece lider düşman ekleyebilir."),
                    plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        String fromClan = storage.getPlayerClan(player);
        if (fromClan == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("rival.no-clan", "Klanınız yok."),
                    plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        if (fromClan.equalsIgnoreCase(targetClanName)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("rival.cannot-invite-self", "Kendinize düşman olamazsınız."),
                    plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        String targetLeaderName = storage.getClanLeader(targetClanName);
        if (targetLeaderName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("rival.target-clan-not-found", "Hedef klan bulunamadı."),
                    plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        Player targetLeader = Bukkit.getPlayerExact(targetLeaderName);
        if (targetLeader == null || !targetLeader.isOnline()) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("rival.target-leader-offline", "Düşman klan lideri çevrimdışı olduğu için davet gönderilemedi."),
                    plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        rivalManager.sendRivalInvite(fromClan, targetClanName);
        List<String> invites = pendingRivalInvites.getOrDefault(targetClanName, new ArrayList<>());
        invites.add(fromClan);
        pendingRivalInvites.put(targetClanName, invites);
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("rival.invite-sent", "Düşman daveti gönderildi."),
                plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
        targetLeader.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("rival.invite-received", fromClan + " klanı sizi düşman olarak ekledi."),
                plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
    }
}
