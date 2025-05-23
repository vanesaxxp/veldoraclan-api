package dev.eggsv31.veldora.veldoraClan.commands.utilities;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ClanStatsCommand {
    private final IClanDataStorage IClanDataStorage;
    private final YamlLangManager langManager;
    private final JavaPlugin plugin;

    public ClanStatsCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage, YamlLangManager langManager) {
        this.plugin = plugin;
        this.IClanDataStorage = IClanDataStorage;
        this.langManager = langManager;
    }

    public void handleClanStats(CommandSender sender, String clanName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.langManager.getMessage("general.only-players", "Bu komut yalnızca oyuncular tarafından kullanılabilir."));
            return;
        }

        if (!IClanDataStorage.clanExists(clanName)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(this.langManager.getMessage("clan_stats_messages.clan-stats-not-found", "Klan bulunamadı: ") , getPrefix()));
            return;
        }

        String leader = IClanDataStorage.getClanLeader(clanName);
        int playerCount = IClanDataStorage.getClanMembers(clanName).size();
        int totalKills = IClanDataStorage.getClanKills(clanName);
        int totalDeaths = IClanDataStorage.getClanDeaths(clanName);
        double kdr = totalDeaths > 0 ? (double) totalKills / totalDeaths : (double) totalKills;
        List<String> members = IClanDataStorage.getPlayerNames(IClanDataStorage.getClanMembers(clanName));
        List<String> allies = IClanDataStorage.getClanAllies(clanName);
        int ranking = IClanDataStorage.getClanRanking();

        String titleMessage = this.langManager.getMessage("clan_stats_messages.clan-stats-title", "&6%clan% adlı Klanın Bilgileri:")
                .replace("%clan%", clanName);
        String leaderMessage = this.langManager.getMessage("clan_stats_messages.clan-stats-leader", "&eLider: &f%leader%").replace("%leader%", leader);
        String playerCountMessage = this.langManager.getMessage("clan_stats_messages.clan-stats-players", "&eOyuncu Sayısı: &f%player-count%").replace("%player-count%", String.valueOf(playerCount));
        String totalKillsMessage = this.langManager.getMessage("clan_stats_messages.clan-stats-kills", "&eToplam Öldürme: &f%total-kills%").replace("%total-kills%", String.valueOf(totalKills));
        String totalDeathsMessage = this.langManager.getMessage("clan_stats_messages.clan-stats-deaths", "&eToplam Ölüm: &f%total-deaths%").replace("%total-deaths%", String.valueOf(totalDeaths));
        String kdrMessage = this.langManager.getMessage("clan_stats_messages.clan-stats-kdr", "&eKDR: &f%kdr%").replace("%kdr%", String.format("%.2f", kdr));
        String membersMessage = this.langManager.getMessage("clan_stats_messages.clan-stats-members", "&eÜyeler: &f%members%").replace("%members%", members.isEmpty() ? "" : String.join(", ", members));
        String allyMessage = allies.isEmpty()
                ? this.langManager.getMessage("clan_stats_messages.clan-stats-no-allies", "&7Müttefikler: &cYok")
                : this.langManager.getMessage("clan_stats_messages.clan-stats-allies", "&7Müttefikler: &a%allies%").replace("%allies%", String.join(", ", allies));
        String rankingMessage = this.langManager.getMessage("clan_stats_messages.clan-stats-ranking", "&eRütbe: &f%rank%").replace("%rank%", String.valueOf(ranking));

        String prefix = this.getPrefix();
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(titleMessage, prefix));
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(leaderMessage, prefix));
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(playerCountMessage, prefix));
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(totalKillsMessage, prefix));
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(totalDeathsMessage, prefix));
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(kdrMessage, prefix));
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(membersMessage, prefix));
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(allyMessage, prefix));
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(rankingMessage, prefix));
    }

    private String getPrefix() {
        return this.plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
