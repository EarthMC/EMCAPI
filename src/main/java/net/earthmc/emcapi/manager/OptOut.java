package net.earthmc.emcapi.manager;

import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.object.optout.OptOutSettings;
import net.earthmc.emcapi.object.optout.OptOutType;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OptOut {
    public final Map<UUID, OptOutSettings> opted = new ConcurrentHashMap<>();
    private final EMCAPI plugin;

    public OptOut(final EMCAPI plugin) {
        this.plugin = plugin;
    }

    public boolean playerOptedOut(UUID uuid, OptOutType type) {
        return opted.containsKey(uuid) && opted.get(uuid).optedOut(type);
    }

    public @Nullable OptOutSettings getPlayerSettings(UUID uuid) {
        return opted.get(uuid);
    }

    public void saveOptOut(UUID uuid) {
        OptOutSettings settings = opted.get(uuid);
        if (settings == null) {
            return;
        }

        if (!plugin.getDatabase().ready()) {
            plugin.getSLF4JLogger().warn("The database has not been properly configured yet, opt out changes will not persist across restarts.");
            return;
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            boolean delete = settings.isRedundant();
            try (Connection connection = plugin.getDatabase().getConnection();
                 PreparedStatement ps = connection.prepareStatement(delete ? "DELETE FROM opt_out WHERE uuid = ?"
                : "INSERT INTO opt_out (uuid, override_all, towny_resident, online_status, quickshops, mcmmo_stats) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "override_all = VALUES(override_all), " +
                     "towny_resident = VALUES(towny_resident), " +
                     "online_status = VALUES(online_status), " +
                     "quickshops = VALUES(quickshops), " +
                     "mcmmo_stats = VALUES(mcmmo_stats)"
            )) {
                ps.setString(1, uuid.toString());
                if (!delete) {
                    ps.setBoolean(2, settings.override());
                    ps.setBoolean(3, settings.townyResident());
                    ps.setBoolean(4, settings.onlineStatus());
                    ps.setBoolean(5, settings.quickShops());
                    ps.setBoolean(6, settings.mcmmo());
                }

                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getSLF4JLogger().warn("Failed to update opt out status for {}", uuid, e);
            }
        });
    }

    public void loadOptOut() {
        try (final Connection connection = plugin.getDatabase().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM opt_out");
             final ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    OptOutSettings settings = new OptOutSettings(rs.getBoolean("override_all"), rs.getBoolean("towny_resident"), rs.getBoolean("online_status"), rs.getBoolean("quickshops"), rs.getBoolean("mcmmo_stats"));
                    opted.put(uuid, settings);
                } catch (IllegalArgumentException e) {
                    plugin.getSLF4JLogger().warn("Invalid uuid format '{}' for value in row of table opt_out", rs.getString("uuid"));
                } catch (SQLException e) {
                    plugin.getSLF4JLogger().warn("SQLException while loading opt out", e);
                }
            }
        } catch (SQLException e) {
            plugin.getSLF4JLogger().warn("Failed to load opted out players", e);
        }
    }
}
