package PlaceHolderAPIHook;

import dev.eggsv31.veldora.veldoraClan.VeldoraClan;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class ClipPlaceholder extends PlaceholderExpansion {

    private final IClanDataStorage storage;
    private final VeldoraClan plugin;

    private static final long TTL = 5000;

    private long lastCache = 0L;
    private List<Map.Entry<String, Integer>> cachedKills = new ArrayList<>();
    private List<Map.Entry<String, Integer>> cachedDeaths = new ArrayList<>();
    private List<Map.Entry<String, Integer>> cachedMembers = new ArrayList<>();
    private List<Map.Entry<String, Integer>> cachedKdr = new ArrayList<>();

    public ClipPlaceholder(VeldoraClan plugin, IClanDataStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "veldora";
    }

    @Override
    public @NotNull String getAuthor() {
        return "eggsv31";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.3.5";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String id) {
        if (player == null) return "";

        String key = id.toLowerCase();

        if (key.startsWith("clan_top:")) {
            return handleTop(id);
        }
        if (key.startsWith("clan_top_")) {
            return ChatColor.RED + "This placeholder has changed! Use %veldora_clan_top:[kdr/death/member/kill]:[1..100]%." + ChatColor.RESET;
        }
        if (key.startsWith("clan_ally:")) {
            return handleAllyByIndex(key, player);
        }
        if ("clan_allies".equals(key)) {
            return alliesList(player);
        }

        return switch (key) {
            case "clan_creation_date" -> clanCreationDate(player, false);
            case "clan_creation_date_formated" -> clanCreationDate(player, true);
            case "clan_tag" -> clanTag(player, false);
            case "clan_tag_color" -> clanTag(player, true);
            case "clan_leader" -> clanLeader(player);
            case "clan_kill" -> clanKills(player);
            case "clan_deaths" -> clanDeaths(player);
            case "clan_kdr" -> String.format("%.2f", clanKdr(player));
            case "clan_member_count" -> String.valueOf(memberCount(player));
            case "clan_online_count" -> String.valueOf(onlineCount(player));
            case "clan_rank" -> clanRank(player);
            case "clan_limit_members" -> String.valueOf(limitMembers(player));
            case "clan_limit_ally" -> String.valueOf(limitAllies(player));
            case "clan_leader_last_joindate" -> leaderLast(player);
            case "clan_member_joined" -> String.valueOf(joinedTotal(player));
            case "clan_member_leave" -> String.valueOf(leftTotal(player));
            case "clan_member_kicked" -> String.valueOf(kickedTotal(player));
            default -> "";
        };
    }

    private String handleTop(String raw) {
        String[] p = raw.split(":");
        if (p.length < 3) {
            return ChatColor.RED + "Invalid format" + ChatColor.RESET;
        }

        String crit = p[1].toLowerCase();
        String rankPart = p[2].toLowerCase();
        boolean wantName = rankPart.endsWith("_name");
        if (wantName) {
            rankPart = rankPart.replace("_name", "");
        }

        int idx;
        try {
            idx = Integer.parseInt(rankPart);
        } catch (NumberFormatException e) {
            return ChatColor.RED + "Invalid rank" + ChatColor.RESET;
        }

        if (idx < 1 || idx > 100) {
            return ChatColor.RED + "Rank must be between 1 and 100" + ChatColor.RESET;
        }

        refreshCacheIfNeeded();

        List<Map.Entry<String, Integer>> list;
        switch (crit) {
            case "kill" -> list = cachedKills;
            case "death" -> list = cachedDeaths;
            case "member" -> list = cachedMembers;
            case "kdr" -> list = cachedKdr;
            default -> list = null;
        }

        if (list == null) {
            return ChatColor.RED + "Unknown criterion" + ChatColor.RESET;
        }
        if (idx > list.size()) {
            return ChatColor.RED + "No clan at this rank" + ChatColor.RESET;
        }

        String cName = list.get(idx - 1).getKey();
        if (wantName) {
            return cName;
        }
        if ("kdr".equals(crit)) {
            return String.format("%.2f", kdr(cName));
        }
        return String.valueOf(list.get(idx - 1).getValue());
    }

    private String alliesList(Player p) {
        String c = playerClan(p);
        if (c == null) {
            return ChatColor.RED + "N/A" + ChatColor.RESET;
        }

        List<String> all = storage.getClanAllies(c);
        if (all.isEmpty()) {
            return ChatColor.RED + "❌" + ChatColor.RESET;
        }

        int max = plugin.getConfig().getInt("display.allies", 5);
        if (all.size() <= max) {
            return String.join(", ", all);
        }

        List<String> sub = all.subList(0, max);
        return String.join(", ", sub)
                + ChatColor.GRAY
                + String.format(" and %d more", all.size() - max)
                + ChatColor.RESET;
    }

    private String handleAllyByIndex(String key, Player p) {
        String[] parts = key.split(":");
        if (parts.length != 2) {
            return ChatColor.RED + "Invalid format" + ChatColor.RESET;
        }

        int idx;
        try {
            idx = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return ChatColor.RED + "Invalid index" + ChatColor.RESET;
        }

        String c = playerClan(p);
        if (c == null) {
            return ChatColor.RED + "N/A" + ChatColor.RESET;
        }

        List<String> all = storage.getClanAllies(c);
        if (idx < 1 || idx > all.size()) {
            return ChatColor.RED + "❌" + ChatColor.RESET;
        }
        return all.get(idx - 1);
    }

    private void refreshCacheIfNeeded() {
        if (Instant.now().toEpochMilli() - lastCache < TTL) {
            return;
        }

        List<String> clans = storage.getAllClans();
        List<Map.Entry<String, Integer>> k = new ArrayList<>();
        List<Map.Entry<String, Integer>> d = new ArrayList<>();
        List<Map.Entry<String, Integer>> m = new ArrayList<>();
        List<Map.Entry<String, Integer>> kd = new ArrayList<>();

        for (String clan : clans) {
            k.add(new SimpleEntry<>(clan, kills(clan)));
            d.add(new SimpleEntry<>(clan, deaths(clan)));
            m.add(new SimpleEntry<>(clan, storage.getClanMembers(clan).size()));
            kd.add(new SimpleEntry<>(clan, (int)(kdr(clan) * 100_000)));
        }

        Comparator<Map.Entry<String, Integer>> comp = (a, b) -> b.getValue() - a.getValue();
        k.sort(comp);
        d.sort(comp);
        m.sort(comp);
        kd.sort(comp);

        cachedKills = k;
        cachedDeaths = d;
        cachedMembers = m;
        cachedKdr = kd;
        lastCache = Instant.now().toEpochMilli();
    }

    private int kills(String clan) {
        return storage.getClanConfig(clan).getInt("total-kills", 0);
    }

    private int deaths(String clan) {
        return storage.getClanConfig(clan).getInt("total-deaths", 0);
    }

    private double kdr(String clan) {
        int k = kills(clan);
        int d = deaths(clan);
        return d == 0 ? k : (double) k / d;
    }

    private String playerClan(Player p) {
        return storage.getPlayerClan(p);
    }

    private String clanCreationDate(Player p, boolean formatted) {
        String c = playerClan(p);
        if (c == null) return ChatColor.RED + "N/A" + ChatColor.RESET;
        String raw = storage.getClanConfig(c).getString("creation-date");
        if (raw == null) return ChatColor.RED + "N/A" + ChatColor.RESET;
        if (!formatted) return raw;
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat out = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
            return out.format(in.parse(raw));
        } catch (Exception e) {
            return ChatColor.RED + "FORMAT ERROR" + ChatColor.RESET;
        }
    }

    private String clanTag(Player p, boolean onlyColor) {
        String c = playerClan(p);
        if (c == null) {
            return onlyColor ? ChatColor.RED.toString() : ChatColor.RED + "❌" + ChatColor.RESET;
        }
        ChatColor col = storage.getClanColor(c);
        if (col == null) {
            String def = plugin.getConfig().getString("color.default", "&e");
            String txt = ChatColor.translateAlternateColorCodes('&', def);
            return onlyColor ? txt : txt + c + ChatColor.RESET;
        }
        return onlyColor ? col.toString() : col + c + ChatColor.RESET;
    }

    private String clanLeader(Player p) {
        String c = playerClan(p);
        if (c == null) return ChatColor.RED + "❌" + ChatColor.RESET;
        String l = storage.getClanLeader(c);
        return l != null ? l : ChatColor.RED + "N/A" + ChatColor.RESET;
    }

    private String clanKills(Player p) {
        String c = playerClan(p);
        return c == null ? ChatColor.RED + "0" + ChatColor.RESET : String.valueOf(kills(c));
    }

    private String clanDeaths(Player p) {
        String c = playerClan(p);
        return c == null ? ChatColor.RED + "❌" + ChatColor.RESET : String.valueOf(deaths(c));
    }

    private double clanKdr(Player p) {
        String c = playerClan(p);
        return c == null ? 0.0 : kdr(c);
    }

    private int memberCount(Player p) {
        String c = playerClan(p);
        return c == null ? 0 : storage.getClanMembers(c).size();
    }

    private int onlineCount(Player p) {
        String c = playerClan(p);
        return c == null ? 0 : storage.getOnlineClanMembers(c).size();
    }

    private String clanRank(Player p) {
        String c = playerClan(p);
        if (c == null) return "N/A";
        refreshCacheIfNeeded();
        for (int i = 0; i < cachedKills.size(); i++) {
            if (cachedKills.get(i).getKey().equals(c)) {
                return String.valueOf(i + 1);
            }
        }
        return "N/A";
    }

    private int limitMembers(Player p) {
        int def = plugin.getConfig().getInt("limit.default", 10);
        for (int i = 100; i >= 1; i--) {
            if (p.hasPermission("veldora.clan.limit." + i)) {
                return i;
            }
        }
        return def;
    }

    private int limitAllies(Player p) {
        int def = plugin.getConfig().getInt("allies.allyLimit", 5);
        for (int i = 100; i >= 1; i--) {
            if (p.hasPermission("veldora.clan.ally.limit." + i)) {
                return i;
            }
        }
        return def;
    }

    private String leaderLast(Player p) {
        String c = playerClan(p);
        if (c == null) return "";
        long t = storage.getLeaderLastLogin(c);
        if (t == 0) return "N/A";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(t));
    }

    private int joinedTotal(Player p) {
        String c = playerClan(p);
        return c == null ? 0 : storage.getTotalJoined(c);
    }

    private int leftTotal(Player p) {
        String c = playerClan(p);
        return c == null ? 0 : storage.getTotalLeft(c);
    }

    private int kickedTotal(Player p) {
        String c = playerClan(p);
        return c == null ? 0 : storage.getTotalKicked(c);
    }
}
