package dev.eggsv31.veldora.veldoraClan.events.clan;

import dev.eggsv31.veldora.veldoraClan.discord.DiscordWebhook;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.ConfigManager;
import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClanAllyAddListener {
    private final ConfigManager configManager;

    public ClanAllyAddListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void sendDiscordWebhookOnAllyAdd(String clanName, String allyName, String playerName) {
        var discordConfig = configManager.getDiscordConfig();

        if (!discordConfig.getBoolean("ClanAllyAdd.enabled", true)) {
            return;
        }

        String webhookUrl = discordConfig.getString("ClanAllyAdd.webhook-url", "");
        if (webhookUrl.isEmpty()) {
            configManager.getPlugin().getLogger().warning("ClanAllyAdd webhook URL is missing.");
            return;
        }

        DiscordWebhook webhook = new DiscordWebhook(webhookUrl);

        webhook.setTitle(discordConfig.getString("ClanAllyAdd.webhook-title", ":handshake: New Alliance!"));
        webhook.setDescription(
                discordConfig.getString("ClanAllyAdd.webhook-description", "{clan_name} and {ally_name} are now allies, initiated by {player}!")
                        .replace("{clan_name}", clanName)
                        .replace("{ally_name}", allyName)
                        .replace("{player}", playerName)
        );
        webhook.setThumbnail(discordConfig.getString("ClanAllyAdd.webhook-thumbnail", "https://minotar.net/avatar/{player}/128")
                .replace("{player}", playerName));
        webhook.setColor(discordConfig.getString("ClanAllyAdd.webhook-color", "2ecc71"));
        webhook.setFooter(
                discordConfig.getString("ClanAllyAdd.footer.text", "VeldoraClan - Elite Alliances"),
                discordConfig.getString("ClanAllyAdd.footer.icon-url", "https://minotar.net/avatar/{player}/128")
                        .replace("{player}", playerName)
        );
        webhook.setAuthor(
                discordConfig.getString("ClanAllyAdd.author.name", "{player}")
                        .replace("{player}", playerName),
                discordConfig.getString("ClanAllyAdd.author.url", "https://example.com/player/{player}")
                        .replace("{player}", playerName),
                discordConfig.getString("ClanAllyAdd.author.icon-url", "https://minotar.net/avatar/{player}/128")
                        .replace("{player}", playerName)
        );

        Map<String, String> fields = new HashMap<>();
        var fieldSection = discordConfig.getConfigurationSection("ClanAllyAdd.fields");
        if (fieldSection != null) {
            for (String key : fieldSection.getKeys(false)) {
                String value = discordConfig.getString("ClanAllyAdd.fields." + key, "")
                        .replace("{clan_name}", clanName)
                        .replace("{ally_name}", allyName)
                        .replace("{player}", playerName)
                        .replace("{alliance_date}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                fields.put(key, value);
            }
        }
        webhook.setFields(fields);

        webhook.setInline(discordConfig.getBoolean("ClanAllyAdd.inline", true));
        webhook.setTimestamp(discordConfig.getBoolean("ClanAllyAdd.timestamp", true));

        Bukkit.getScheduler().runTaskAsynchronously(configManager.getPlugin(), () -> {
            boolean success = webhook.send();
            if (!success) {
                configManager.getPlugin().getLogger().warning("Failed to send ClanAllyAdd webhook.");
            }
        });
    }
}