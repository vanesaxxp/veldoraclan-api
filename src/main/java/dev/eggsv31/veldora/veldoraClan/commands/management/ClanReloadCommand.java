package dev.eggsv31.veldora.veldoraClan.commands.management;

import dev.eggsv31.veldora.veldoraClan.VeldoraClan;
import dev.eggsv31.veldora.veldoraClan.config.language.GuiLangManager;
import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanReloadCommand {
    private final VeldoraClan plugin;
    private final YamlLangManager langManager;
    private GuiLangManager guiLangManager;
    private String prefix;

    public ClanReloadCommand(JavaPlugin plugin, YamlLangManager langManager, GuiLangManager guiLangManager) {
        this.plugin = (VeldoraClan) plugin;
        this.langManager = langManager;
        this.guiLangManager = guiLangManager;
        this.prefix = getPrefixFromConfig();
    }

    public void handleReload(CommandSender sender) {
        if (sender instanceof Player && !sender.hasPermission("veldora.clan.reload")) {
            sender.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.langManager.getMessage("commands.no-permission", "Bunu yapmaya yetkiniz yok."), this.prefix));
        } else {
            long startTime = System.currentTimeMillis();

            this.plugin.reloadConfig();
            this.plugin.getConfigManager().reloadDiscordConfig();
            this.langManager.reloadLangFile();
            this.prefix = getPrefixFromConfig();
            this.guiLangManager.reloadGuiLang();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            sender.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                    this.langManager.getMessage("commands.config-reloaded", "Config ve dil dosyası yeniden yüklendi.") + " §7(" + duration + "ms)",
                    this.prefix));
        }
    }

    private String getPrefixFromConfig() {
        return this.plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] ");
    }
}