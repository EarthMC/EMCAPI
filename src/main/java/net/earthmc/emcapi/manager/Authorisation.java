package net.earthmc.emcapi.manager;

import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.object.optout.AuthSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Authorisation {
    public final Map<UUID, AuthSettings> authMap = new ConcurrentHashMap<>();
    private final EMCAPI plugin;

    public Authorisation(final EMCAPI plugin) {
        this.plugin = plugin;
    }

    /**
     * @param owner The owner of the information, the one who authorises others
     * @param type The type to check for
     * @param target The target to be authorised
     * @return Whether the target is authorised for this action
     */
    public boolean authorize(UUID owner, AuthSettings.Type type, UUID target) {
        return authMap.containsKey(owner) && authMap.get(owner).authorize(type, target);
    }

    public void saveAuthSettings(UUID uuid) {
        AuthSettings settings = authMap.get(uuid);
        if (settings == null) {
            return;
        }

        if (!plugin.getDatabase().ready()) {
            plugin.getSLF4JLogger().warn("The database has not been properly configured yet, auth changes will not persist across restarts.");
            return;
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            boolean delete = settings.isRedundant();
            try (Connection connection = plugin.getDatabase().getConnection();
                 PreparedStatement ps = connection.prepareStatement(delete ? "DELETE FROM authorised WHERE uuid = ?"
                     : "INSERT INTO authorised (uuid, shop_sse, shop_query) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "shop_sse = VALUES(shop_sse), " +
                     "shop_query = VALUES(shop_query)"
                 )) {
                ps.setString(1, uuid.toString());
                if (!delete) {
                    ps.setString(2, settings.getStringForType(AuthSettings.Type.SHOP_SSE));
                    ps.setString(3, settings.getStringForType(AuthSettings.Type.SHOP_QUERY));
                }

                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getSLF4JLogger().warn("Failed to update authorisation settings for {}", uuid, e);
            }
        });
    }

    public void loadAuthSettings() {
        try (Connection connection = plugin.getDatabase().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM authorised");
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    Map<AuthSettings.Type, String> map = Map.of(
                        AuthSettings.Type.SHOP_SSE, rs.getString("shop_sse"),
                        AuthSettings.Type.SHOP_QUERY, rs.getString("shop_query")
                    );

                    AuthSettings settings = AuthSettings.parse(map);
                    authMap.put(uuid, settings);
                } catch (IllegalArgumentException e) {
                    plugin.getSLF4JLogger().warn("Invalid uuid format '{}' for value in row of table authorised", rs.getString("uuid"));
                } catch (SQLException e) {
                    plugin.getSLF4JLogger().warn("SQLException while loading authorisation data", e);
                }
            }
        } catch (SQLException e) {
            plugin.getSLF4JLogger().warn("Failed to load authorisation data", e);
        }
    }
}
