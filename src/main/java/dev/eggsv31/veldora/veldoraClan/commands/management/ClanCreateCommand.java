package dev.eggsv31.veldora.veldoraClan.commands.management;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClanCreateCommand implements Listener {

    private final JavaPlugin plugin;
    private final IClanDataStorage clanDataStorage;
    private final YamlLangManager langManager;
    private final Economy economy;
    private final Map<UUID, Boolean> activeClanCreation;
    private final Map<UUID, Long> clanCreationCooldown;

    public ClanCreateCommand(JavaPlugin plugin, IClanDataStorage clanDataStorage, YamlLangManager langManager, Economy economy) {
        this.plugin = plugin;
        this.clanDataStorage = clanDataStorage;
        this.langManager = langManager;
        this.economy = economy;
        this.activeClanCreation = new HashMap<>();
        this.clanCreationCooldown = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void handleClanCreate(Player player) {
        long cooldownSeconds = plugin.getConfig().getLong("Clan.create.cooldown", 300);
        long cooldownMillis = cooldownSeconds * 1000;
        UUID playerId = player.getUniqueId();
        if (clanCreationCooldown.containsKey(playerId)) {
            long lastCreation = clanCreationCooldown.get(playerId);
            long timeLeft = (lastCreation + cooldownMillis) - System.currentTimeMillis();
            if (timeLeft > 0) {
                long secondsLeft = timeLeft / 1000;
                String formattedTime = formatTime(secondsLeft);
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                        langManager.getMessage("commands.create-cooldown", "You must wait %time% before creating another clan")
                                .replace("%time%", formattedTime),
                        getPrefix()
                ));
                return;
            }
        }

        if (activeClanCreation.getOrDefault(playerId, false)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.already-creating", ""), getPrefix()));
            return;
        }

        if (clanDataStorage.getPlayerClan(player) != null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.already-in-clan", ""), getPrefix()));
            return;
        }

        activeClanCreation.put(playerId, true);
        sendClanCreationPrompt(player);
    }

    private void sendClanCreationPrompt(Player player) {
        int lineIndex = 1;
        while (true) {
            String line = langManager.getMessage("CHAT.clan-create.message." + lineIndex, null);
            if (line == null) break;
            if (line.contains("<click:") || line.contains("<hover:")) {
                Component formattedMessage = ChatUtils.formatMiniMessageWithLegacySupport(line);
                player.sendMessage(formattedMessage);
            } else {
                player.sendMessage(ChatUtils.formatMessage(line));
            }
            lineIndex++;
        }
        String actionbar = langManager.getMessage("CHAT.clan-create.actionbar", "");
        if (!actionbar.isEmpty()) {
            player.sendActionBar(ChatUtils.formatMiniMessageWithLegacySupport(actionbar));
        }
        showTitle(player, "CHAT.clan-create.title.line", "CHAT.clan-create.title.subline");
    }

    private void showTitle(Player player, String titlePath, String subtitlePath) {
        String title = langManager.getMessage(titlePath, "");
        String subtitle = langManager.getMessage(subtitlePath, "");
        if (!title.isEmpty() || !subtitle.isEmpty()) {
            player.sendTitle(
                    ChatUtils.formatMessage(title),
                    ChatUtils.formatMessage(subtitle),
                    10,
                    70,
                    10
            );
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (!activeClanCreation.getOrDefault(playerId, false)) return;
        event.setCancelled(true);
        String message = event.getMessage();
        if (message.equalsIgnoreCase("cancel")) {
            Bukkit.getScheduler().runTask(plugin, () -> cancelClanCreation(player));
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> processClanName(player, message));
        }
    }

    private void processClanName(Player player, String clanName) {
        if (clanDataStorage.clanExists(clanName)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.clan-name-taken", "Bu isimde bir klan zaten mevcut"),
                    getPrefix()
            ));
            retryClanCreation(player);
            return;
        }
        List<String> blacklistedNames = plugin.getConfig().getStringList("clan.name.blacklistedNames");
        for (String blacklisted : blacklistedNames) {
            if (clanName.toLowerCase().contains(blacklisted.toLowerCase())) {
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                        langManager.getMessage("commands.clan-name-banned", ""), getPrefix()));
                retryClanCreation(player);
                return;
            }
        }
        int maxClanNameLength = plugin.getConfig().getInt("Clan.name.maxLength", 16);
        int minClanNameLength = plugin.getConfig().getInt("Clan.name.minLength", 2);
        if (clanName.length() > maxClanNameLength) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.clan-name-too-long", "").replace("%maxlength%", String.valueOf(maxClanNameLength)),
                    getPrefix()
            ));
            retryClanCreation(player);
            return;
        }
        if (clanName.length() < minClanNameLength) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.clan-name-too-short", "").replace("%minlength%", String.valueOf(minClanNameLength)),
                    getPrefix()
            ));
            retryClanCreation(player);
            return;
        }
        if (plugin.getConfig().getBoolean("Clan.create.killToCreate", false) && getPlayerKills(player) < plugin.getConfig().getInt("Clan.create.killToCreate", 15)) {
            int required = plugin.getConfig().getInt("Clan.create.killToCreate", 15) - getPlayerKills(player);
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.not-enough-kills", "").replace("%remainingkill%", String.valueOf(required)),
                    getPrefix()
            ));
            retryClanCreation(player);
            return;
        }
        if (plugin.getConfig().getBoolean("VaultSupport.enabled", false)) {
            double price = plugin.getConfig().getDouble("VaultSupport.create", 100.0);
            if (economy == null || !economy.has(player, price)) {
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                        langManager.getMessage("commands.Vault.CreatePriceFailed", "").replace("%amount%", String.valueOf(price)),
                        getPrefix()
                ));
                retryClanCreation(player);
                return;
            }
            economy.withdrawPlayer(player, price);
        }
        finalizeClanCreation(player, clanName);
    }

    private void finalizeClanCreation(Player player, String clanName) {
        clanDataStorage.createClan(clanName, player);
        UUID playerId = player.getUniqueId();
        activeClanCreation.remove(playerId);
        clanCreationCooldown.put(playerId, System.currentTimeMillis());
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("commands.clan-created", "").replace("%clan%", clanName),
                getPrefix()
        ));
    }

    private void retryClanCreation(Player player) {
        activeClanCreation.remove(player.getUniqueId());
    }

    public void cancelClanCreation(Player player) {
        activeClanCreation.remove(player.getUniqueId());
        player.sendMessage("");
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("commands.canceled", ""), getPrefix()));
        player.sendMessage("");
    }

    private int getPlayerKills(Player player) {
        return player.getStatistic(Statistic.PLAYER_KILLS);
    }

    public Map<UUID, Boolean> getActiveClanCreation() {
        return activeClanCreation;
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }

    private String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}
