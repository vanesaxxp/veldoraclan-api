package dev.eggsv31.veldora.veldoraClan.events.player.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinEventHandler implements Listener {
    private final JavaPlugin plugin;
    private final String latestVersion;

    public PlayerJoinEventHandler(JavaPlugin plugin, String latestVersion) {
        this.plugin = plugin;
        this.latestVersion = latestVersion;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && latestVersion != null) {
            String currentVersion = plugin.getDescription().getVersion();
            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                int versionDifference = compareVersions(currentVersion, latestVersion);
                player.sendMessage("");
                player.sendMessage("§7[§cVeldoraClan§7] §7A §anew update §7is available §8[ §bMinecraft Compatibility Update §8]. §a" + versionDifference + " §7release(s) behind. Visit §b§nhttps://www.spigotmc.org/resources/%E2%9A%94%EF%B8%8F-veldoraclan-revolutionize-teamplay-with-the-ultimate-clan-experience-1-21-4.119698/ §7to download.");
                player.sendMessage("");
            }
        }
    }

    private int compareVersions(String currentVersion, String latestVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");
        int length = Math.max(currentParts.length, latestParts.length);

        for (int i = 0; i < length; i++) {
            int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            if (currentPart != latestPart) {
                return latestPart - currentPart;
            }
        }
        return 0;
    }
}
