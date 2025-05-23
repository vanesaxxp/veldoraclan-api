package dev.eggsv31.veldora.veldoraClan.commands.management;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanTransferCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage IClanDataStorage;
    private final YamlLangManager langManager;

    public ClanTransferCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage, YamlLangManager langManager) {
        this.plugin = plugin;
        this.IClanDataStorage = IClanDataStorage;
        this.langManager = langManager;
    }

    public void handleClanTransfer(Player player, String[] args) {
        String clanName = this.IClanDataStorage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."), this.getPrefix()));
        } else if (!this.IClanDataStorage.isClanLeader(player)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.not-leader", "Bu komutu yalnızca klan liderleri kullanabilir."), this.getPrefix()));
        } else if (args.length < 2) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.no-player-specified", "Lütfen bir oyuncu ismi girin."), this.getPrefix()));
        } else {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.player-not-online", "Bu oyuncu şu anda çevrimdışı."), this.getPrefix()));
            } else {
                String targetPlayerClan = this.IClanDataStorage.getPlayerClan(targetPlayer);
                if (targetPlayerClan != null && !targetPlayerClan.equals(clanName)) {
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.player-in-clan", "Bu oyuncu başka bir klanda."), this.getPrefix()));
                } else if (this.IClanDataStorage.isClanLeader(targetPlayer)) {
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.player-already-leader", "Bu oyuncu zaten bir klanın lideri."), this.getPrefix()));
                } else {
                    this.IClanDataStorage.setClanLeader(targetPlayer, clanName);
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.transfer-success", "%player% adlı oyuncuya liderliği devrettiniz.").replace("%player%", targetPlayer.getName()), this.getPrefix()));
                    targetPlayer.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("commands.you-are-now-leader", "%player% sizi klan lideri yaptı.").replace("%player%", player.getName()), this.getPrefix()));
                }
            }
        }
    }

    private String getPrefix() {
        return this.plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
