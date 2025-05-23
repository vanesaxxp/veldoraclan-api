package dev.eggsv31.veldora.veldoraClan.commands.Rival;

import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.managers.rival.RivalManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RivalRemoveCommand {
    private final JavaPlugin plugin;
    private final RivalManager rivalManager;
    private final IClanDataStorage storage;
    private final YamlLangManager langManager;
    public RivalRemoveCommand(JavaPlugin plugin, RivalManager rivalManager, IClanDataStorage storage, YamlLangManager langManager) {
        this.plugin = plugin;
        this.rivalManager = rivalManager;
        this.storage = storage;
        this.langManager = langManager;
    }
    public void handleRivalRemove(Player player, String targetClanName) {
        String fromClan = storage.getPlayerClan(player);
        if(fromClan == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("rival.no-clan", "Klanınız yok."), plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
            return;
        }
        rivalManager.denyRivalInvite(fromClan, targetClanName);
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(langManager.getMessage("rival.invite-removed", "Düşman daveti kaldırıldı."), plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")));
    }
}
