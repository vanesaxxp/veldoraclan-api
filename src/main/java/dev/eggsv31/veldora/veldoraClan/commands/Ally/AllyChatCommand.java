package dev.eggsv31.veldora.veldoraClan.commands.Ally;

import java.util.List;
import dev.eggsv31.veldora.veldoraClan.events.chat.ClanAllyChatEvent;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AllyChatCommand {
    private final IClanDataStorage IClanDataStorage;
    private final YamlLangManager yamlLangManager;
    private final ClanAllyChatEvent clanallyChat;
    private final JavaPlugin plugin;

    public AllyChatCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage, YamlLangManager yamlLangManager, ClanAllyChatEvent clanallyChat) {
        this.plugin = plugin;
        this.IClanDataStorage = IClanDataStorage;
        this.yamlLangManager = yamlLangManager;
        this.clanallyChat = clanallyChat;
    }

    public void handleAllyChat(Player player) {
        String playerClan = this.IClanDataStorage.getPlayerClan(player);
        if (playerClan == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.yamlLangManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."), this.getPrefix()));
        } else {
            List<String> allyClans = this.IClanDataStorage.getClanAllies(playerClan);
            if (allyClans != null && !allyClans.isEmpty()) {
                this.clanallyChat.handleAllyChatToggle(player);
            } else {
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.yamlLangManager.getMessage("AllyChatMessages.no-allies", "Hiçbir müttefikiniz yok."), this.getPrefix()));
            }
        }
    }

    private String getPrefix() {
        return this.plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
