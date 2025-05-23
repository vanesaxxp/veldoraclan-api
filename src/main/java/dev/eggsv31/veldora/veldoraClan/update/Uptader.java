package dev.eggsv31.veldora.veldoraClan.update;

import dev.eggsv31.veldora.veldoraClan.events.player.events.PlayerJoinEventHandler;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Uptader {
    private final JavaPlugin plugin;
    private final String resourceId;
    private final String updateUrl;
    private String latestVersion;

    public Uptader(JavaPlugin plugin, String resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.updateUrl = "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId + "/";

        int pluginId1 = 23518;
        int pluginId2 = 24457;

        new Metrics(plugin, pluginId1);
        new Metrics(plugin, pluginId2);

        this.startUpdateChecker();
    }

    private void startUpdateChecker() {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, this::checkForUpdates);
    }

    public void checkForUpdates() {
        try {
            latestVersion = this.fetchLatestVersion();
            if (latestVersion == null) {
                return;
            }

            String currentVersion = this.plugin.getDescription().getVersion();
            if (this.isUpdateAvailable(currentVersion, latestVersion)) {
                int versionDifference = compareVersions(currentVersion, latestVersion);
                this.displayInfoMessage(currentVersion, latestVersion, versionDifference);
            }

            Bukkit.getPluginManager().registerEvents(new PlayerJoinEventHandler(plugin, latestVersion), plugin);
        } catch (Exception e) {
        }
    }

    private String fetchLatestVersion() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) (new URL(this.updateUrl)).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return in.readLine();
        }
    }

    private boolean isUpdateAvailable(String currentVersion, String latestVersion) {
        return !currentVersion.equalsIgnoreCase(latestVersion);
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

    private void displayInfoMessage(String currentVersion, String latestVersion, int versionDifference) {
        String resourceLink = "https://www.spigotmc.org/resources/veldora-clan-1-16-1-20-2-6." + this.resourceId + "/";

        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "================================================================");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "     __      __");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "      \\ \\    / /");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "       \\ \\  / / ");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "        \\ \\/ /  ");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "         \\  /   ");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "          \\/    ");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "================================================================");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Veldora Clan Plugin Information");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "Running Version: " + ChatColor.WHITE + currentVersion);
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "Latest Version: " + ChatColor.GREEN + latestVersion);
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "You are currently " + ChatColor.AQUA + versionDifference + ChatColor.GRAY + " versions behind.");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "Download the latest version here: " + ChatColor.AQUA + resourceLink);
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Discord: " + ChatColor.WHITE + "https://discord.com/invite/PyhmWdUa8m");
        this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "================================================================");
    }
}
