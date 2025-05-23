package dev.eggsv31.veldora.veldoraClan.events.clan;

import dev.eggsv31.veldora.veldoraClan.discord.DiscordWebhook;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.ConfigManager;
import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClanAllyRemoveListener {
    private final ConfigManager configManager;

    public ClanAllyRemoveListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void sendDiscordWebhookOnAllyRemove(String clanName, String allyName, String playerName) {
        var discordConfig = configManager.getDiscordConfig();

        if (!discordConfig.getBoolean("ClanAllyRemove.enabled", true)) {
            return;
        }

        String webhookUrl = discordConfig.getString("ClanAllyRemove.webhook-url", "");
        if (webhookUrl.isEmpty()) {
            configManager.getPlugin().getLogger().warning("ClanAllyRemove webhook URL is missing.");
            return;
        }

        DiscordWebhook webhook = new DiscordWebhook(webhookUrl);

        webhook.setTitle(discordConfig.getString("ClanAllyRemove.webhook-title", ":broken_heart: Alliance Ended"));
        webhook.setDescription(
                discordConfig.getString("ClanAllyRemove.webhook-description", "{clan_name} and {ally_name} are no longer allies, ended by {player}!")
                        .replace("{clan_name}", clanName)
                        .replace("{ally_name}", allyName)
                        .replace("{player}", playerName)
        );
        webhook.setThumbnail(discordConfig.getString("ClanAllyRemove.webhook-thumbnail", "https://minotar.net/avatar/{player}/128")
                .replace("{player}", playerName));
        webhook.setColor(discordConfig.getString("ClanAllyRemove.webhook-color", "e74c3c"));
        webhook.setFooter(
                discordConfig.getString("ClanAllyRemove.footer.text", "VeldoraClan - Alliances Broken..."),
                discordConfig.getString("ClanAllyRemove.footer.icon-url", "https://minotar.net/avatar/{player}/128")
                        .replace("{player}", playerName)
        );
        webhook.setAuthor(
                discordConfig.getString("ClanAllyRemove.author.name", "{player}")
                        .replace("{player}", playerName),
                discordConfig.getString("ClanAllyRemove.author.url", "https://example.com/player/{player}")
                        .replace("{player}", playerName),
                discordConfig.getString("ClanAllyRemove.author.icon-url", "https://minotar.net/avatar/{player}/128")
                        .replace("{player}", playerName)
        );

        Map<String, String> fields = new HashMap<>();
        var fieldSection = discordConfig.getConfigurationSection("ClanAllyRemove.fields");
        if (fieldSection != null) {
            for (String key : fieldSection.getKeys(false)) {
                String value = discordConfig.getString("ClanAllyRemove.fields." + key, "")
                        .replace("{clan_name}", clanName)
                        .replace("{ally_name}", allyName)
                        .replace("{player}", playerName)
                        .replace("{termination_date}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                fields.put(key, value);
            }
        }
        webhook.setFields(fields);

        webhook.setInline(discordConfig.getBoolean("ClanAllyRemove.inline", false));
        webhook.setTimestamp(discordConfig.getBoolean("ClanAllyRemove.timestamp", true));

        Bukkit.getScheduler().runTaskAsynchronously(configManager.getPlugin(), () -> {
            boolean success = webhook.send();
            if (!success) {
                configManager.getPlugin().getLogger().warning("Failed to send ClanAllyRemove webhook.");
            }
        });
    }
}