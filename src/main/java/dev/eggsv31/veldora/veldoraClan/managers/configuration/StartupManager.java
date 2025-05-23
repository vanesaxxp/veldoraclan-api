package dev.eggsv31.veldora.veldoraClan.managers.configuration;

import dev.eggsv31.veldora.veldoraClan.Chest.event.ClanChestClickListener;
import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.events.chat.ClanAllyChatEvent;
import dev.eggsv31.veldora.veldoraClan.events.combat.ClanDeathsEvent;
import dev.eggsv31.veldora.veldoraClan.events.combat.ClanDamageEvent;
import dev.eggsv31.veldora.veldoraClan.events.combat.ClanKillEvent;
import dev.eggsv31.veldora.veldoraClan.events.player.events.LeaderLastLoginListener;
import dev.eggsv31.veldora.veldoraClan.events.player.events.PlayerJoinQuitListener;
import dev.eggsv31.veldora.veldoraClan.managers.clan.ClanMemberManager;
import dev.eggsv31.veldora.veldoraClan.managers.gui.ClanMainMenu;
import dev.eggsv31.veldora.veldoraClan.managers.gui.menu.ClanGuiClickListener;
import dev.eggsv31.veldora.veldoraClan.managers.gui.menu.listeners.ClanDeleteClickListener;
import dev.eggsv31.veldora.veldoraClan.storage.save.ClanChestSaveEvent;
import dev.eggsv31.veldora.veldoraClan.VeldoraClan;
import dev.eggsv31.veldora.veldoraClan.Chest.ClanChest;
import dev.eggsv31.veldora.veldoraClan.events.combat.managers.CombatManager;
import dev.eggsv31.veldora.veldoraClan.managers.ally.AllyManager;
import dev.eggsv31.veldora.veldoraClan.managers.commands.ClanCommandManager;
import dev.eggsv31.veldora.veldoraClan.managers.commands.managers.ClanTabCompletor;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.datatype.ClanDataStorageYAML;
import dev.eggsv31.veldora.veldoraClan.storage.DataStorage;
import dev.eggsv31.veldora.veldoraClan.update.Uptader;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class StartupManager {

    private final VeldoraClan plugin;
    private ClanMainMenu clanMainMenu;
    private ClanChest clanChest;
    private IClanDataStorage clanDataStorage;
    private ClanMemberManager    clanMemberManager;
    private YamlLangManager langManager;
    private GuiLangManager guiLangManager;
    private ConfigManager configManager;
    private CombatManager combatManager;

    public StartupManager(VeldoraClan plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            createStorageFiles();
            setupEconomy();
            this.guiLangManager = new GuiLangManager(plugin);
            configManager = new ConfigManager(plugin);
            configManager.createDiscordConfig();
            String dbType = plugin.getConfig().getString("Storage.type", "YAML").toUpperCase(Locale.ROOT);
            DataStorage clanStorageBackend;
            if ("YAML".equals(dbType)) {
                clanStorageBackend = new DataStorage(plugin);
            } else {
                plugin.getLogger().warning("Unknown Storage.type '" + dbType + "'. Defaulting to YAML.");
                clanStorageBackend = new DataStorage(plugin);
            }

            this.clanDataStorage = new ClanDataStorageYAML(
                    plugin, plugin.getConfig(), null, configManager, clanStorageBackend, clanDataStorage
            );
            AllyManager allyManager = new AllyManager(clanDataStorage);
            combatManager = new CombatManager(clanDataStorage, allyManager, plugin);
            clanMemberManager = new ClanMemberManager(clanDataStorage, combatManager);
            createLanguageFiles();
            String language = plugin.getConfig().getString("language.messages", "ENG");
            langManager = new YamlLangManager(plugin, language);
            this.clanDataStorage.setLangManager(langManager);
            clanChest = new ClanChest(clanDataStorage, langManager, guiLangManager, plugin.getDataFolder(), plugin.getConfig(), plugin, plugin.getEconomy());
            new ClanGuiClickListener(clanDataStorage, plugin, clanChest, guiLangManager);
            new ClanDeleteClickListener(clanDataStorage, guiLangManager, plugin);
            configManager.checkAndAddMissingConfigValues();
            this.clanDataStorage.fixClanFiles();
            registerCommands();
            registerEvents();
            plugin.setClanAPI();
            plugin.setStatsAPI();
            this.guiLangManager = new GuiLangManager(plugin);
            new Uptader(plugin, "119698").checkForUpdates();
            printStartupMessage();
        } catch (Exception e) {
            plugin.getLogger().severe("An error occurred during initialization: " + e.getMessage());
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin not found, disabling economy features");
            plugin.setEconomy(null);
            return;
        }
        var rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Economy service not found, disabling economy features");
            plugin.setEconomy(null);
            return;
        }
        plugin.setEconomy(rsp.getProvider());
    }

    private void registerCommands() {
        PluginCommand clanCommand = plugin.getCommand("clan");
        if (clanCommand != null) {
            ClanCommandManager commandHandler = new ClanCommandManager(
                    clanMemberManager,
                    combatManager,
                    plugin,
                    clanDataStorage,
                    clanChest,
                    langManager,
                    plugin.getEconomy(),
                    guiLangManager
            );
            clanCommand.setExecutor(commandHandler);
            clanCommand.setTabCompleter(new ClanTabCompletor(
                    clanDataStorage,
                    plugin,
                    commandHandler.getClanInviteCommand(),
                    commandHandler.getAllyAddCommand()
            ));
        } else {
            plugin.getLogger().warning("The 'clan' command could not be found.");
        }
    }

    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(new ClanDeathsEvent(combatManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ClanGuiClickListener(clanDataStorage, plugin, clanChest, guiLangManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ClanChestClickListener(clanChest, clanDataStorage, guiLangManager, langManager, plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ClanKillEvent(combatManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ClanDamageEvent(plugin, combatManager, langManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ClanChestSaveEvent(clanDataStorage, clanChest), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ClanAllyChatEvent(plugin, clanDataStorage, langManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new LeaderLastLoginListener(clanDataStorage), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(clanDataStorage), plugin);
        plugin.getServer().getPluginManager().registerEvents(new dev.eggsv31.veldora.veldoraClan.commands.management.ClanCreateCommand(plugin, clanDataStorage, langManager, plugin.getEconomy()), plugin);
    }

    public IClanDataStorage getClanDataStorage() {
        return clanDataStorage;
    }

    public YamlLangManager getLangManager() {
        return langManager;
    }

    private void createLanguageFiles() {
        File langFolder = new File(plugin.getDataFolder(), "Language");
        File guiFolder = new File(langFolder, "GUI");
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create language folder.");
            return;
        }
        if (!guiFolder.exists() && !guiFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create GUI language folder.");
            return;
        }
        for (String lang : new String[]{"ENG.yml", "ES.yml", "TR.yml", "RU.yml", "FR.yml", "JP.yml"}) {
            File mainLangFile = new File(langFolder, lang);
            if (!mainLangFile.exists()) {
                try {
                    plugin.saveResource("Language/" + lang, false);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Resource not found: Language/" + lang);
                }
            }
            File guiLangFile = new File(guiFolder, lang);
            if (!guiLangFile.exists()) {
                try {
                    plugin.saveResource("Language/GUI/" + lang, false);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Resource not found: Language/GUI/" + lang);
                }
            }
        }
    }

    private void createStorageFiles() {
        File storageFolder = new File(plugin.getDataFolder(), "Storage");
        File yamlFolder = new File(storageFolder, "YAML");
        File sqliteFolder = new File(storageFolder, "SQLITE");
        if (!yamlFolder.exists()) {
            yamlFolder.mkdirs();
        }
        if (!sqliteFolder.exists()) {
            sqliteFolder.mkdirs();
        }
        File sqliteDatabase = new File(sqliteFolder, "SQLITE.db");
        if (!sqliteDatabase.exists()) {
            try {
                sqliteDatabase.createNewFile();
            } catch (IOException ignored) {
            }
        }
    }

    public void shutdown() {
        if (clanChest != null) {
            clanChest.saveAllClanChests();
        }
        if (clanDataStorage != null) {
            clanDataStorage.saveAllClansToDisk();
        }
        printShutdownMessage();
    }

    private void printStartupMessage() {
        ConsoleCommandSender console = plugin.getServer().getConsoleSender();
        console.sendMessage(ChatColor.DARK_GRAY + "============================================");
        console.sendMessage(ChatColor.AQUA + "     __      __");
        console.sendMessage(ChatColor.AQUA + "      \\ \\    / /");
        console.sendMessage(ChatColor.AQUA + "       \\ \\  / / ");
        console.sendMessage(ChatColor.AQUA + "        \\ \\/ /  ");
        console.sendMessage(ChatColor.AQUA + "         \\  /   ");
        console.sendMessage(ChatColor.AQUA + "          \\/    ");
        console.sendMessage("");
        console.sendMessage(ChatColor.GREEN + "Veldora Clan Plugin");
        console.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        console.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + "eggsv31");
        console.sendMessage(ChatColor.YELLOW + "Discord: " + ChatColor.WHITE + "https://discord.com/invite/PyhmWdUa8m");
        console.sendMessage(ChatColor.YELLOW + "Spigot: " + ChatColor.AQUA + "https://www.spigotmc.org/resources/%E2%9A%94%EF%B8%8F-veldoraclan-revolutionize-teamplay-with-the-ultimate-clan-experience-1-21-4.119698/");
        console.sendMessage(ChatColor.DARK_AQUA + "--------------------------------------------");
        console.sendMessage(ChatColor.GOLD + "  The plugin has been successfully enabled! ");
        console.sendMessage(ChatColor.DARK_GRAY + "============================================");
    }

    private void printShutdownMessage() {
        ConsoleCommandSender console = plugin.getServer().getConsoleSender();
        console.sendMessage(ChatColor.DARK_GRAY + "============================================");
        console.sendMessage(ChatColor.RED + "      __     __ ");
        console.sendMessage(ChatColor.RED + "       \\ \\   / /");
        console.sendMessage(ChatColor.RED + "        \\ \\ / / ");
        console.sendMessage(ChatColor.RED + "         \\ V /  ");
        console.sendMessage(ChatColor.RED + "          \\_/   ");
        console.sendMessage("");
        console.sendMessage(ChatColor.RED + "Veldora Clan Plugin");
        console.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        console.sendMessage(ChatColor.DARK_RED + "  The plugin has been successfully disabled. ");
        console.sendMessage(ChatColor.DARK_GRAY + "============================================");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GuiLangManager getGuiLangManager() {
        return guiLangManager;
    }

    public IClanDataStorage getYamlClanManager() {
        return clanDataStorage;
    }
}
