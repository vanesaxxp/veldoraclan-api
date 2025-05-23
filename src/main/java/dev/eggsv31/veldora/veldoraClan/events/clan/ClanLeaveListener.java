package dev.eggsv31.veldora.veldoraClan.events.clan;

import dev.eggsv31.veldora.veldoraClan.discord.DiscordWebhook;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.ConfigManager;
import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClanLeaveListener {
    private final ConfigManager configManager;

    public ClanLeaveListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void sendDiscordWebhookOnLeave(String clanName, String playerName) {
        var discordConfig = configManager.getDiscordConfig();

        if (!discordConfig.getBoolean("ClanLeave.enabled", true)) {
            return;
        }

        String webhookUrl = discordConfig.getString("ClanLeave.webhook-url", "");
        if (webhookUrl.isEmpty()) {
            configManager.getPlugin().getLogger().warning("ClanLeave webhook URL is missing.");
            return;
        }

        DiscordWebhook webhook = new DiscordWebhook(webhookUrl);

        webhook.setTitle(discordConfig.getString("ClanLeave.webhook-title", ":wave: Member Left!"));
        webhook.setDescription(
                discordConfig.getString("ClanLeave.webhook-description", "")
                        .replace("{player}", playerName)
                        .replace("{clan_name}", clanName)
        );
        webhook.setThumbnail(discordConfig.getString("ClanLeave.webhook-thumbnail", "")
                .replace("{player}", playerName));
        webhook.setColor(discordConfig.getString("ClanLeave.webhook-color", "e74c3c"));
        webhook.setFooter(
                discordConfig.getString("ClanLeave.footer.text", "VeldoraClan - Elite Clans System"),
                discordConfig.getString("ClanLeave.footer.icon-url", "https://minotar.net/avatar/{player}/225")
                        .replace("{player}", playerName)
        );
        webhook.setAuthor(
                discordConfig.getString("ClanLeave.author.name", "{player}")
                        .replace("{player}", playerName),
                discordConfig.getString("ClanLeave.author.url", "https://example.com/player/{player}")
                        .replace("{player}", playerName),
                discordConfig.getString("ClanLeave.author.icon-url", "https://minotar.net/avatar/{player}/225")
                        .replace("{player}", playerName)
        );

        Map<String, String> fields = new HashMap<>();
        var fieldSection = discordConfig.getConfigurationSection("ClanLeave.fields");
        if (fieldSection != null) {
            for (String key : fieldSection.getKeys(false)) {
                String value = discordConfig.getString("ClanLeave.fields." + key, "")
                        .replace("{player}", playerName)
                        .replace("{clan_name}", clanName)
                        .replace("{leave_date}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                fields.put(key, value);
            }
        }
        webhook.setFields(fields);

        webhook.setInline(discordConfig.getBoolean("ClanLeave.inline", false));
        webhook.setTimestamp(discordConfig.getBoolean("ClanLeave.timestamp", true));

        Bukkit.getScheduler().runTaskAsynchronously(configManager.getPlugin(), () -> {
            boolean success = webhook.send();
            if (!success) {
                configManager.getPlugin().getLogger().warning("Failed to send ClanLeave webhook.");
            }
        });
    }
}
