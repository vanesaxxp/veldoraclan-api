package dev.eggsv31.veldora.veldoraClan.commands.management;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanPvpCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage clanDataStorage;
    private final YamlLangManager langManager;

    public ClanPvpCommand(JavaPlugin plugin, IClanDataStorage clanDataStorage, YamlLangManager langManager) {
        this.plugin = plugin;
        this.clanDataStorage = clanDataStorage;
        this.langManager = langManager;
    }

    public void handlePvpCommand(Player player, String[] args) {
        String clanName = clanDataStorage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan", "&cBir klana üye değilsiniz."),
                    getPrefix()
            ));
            return;
        }

        if (!clanDataStorage.isClanLeader(player)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.not-leader", "&cBu komutu yalnızca klan lideri kullanabilir."),
                    getPrefix()
            ));
            return;
        }

        FileConfiguration clanConfig = clanDataStorage.getClanConfig(clanName);
        boolean defaultClanDamage = plugin.getConfig().getBoolean("Clan.name.damage", false);
        boolean currentPvpStatus = clanConfig.getBoolean("pvp", defaultClanDamage);
        boolean newPvpStatus = !currentPvpStatus;

        clanConfig.set("pvp", newPvpStatus);
        clanDataStorage.saveClanConfig(clanConfig, clanName);

        if (newPvpStatus) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.pvp-enabled", "&aPvP etkinleştirildi."),
                    getPrefix()
            ));
        } else {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.pvp-disabled", "&cPvP devre dışı bırakıldı."),
                    getPrefix()
            ));
        }
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
