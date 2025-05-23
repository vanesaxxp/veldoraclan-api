package dev.eggsv31.veldora.veldoraClan.events.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.clip.placeholderapi.PlaceholderAPI;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanChatEvent implements Listener {
    private final JavaPlugin plugin;
    private final IClanDataStorage storage;
    private final YamlLangManager langManager;
    public static final Map<Player, ChatMode> chatModes = new HashMap<>();

    public enum ChatMode {
        NORMAL,
        CLAN_CHAT,
        ALLY_CHAT
    }

    public ClanChatEvent(JavaPlugin plugin, IClanDataStorage storage, YamlLangManager langManager) {
        this.plugin = plugin;
        this.storage = storage;
        this.langManager = langManager;
    }

    public void handleClanChatToggle(Player player) {
        String playerClan = storage.getPlayerClan(player);
        if (playerClan == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan", "Bir klanınız yok, klan sohbetini açamazsınız."),
                    getPrefix()
            ));
            return;
        }
        if (chatModes.getOrDefault(player, ChatMode.NORMAL) == ChatMode.ALLY_CHAT) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.cannot-enable-clan-chat", "You cannot open clan chat while ally chat is open."),
                    getPrefix()
            ));
            return;
        }

        if (chatModes.getOrDefault(player, ChatMode.NORMAL) == ChatMode.CLAN_CHAT) {
            chatModes.put(player, ChatMode.NORMAL);
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.clan-chat-disabled", "Klan sohbeti kapatıldı."),
                    getPrefix()
            ));
        } else {
            chatModes.put(player, ChatMode.CLAN_CHAT);
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.clan-chat-enabled", "Klan sohbeti açıldı."),
                    getPrefix()
            ));
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (chatModes.getOrDefault(player, ChatMode.NORMAL) != ChatMode.CLAN_CHAT) {
            return;
        }

        String clanName = storage.getPlayerClan(player);
        if (clanName == null) {
            return;
        }

        event.setCancelled(true);
        event.getRecipients().clear();

        sendClanMessage(player, event.getMessage(), clanName);
    }

    private void sendClanMessage(Player sender, String message, String clanName) {
        List<Player> members = storage.getOnlineClanMembers(clanName);
        String formatted = formatClanMessage(sender, message, clanName);
        for (Player member : members) {
            member.sendMessage(formatted);
        }
    }

    private String formatClanMessage(Player sender, String message, String clanName) {
        String format = plugin.getConfig().getBoolean("Clan.chat.enabled", true)
                ? plugin.getConfig().getString("Clan.chat.chatFormat", "&c[Clan] &a%player%&7: %message%")
                : "&c[Clan] %player%: %message%";
        format = format
                .replace("%player%", sender.getName())
                .replace("%message%", message)
                .replace("%veldora_clan_tag%", clanName);
        format = PlaceholderAPI.setPlaceholders(sender, format);
        return ChatColor.translateAlternateColorCodes('&', format);
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
