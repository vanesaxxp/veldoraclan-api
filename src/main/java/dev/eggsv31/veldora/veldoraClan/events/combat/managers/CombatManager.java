package dev.eggsv31.veldora.veldoraClan.events.combat.managers;

import dev.eggsv31.veldora.veldoraClan.managers.ally.AllyManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.WeakHashMap;

public class CombatManager {

    private final AllyManager allyManager;
    private final IClanDataStorage clanDataStorage;
    private final boolean defaultClanDamage;
    private final Map<Player, String> clanCache = new WeakHashMap<>();

    public CombatManager(IClanDataStorage clanDataStorage, AllyManager allyManager, JavaPlugin plugin) {
        this.clanDataStorage = clanDataStorage;
        this.allyManager = allyManager;
        this.defaultClanDamage = plugin.getConfig().getBoolean("Clan.name.damage", false);
    }

    public boolean isClanPvpEnabled(String clanName) {
        FileConfiguration config = clanDataStorage.getClanConfig(clanName);
        return config != null && config.getBoolean("pvp", defaultClanDamage);
    }

    public void incrementKill(Player killer) {
        updateClanStats(killer, "kills", "total-kills");
    }

    public void incrementDeath(Player player) {
        updateClanStats(player, "deaths", "total-deaths");
    }

    private void updateClanStats(Player player, String statPath, String totalPath) {
        String clanName = getClan(player);
        if (clanName == null) return;
        FileConfiguration clanConfig = clanDataStorage.getClanConfig(clanName);
        if (clanConfig == null) return;

        String playerPath = "members." + player.getUniqueId() + "." + statPath;
        clanConfig.set(playerPath, clanConfig.getInt(playerPath, 0) + 1);
        clanConfig.set(totalPath, clanConfig.getInt(totalPath, 0) + 1);
        clanDataStorage.saveClanConfig(clanConfig, clanName);
    }

    public String getClan(Player player) {
        if (clanCache.containsKey(player)) {
            return clanCache.get(player);
        }
        String clan = clanDataStorage.getPlayerClan(player);
        clanCache.put(player, clan);
        return clan;
    }

    public boolean isClanAlly(String clan1, String clan2) {
        return clan1 != null && clan2 != null && allyManager.isClanAlly(clan1, clan2);
    }

    public void removePlayerFromCache(Player player) {
        clanCache.remove(player);
    }
}
