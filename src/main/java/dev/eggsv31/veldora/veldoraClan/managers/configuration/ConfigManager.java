package dev.eggsv31.veldora.veldoraClan.managers.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final File configFile;
    private final FileConfiguration config;

    private FileConfiguration discordConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadDiscordConfig();
    }

    public void checkAndAddMissingConfigValues() {
        Map<String, Object> defaultValues = new LinkedHashMap<>();

        defaultValues.put("prefix", "&7[&cVeldoraClan&7]");
        defaultValues.put("language.messages", "ENG");
        defaultValues.put("language.gui", "ENG");

        defaultValues.put("Storage.type", "YAML");

        defaultValues.put("Clan.name.damage", false);
        defaultValues.put("Clan.name.pvp_toggle", true);
        defaultValues.put("Clan.name.disableExecutableitemsForClanMembers", false);
        defaultValues.put("Clan.name.minLength", 2);
        defaultValues.put("Clan.name.maxLength", 16);
        defaultValues.put("Clan.name.regex", "^[a-zA-Z0-9]*$");
        defaultValues.put("Clan.name.blacklistedNames", Arrays.asList("admin", "mod"));

        defaultValues.put("Clan.create.killToCreate", false);
        defaultValues.put("Clan.create.killCount", 15);
        defaultValues.put("Clan.create.cooldown", 300);

        defaultValues.put("Clan.chat.enabled", true);
        defaultValues.put("Clan.chat.chatFormat", "&7[&b%veldora_clan_tag%&7] &aClan Chat &b%player% &8» &f%message%");
        defaultValues.put("Clan.chat.allyChatFormat", "&7[&b%veldora_clan_tag%&7] &eAlly Chat &b%player% &8» &f%message%");

        defaultValues.put("stats.includeStats", true);

        defaultValues.put("color.default", "&e");
        defaultValues.put("color.cooldown", 24);

        defaultValues.put("invite.player.invite-cooldown", 30);
        defaultValues.put("invite.player.response-time", 30);
        defaultValues.put("invite.cooldown-invite-ally", 30);

        defaultValues.put("rename.cooldown", 24);

        defaultValues.put("limit.default", 10);
        defaultValues.put("limit.clanLimit", true);
        defaultValues.put("allies.enabled", true);
        defaultValues.put("allies.damage", false);
        defaultValues.put("allies.allyChat", true);
        defaultValues.put("allies.limit", true);
        defaultValues.put("allies.allyLimit", 4);

        defaultValues.put("VaultSupport.enabled", false);
        defaultValues.put("VaultSupport.create", 100.0);
        defaultValues.put("VaultSupport.rename", 100.0);
        defaultValues.put("VaultSupport.chest", 100.0);

        defaultValues.put("Chest.enabled", true);
        defaultValues.put("Chest.slots", 54);
        defaultValues.put("Chest.title", "&6%clan% &cClan Chest");

        defaultValues.put("broadcasts.create", true);
        defaultValues.put("broadcasts.join", true);
        defaultValues.put("broadcasts.delete", true);
        defaultValues.put("broadcasts.leave", true);

        defaultValues.put("BroadCastMessages.clan-create-messages", "%prefix% &7| &a%player% &ehas created a new clan named &b%clan%");
        defaultValues.put("BroadCastMessages.clan-join-player-messages", "%prefix% &7| &a%player% &ehas joined the clan &b%clan%");
        defaultValues.put("BroadCastMessages.clan-delete-messages", "%prefix% &7| &eThe clan &b%clan% &ehas been deleted by &a%player%");
        defaultValues.put("BroadCastMessages.clan-leave-player-messages", "%prefix% &7| &a%player% &ehas left the clan &b%clan%");

        defaultValues.put("Permission", false);
        defaultValues.put("PermissionClanCreate", "veldora.clan.create");
        defaultValues.put("PermissionClanInvite", "veldora.clan.invite");
        defaultValues.put("PermissionClanAccept", "veldora.clan.accept");
        defaultValues.put("PermissionClanChat", "veldora.clan.chat");
        defaultValues.put("PermissionClanKick", "veldora.clan.kick");
        defaultValues.put("PermissionClanTransfer", "veldora.clan.transfer");
        defaultValues.put("PermissionClanColor", "veldora.clan.set.color");
        defaultValues.put("PermissionClanRename", "veldora.clan.set.rename");
        defaultValues.put("PermissionClanChest", "veldora.clan.chest");
        defaultValues.put("PermissionClanPvp", "veldora.clan.pvp");
        defaultValues.put("PermissionClanBan", "veldora.clan.ban");
        defaultValues.put("PermissionClanUnBan", "veldora.clan.unban");
        defaultValues.put("PermissionClanAllyAdd", "veldora.clan.ally.add");
        defaultValues.put("PermissionClanAllyRemove", "veldora.clan.ally.remove");
        defaultValues.put("PermissionClanAllyAccept", "veldora.clan.ally.accept");
        defaultValues.put("PermissionClanAllyDeny", "veldora.clan.ally.deny");
        defaultValues.put("PermissionClanAllyChat", "veldora.clan.ally.chat");
        defaultValues.put("PermissionClanTopStats", "veldora.clan.topstats");
        defaultValues.put("PermissionClanStats", "veldora.clan.stats");
        defaultValues.put("PermissionClanLeave", "veldora.clan.leave");
        defaultValues.put("PermissionClanDelete", "veldora.clan.delete");
        defaultValues.put("PermissionClanList", "veldora.clan.list");
        defaultValues.put("PermissionClanReload", "veldora.clan.reload");
        defaultValues.put("PermissionClanChestLimit", "veldora.clan.chest.limit.X");
        defaultValues.put("PermissionClanLimit", "veldora.clan.limit.X");
        defaultValues.put("PermissionClanAllyLimit", "veldora.clan.ally.limit.X");

        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            if (!config.isSet(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
            }
        }

        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config.yml: " + e.getMessage());
        }
    }

    public void createDiscordConfig() {
        File discordConfigFile = new File(plugin.getDataFolder(), "discord.yml");
        if (!discordConfigFile.exists()) {
            FileConfiguration discordConfig = YamlConfiguration.loadConfiguration(discordConfigFile);

            discordConfig.set("ClanCreate.enabled", false);
            discordConfig.set("ClanCreate.webhook-url", "https://discord.com/api/webhooks/your_webhook_url");
            discordConfig.set("ClanCreate.webhook-title", ":star2: New Clan Created!");
            discordConfig.set("ClanCreate.webhook-description", ":white_check_mark: A new clan has been successfully created!");
            discordConfig.set("ClanCreate.webhook-thumbnail", "https://minotar.net/avatar/{creator}/256");
            discordConfig.set("ClanCreate.webhook-color", "3498db");
            discordConfig.set("ClanCreate.inline", false);
            discordConfig.set("ClanCreate.timestamp", true);

            Map<String, String> clanCreateFields = new LinkedHashMap<>();
            clanCreateFields.put(":shield: **Clan Name**:", "`{clan_name}`");
            clanCreateFields.put(":crown: **Created By**:", "`{creator}`");
            clanCreateFields.put(":calendar: **Creation Date**:", "`{creation_date}`");
            clanCreateFields.put(":trophy: **Total Clans Created**:", "`{total_clans}`");

            for (Map.Entry<String, String> entry : clanCreateFields.entrySet()) {
                discordConfig.set("ClanCreate.fields." + entry.getKey(), entry.getValue());
            }

            discordConfig.set("ClanCreate.footer.text", "VeldoraClan - Elite Clans System");
            discordConfig.set("ClanCreate.footer.icon-url", "https://minotar.net/avatar/{creator}/256");
            discordConfig.set("ClanCreate.author.name", "{creator}");
            discordConfig.set("ClanCreate.author.url", "https://example.com/player/{creator}");
            discordConfig.set("ClanCreate.author.icon-url", "https://minotar.net/avatar/{creator}/256");

            discordConfig.set("ClanJoin.enabled", false);
            discordConfig.set("ClanJoin.webhook-url", "https://discord.com/api/webhooks/your_webhook_url");
            discordConfig.set("ClanJoin.webhook-title", ":wave: New Member Joined!");
            discordConfig.set("ClanJoin.webhook-description", ":white_check_mark: `{player}` has joined `{clan_name}`!");
            discordConfig.set("ClanJoin.webhook-thumbnail", "https://minotar.net/avatar/{player}/256");
            discordConfig.set("ClanJoin.webhook-color", "2ecc71");
            discordConfig.set("ClanJoin.inline", false);
            discordConfig.set("ClanJoin.timestamp", true);

            Map<String, String> clanJoinFields = new LinkedHashMap<>();
            clanJoinFields.put(":shield: **Clan Name**:", "`{clan_name}`");
            clanJoinFields.put(":bust_in_silhouette: **Player Name**:", "`{player}`");
            clanJoinFields.put(":calendar: **Join Date**:", "`{join_date}`");

            for (Map.Entry<String, String> entry : clanJoinFields.entrySet()) {
                discordConfig.set("ClanJoin.fields." + entry.getKey(), entry.getValue());
            }

            discordConfig.set("ClanJoin.footer.text", "VeldoraClan - Elite Clans System");
            discordConfig.set("ClanJoin.footer.icon-url", "https://minotar.net/avatar/{player}/256");
            discordConfig.set("ClanJoin.author.name", "{player}");
            discordConfig.set("ClanJoin.author.url", "https://example.com/player/{player}");
            discordConfig.set("ClanJoin.author.icon-url", "https://minotar.net/avatar/{player}/256");

            discordConfig.set("ClanLeave.enabled", false);
            discordConfig.set("ClanLeave.webhook-url", "https://discord.com/api/webhooks/your_webhook_url");
            discordConfig.set("ClanLeave.webhook-title", ":wave: Member Left!");
            discordConfig.set("ClanLeave.webhook-description", ":x: `{player}` has left `{clan_name}`!");
            discordConfig.set("ClanLeave.webhook-thumbnail", "https://minotar.net/avatar/{player}/256");
            discordConfig.set("ClanLeave.webhook-color", "e74c3c");
            discordConfig.set("ClanLeave.inline", false);
            discordConfig.set("ClanLeave.timestamp", true);

            Map<String, String> clanLeaveFields = new LinkedHashMap<>();
            clanLeaveFields.put(":shield: **Clan Name**:", "`{clan_name}`");
            clanLeaveFields.put(":bust_in_silhouette: **Player Name**:", "`{player}`");
            clanLeaveFields.put(":calendar: **Leave Date**:", "`{leave_date}`");

            for (Map.Entry<String, String> entry : clanLeaveFields.entrySet()) {
                discordConfig.set("ClanLeave.fields." + entry.getKey(), entry.getValue());
            }

            discordConfig.set("ClanLeave.footer.text", "VeldoraClan - Elite Clans System");
            discordConfig.set("ClanLeave.footer.icon-url", "https://minotar.net/avatar/{player}/256");
            discordConfig.set("ClanLeave.author.name", "{player}");
            discordConfig.set("ClanLeave.author.url", "https://example.com/player/{player}");
            discordConfig.set("ClanLeave.author.icon-url", "https://minotar.net/avatar/{player}/256");

            discordConfig.set("ClanAllyAdd.enabled", false);
            discordConfig.set("ClanAllyAdd.webhook-url", "https://discord.com/api/webhooks/your_webhook_url_ally_add");
            discordConfig.set("ClanAllyAdd.webhook-title", ":handshake: New Alliance Formed!");
            discordConfig.set("ClanAllyAdd.webhook-description", ":white_check_mark: `{clan_name}` and `{ally_name}` are now allies, initiated by `{player}`!");
            discordConfig.set("ClanAllyAdd.webhook-thumbnail", "https://minotar.net/avatar/{player}/256");
            discordConfig.set("ClanAllyAdd.webhook-color", "2ecc71");
            discordConfig.set("ClanAllyAdd.inline", true);
            discordConfig.set("ClanAllyAdd.timestamp", true);

            Map<String, String> clanAllyAddFields = new LinkedHashMap<>();
            clanAllyAddFields.put(":shield: **Clan Name**:", "`{clan_name}`");
            clanAllyAddFields.put(":handshake: **New Ally**:", "`{ally_name}`");
            clanAllyAddFields.put(":crown: **Initiator**:", "`{player}`");
            clanAllyAddFields.put(":calendar: **Alliance Date**:", "`{alliance_date}`");

            for (Map.Entry<String, String> entry : clanAllyAddFields.entrySet()) {
                discordConfig.set("ClanAllyAdd.fields." + entry.getKey(), entry.getValue());
            }

            discordConfig.set("ClanAllyAdd.footer.text", "VeldoraClan - Elite Alliances");
            discordConfig.set("ClanAllyAdd.footer.icon-url", "https://minotar.net/avatar/{player}/256");
            discordConfig.set("ClanAllyAdd.author.name", "{player}");
            discordConfig.set("ClanAllyAdd.author.url", "https://example.com/player/{player}");
            discordConfig.set("ClanAllyAdd.author.icon-url", "https://minotar.net/avatar/{player}/256");

            discordConfig.set("ClanAllyRemove.enabled", false);
            discordConfig.set("ClanAllyRemove.webhook-url", "https://discord.com/api/webhooks/your_webhook_url_ally_remove");
            discordConfig.set("ClanAllyRemove.webhook-title", ":broken_heart: Alliance Ended");
            discordConfig.set("ClanAllyRemove.webhook-description", ":x: `{clan_name}` and `{ally_name}` are no longer allies, ended by `{player}`!");
            discordConfig.set("ClanAllyRemove.webhook-thumbnail", "https://minotar.net/avatar/{player}/256");
            discordConfig.set("ClanAllyRemove.webhook-color", "e74c3c");
            discordConfig.set("ClanAllyRemove.inline", false);
            discordConfig.set("ClanAllyRemove.timestamp", true);

            Map<String, String> clanAllyRemoveFields = new LinkedHashMap<>();
            clanAllyRemoveFields.put(":shield: **Clan Name**:", "`{clan_name}`");
            clanAllyRemoveFields.put(":broken_heart: **Former Ally**:", "`{ally_name}`");
            clanAllyRemoveFields.put(":crown: **Initiator**:", "`{player}`");
            clanAllyRemoveFields.put(":calendar: **Termination Date**:", "`{termination_date}`");

            for (Map.Entry<String, String> entry : clanAllyRemoveFields.entrySet()) {
                discordConfig.set("ClanAllyRemove.fields." + entry.getKey(), entry.getValue());
            }

            discordConfig.set("ClanAllyRemove.footer.text", "VeldoraClan - Alliances Broken...");
            discordConfig.set("ClanAllyRemove.footer.icon-url", "https://minotar.net/avatar/{player}/256");
            discordConfig.set("ClanAllyRemove.author.name", "{player}");
            discordConfig.set("ClanAllyRemove.author.url", "https://example.com/player/{player}");
            discordConfig.set("ClanAllyRemove.author.icon-url", "https://minotar.net/avatar/{player}/256");

            try {
                discordConfig.save(discordConfigFile);
            } catch (IOException ignored) {
            }
        }
    }

    public void reloadDiscordConfig() {
        File discordConfigFile = new File(plugin.getDataFolder(), "discord.yml");
        if (discordConfigFile.exists()) {
            discordConfig = YamlConfiguration.loadConfiguration(discordConfigFile);
        } else {
            createDiscordConfig();
        }
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    private void loadDiscordConfig() {
        File discordConfigFile = new File(plugin.getDataFolder(), "discord.yml");
        if (discordConfigFile.exists()) {
            discordConfig = YamlConfiguration.loadConfiguration(discordConfigFile);
        }
    }

    public FileConfiguration getDiscordConfig() {
        return discordConfig;
    }
}
