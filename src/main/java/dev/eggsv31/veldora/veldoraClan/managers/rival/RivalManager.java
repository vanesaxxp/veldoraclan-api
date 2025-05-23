package dev.eggsv31.veldora.veldoraClan.managers.rival;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import org.bukkit.entity.Player;

public class RivalManager {
    private final IClanDataStorage storage;
    public RivalManager(IClanDataStorage storage) {
        this.storage = storage;
    }
    public void sendRivalInvite(String fromClan, String toClan) {
        storage.addRivalInviteLog(fromClan, toClan, System.currentTimeMillis(), "pending");
    }
    public void acceptRivalInvite(String fromClan, String toClan) {
        storage.addRivalInviteLog(fromClan, toClan, System.currentTimeMillis(), "accepted");
    }
    public void denyRivalInvite(String fromClan, String toClan) {
        storage.addRivalInviteLog(fromClan, toClan, System.currentTimeMillis(), "denied");
    }
}
