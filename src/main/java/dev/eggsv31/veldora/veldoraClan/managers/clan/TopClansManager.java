package dev.eggsv31.veldora.veldoraClan.managers.clan;

import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.storage.DataStorage;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.entity.Player;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TopClansManager {
    private final DataStorage dataStorage;
    private final YamlLangManager langManager;
    private final String prefix;

    public TopClansManager(DataStorage dataStorage, YamlLangManager langManager, String prefix) {
        this.dataStorage = dataStorage;
        this.langManager = langManager;
        this.prefix = prefix;
    }

    public void showTopClans(Player player) {
        Map<String, Integer> clanKills = new HashMap<>();
        dataStorage.getAllCachedClans().forEach((clanName, config) -> {
            int totalKills = config.getInt("total-kills", 0);
            clanKills.put(clanName, totalKills);
        });

        if (clanKills.isEmpty()) {
            String noInfoMessage = langManager.getMessage("clan_top_messages.no-clan-info", "&cKlan bilgisi bulunamadı!");
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(noInfoMessage, prefix));
            return;
        }

        String header = langManager.getMessage("clan_top_messages.top-clans-header", "&6&lTOP CLANS");
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(header, prefix));

        AtomicInteger rankCounter = new AtomicInteger(1);
        clanKills.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(100)
                .forEach(entry -> {
                    int rank = rankCounter.getAndIncrement();
                    String formatKey = "clan_top_messages.top-clans-format" + rank;
                    String defaultFormat = "&e%clan% &7- &c%kills% &7kills";
                    String format = langManager.getMessage(formatKey, defaultFormat);
                    format = format.replace("%clan%", entry.getKey())
                            .replace("%kills%", String.valueOf(entry.getValue()));
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(format, prefix));
                });

        String footer = langManager.getMessage("clan_top_messages.top-clans-footer", "&r");
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(footer, prefix));
    }
}
