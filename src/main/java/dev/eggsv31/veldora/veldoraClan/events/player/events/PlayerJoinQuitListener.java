package dev.eggsv31.veldora.veldoraClan.events.player.events;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class PlayerJoinQuitListener implements Listener {
    private final IClanDataStorage dataStorage;

    public PlayerJoinQuitListener(IClanDataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String clanName = dataStorage.getPlayerClan(player);
        if (clanName != null) {
            UUID uuid = player.getUniqueId();
            long timeNow = System.currentTimeMillis();
            dataStorage.setMemberLastLogin(clanName, uuid, timeNow);
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timeNow));
        }
    }
}
