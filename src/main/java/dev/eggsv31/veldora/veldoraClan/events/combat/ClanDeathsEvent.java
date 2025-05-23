package dev.eggsv31.veldora.veldoraClan.events.combat;

import dev.eggsv31.veldora.veldoraClan.events.combat.managers.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ClanDeathsEvent implements Listener {
    private final CombatManager combatManager;

    public ClanDeathsEvent(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        combatManager.incrementDeath(event.getEntity());
    }
}
