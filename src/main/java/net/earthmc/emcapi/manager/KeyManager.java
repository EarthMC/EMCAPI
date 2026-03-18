package net.earthmc.emcapi.manager;

import net.earthmc.emcapi.EMCAPI;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public class KeyManager {
    private static final Random RANDOM = new SecureRandom();
    public static final int MAX_KEY_LENGTH = 256;
    public static final int KEY_BYTES = 128;

    private static final Map<UUID, String> PLAYER_KEY_MAP = new ConcurrentHashMap<>();
    private static final Map<String, UUID> KEY_PLAYER_MAP = new ConcurrentHashMap<>();

    public static void loadApiKeys(final EMCAPI plugin) throws SQLException {
        if (!plugin.getDatabase().ready()) {
            plugin.getSLF4JLogger().warn("Unable to load API keys, database is not ready.");
            return;
        }

        try (final Connection connection = plugin.getDatabase().getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT uuid, api_key FROM api_keys"); final ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final UUID uuid;
                try {
                    uuid = UUID.fromString(rs.getString("uuid"));
                } catch (IllegalArgumentException e) {
                    plugin.getSLF4JLogger().warn("Invalid UUID format '{}' for row in the api_keys table", rs.getString("uuid"));
                    continue;
                }

                final String key = rs.getString("api_key");

                PLAYER_KEY_MAP.put(uuid, key);
                KEY_PLAYER_MAP.put(key, uuid);
            }
        }
    }

    public static @Nullable String getPlayerKey(UUID player) {
        return PLAYER_KEY_MAP.get(player);
    }

    public static String createApiKey(UUID player) {
        byte[] array = new byte[KEY_BYTES];
        RANDOM.nextBytes(array);
        String key = Base64.getUrlEncoder().withoutPadding().encodeToString(array);

        if (key.length() > MAX_KEY_LENGTH) {
            key = key.substring(0, MAX_KEY_LENGTH);
        }

        PLAYER_KEY_MAP.put(player, key);
        KEY_PLAYER_MAP.put(key, player);

        final EMCAPI plugin = EMCAPI.instance;
        if (!plugin.getDatabase().ready()) {
            plugin.getSLF4JLogger().warn("The database has not been properly configured yet, API keys will not persist across restarts.");
            return key;
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            try (final Connection connection = plugin.getDatabase().getConnection(); final PreparedStatement ps = connection.prepareStatement("INSERT INTO api_keys (uuid, api_key) VALUES (?, ?) ON DUPLICATE KEY UPDATE api_key = VALUES(api_key)")) {
                ps.setString(1, player.toString());
                ps.setString(2, key);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getSLF4JLogger().warn("Failed to insert api key for player {} into database", player, e);
            }
        });

        return key;
    }

    public static void deletePlayerKey(UUID player) {
        String key = PLAYER_KEY_MAP.remove(player);
        if (key == null) {
            return;
        }

        KEY_PLAYER_MAP.remove(key);

        final EMCAPI plugin = EMCAPI.instance;
        if (!plugin.getDatabase().ready()) {
            return;
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            try (final Connection connection = plugin.getDatabase().getConnection(); final PreparedStatement ps = connection.prepareStatement("DELETE FROM api_keys WHERE uuid = ? LIMIT 1")) {
                ps.setString(1, player.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getSLF4JLogger().warn("Failed delete api key for player {} from database", player, e);
            }
        });
    }

    @Contract("null -> null")
    public static @Nullable UUID getKeyOwner(@Nullable String key) {
        if (key == null) {
            return null;
        }

        return KEY_PLAYER_MAP.get(key);
    }
}
