package dev.eggsv31.veldora.veldoraClan.commands.utilities;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanListCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage IClanDataStorage;
    private final YamlLangManager langManager;

    public ClanListCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage, YamlLangManager langManager) {
        this.plugin = plugin;
        this.IClanDataStorage = IClanDataStorage;
        this.langManager = langManager;
    }

    public void handleClanList(Player player) {
        String clanName = this.IClanDataStorage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."), this.getPrefix()));
        } else {
            this.IClanDataStorage.showClanInfo(player, clanName);
        }

    }

    private String getPrefix() {
        return this.plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
