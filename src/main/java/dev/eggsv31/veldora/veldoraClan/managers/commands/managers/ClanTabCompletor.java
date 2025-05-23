package dev.eggsv31.veldora.veldoraClan.managers.commands.managers;

import dev.eggsv31.veldora.veldoraClan.commands.members.ClanInviteCommand;
import dev.eggsv31.veldora.veldoraClan.commands.Ally.AllyAddCommand;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class ClanTabCompletor implements TabCompleter {

    private final IClanDataStorage IClanDataStorage;
    private final JavaPlugin plugin;
    private final ClanInviteCommand clanInviteCommand;
    private final AllyAddCommand allyAddCommand;

    public ClanTabCompletor(IClanDataStorage IClanDataStorage, JavaPlugin plugin, ClanInviteCommand clanInviteCommand, AllyAddCommand allyAddCommand) {
        this.IClanDataStorage = IClanDataStorage;
        this.plugin = plugin;
        this.clanInviteCommand = clanInviteCommand;
        this.allyAddCommand = allyAddCommand;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        if (!command.getName().equalsIgnoreCase("clan")) {
            return Collections.emptyList();
        }
        boolean isPvpToggleEnabled = plugin.getConfig().getBoolean("Clan.name.pvp_toggle", true);
        boolean isClanChatEnabled = plugin.getConfig().getBoolean("Clan.chat.enabled", true);
        boolean isAlliesEnabled = plugin.getConfig().getBoolean("allies.enabled", true);
        boolean isAllyChatEnabled = plugin.getConfig().getBoolean("allies.allyChat", true);
        boolean isChestEnabled = plugin.getConfig().getBoolean("Chest.enabled", true);
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList(
                    "create", "cancel", "invite", "accept", "deny", "leave", "kick",
                    "delete", "top", "list", "set", "transfer", "help", "stats", "reload", "ban", "unban", "promote", "demote"
            ));
            if (isPvpToggleEnabled) subCommands.add("pvp");
            if (isClanChatEnabled) subCommands.add("chat");
            if (isAlliesEnabled) subCommands.add("ally");
            if (isChestEnabled) subCommands.add("chest");
            return filterStartingWith(subCommands, args[0].toLowerCase());
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            ArrayList<String> suggestions = new ArrayList<>();
            String currentArg = args[1].toLowerCase();
            if (subCommand.equals("ally") && (currentArg.equals("accept") || currentArg.equals("deny"))) {
                Map<Player, String> pending = allyAddCommand.getAllyInvitePending();
                if (pending.containsKey(player)) {
                    suggestions.add(pending.get(player));
                }
                return filterStartingWith(suggestions, currentArg);
            }
            switch (subCommand) {
                case "accept", "deny" -> suggestions.addAll(clanInviteCommand.getPendingInvites(player));
                case "set" -> {
                    suggestions.add("color");
                    suggestions.add("rename");
                }
                case "invite" -> {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (!onlinePlayer.equals(player)) {
                            String playerClan = IClanDataStorage.getPlayerClan(player);
                            if (playerClan == null) return Collections.emptyList();
                            if (!IClanDataStorage.getClanMembers(playerClan).contains(onlinePlayer.getUniqueId().toString())
                                    && !IClanDataStorage.isClanLeader(onlinePlayer)) {
                                if (onlinePlayer.getName().toLowerCase().startsWith(currentArg)) {
                                    suggestions.add(onlinePlayer.getName());
                                }
                            }
                        }
                    }
                }
                case "kick" -> {
                    String clanName = IClanDataStorage.getPlayerClan(player);
                    if (clanName != null) {
                        List<String> clanMembers = IClanDataStorage.getClanMembers(clanName);
                        for (String memberUUID : clanMembers) {
                            OfflinePlayer clanMember = Bukkit.getOfflinePlayer(UUID.fromString(memberUUID));
                            if (clanMember.getName() != null && clanMember.getName().toLowerCase().startsWith(currentArg)) {
                                suggestions.add(clanMember.getName());
                            }
                        }
                    }
                }
                case "ally" -> {
                    suggestions.addAll(Arrays.asList("add", "remove", "accept", "deny"));
                    if (isAllyChatEnabled) suggestions.add("chat");
                }
                case "stats" -> {
                    List<String> allClans = IClanDataStorage.getAllClans();
                    String playerClan = IClanDataStorage.getPlayerClan(player);
                    if (playerClan != null) allClans.remove(playerClan);
                    suggestions.addAll(allClans);
                }
                case "ban" -> {
                    String clanName = IClanDataStorage.getPlayerClan(player);
                    if (clanName != null) {
                        FileConfiguration cfg = IClanDataStorage.getClanConfig(clanName);
                        List<String> clanMembers = cfg.getStringList("clan-members");
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> bannedRecords = (List<Map<String, Object>>) cfg.get("banned");
                        List<String> activeBanned = new ArrayList<>();
                        if (bannedRecords != null) {
                            for (Map<String, Object> record : bannedRecords) {
                                if (Boolean.TRUE.equals(record.get("active"))) {
                                    activeBanned.add((String) record.get("uuid"));
                                }
                            }
                        }
                        boolean isLeader = IClanDataStorage.isClanLeader(player);
                        for (String memberUUID : clanMembers) {
                            if (isLeader && memberUUID.equals(player.getUniqueId().toString())) continue;
                            OfflinePlayer clanMember = Bukkit.getOfflinePlayer(UUID.fromString(memberUUID));
                            if (clanMember.isOnline() && !activeBanned.contains(memberUUID)) {
                                String name = clanMember.getName();
                                if (name != null && name.toLowerCase().startsWith(currentArg)) {
                                    suggestions.add(name);
                                }
                            }
                        }
                    }
                }
                case "promote" -> {
                    String clan = IClanDataStorage.getPlayerClan(player);
                    if (clan == null) return Collections.emptyList();
                    return IClanDataStorage.getClanMembers(clan).stream()
                            .map(UUID::fromString)
                            .map(Bukkit::getOfflinePlayer)
                            .filter(op -> {
                                UUID u = op.getUniqueId();
                                boolean isLeader = IClanDataStorage.getClanStatsManager().isFounder(clan, u);
                                boolean isMod    = IClanDataStorage.getClanStatsManager().isModerator(clan, u);
                                String name      = op.getName();
                                return name != null && !isLeader && !isMod && name.toLowerCase().startsWith(currentArg);
                            })
                            .map(OfflinePlayer::getName)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
                case "demote" -> {
                    String clan = IClanDataStorage.getPlayerClan(player);
                    if (clan == null) return Collections.emptyList();
                    return IClanDataStorage.getClanStatsManager().getModerators(clan).stream()
                            .map(Bukkit::getOfflinePlayer)
                            .map(OfflinePlayer::getName)
                            .filter(Objects::nonNull)
                            .filter(n -> n.toLowerCase().startsWith(currentArg))
                            .collect(Collectors.toList());
                }
                case "unban" -> {
                    String clanName = IClanDataStorage.getPlayerClan(player);
                    if (clanName != null) {
                        FileConfiguration cfg = IClanDataStorage.getClanConfig(clanName);
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> bannedRecords = (List<Map<String, Object>>) cfg.get("banned");
                        if (bannedRecords != null) {
                            for (Map<String, Object> record : bannedRecords) {
                                if (Boolean.TRUE.equals(record.get("active"))) {
                                    String uuidString = (String) record.get("uuid");
                                    OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuidString));
                                    String name = bannedPlayer.getName();
                                    if (name != null && name.toLowerCase().startsWith(currentArg)) {
                                        suggestions.add(name);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return filterStartingWith(suggestions, currentArg);
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("ally") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                if (!isAlliesEnabled) return Collections.emptyList();
                String currentArg = args[2].toLowerCase();
                List<String> clanNames = args[1].equalsIgnoreCase("add")
                        ? IClanDataStorage.getAllClans()
                        : IClanDataStorage.getClanAllies(IClanDataStorage.getPlayerClan(player));
                clanNames.remove(IClanDataStorage.getPlayerClan(player));
                return filterStartingWith(clanNames, currentArg);
            }
        }
        return Collections.emptyList();
    }

    private List<String> filterStartingWith(List<String> list, String prefix) {
        List<String> completions = new ArrayList<>();
        for (String str : list) {
            if (str.toLowerCase().startsWith(prefix)) {
                completions.add(str);
            }
        }
        return completions;
    }
}
