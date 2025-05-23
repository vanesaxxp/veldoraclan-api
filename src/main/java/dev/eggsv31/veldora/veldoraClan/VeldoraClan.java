package dev.eggsv31.veldora.veldoraClan;

import dev.eggsv31.veldora.veldoraClan.Chest.ClanChest;
import dev.eggsv31.veldora.veldoraClan.api.VeldoraClanAPI;
import dev.eggsv31.veldora.veldoraClan.api.VeldoraClanAPIImpl;
import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.StartupManager;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.ConfigManager;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.managers.gui.ClanMainMenu;
import dev.eggsv31.veldora.veldoraClan.metrics.MetricsManager;
import PlaceHolderAPIHook.ClipPlaceholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class VeldoraClan extends JavaPlugin {

    private StartupManager startupManager;
    private ClanChest clanChest;
    private ClanMainMenu clanMainMenu;
    private IClanDataStorage clanDataStorage;
    private YamlLangManager langManager;
    private GuiLangManager guiLangManager;
    private Economy economy;
    private MetricsManager metricsManager;
    private VeldoraClanAPI api;

    @Override
    public void onEnable() {
        startupManager = new StartupManager(this);
        startupManager.initialize();
        clanDataStorage = startupManager.getClanDataStorage();
        langManager = startupManager.getLangManager();
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
        } else {
            getLogger().severe("Vault bulunamadı! Ekonomi özellikleri devre dışı bırakıldı.");
            economy = null;
        }
        metricsManager = new MetricsManager(this);
        clanChest = new ClanChest(
                clanDataStorage,
                langManager,
                guiLangManager,
                getDataFolder(),
                getConfig(),
                this,
                economy
        );
        api = new VeldoraClanAPIImpl(this, clanDataStorage, guiLangManager);

        getServer()
                .getServicesManager()
                .register(VeldoraClanAPI.class, api, this, ServicePriority.Normal);
        getLogger().info("VeldoraClanAPI servisi kaydedildi.");
        Player examplePlayer = getServer().getOnlinePlayers().stream().findFirst().orElse(null);
        if (examplePlayer != null) {
            clanMainMenu = new ClanMainMenu(
                    examplePlayer,
                    clanDataStorage,
                    clanChest,
                    guiLangManager
            );
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClipPlaceholder(this, startupManager.getYamlClanManager()).register();
        }
    }

    @Override
    public void onDisable() {
        if (startupManager != null) {
            startupManager.shutdown();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        var rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public void setEconomy(Economy economy) {
        this.economy = economy;
    }

    public void setClanAPI() {
    }

    public void setStatsAPI() {
    }

    public ConfigManager getConfigManager() {
        return startupManager.getConfigManager();
    }

    public Economy getEconomy() {
        return economy;
    }

    public IClanDataStorage getClanDataStorage() {
        return clanDataStorage;
    }

    public YamlLangManager getLangManager() {
        return langManager;
    }

    public GuiLangManager getGuiLangManager() {
        return guiLangManager;
    }

    public VeldoraClanAPI getApi() { return api; }

}
