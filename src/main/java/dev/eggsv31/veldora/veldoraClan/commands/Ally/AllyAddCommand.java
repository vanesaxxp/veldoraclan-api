package dev.eggsv31.veldora.veldoraClan.commands.Ally;

import dev.eggsv31.veldora.veldoraClan.managers.ally.AllyManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class AllyAddCommand {
    private final JavaPlugin plugin;
    private final AllyManager allyManager;
    private final IClanDataStorage IClanDataStorage;
    private final BukkitAudiences audiences;
    private final YamlLangManager yamlLangManager;
    private final Map<Player, String> allyInvitePending = new HashMap<>();
    private final Map<Player, Map<String, Long>> allyInviteCooldowns = new HashMap<>();
    private final Map<Player, BukkitRunnable> bossBarTasks = new HashMap<>();
    private final Map<Player, BossBar> bossBars = new HashMap<>();
    private final int allyInviteWaitTime;
    private final int allyInviteCooldown;
    private final int defaultClanAllyLimit;

    public AllyAddCommand(JavaPlugin plugin, IClanDataStorage IClanDataStorage, YamlLangManager yamlLangManager, AllyManager allyManager) {
        this.plugin = plugin;
        this.allyManager = allyManager;
        this.IClanDataStorage = IClanDataStorage;
        this.yamlLangManager = yamlLangManager;
        this.audiences = BukkitAudiences.create(plugin);
        this.allyInviteWaitTime = plugin.getConfig().getInt("invite.cooldown-invite-ally", 30);
        this.allyInviteCooldown = plugin.getConfig().getInt("invite.cooldown-invite-ally", 30);
        this.defaultClanAllyLimit = plugin.getConfig().getInt("allies.allyLimit", 5);
    }

    public Map<Player, String> getAllyInvitePending() {
        return this.allyInvitePending;
    }

    public void handleAllyAdd(Player player, String targetClanName) {
        String playerClan = this.IClanDataStorage.getPlayerClan(player);
        if (playerClan == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("commands.no-clan", "Bir klanda değilsiniz."),
                    this.getPrefix()));
            return;
        }

        if (!this.IClanDataStorage.isClanLeader(player)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("commands.not-leader", "Bu komutu yalnızca klan liderleri kullanabilir."),
                    this.getPrefix()));
            return;
        }

        if (playerClan.equalsIgnoreCase(targetClanName)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("ally.commands.ally-own-clan", "Kendi klanınızı müttefik yapamazsınız."),
                    this.getPrefix()));
            return;
        }

        if (this.allyManager.isClanAlly(playerClan, targetClanName)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("ally.commands.already-ally", "Bu klan zaten müttefik."),
                    this.getPrefix()));
            return;
        }

        int currentAllyCount = this.allyManager.getAllyCount(playerClan);
        int allyLimit = this.getAllyLimit(player);

        if (currentAllyCount >= allyLimit) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("ally.commands.ally-limit-reached",
                                    "Klanınızın müttefik limiti doldu (%limit%).")
                            .replace("%limit%", String.valueOf(allyLimit)),
                    this.getPrefix()));
            return;
        }

        if (this.allyInviteCooldowns.containsKey(player) && this.allyInviteCooldowns.get(player).containsKey(targetClanName)) {
            long remainingTime = this.allyInviteCooldowns.get(player).get(targetClanName) - System.currentTimeMillis();
            if (remainingTime > 0L) {
                int remainingSeconds = (int) (remainingTime / 1000L);
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                        this.yamlLangManager.getMessage("ally.commands.ally-invite-cooldown",
                                        "Bu klana tekrar müttefik daveti göndermek için %remaining_time% saniye bekleyin.")
                                .replace("%remaining_time%", String.valueOf(remainingSeconds)),
                        this.getPrefix()));
                return;
            }
        }

        Player targetLeader = this.getClanLeader(targetClanName);
        if (targetLeader != null) {
            this.allyInvitePending.put(targetLeader, playerClan);
            this.allyInviteCooldowns
                    .computeIfAbsent(player, k -> new HashMap<>())
                    .put(targetClanName, System.currentTimeMillis() + ((long) this.allyInviteCooldown * 1000L));

            this.IClanDataStorage.addAllyInviteLog(
                    playerClan,
                    targetClanName,
                    System.currentTimeMillis(),
                    "invite-sent"
            );
            this.IClanDataStorage.addAllyInviteLog(
                    targetClanName,
                    playerClan,
                    System.currentTimeMillis(),
                    "invite-received"
            );

            this.sendAllyInviteMessage(targetLeader, playerClan, player);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                if (this.allyInvitePending.containsKey(targetLeader) && this.allyInvitePending.get(targetLeader).equals(playerClan)) {
                    this.allyInvitePending.remove(targetLeader);
                    targetLeader.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                            this.yamlLangManager.getMessage("ally.commands.invite-expired", "Müttefik davetiniz zaman aşımına uğradı."),
                            this.getPrefix()));
                }
            }, (long) this.allyInviteWaitTime * 20L);
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("ally.commands.invite-sent", "Müttefik daveti gönderildi."),
                    this.getPrefix()));
        } else {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.yamlLangManager.getMessage("ally.commands.target-leader-offline", "Bu klanın lideri çevrimdışı."),
                    this.getPrefix()));
        }
    }

    private int getAllyLimit(Player player) {
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("veldora.clan.ally.limit." + i)) {
                return i;
            }
        }
        return this.defaultClanAllyLimit;
    }

    private Player getClanLeader(String clanName) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (this.IClanDataStorage.isClanLeader(onlinePlayer)
                    && clanName.equalsIgnoreCase(this.IClanDataStorage.getPlayerClan(onlinePlayer))) {
                return onlinePlayer;
            }
        }
        return null;
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void cancelInviteBossBar(Player targetLeader) {
        if (bossBarTasks.containsKey(targetLeader)) {
            bossBarTasks.get(targetLeader).cancel();
            bossBarTasks.remove(targetLeader);
        }
        if (bossBars.containsKey(targetLeader)) {
            audiences.player(targetLeader).hideBossBar(bossBars.get(targetLeader));
            bossBars.remove(targetLeader);
        }
    }

    private void sendAllyInviteMessage(Player targetLeader, String inviterClanName, Player inviter) {
        Component finalMessage = Component.empty();
        int lineIndex = 1;
        while (true) {
            String line = yamlLangManager.getMessage("CHAT.clan-ally-receive.message." + lineIndex, null);
            if (line == null) break;
            line = line.replace("%clan_tag%", inviterClanName)
                    .replace("%inviter_leader%", inviter.getName())
                    .replace("%inviter_clan%", inviterClanName)
                    .replace("%sec_expires%", String.valueOf(allyInviteWaitTime));
            Component lineComponent;
            if (line.contains("<click:") || line.contains("<hover:")) {
                lineComponent = ChatUtils.formatMiniMessageWithLegacySupport(line);
            } else {
                String formattedLine = ChatUtils.formatMessage(line);
                lineComponent = Component.text(formattedLine);
            }
            finalMessage = finalMessage.append(lineComponent).append(Component.newline());
            lineIndex++;
        }
        if (!finalMessage.equals(Component.empty())) {
            targetLeader.sendMessage(finalMessage);
        }
        String actionbar = yamlLangManager.getMessage("CHAT.clan-ally-receive.actionbar", "");
        if (!actionbar.isEmpty()) {
            targetLeader.sendActionBar(ChatUtils.formatMiniMessageWithLegacySupport(actionbar));
        }
        String titleLine = yamlLangManager.getMessage("CHAT.clan-ally-receive.title.line", "");
        String titleSubline = yamlLangManager.getMessage("CHAT.clan-ally-receive.title.subline", "");
        if (!titleLine.isEmpty() || !titleSubline.isEmpty()) {
            targetLeader.sendTitle(
                    ChatUtils.formatMessage(titleLine),
                    ChatUtils.formatMessage(titleSubline),
                    10,
                    70,
                    10
            );
        }
        String bossbarTemplate = yamlLangManager.getMessage("CHAT.clan-ally-receive.bossbar", "");
        if (!bossbarTemplate.isEmpty()) {
            int totalTime = allyInviteCooldown;
            String formattedTime = formatTime(totalTime);
            String initialText = bossbarTemplate.replace("{time}", formattedTime);
            BossBar bossBar = BossBar.bossBar(
                    ChatUtils.formatMiniMessageWithLegacySupport(initialText),
                    1.0f,
                    BossBar.Color.YELLOW,
                    BossBar.Overlay.PROGRESS
            );
            audiences.player(targetLeader).showBossBar(bossBar);
            bossBars.put(targetLeader, bossBar);
            BukkitRunnable task = new BukkitRunnable() {
                int remaining = totalTime;
                @Override
                public void run() {
                    remaining--;
                    if (remaining <= 0) {
                        audiences.player(targetLeader).hideBossBar(bossBar);
                        bossBars.remove(targetLeader);
                        bossBarTasks.remove(targetLeader);
                        cancel();
                        return;
                    }
                    float progress = (float) remaining / (float) totalTime;
                    String newTime = formatTime(remaining);
                    String updatedText = bossbarTemplate.replace("{time}", newTime);
                    bossBar.name(ChatUtils.formatMiniMessageWithLegacySupport(updatedText));
                    bossBar.progress(progress);
                }
            };
            task.runTaskTimer(plugin, 20L, 20L);
            bossBarTasks.put(targetLeader, task);
        }
    }

    private String getPrefix() {
        return this.plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
