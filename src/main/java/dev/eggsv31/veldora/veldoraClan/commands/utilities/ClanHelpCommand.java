package dev.eggsv31.veldora.veldoraClan.commands.utilities;

import dev.eggsv31.veldora.veldoraClan.config.language.YamlLangManager;
import dev.eggsv31.veldora.veldoraClan.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanHelpCommand {
    private final JavaPlugin plugin;
    private final YamlLangManager langManager;

    public ClanHelpCommand(JavaPlugin plugin, YamlLangManager langManager) {
        this.plugin = plugin;
        this.langManager = langManager;
    }

    public void showClanHelp(Player player, int page) {
        switch (page) {
            case 1 -> showFirstPage(player);
            case 2 -> showSecondPage(player);
            case 3 -> showThirdPage(player);
            default -> sendInvalidPageMessage(player);
        }
    }

    private void showFirstPage(Player player) {
        sendPageHeader(player, 1, 3);

        for (int i = 1; i <= 9; i++) {
            sendCommandMessage(player, i);
        }

        sendNavigationButtons(player, 1, true, false);
    }

    private void showSecondPage(Player player) {
        sendPageHeader(player, 2, 3);

        for (int i = 10; i <= 18; i++) {
            sendCommandMessage(player, i);
        }

        sendNavigationButtons(player, 2, true, true);
    }

    private void showThirdPage(Player player) {
        sendPageHeader(player, 3, 3);

        for (int i = 19; i <= 23; i++) {
            sendCommandMessage(player, i);
        }

        sendNavigationButtons(player, 3, false, true);
    }

    private void sendPageHeader(Player player, int currentPage, int totalPages) {
        String topBorderTemplate = langManager.getMessage("clan-help.border", "");
        String topBorder = ChatUtils.formatMessage(topBorderTemplate);

        if (!topBorder.isEmpty() && !topBorder.equalsIgnoreCase("&r")) {
            player.sendMessage(topBorder);
            player.sendMessage("");
        }

        String headerTemplate = langManager.getMessage("clan-help.header", "&bClan Help: &7(%page%/%total%)");
        String header = ChatUtils.formatMessage(headerTemplate
                .replace("%page%", String.valueOf(currentPage))
                .replace("%total%", String.valueOf(totalPages)));

        player.sendMessage(header);
        player.sendMessage(ChatUtils.formatMessage(langManager.getMessage("clan-help.description", "   &a&l[!] &7Discover your commands!")));
        player.sendMessage("");
    }

    private void sendCommandMessage(Player player, int commandIndex) {
        String command = langManager.getMessage("clan-help.commands." + commandIndex, "");
        if (!command.isEmpty()) {
            player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(command, getPrefix()));
        }
    }

    private void sendNavigationButtons(Player player, int currentPage, boolean hasNext, boolean hasPrevious) {
        Component navigationComponent = Component.text("");

        if (hasPrevious) {
            String previousButtonTextTemplate = langManager.getMessage("clan-help.footer.previous", "&b« Back");
            String previousButtonText = ChatUtils.formatMessage(previousButtonTextTemplate.replace("%page%", String.valueOf(currentPage - 1)));
            Component previousButton = Component.text(previousButtonText)
                    .color(NamedTextColor.AQUA)
                    .decorate(TextDecoration.BOLD)
                    .hoverEvent(HoverEvent.showText(Component.text("Go to previous page").color(NamedTextColor.GREEN)))
                    .clickEvent(ClickEvent.runCommand("/clan help " + (currentPage - 1)));
            navigationComponent = navigationComponent.append(previousButton);
        }

        if (hasNext) {
            if (hasPrevious) {
                navigationComponent = navigationComponent.append(Component.text(" | ").color(NamedTextColor.GRAY));
            }
            String nextButtonTextTemplate = langManager.getMessage("clan-help.footer.next", "&bNext »");
            String nextButtonText = ChatUtils.formatMessage(nextButtonTextTemplate.replace("%page%", String.valueOf(currentPage + 1)));
            Component nextButton = Component.text(nextButtonText)
                    .color(NamedTextColor.AQUA)
                    .decorate(TextDecoration.BOLD)
                    .hoverEvent(HoverEvent.showText(Component.text("Next page").color(NamedTextColor.GREEN)))
                    .clickEvent(ClickEvent.runCommand("/clan help " + (currentPage + 1)));
            navigationComponent = navigationComponent.append(nextButton);
        }

        player.sendMessage("");
        player.sendMessage(navigationComponent);

        String topBorderTemplate = langManager.getMessage("clan-help.border", "");
        String topBorder = ChatUtils.formatMessage(topBorderTemplate);
        if (!topBorder.isEmpty() && !topBorder.equalsIgnoreCase("&r")) {
            player.sendMessage("");
            player.sendMessage(topBorder);
        }
    }

    private void sendInvalidPageMessage(Player player) {
        player.sendMessage(ChatUtils.formatMessageWithOptionalPrefix(
                langManager.getMessage("clan-help.invalid-page", "&cInvalid page number!"), getPrefix()));
    }

    private String getPrefix() {
        return ChatUtils.formatMessage(this.plugin.getConfig().getString("prefix", "&7[&bVeldoraClan&7] "));
    }
}
