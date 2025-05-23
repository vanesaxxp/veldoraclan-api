package dev.eggsv31.veldora.veldoraClan.events.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import dev.eggsv31.veldora.veldoraClan.events.chat.ClanChatEvent.ChatMode;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ClanAllyChatEvent implements Listener {
    private final JavaPlugin plugin;
    private final IClanDataStorage storage;
    private final YamlLangManager langManager;

    public ClanAllyChatEvent(JavaPlugin plugin, IClanDataStorage storage, YamlLangManager langManager) {
        this.plugin = plugin;
        this.storage = storage;
        this.langManager = langManager;
    }

    public void handleAllyChatToggle(Player player) {
        ChatMode mode = ClanChatEvent.chatModes.getOrDefault(player, ChatMode.NORMAL);

        if (mode == ChatMode.CLAN_CHAT) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("AllyChatMessages.cannot-enable-ally-chat", "%prefix% &cYou cannot open ally chat while clan chat is active."),
                    getPrefix()
            ));
            return;
        }

        if (mode == ChatMode.ALLY_CHAT) {
            ClanChatEvent.chatModes.put(player, ChatMode.NORMAL);
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("AllyChatMessages.ally-chat-disabled", "%prefix% &7Ally chat is &cdisabled ❌"),
                    getPrefix()
            ));
            return;
        }

        String clanName = storage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("AllyChatMessages.no-allies", "%prefix% &7You do not have an &cAlly clan, &7you cannot open the ally chat &c❌"),
                    getPrefix()
            ));
            return;
        }

        List<String> allies = storage.getClanAllies(clanName);
        if (allies == null || allies.isEmpty()) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("AllyChatMessages.no-allies", "%prefix% &7You do not have an &cAlly clan, &7you cannot open the ally chat &c❌"),
                    getPrefix()
            ));
            return;
        }

        ClanChatEvent.chatModes.put(player, ChatMode.ALLY_CHAT);
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("AllyChatMessages.ally-chat-enabled", "%prefix% &7Ally chat &aenabled ✔"),
                getPrefix()
        ));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        if (ClanChatEvent.chatModes.getOrDefault(sender, ChatMode.NORMAL) != ChatMode.ALLY_CHAT) {
            return;
        }

        String clanName = storage.getPlayerClan(sender);
        if (clanName == null) {
            return;
        }

        event.setCancelled(true);
        event.getRecipients().clear();

        sendAllyMessage(sender, event.getMessage(), clanName);
    }

    private void sendAllyMessage(Player sender, String message, String clanName) {
        List<String> allyClans = storage.getClanAllies(clanName);
        if (allyClans == null || allyClans.isEmpty()) {
            sender.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("AllyChatMessages.no-allies", "Hiçbir müttefikiniz yok."),
                    getPrefix()
            ));
            return;
        }

        String formatted = formatAllyMessage(sender, message, clanName);
        for (Player online : Bukkit.getOnlinePlayers()) {
            String pc = storage.getPlayerClan(online);
            if (pc != null && (pc.equalsIgnoreCase(clanName) || allyClans.contains(pc))) {
                online.sendMessage(formatted);
            }
        }
    }

    private String formatAllyMessage(Player sender, String message, String clanName) {
        String fmt = plugin.getConfig().getBoolean("allies.allyChat", true)
                ? plugin.getConfig().getString("Clan.chat.allyChatFormat", "&9[Ally] &a%player%&7: %message%")
                : "&9[Ally] %player%: %message%";
        fmt = fmt
                .replace("%player%", sender.getName())
                .replace("%message%", message)
                .replace("%veldora_clan_tag%", clanName);
        fmt = PlaceholderAPI.setPlaceholders(sender, fmt);
        return ChatColor.translateAlternateColorCodes('&', fmt);
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
