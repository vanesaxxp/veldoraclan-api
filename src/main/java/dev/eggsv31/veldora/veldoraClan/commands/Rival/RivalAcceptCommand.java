package dev.eggsv31.veldora.veldoraClan.commands.Rival;

import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.managers.rival.RivalManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.Map;

public class RivalAcceptCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage storage;
    private final YamlLangManager langManager;
    private final Map<String, List<String>> pendingRivalInvites;
    private final RivalManager rivalManager;

    public RivalAcceptCommand(JavaPlugin plugin, IClanDataStorage storage, YamlLangManager langManager, Map<String, List<String>> pendingRivalInvites, RivalManager rivalManager) {
        this.plugin = plugin;
        this.storage = storage;
        this.langManager = langManager;
        this.pendingRivalInvites = pendingRivalInvites;
        this.rivalManager = rivalManager;
    }

    public void handleRivalAccept(Player player) {
        String targetClan = storage.getPlayerClan(player);
        if (targetClan == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("rival.no-clan", "Klanınız yok."), plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        List<String> invites = pendingRivalInvites.get(targetClan);
        if (invites == null || invites.isEmpty()) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("rival.no-invites", "Kabul edilecek düşman daveti bulunamadı."), plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        String fromClan = invites.remove(0);
        rivalManager.acceptRivalInvite(fromClan, targetClan);
        String prefix = plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
        int lineIndex = 1;
        while (true) {
            String line = langManager.getMessage("rival-accept.message." + lineIndex, null);
            if (line == null) break;
            line = line.replace("%fromClan%", fromClan);
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(line, prefix));
            lineIndex++;
        }
        String actionbar = langManager.getMessage("rival-accept.actionbar", "");
        if (!actionbar.isEmpty()) {
            actionbar = actionbar.replace("%fromClan%", fromClan);
            player.sendActionBar(ChatUtils.formatMessageWithOptionalPrefix(actionbar, prefix));
        }
        String titleLine = langManager.getMessage("rival-accept.title.line", "");
        String subtitle = langManager.getMessage("rival-accept.title.subline", "");
        if (!titleLine.isEmpty() || !subtitle.isEmpty()) {
            titleLine = titleLine.replace("%fromClan%", fromClan);
            subtitle = subtitle.replace("%fromClan%", fromClan);
            player.sendTitle(ChatUtils.formatMessage(titleLine), ChatUtils.formatMessage(subtitle), 10, 70, 10);
        }
    }
}
