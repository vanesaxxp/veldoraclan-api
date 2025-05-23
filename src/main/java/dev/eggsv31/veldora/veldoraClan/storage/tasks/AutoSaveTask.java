package dev.eggsv31.veldora.veldoraClan.storage.tasks;

import dev.eggsv31.veldora.veldoraClan.managers.configuration.IClanDataStorage;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoSaveTask extends BukkitRunnable {
    private final IClanDataStorage IClanDataStorage;

    public AutoSaveTask(IClanDataStorage IClanDataStorage) {
        this.IClanDataStorage = IClanDataStorage;
    }

    @Override
    public void run() {
        IClanDataStorage.saveAllClansToDisk();
    }
}
