package dev.eggsv31.veldora.veldoraClan.managers.commands;

import dev.eggsv31.veldora.veldoraClan.commands.Ally.AllyAcceptCommand;
import dev.eggsv31.veldora.veldoraClan.commands.Ally.AllyAddCommand;
import dev.eggsv31.veldora.veldoraClan.commands.Ally.AllyChatCommand;
import dev.eggsv31.veldora.veldoraClan.commands.Ally.AllyDenyCommand;
import dev.eggsv31.veldora.veldoraClan.commands.Ally.AllyRemoveCommand;
import dev.eggsv31.veldora.veldoraClan.commands.management.ClanBanCommand;
import dev.eggsv31.veldora.veldoraClan.commands.management.ClanCreateCommand;
import dev.eggsv31.veldora.veldoraClan.commands.management.ClanDeleteCommand;
import dev.eggsv31.veldora.veldoraClan.commands.management.ClanKickCommand;
import dev.eggsv31.veldora.veldoraClan.commands.management.ClanPvpCommand;
import dev.eggsv31.veldora.veldoraClan.commands.management.ClanReloadCommand;
import dev.eggsv31.veldora.veldoraClan.commands.management.ClanRenameCommand;
import dev.eggsv31.veldora.veldoraClan.commands.management.ClanTransferCommand;
import dev.eggsv31.veldora.veldoraClan.commands.management.ClanUnbanCommand;
import dev.eggsv31.veldora.veldoraClan.commands.utilities.ClanHelpCommand;
import dev.eggsv31.veldora.veldoraClan.commands.utilities.ClanListCommand;
import dev.eggsv31.veldora.veldoraClan.commands.utilities.ClanSetColorCommand;
import dev.eggsv31.veldora.veldoraClan.commands.utilities.ClanStatsCommand;
import dev.eggsv31.veldora.veldoraClan.commands.utilities.ClanTopCommand;
import dev.eggsv31.veldora.veldoraClan.commands.members.ClanAcceptCommand;
import dev.eggsv31.veldora.veldoraClan.commands.members.ClanDenyCommand;
import dev.eggsv31.veldora.veldoraClan.commands.members.ClanInviteCommand;
import dev.eggsv31.veldora.veldoraClan.commands.members.ClanLeaveCommand;
import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.events.chat.ClanAllyChatEvent;
import dev.eggsv31.veldora.veldoraClan.events.chat.ClanChatEvent;
import dev.eggsv31.veldora.veldoraClan.events.combat.managers.CombatManager;
import dev.eggsv31.veldora.veldoraClan.managers.ally.AllyManager;
import dev.eggsv31.veldora.veldoraClan.managers.clan.ClanMemberManager;
import dev.eggsv31.veldora.veldoraClan.managers.clan.TopClansManager;
import dev.eggsv31.veldora.veldoraClan.managers.commands.managers.ClanTabCompletor;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.Chest.ClanChest;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import dev.eggsv31.veldora.veldoraClan.commands.Rival.RivalAddCommand;
import dev.eggsv31.veldora.veldoraClan.commands.Rival.RivalRemoveCommand;
import dev.eggsv31.veldora.veldoraClan.commands.Rival.RivalAcceptCommand;
import dev.eggsv31.veldora.veldoraClan.commands.Rival.RivalDenyCommand;
import dev.eggsv31.veldora.veldoraClan.managers.rival.RivalManager;
import dev.eggsv31.veldora.veldoraClan.utils.PendingLinkStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ClanCommandManager implements CommandExecutor, Listener {

    private final AllyManager allyManager;
    private final JavaPlugin plugin;
    private final IClanDataStorage IClanDataStorage;
    private final ClanMemberManager clanMemberManager;
    private CombatManager combatManager;
    private final ClanChest clanChest;
    private final ClanChatEvent clanChat;
    private final YamlLangManager langManager;
    private final Economy economy;
    private final ClanInviteCommand clanInviteCommand;
    private final ClanCreateCommand clanCreateCommand;
    private final ClanRenameCommand clanRenameCommand;
    private final ClanAcceptCommand clanAcceptCommand;
    private final ClanDenyCommand clanDenyCommand;
    private final ClanKickCommand clanKickCommand;
    private final ClanReloadCommand clanReloadCommand;
    private final ClanHelpCommand clanHelpCommand;
    private final ClanLeaveCommand clanLeaveCommand;
    private final ClanDeleteCommand clanDeleteCommand;
    private final ClanListCommand clanListCommand;
    private final ClanStatsCommand clanStatsCommand;
    private final ClanTopCommand clanTopCommand;
    private final AllyAddCommand allyAddCommand;
    private final AllyRemoveCommand allyRemoveCommand;
    private final AllyChatCommand allyChatCommand;
    private final AllyAcceptCommand allyAcceptCommand;
    private final AllyDenyCommand allyDenyCommand;
    private final ClanTransferCommand clanTransferCommand;
    private final ClanSetColorCommand clanSetColorCommand;
    private final ClanBanCommand clanBanCommand;
    private final ClanUnbanCommand clanUnbanCommand;
    private final GuiLangManager guiLangManager;
    private final RivalManager rivalManager;
    private final RivalAddCommand rivalAddCommand;
    private final RivalRemoveCommand rivalRemoveCommand;
    private final RivalAcceptCommand rivalAcceptCommand;
    private final RivalDenyCommand rivalDenyCommand;

    public ClanCommandManager(
            ClanMemberManager clanMemberManager,
            CombatManager combatManager,
            JavaPlugin plugin,
            IClanDataStorage clanDataStorage,
            ClanChest clanChest,
            YamlLangManager langManager,
            Economy economy,
            GuiLangManager guiLangManager
    ) {
        this.clanMemberManager = clanMemberManager;
        this.combatManager     = combatManager;
        this.plugin            = plugin;
        this.IClanDataStorage   = clanDataStorage;
        this.clanChest         = clanChest;
        this.langManager       = langManager;
        this.economy           = economy;
        this.guiLangManager    = guiLangManager;

        this.allyManager = new AllyManager(clanDataStorage);
        this.clanChat    = new ClanChatEvent(plugin, clanDataStorage, langManager);

        TopClansManager topClansManager = new TopClansManager(
                clanDataStorage.getDataStorage(),
                langManager,
                plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ")
        );
        this.clanInviteCommand = new ClanInviteCommand(plugin, IClanDataStorage, langManager);
        this.clanCreateCommand = new ClanCreateCommand(plugin, IClanDataStorage, langManager, economy);
        this.clanRenameCommand = new ClanRenameCommand(IClanDataStorage, langManager, economy, plugin);
        this.clanAcceptCommand = new ClanAcceptCommand(plugin, clanMemberManager, this.clanInviteCommand, langManager);
        this.clanDenyCommand = new ClanDenyCommand(plugin, this.clanInviteCommand, langManager);
        this.clanKickCommand = new ClanKickCommand(plugin, IClanDataStorage, langManager, clanMemberManager);
        this.clanReloadCommand = new ClanReloadCommand(plugin, langManager, guiLangManager);
        this.clanHelpCommand = new ClanHelpCommand(plugin, langManager);
        this.clanLeaveCommand = new ClanLeaveCommand(plugin, IClanDataStorage, langManager, clanMemberManager);
        this.clanDeleteCommand = new ClanDeleteCommand(plugin, IClanDataStorage, langManager, guiLangManager);
        this.clanListCommand = new ClanListCommand(plugin, IClanDataStorage, langManager);
        this.clanStatsCommand = new ClanStatsCommand(plugin, IClanDataStorage, langManager);
        this.clanTopCommand = new ClanTopCommand(topClansManager);
        this.allyAddCommand = new AllyAddCommand(plugin, IClanDataStorage, langManager, allyManager);
        this.allyRemoveCommand = new AllyRemoveCommand(plugin, allyManager, IClanDataStorage, langManager);
        this.allyChatCommand = new AllyChatCommand(plugin, IClanDataStorage, langManager, new ClanAllyChatEvent(plugin, IClanDataStorage, langManager));
        this.allyAcceptCommand = new AllyAcceptCommand(plugin, IClanDataStorage, langManager, this.allyAddCommand.getAllyInvitePending(), allyManager, this.allyAddCommand);
        this.allyDenyCommand = new AllyDenyCommand(plugin, IClanDataStorage, langManager, this.allyAddCommand.getAllyInvitePending(), allyManager, this.allyAddCommand);
        this.clanTransferCommand = new ClanTransferCommand(plugin, IClanDataStorage, langManager);
        this.clanSetColorCommand = new ClanSetColorCommand(plugin, IClanDataStorage, langManager, guiLangManager);
        this.clanBanCommand = new ClanBanCommand(plugin, IClanDataStorage, langManager);
        this.clanUnbanCommand = new ClanUnbanCommand(plugin, IClanDataStorage, langManager);
        this.rivalManager = new RivalManager(IClanDataStorage);
        this.rivalAddCommand = new RivalAddCommand(plugin, IClanDataStorage, langManager, rivalManager);
        this.rivalRemoveCommand = new RivalRemoveCommand(plugin, rivalManager, IClanDataStorage, langManager);
        this.rivalAcceptCommand = new RivalAcceptCommand(plugin, IClanDataStorage, langManager, this.rivalAddCommand.getRivalInvitePending(), rivalManager);
        this.rivalDenyCommand = new RivalDenyCommand(plugin, IClanDataStorage, langManager, this.rivalAddCommand.getRivalInvitePending(), rivalManager);
        plugin.getServer().getPluginManager().registerEvents(this.clanChat, plugin);
        plugin.getCommand("clan").setTabCompleter(new ClanTabCompletor(IClanDataStorage, plugin, clanInviteCommand, allyAddCommand));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {
        if (!command.getName().equalsIgnoreCase("clan")) {
            return false;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessage("general.only-players", "Only players can use this command."));
            return true;
        }
        if (args.length == 0) {
            int page = 1;
            clanHelpCommand.showClanHelp(player, page);
            return true;
        }
        boolean permissionEnabled = plugin.getConfig().getBoolean("Permission", false);
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("veldora.clan.reload") || !permissionEnabled) {
                clanReloadCommand.handleReload(player);
            } else {
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                        langManager.getMessage("commands.no-permission", "Bunu yapmaya yetkiniz yok."),
                        getPrefix()
                ));
            }
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help" -> {
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {}
                }
                clanHelpCommand.showClanHelp(player, page);
            }
            case "list" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.list")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    clanListCommand.handleClanList(player);
                }
            }
            case "stats" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.stats")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    if (args.length < 2) {
                        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                langManager.getMessage("commands.no-clan-name", "Lütfen bir klan adı belirtin."),
                                getPrefix()
                        ));
                    } else {
                        clanStatsCommand.handleClanStats(player, args[1]);
                    }
                }
            }
            case "create" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.create")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    clanCreateCommand.handleClanCreate(player);
                }
            }
            case "invite" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.invite")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    clanInviteCommand.handleClanInvite(player, args);
                }
            }
            case "kick" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.kick")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    clanKickCommand.handleClanKick(player, args, permissionEnabled);
                }
            }
            case "accept" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.accept")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    if (args.length < 2 || args[1].isBlank()) {
                        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                langManager.getMessage("commands.no-clan-specified-accept", "Lütfen kabul etmek için bir klan adı belirtin."),
                                getPrefix()
                        ));
                    } else {
                        clanAcceptCommand.handleClanAccept(player, args[1]);
                    }
                }
            }
            case "deny" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.deny")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    if (args.length < 2 || args[1].isBlank()) {
                        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                langManager.getMessage("commands.no-clan-specified-deny", "Lütfen reddetmek için bir klan adı belirtin."),
                                getPrefix()
                        ));
                    } else {
                        clanDenyCommand.handleClanDeny(player, args[1]);
                    }
                }
            }
            case "leave" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.leave")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    clanLeaveCommand.handleClanLeave(player);
                }
            }
            case "chest" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.chest")) {
                    player.sendMessage(noPermissionMessage());
                } else if (plugin.getConfig().getBoolean("ClanChest", true)) {
                    clanChest.openClanChest(player);
                } else {
                    player.sendMessage(unknownCommandMessage());
                }
            }
            case "pvp" -> {
                boolean isPvpToggleEnabled = plugin.getConfig().getBoolean("clan_pvp_toggle", true);
                if (!isPvpToggleEnabled) {
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                            langManager.getMessage("commands.unknown-command", "Klan PvP sistemi şu anda devre dışı!"),
                            getPrefix()
                    ));
                    return true;
                }
                if (permissionEnabled && !player.hasPermission("veldora.clan.pvp")) {
                    player.sendMessage(noPermissionMessage());
                    return true;
                }
                new ClanPvpCommand(plugin, IClanDataStorage, langManager).handlePvpCommand(player, args);
            }
            case "cancel" -> {
                if (!clanCreateCommand.getActiveClanCreation().containsKey(player) ||
                        !clanCreateCommand.getActiveClanCreation().get(player)) {
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                            langManager.getMessage("commands.no-clan-creation", "Şu anda bir klan oluşturma sürecinde değilsiniz."),
                            getPrefix()
                    ));
                } else {
                    clanCreateCommand.cancelClanCreation(player);
                }
                return true;
            }
            case "set" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                            langManager.getMessage("commands.unknown-command", "Lütfen bir alt komut belirtin: color veya rename."),
                            getPrefix()
                    ));
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "color" -> {
                        if (permissionEnabled && !player.hasPermission("veldora.clan.set.color")) {
                            player.sendMessage(noPermissionMessage());
                        } else {
                            clanSetColorCommand.handleClanSetColor(player);
                        }
                    }
                    case "rename" -> {
                        if (permissionEnabled && !player.hasPermission("veldora.clan.set.rename")) {
                            player.sendMessage(noPermissionMessage());
                        } else {
                            if (args.length < 3 || args[2].isBlank()) {
                                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                        langManager.getMessage("commands.no-clan-name", "Lütfen yeni bir isim belirtin."),
                                        getPrefix()
                                ));
                            } else {
                                clanRenameCommand.handleClanRename(player, args[2]);
                            }
                        }
                    }
                    default -> {
                        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                langManager.getMessage("commands.unknown-command", "Bilinmeyen alt komut. Kullanılabilir: color, rename."),
                                getPrefix()
                        ));
                    }
                }
            }
            case "transfer" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.transfer")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    clanTransferCommand.handleClanTransfer(player, args);
                }
            }
            case "discord-link" -> {
                UUID uuid = player.getUniqueId();

                Long existingDiscordId = IClanDataStorage.getDiscordId(uuid);
                if (existingDiscordId != null) {
                    String alreadyMsg = langManager.getMessage(
                            "commands.discord.link-already",
                            "Zaten Discord ID %discord% ile bağlısınız."
                    ).replace("%discord%", existingDiscordId.toString());

                    player.sendMessage(
                            ChatUtils.formatMessageWithOptionalPrefix(alreadyMsg, getPrefix())
                    );
                    return true;
                }

                long cooldownSec = plugin.getConfig().getLong("discord-link.cooldown", 3600L);
                long now = System.currentTimeMillis();

                Long last = PendingLinkStore.lastRequestTimestamps.get(uuid);
                if (last != null) {
                    long elapsed = now - last;
                    long cooldownMs = cooldownSec * 1000;
                    if (elapsed < cooldownMs) {
                        long remainingMs = cooldownMs - elapsed;
                        java.time.Duration d = java.time.Duration.ofMillis(remainingMs);
                        long hours   = d.toHours();
                        long minutes = d.minusHours(hours).toMinutes();
                        long seconds = d.minusHours(hours).minusMinutes(minutes).getSeconds();
                        String timeStr = String.format("%d:%02d:%02d", hours, minutes, seconds);

                        String msg = langManager.getMessage(
                                "commands.discord.link-cooldown",
                                "Tekrar kod almak için %time% beklemelisiniz!"
                        ).replace("%time%", timeStr);

                        player.sendMessage(
                                ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix())
                        );
                        return true;
                    }
                }

                int codeNum = ThreadLocalRandom.current().nextInt(0, 1_000_000);
                String code = String.format("%06d", codeNum);

                PendingLinkStore.lastRequestTimestamps.put(uuid, now);
                PendingLinkStore.pendingCodes.put(code, uuid);

                String template = langManager.getMessage(
                        "commands.discord.link-code",
                        "Discord’a bağlamak için kodunuz: %code%"
                );
                String formatted = template.replace("%code%", code);
                player.sendMessage(
                        ChatUtils.formatMessageWithOptionalPrefix(formatted, getPrefix())
                );
                return true;
            }

            case "ally" -> {
                if (!plugin.getConfig().getBoolean("allies.enabled", true)) {
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                            langManager.getMessage("commands.unknown-command", "Ally sistemi şu anda devre dışı."),
                            getPrefix()
                    ));
                    return true;
                }
                handleAllyCommands(player, args, permissionEnabled);
            }
            case "delete" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.delete")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    clanDeleteCommand.handleClanDelete(player);
                }
            }
            case "chat" -> {
                if (!plugin.getConfig().getBoolean("ChatFormatEnabled", true)) {
                    player.sendMessage(unknownCommandMessage());
                    return true;
                }
                if (permissionEnabled && !player.hasPermission("veldora.clan.chat")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    clanChat.handleClanChatToggle(player);
                }
            }
            case "ban" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.ban")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    if (args.length == 1) {
                        String clanName = IClanDataStorage.getPlayerClan(player);
                        if (clanName == null) {
                            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                    langManager.getMessage("commands.not-in-clan", "Klanınız yok."), getPrefix()));
                            break;
                        }
                        FileConfiguration cfg = IClanDataStorage.getClanConfig(clanName);
                        List<String> clanMembers = cfg.getStringList("clan-members");
                        List<Map<String, Object>> bannedRecords = (List<Map<String, Object>>) cfg.get("banned");
                        List<String> activeBanned = new ArrayList<>();
                        if (bannedRecords != null) {
                            for (Map<String, Object> record : bannedRecords) {
                                if (Boolean.TRUE.equals(record.get("active"))) {
                                    activeBanned.add((String) record.get("uuid"));
                                }
                            }
                        }
                        List<String> available = new ArrayList<>();
                        for (String memberUUID : clanMembers) {
                            if (!activeBanned.contains(memberUUID)) {
                                Player p = Bukkit.getPlayer(UUID.fromString(memberUUID));
                                if (p != null && p.getName() != null) {
                                    available.add(p.getName());
                                } else {
                                    String name = Bukkit.getOfflinePlayer(UUID.fromString(memberUUID)).getName();
                                    if (name != null) {
                                        available.add(name);
                                    }
                                }
                            }
                        }
                        if (available.isEmpty()) {
                            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                    langManager.getMessage("commands.no-available-to-ban", "Banlanabilecek oyuncu bulunamadı."),
                                    getPrefix()
                            ));
                        }
                    } else {
                        clanBanCommand.handleClanBan(player, args);
                    }
                }
            }
            case "unban" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.unban")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    if (args.length == 1) {
                        String clanName = IClanDataStorage.getPlayerClan(player);
                        if (clanName == null) {
                            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                    langManager.getMessage("commands.not-in-clan", "Klanınız yok."), getPrefix()));
                            break;
                        }
                        FileConfiguration cfg = IClanDataStorage.getClanConfig(clanName);
                        List<Map<String, Object>> bannedRecords = (List<Map<String, Object>>) cfg.get("banned");
                        List<String> activeBannedNames = new ArrayList<>();
                        if (bannedRecords != null) {
                            for (Map<String, Object> record : bannedRecords) {
                                if (Boolean.TRUE.equals(record.get("active"))) {
                                    String uuidString = (String) record.get("uuid");
                                    activeBannedNames.add(uuidString);
                                }
                            }
                        }
                        if (activeBannedNames.isEmpty()) {
                            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                    langManager.getMessage("commands.no-banned", "Banlı oyuncu bulunamadı."), getPrefix()
                            ));
                        }
                    } else {
                        clanUnbanCommand.handleClanUnban(player, args);
                    }
                }
            }
            case "promote" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.promote")) {
                    String msg = langManager.getMessage("commands.no-permission", "You don’t have permission.");
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    String msg = langManager.getMessage("commands.player-not-online", "Player not found.");
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                    return true;
                }

                String clan = IClanDataStorage.getPlayerClan(player);
                if (clan == null) {
                    String msg = langManager.getMessage("commands.player-not-in-clan", "You are not in a clan.");
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                    return true;
                }

                if (!IClanDataStorage.getClanStatsManager().isFounder(clan, player.getUniqueId())) {
                    String msg = langManager.getMessage("commands.not-leader", "Only clan founders can do this.");
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                    return true;
                }

                List<String> members = IClanDataStorage.getClanMembers(clan);
                if (!members.contains(target.getUniqueId().toString())) {
                    String msg = langManager.getMessage("commands.not-your-member", "%player% is not in your clan.")
                            .replace("%player%", target.getName());
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                    return true;
                }

                if (IClanDataStorage.getClanStatsManager().isModerator(clan, target.getUniqueId())) {
                    String msg = langManager.getMessage("commands.already-moderator", "%player% is already a moderator.")
                            .replace("%player%", target.getName());
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                } else {
                    IClanDataStorage.getClanStatsManager().addModerator(clan, target.getUniqueId());

                    String msg1 = langManager.getMessage("commands.promote-success", "%player% has been promoted to moderator.")
                            .replace("%player%", target.getName());
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg1, getPrefix()));

                    String msg2 = langManager.getMessage("commands.promoted-notify", "You have been promoted to moderator of clan “%clan%.”")
                            .replace("%clan%", clan);
                    target.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg2, getPrefix()));
                }
                return true;
            }

            case "demote" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.demote")) {
                    String msg = langManager.getMessage("commands.no-permission", "You don’t have permission.");
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    String msg = langManager.getMessage("commands.player-not-online", "Player not found.");
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                    return true;
                }

                String clan = IClanDataStorage.getPlayerClan(player);
                if (clan == null) {
                    String msg = langManager.getMessage("commands.player-not-in-clan", "You are not in a clan.");
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                    return true;
                }

                if (!IClanDataStorage.getClanStatsManager().isFounder(clan, player.getUniqueId())) {
                    String msg = langManager.getMessage("commands.not-leader", "Only clan founders can do this.");
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                    return true;
                }

                if (!IClanDataStorage.getClanStatsManager().isModerator(clan, target.getUniqueId())) {
                    String msg = langManager.getMessage("commands.not-moderator", "%player% is not a moderator.")
                            .replace("%player%", target.getName());
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg, getPrefix()));
                } else {
                    IClanDataStorage.getClanStatsManager().removeModerator(clan, target.getUniqueId());

                    String msg1 = langManager.getMessage("commands.demote-success", "%player% has been demoted from moderator.")
                            .replace("%player%", target.getName());
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg1, getPrefix()));

                    String msg2 = langManager.getMessage("commands.demoted-notify", "Your moderator role in clan “%clan%” has been revoked.")
                            .replace("%clan%", clan);
                    target.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(msg2, getPrefix()));
                }
                return true;
            }
            case "top" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.topstats")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    clanTopCommand.handleClanTop(player);
                }
            }
            default -> showUnknownCommandTitle(player);
        }
        return true;
    }

    private void handleAllyCommands(Player player, String[] args, boolean permissionEnabled) {
        if (args.length < 2) {
            player.sendMessage(unknownAllyCommandMessage());
            return;
        }
        switch (args[1].toLowerCase()) {
            case "add" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.ally.add")) {
                    player.sendMessage(noPermissionMessage());
                } else if (args.length < 3) {
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                            langManager.getMessage("commands.no-clan-name", "Bir İsim Girin."),
                            getPrefix()
                    ));
                } else {
                    allyAddCommand.handleAllyAdd(player, args[2]);
                }
            }
            case "remove" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.ally.remove")) {
                    player.sendMessage(noPermissionMessage());
                } else if (args.length < 3) {
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                            langManager.getMessage("commands.no-clan-name", "Bir İsim Girin."),
                            getPrefix()
                    ));
                } else {
                    allyRemoveCommand.handleAllyRemove(player, args[2]);
                }
            }
            case "chat" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.ally.chat")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    allyChatCommand.handleAllyChat(player);
                }
            }
            case "accept" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.ally.accept")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    if (args.length < 2 || args[1].isBlank()) {
                        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                langManager.getMessage("commands.no-clan-specified-accept", "Lütfen kabul etmek için bir klan adı belirtin."),
                                getPrefix()
                        ));
                    } else {
                        allyAcceptCommand.handleAllyAccept(player);
                    }
                }
            }
            case "deny" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.ally.deny")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    if (args.length < 2 || args[1].isBlank()) {
                        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                                langManager.getMessage("commands.no-clan-specified-deny", "Lütfen reddetmek için bir klan adı belirtin."),
                                getPrefix()
                        ));
                    } else {
                        allyDenyCommand.handleAllyDeny(player);
                    }
                }
            }
            default -> player.sendMessage(unknownAllyCommandMessage());
        }
    }

    private void handleRivalCommands(Player player, String[] args, boolean permissionEnabled) {
        if (args.length < 2) {
            player.sendMessage(unknownRivalCommandMessage());
            return;
        }
        switch (args[1].toLowerCase()) {
            case "add" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.rival.add")) {
                    player.sendMessage(noPermissionMessage());
                } else if (args.length < 3) {
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                            langManager.getMessage("commands.no-clan-name", "Bir İsim Girin."),
                            getPrefix()
                    ));
                } else {
                    rivalAddCommand.handleRivalAdd(player, args[2]);
                }
            }
            case "remove" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.rival.remove")) {
                    player.sendMessage(noPermissionMessage());
                } else if (args.length < 3) {
                    player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                            langManager.getMessage("commands.no-clan-name", "Bir İsim Girin."),
                            getPrefix()
                    ));
                } else {
                    rivalRemoveCommand.handleRivalRemove(player, args[2]);
                }
            }
            case "accept" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.rival.accept")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    rivalAcceptCommand.handleRivalAccept(player);
                }
            }
            case "deny" -> {
                if (permissionEnabled && !player.hasPermission("veldora.clan.rival.deny")) {
                    player.sendMessage(noPermissionMessage());
                } else {
                    rivalDenyCommand.handleRivalDeny(player);
                }
            }
            default -> player.sendMessage(unknownRivalCommandMessage());
        }
    }

    private String noPermissionMessage() {
        return ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("commands.no-permission", "Bunu yapmaya yetkiniz yok."),
                getPrefix()
        );
    }

    private String unknownCommandMessage() {
        return ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("messages.unknown-command", "Unknown Command."),
                getPrefix()
        );
    }

    private String unknownAllyCommandMessage() {
        return ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("ally.commands.unknown-ally-command", "Bilinmeyen ally komutu."),
                getPrefix()
        );
    }

    private String unknownRivalCommandMessage() {
        return ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("rival.commands.unknown-rival-command", "Bilinmeyen düşman komutu."),
                getPrefix()
        );
    }

    private void showUnknownCommandTitle(Player player) {
        String prefix = getPrefix();
        String title = langManager.getMessage("CHAT.command-unknown-player.title.line", "<red>! <light_purple>/{_clan} {_help}");
        String subtitle = langManager.getMessage("CHAT.command-unknown-player.title.subline", "<yellow>To see commands!");
        title = title.replace("%prefix%", prefix);
        subtitle = subtitle.replace("%prefix%", prefix);
        Component titleComponent = MiniMessage.miniMessage().deserialize(ChatUtils.convertLegacyColorsToMiniMessage(title));
        Component subtitleComponent = MiniMessage.miniMessage().deserialize(ChatUtils.convertLegacyColorsToMiniMessage(subtitle));
        player.showTitle(Title.title(titleComponent, subtitleComponent));
        String actionbar = langManager.getMessage("CHAT.command-unknown-player.actionbar", "<light_purple>/{_clan} {_help} <red>to see available commands.");
        if (!actionbar.isEmpty()) {
            actionbar = actionbar.replace("%prefix%", prefix);
            Component actionbarComponent = MiniMessage.miniMessage().deserialize(ChatUtils.convertLegacyColorsToMiniMessage(actionbar));
            player.sendActionBar(actionbarComponent);
        }
        int lineIndex = 1;
        while (true) {
            String line = langManager.getMessage("CHAT.command-unknown-player.message." + lineIndex, null);
            if (line == null) break;
            line = line.replace("%prefix%", prefix);
            Component messageComponent = MiniMessage.miniMessage().deserialize(ChatUtils.convertLegacyColorsToMiniMessage(line));
            player.sendMessage(messageComponent);
            lineIndex++;
        }
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 6.0f);
    }

    public ClanInviteCommand getClanInviteCommand() {
        return clanInviteCommand;
    }

    public AllyAddCommand getAllyAddCommand() {
        return allyAddCommand;
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
