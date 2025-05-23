package dev.eggsv31.veldora.veldoraClan.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PendingLinkStore {

    public static final Map<String, UUID> pendingCodes = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> lastRequestTimestamps = new ConcurrentHashMap<>();
}
