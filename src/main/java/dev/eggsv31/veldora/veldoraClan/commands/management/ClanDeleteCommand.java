package dev.eggsv31.veldora.veldoraClan.commands.management;

import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.gui.menu.ClanDeleteMenu;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanDeleteCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage IClanDataStorage;
    private ClanDeleteMenu ClanDeleteMenu;
    private final YamlLangManager langManager;
    private final GuiLangManager guiLangManager;
    private final Map<Player, Long> deleteConfirmation = new ConcurrentHashMap();

    public ClanDeleteCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage, YamlLangManager langManager, GuiLangManager guiLangManager) {
        this.plugin = plugin;
        this.ClanDeleteMenu = ClanDeleteMenu;
        this.IClanDataStorage = IClanDataStorage;
        this.langManager = langManager;
        this.guiLangManager = guiLangManager;
    }

    public void handleClanDelete(Player player) {
        String clanName = this.IClanDataStorage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."), this.getPrefix()));
        } else {
            if (!this.IClanDataStorage.isClanLeader(player)) {
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.not-leader", "Klan lideri olmadığınız için bu komutu kullanamazsınız."), this.getPrefix()));
                return;
            }

            ClanDeleteMenu.openClanDeleteConfirmation(player, guiLangManager);
        }
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}