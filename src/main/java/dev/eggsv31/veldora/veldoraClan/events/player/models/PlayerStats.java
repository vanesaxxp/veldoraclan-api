package dev.eggsv31.veldora.veldoraClan.events.player.models;

import org.bukkit.OfflinePlayer;

public class PlayerStats {
    private final OfflinePlayer player;
    private int kills;
    private int deaths;

    public PlayerStats(OfflinePlayer player) {
        this.player = player;
        this.kills = player.getStatistic(org.bukkit.Statistic.PLAYER_KILLS);
        this.deaths = player.getStatistic(org.bukkit.Statistic.DEATHS);
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public double getKDR() {
        return deaths > 0 ? (double) kills / deaths : (double) kills;
    }

    @Override
    public String toString() {
        return "PlayerStats{" +
                "player=" + player.getName() +
                ", kills=" + kills +
                ", deaths=" + deaths +
                ", kdr=" + String.format("%.2f", getKDR()) +
                '}';
    }
}