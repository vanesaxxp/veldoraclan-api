package dev.eggsv31.veldora.veldoraClan.events.clan;

import dev.eggsv31.veldora.veldoraClan.discord.DiscordWebhook;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.ConfigManager;
import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClanCreateListener {
    private final ConfigManager configManager;

    public ClanCreateListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void sendDiscordWebhookAsync(String clanName, String creatorName, int totalClans) {
        var discordConfig = configManager.getDiscordConfig();

        if (!discordConfig.getBoolean("ClanCreate.enabled", true)) {
            return;
        }

        String webhookUrl = discordConfig.getString("ClanCreate.webhook-url", "");
        if (webhookUrl.isEmpty()) {
            configManager.getPlugin().getLogger().warning("ClanCreate webhook URL is missing.");
            return;
        }

        DiscordWebhook webhook = new DiscordWebhook(webhookUrl);

        webhook.setTitle(discordConfig.getString("ClanCreate.webhook-title", ":star2: New Clan Created!"));
        webhook.setDescription(
                discordConfig.getString("ClanCreate.webhook-description", "")
                        .replace("{clan_name}", clanName)
                        .replace("{creator}", creatorName)
                        .replace("{total_clans}", String.valueOf(totalClans))
        );
        webhook.setThumbnail(discordConfig.getString("ClanCreate.webhook-thumbnail", "")
                .replace("{creator}", creatorName));
        webhook.setColor(discordConfig.getString("ClanCreate.webhook-color", "3498db"));
        webhook.setFooter(
                discordConfig.getString("ClanCreate.footer.text", "VeldoraClan - Elite Clans System"),
                discordConfig.getString("ClanCreate.footer.icon-url", "https://minotar.net/avatar/{creator}/225")
                        .replace("{creator}", creatorName)
        );
        webhook.setAuthor(
                discordConfig.getString("ClanCreate.author.name", "{creator}")
                        .replace("{creator}", creatorName),
                discordConfig.getString("ClanCreate.author.url", "https://example.com/player/{creator}")
                        .replace("{creator}", creatorName),
                discordConfig.getString("ClanCreate.author.icon-url", "https://minotar.net/avatar/{creator}/225")
                        .replace("{creator}", creatorName)
        );

        Map<String, String> fields = new HashMap<>();
        var fieldSection = discordConfig.getConfigurationSection("ClanCreate.fields");
        if (fieldSection != null) {
            for (String key : fieldSection.getKeys(false)) {
                String value = discordConfig.getString("ClanCreate.fields." + key, "")
                        .replace("{clan_name}", clanName)
                        .replace("{creator}", creatorName)
                        .replace("{creation_date}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                        .replace("{total_clans}", String.valueOf(totalClans));
                fields.put(key, value);
            }
        }
        webhook.setFields(fields);

        webhook.setInline(discordConfig.getBoolean("ClanCreate.inline", false));
        webhook.setTimestamp(discordConfig.getBoolean("ClanCreate.timestamp", true));

        Bukkit.getScheduler().runTaskAsynchronously(configManager.getPlugin(), () -> {
            boolean success = webhook.send();
            if (!success) {
                configManager.getPlugin().getLogger().warning("Failed to send ClanCreate webhook.");
            }
        });
    }
}