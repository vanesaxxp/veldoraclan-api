package dev.eggsv31.veldora.veldoraClan.events.clan;

import dev.eggsv31.veldora.veldoraClan.discord.DiscordWebhook;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.ConfigManager;
import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClanJoinListener {
    private final ConfigManager configManager;

    public ClanJoinListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void sendDiscordWebhookOnJoin(String clanName, String playerName) {
        var discordConfig = configManager.getDiscordConfig();

        if (!discordConfig.getBoolean("ClanJoin.enabled", true)) {
            return;
        }

        String webhookUrl = discordConfig.getString("ClanJoin.webhook-url", "");
        if (webhookUrl.isEmpty()) {
            configManager.getPlugin().getLogger().warning("ClanJoin webhook URL is missing.");
            return;
        }

        DiscordWebhook webhook = new DiscordWebhook(webhookUrl);

        webhook.setTitle(discordConfig.getString("ClanJoin.webhook-title", ":wave: New Member Joined!"));
        webhook.setDescription(
                discordConfig.getString("ClanJoin.webhook-description", "")
                        .replace("{player}", playerName)
                        .replace("{clan_name}", clanName)
        );
        webhook.setThumbnail(discordConfig.getString("ClanJoin.webhook-thumbnail", "")
                .replace("{player}", playerName));
        webhook.setColor(discordConfig.getString("ClanJoin.webhook-color", "2ecc71"));
        webhook.setFooter(
                discordConfig.getString("ClanJoin.footer.text", "VeldoraClan - Elite Clans System"),
                discordConfig.getString("ClanJoin.footer.icon-url", "https://minotar.net/avatar/{player}/225")
                        .replace("{player}", playerName)
        );
        webhook.setAuthor(
                discordConfig.getString("ClanJoin.author.name", "{player}")
                        .replace("{player}", playerName),
                discordConfig.getString("ClanJoin.author.url", "https://example.com/player/{player}")
                        .replace("{player}", playerName),
                discordConfig.getString("ClanJoin.author.icon-url", "https://minotar.net/avatar/{player}/225")
                        .replace("{player}", playerName)
        );

        Map<String, String> fields = new HashMap<>();
        var fieldSection = discordConfig.getConfigurationSection("ClanJoin.fields");
        if (fieldSection != null) {
            for (String key : fieldSection.getKeys(false)) {
                String value = discordConfig.getString("ClanJoin.fields." + key, "")
                        .replace("{player}", playerName)
                        .replace("{clan_name}", clanName)
                        .replace("{join_date}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                fields.put(key, value);
            }
        }
        webhook.setFields(fields);

        webhook.setInline(discordConfig.getBoolean("ClanJoin.inline", false));
        webhook.setTimestamp(discordConfig.getBoolean("ClanJoin.timestamp", true));

        Bukkit.getScheduler().runTaskAsynchronously(configManager.getPlugin(), () -> {
            boolean success = webhook.send();
            if (!success) {
                configManager.getPlugin().getLogger().warning("Failed to send ClanJoin webhook.");
            }
        });
    }
}
