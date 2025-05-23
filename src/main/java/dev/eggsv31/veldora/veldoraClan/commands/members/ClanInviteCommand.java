package dev.eggsv31.veldora.veldoraClan.commands.members;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanInviteCommand {
    private final JavaPlugin plugin;
    private final IClanDataStorage IClanDataStorage;
    private final YamlLangManager langManager;
    private final Map<UUID, List<String>> invitePending = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Long>> inviteCooldowns = new ConcurrentHashMap<>();
    private final Map<Player, BukkitRunnable> inviteBossBarTasks = new HashMap<>();
    private final Map<Player, BossBar> inviteBossBars = new HashMap<>();

    public ClanInviteCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage, YamlLangManager langManager) {
        this.plugin = plugin;
        this.IClanDataStorage = IClanDataStorage;
        this.langManager = langManager;
    }

    public void handleClanInvite(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-player-specified", "Lütfen bir oyuncu ismi girin."), getPrefix()));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.player-not-online", "Bu oyuncu şu anda çevrimdışı."), getPrefix()));
            return;
        }
        if (IClanDataStorage.getPlayerClan(target) != null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.player-in-clan", "Bu oyuncu zaten bir klana sahip."), getPrefix()));
            return;
        }
        invitePlayerToClan(player, target);
    }

    private void invitePlayerToClan(Player player, Player target) {
        String clanName = IClanDataStorage.getPlayerClan(player);
        if (clanName == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."), getPrefix()));
            return;
        }
        FileConfiguration cfg = IClanDataStorage.getClanConfig(clanName);
        int currentPlayers = cfg.getInt("player-count", 0);
        int maxPlayers = getClanLimitForPlayer(player);
        if (currentPlayers >= maxPlayers) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.clan-full", "Bu klan maksimum oyuncu sınırına ulaştı."), getPrefix()));
            return;
        }
        int inviteCooldown = plugin.getConfig().getInt("invite.player.invite-cooldown", 20);
        int responseTime = plugin.getConfig().getInt("invite.player.response-time", 20);
        if (isOnCooldown(player, target)) {
            long remainingTime = getRemainingCooldownTime(player, target);
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.invite-cooldown", "Lütfen bu oyuncuya davet göndermek için %remaining_time% saniye bekleyin.")
                            .replace("%remaining_time%", String.valueOf(remainingTime)), getPrefix()));
            return;
        }
        invitePending.computeIfAbsent(target.getUniqueId(), k -> new ArrayList<>()).add(clanName);
        setCooldown(player, target, inviteCooldown);
        IClanDataStorage.incrementInviteSentCount(clanName);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            List<String> invites = invitePending.get(target.getUniqueId());
            if (invites != null && invites.contains(clanName)) {
                invites.remove(clanName);
                if (invites.isEmpty()) {
                    invitePending.remove(target.getUniqueId());
                }
                target.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                        langManager.getMessage("commands.invite-expired", "Klan davetiniz zaman aşımına uğradı."), getPrefix()));
                cancelInviteBossBar(target);
            }
        }, responseTime * 20L);
        sendInviteMessage(player, target, clanName, responseTime);
    }

    private boolean isOnCooldown(Player inviter, Player target) {
        Map<UUID, Long> targetCooldowns = inviteCooldowns.get(inviter.getUniqueId());
        if (targetCooldowns == null) {
            return false;
        }
        Long cooldownEnd = targetCooldowns.get(target.getUniqueId());
        return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
    }

    private long getRemainingCooldownTime(Player inviter, Player target) {
        Map<UUID, Long> targetCooldowns = inviteCooldowns.get(inviter.getUniqueId());
        if (targetCooldowns == null) {
            return 0;
        }
        Long cooldownEnd = targetCooldowns.get(target.getUniqueId());
        if (cooldownEnd == null) {
            return 0;
        }
        return Math.max(0, (cooldownEnd - System.currentTimeMillis()) / 1000L);
    }

    private void setCooldown(Player inviter, Player target, int cooldownSeconds) {
        inviteCooldowns.computeIfAbsent(inviter.getUniqueId(), k -> new HashMap<>())
                .put(target.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }

    private void sendInviteMessage(Player inviter, Player target, String clanName, int responseTime) {
        Component finalMessage = Component.empty();
        int lineIndex = 1;
        while (true) {
            String line = langManager.getMessage("CHAT.invite-receive.message." + lineIndex, null);
            if (line == null) break;
            line = line.replace("%clan_tag%", clanName);
            Component lineComponent = line.contains("<click:") || line.contains("<hover:") ?
                    ChatUtils.formatMiniMessageWithLegacySupport(line) :
                    Component.text(ChatUtils.formatMessage(line));
            finalMessage = finalMessage.append(lineComponent).append(Component.newline());
            lineIndex++;
        }
        if (!finalMessage.equals(Component.empty())) {
            target.sendMessage(finalMessage);
        }
        String actionbar = langManager.getMessage("CHAT.invite-receive.actionbar", "");
        if (!actionbar.isEmpty()) {
            actionbar = actionbar.replace("%clan_tag%", clanName);
            target.sendActionBar(ChatUtils.formatMiniMessageWithLegacySupport(actionbar));
        }
        String titleLine = langManager.getMessage("CHAT.invite-receive.title.line", "");
        String titleSubline = langManager.getMessage("CHAT.invite-receive.title.subline", "");
        if (!titleLine.isEmpty() || !titleSubline.isEmpty()) {
            titleLine = titleLine.replace("%clan_tag%", clanName);
            titleSubline = titleSubline.replace("%clan_tag%", clanName);
            target.sendTitle(ChatUtils.formatMessage(titleLine), ChatUtils.formatMessage(titleSubline), 10, 70, 10);
        }
        String bossbarTemplate = langManager.getMessage("CHAT.invite-receive.bossbar", "");
        if (!bossbarTemplate.isEmpty()) {
            String formattedTime = formatTime(responseTime);
            String initialText = bossbarTemplate.replace("{time}", formattedTime).replace("%clan_tag%", clanName);
            BossBar bossBar = BossBar.bossBar(ChatUtils.formatMiniMessageWithLegacySupport(initialText), 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
            BukkitAudiences audiences = BukkitAudiences.create(plugin);
            audiences.player(target).showBossBar(bossBar);
            BukkitRunnable task = new BukkitRunnable() {
                int remaining = responseTime;
                @Override
                public void run() {
                    remaining--;
                    if (remaining <= 0) {
                        audiences.player(target).hideBossBar(bossBar);
                        cancel();
                        return;
                    }
                    float progress = (float) remaining / responseTime;
                    String newTime = formatTime(remaining);
                    String updatedText = bossbarTemplate.replace("{time}", newTime).replace("%clan_tag%", clanName);
                    bossBar.name(ChatUtils.formatMiniMessageWithLegacySupport(updatedText));
                    bossBar.progress(progress);
                }
            };
            task.runTaskTimer(plugin, 20L, 20L);
            inviteBossBars.put(target, bossBar);
            inviteBossBarTasks.put(target, task);
        }
        inviter.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("commands.invite-sent", "Davet gönderildi!"), getPrefix()));
    }

    public void cancelInviteBossBar(Player target) {
        if (inviteBossBarTasks.containsKey(target)) {
            inviteBossBarTasks.get(target).cancel();
            inviteBossBarTasks.remove(target);
        }
        if (inviteBossBars.containsKey(target)) {
            BukkitAudiences.create(plugin).player(target).hideBossBar(inviteBossBars.get(target));
            inviteBossBars.remove(target);
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private int getClanLimitForPlayer(Player player) {
        boolean clanLimitEnabled = plugin.getConfig().getBoolean("limit.clanLimit", false);
        if (!clanLimitEnabled) {
            return Integer.MAX_VALUE;
        }
        int maxLimit = plugin.getConfig().getInt("limit.default", 10);
        for (int i = 1; i <= 100; i++) {
            if (player.hasPermission("veldora.clan.limit." + i)) {
                maxLimit = i;
            }
        }
        return maxLimit;
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }

    public boolean hasPendingInvite(Player player, String clanName) {
        List<String> invites = invitePending.get(player.getUniqueId());
        return invites != null && invites.contains(clanName);
    }

    public List<String> getPendingInvites(Player player) {
        return invitePending.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    public void removePendingInvite(Player player, String clanName) {
        List<String> invites = invitePending.get(player.getUniqueId());
        if (invites != null) {
            invites.remove(clanName);
            if (invites.isEmpty()) {
                invitePending.remove(player.getUniqueId());
            }
        }
    }

    public IClanDataStorage getIClanDataStorage() {
        return IClanDataStorage;
    }
}
