package dev.eggsv31.veldora.veldoraClan.events.player.events;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LeaderLastLoginListener implements Listener {
    private final IClanDataStorage clanDataStorage;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LeaderLastLoginListener(IClanDataStorage clanDataStorage) {
        this.clanDataStorage = clanDataStorage;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (clanDataStorage.isClanLeader(player)) {
            long now = System.currentTimeMillis();
            clanDataStorage.setLeaderLastLogin(clanDataStorage.getPlayerClan(player), now);
            String formattedDate = dateFormat.format(new Date(now));
        }
    }
}
