package net.earthmc.emcapi.util;

import io.javalin.http.TooManyRequestsResponse;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownUtil {
    private static final Map<String, Map<String, Long>> COOLDOWNS = new ConcurrentHashMap<>();

    public static void refresh() {
        long now = Instant.now().getEpochSecond();
        COOLDOWNS.values().forEach((map) -> map.entrySet().removeIf(entry -> entry.getValue() < now));
    }

    public static void checkAndAddCooldownOrThrow(String type, String id, long seconds) {
        long now = Instant.now().getEpochSecond();
        Map<String, Long> cooldowns = COOLDOWNS.get(type);
        if (cooldowns != null && cooldowns.containsKey(id)) {
            long timeUntilNext = cooldowns.get(id) - now;
            if (timeUntilNext > 0) {
                throw new TooManyRequestsResponse("Too Many Requests. Try again in " + timeUntilNext + " second(s)", Map.of("retry", String.valueOf(timeUntilNext)));
            }
        }
        COOLDOWNS.computeIfAbsent(type, k -> new ConcurrentHashMap<>()).put(id, now + seconds);
    }
}
