package dev.eggsv31.veldora.veldoraClan.events.combat;

import dev.eggsv31.veldora.veldoraClan.events.combat.managers.CombatManager;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;

public class ClanDamageEvent implements Listener {

    private final CombatManager combatManager;
    private final YamlLangManager langManager;
    private final boolean allyPvpEnabled;
    private final String prefix;
    private final Map<Player, Long> lastMessageTime = new HashMap<>();
    private static final long MESSAGE_COOLDOWN = 4000L;

    public ClanDamageEvent(JavaPlugin plugin, CombatManager combatManager, YamlLangManager langManager) {
        this.combatManager = combatManager;
        this.langManager = langManager;
        this.allyPvpEnabled = plugin.getConfig().getBoolean("allies.damage", false);
        this.prefix = plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Player damager = getDamager(event.getDamager());
        if (damager == null || !(event.getEntity() instanceof Player)) {
            return;
        }
        Player target = (Player) event.getEntity();

        String damagerClan = combatManager.getClan(damager);
        String targetClan = combatManager.getClan(target);
        if (damagerClan == null || targetClan == null) {
            return;
        }

        if (damagerClan.equals(targetClan)) {
            if (!combatManager.isClanPvpEnabled(damagerClan)) {
                event.setCancelled(true);
                sendMessageWithCooldown(damager, "commands.cannot-attack-clan", "%prefix% &7Bu klan için PvP devre dışı!");
                return;
            }
        }

        if (combatManager.isClanAlly(damagerClan, targetClan) && !allyPvpEnabled) {
            event.setCancelled(true);
            sendMessageWithCooldown(damager, "ally.commands.cannot-attack-ally", "%prefix% &7Müttefik oyunculara saldırı yapılamaz!");
        }
    }

    private void sendMessageWithCooldown(Player player, String messageKey, String defaultMessage) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTime.getOrDefault(player, 0L) >= MESSAGE_COOLDOWN) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage(messageKey, defaultMessage),
                    prefix
            ));
            lastMessageTime.put(player, currentTime);
        }
    }

    private Player getDamager(Entity entity) {
        if (entity instanceof Player) {
            return (Player) entity;
        }
        if (entity instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) entity).getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }
        return null;
    }
}
