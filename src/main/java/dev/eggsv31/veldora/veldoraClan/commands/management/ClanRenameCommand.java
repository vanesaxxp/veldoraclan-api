package dev.eggsv31.veldora.veldoraClan.commands.management;

import net.milkbowl.vault.economy.Economy;
import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanRenameCommand {
    private final IClanDataStorage IClanDataStorage;
    private final YamlLangManager langManager;
    private final Economy economy;
    private final JavaPlugin plugin;

    public ClanRenameCommand(IClanDataStorage IClanDataStorage, YamlLangManager langManager, Economy economy, JavaPlugin plugin) {
        this.IClanDataStorage = IClanDataStorage;
        this.langManager = langManager;
        this.economy = economy;
        this.plugin = plugin;
    }

    public void handleClanRename(Player player, String newName) {
        String prefix = getPrefix();

        String currentClan = IClanDataStorage.getPlayerClan(player);
        if (currentClan == null) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan", "&cBir klana sahip değilsiniz."),
                    prefix
            ));
            return;
        }

        if (newName.isEmpty() || newName.isBlank()) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.no-clan-name", "&cLütfen geçerli bir isim girin."),
                    prefix
            ));
            return;
        }

        int maxClanNameLength = plugin.getConfig().getInt("Clan.name.maxLength", 16);
        int minClanNameLength = plugin.getConfig().getInt("Clan.name.minLength", 2);
        if (newName.length() > maxClanNameLength) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.clan-name-too-long", "Klan ismi çok uzun! Maksimum uzunluk: %maxlength%")
                            .replace("%maxlength%", String.valueOf(maxClanNameLength)),
                    prefix
            ));
            return;
        }
        if (newName.length() < minClanNameLength) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.clan-name-too-short", "Klan ismi çok kısa! Minimum uzunluk: %minlength%")
                            .replace("%minlength%", String.valueOf(minClanNameLength)),
                    prefix
            ));
            return;
        }

        if (IClanDataStorage.clanExists(newName)) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.clan-exists", "&cBu isimde bir klan zaten mevcut."),
                    prefix
            ));
            return;
        }

        int renameCooldownHours = plugin.getConfig().getInt("rename.cooldown", 24);

        long lastRenameTime = IClanDataStorage.getLastRenameTime(currentClan);
        long currentTime = System.currentTimeMillis();
        long cooldownMillis = renameCooldownHours * 3600 * 1000L;

        if ((currentTime - lastRenameTime) < cooldownMillis) {
            long remainingTime = cooldownMillis - (currentTime - lastRenameTime);
            long hours = remainingTime / (3600 * 1000);
            long minutes = (remainingTime % (3600 * 1000)) / (60 * 1000);

            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    langManager.getMessage("commands.rename-cooldown", "%prefix% &7Wait &e%hours% &7hours and &e%minutes% &7minutes to change clan name again.")
                            .replace("%prefix%", prefix)
                            .replace("%hours%", String.valueOf(hours))
                            .replace("%minutes%", String.valueOf(minutes)),
                    prefix
            ));
            return;
        }

        boolean vaultEnabled = plugin.getConfig().getBoolean("VaultSupport.enabled", true);
        double renamePrice = plugin.getConfig().getDouble("VaultSupport.rename", 100.0);

        if (vaultEnabled) {
            double balance = economy.getBalance(player);
            if (balance < renamePrice) {
                player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                        langManager.getMessage("commands.Vault.RenamePriceFailed", "&cKlan ismini değiştirmek için yeterli paranız yok! Gerekli miktar: &b%amount%")
                                .replace("%amount%", String.valueOf(renamePrice)),
                        prefix
                ));
                return;
            }
        }

        IClanDataStorage.renameClan(currentClan, newName);

        if (vaultEnabled) {
            economy.withdrawPlayer(player, renamePrice);
        }

        long newRenameTime = System.currentTimeMillis();
        IClanDataStorage.setLastRenameTime(currentClan, newRenameTime);

        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("commands.rename-success", "&aKlan ismi başarıyla değiştirildi: &b%newName%")
                        .replace("%newName%", newName),
                prefix
        ));
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}
