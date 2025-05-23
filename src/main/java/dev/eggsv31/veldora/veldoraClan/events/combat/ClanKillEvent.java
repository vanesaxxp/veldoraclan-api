package dev.eggsv31.veldora.veldoraClan.events.combat;

import dev.eggsv31.veldora.veldoraClan.events.combat.managers.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ClanKillEvent implements Listener {
    private final CombatManager combatManager;

    public ClanKillEvent(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            combatManager.incrementKill(killer);
        }
    }
}
