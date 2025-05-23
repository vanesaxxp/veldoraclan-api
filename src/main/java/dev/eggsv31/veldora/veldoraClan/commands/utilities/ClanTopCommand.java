package dev.eggsv31.veldora.veldoraClan.commands.utilities;

import dev.eggsv31.veldora.veldoraClan.managers.clan.TopClansManager;
import org.bukkit.entity.Player;

public class ClanTopCommand {
    private final TopClansManager topClansManager;

    public ClanTopCommand(TopClansManager topClansManager) {
        this.topClansManager = topClansManager;
    }

    public void handleClanTop(Player player) {
        this.topClansManager.showTopClans(player);
    }
}
