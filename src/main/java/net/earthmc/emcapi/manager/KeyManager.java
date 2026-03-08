package net.earthmc.emcapi.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KeyManager {
    private static final Map<UUID, Long> playerKeyMap = new ConcurrentHashMap<>();
    private static final Map<Long, UUID> keyPlayerMap = new ConcurrentHashMap<>();
    private static final String apiKeyFile = "api_keys.txt";

    public static void loadApiKeys(Path path) throws IOException {
        final Path file = path.resolve(apiKeyFile);
        if (!Files.exists(file)) {
            return;
        }

        Files.readAllLines(file).forEach(result -> {
            try {
                String[] split = result.split(",");
                if (split.length != 2) return;
                UUID player = UUID.fromString(split[0]);
                Long key = Long.parseLong(split[1]);
                playerKeyMap.put(player, key);
                keyPlayerMap.put(key, player);
            } catch (IllegalArgumentException ignored) {}
        });
    }

    public static void saveApiKeys(Path path) throws IOException {
        final List<String> lines = playerKeyMap.entrySet().stream().map(entry -> entry.getKey() + "," + entry.getValue()).toList();

        Files.write(path.resolve(apiKeyFile), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static @Nullable Long getPlayerKey(UUID player) {
        return playerKeyMap.get(player);
    }

    public static @NotNull Long createApiKey(UUID player) {
        SecureRandom random = new SecureRandom();

        Long key = random.nextLong(100_000_000_000L, 1_000_000_000_000L);
        playerKeyMap.put(player, key);
        keyPlayerMap.put(key, player);
        return key;
    }

    public static void deletePlayerKey(UUID player) {
        Long key = playerKeyMap.remove(player);
        if (key != null) {
            keyPlayerMap.remove(key);
        }
    }

    public static UUID getKeyOwner(Long key) {
        if (key == null) return null;
        return keyPlayerMap.get(key);
    }
}
