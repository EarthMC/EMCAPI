package net.earthmc.emcapi.manager;

import net.earthmc.emcapi.EMCAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OptOut {
    private static final String LEGACY_OPT_OUT_FILE = "opt-out.txt";
    private final Set<UUID> optedOut = ConcurrentHashMap.newKeySet();
    private final EMCAPI plugin;

    public OptOut(final EMCAPI plugin) {
        this.plugin = plugin;
    }

    public boolean playerOptedOut(UUID uuid) {
        return optedOut.contains(uuid);
    }

    public void setOptedOut(UUID uuid, boolean optedOut) {
        boolean modified;

        if (optedOut) {
            modified = this.optedOut.add(uuid);
        } else {
            modified = this.optedOut.remove(uuid);
        }

        if (!modified) {
            return;
        }

        if (!plugin.getDatabase().ready()) {
            plugin.getSLF4JLogger().warn("The database has not been properly configured yet, opt out status will not persist across restarts.");
            return;
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            try (final Connection connection = plugin.getDatabase().getConnection(); final PreparedStatement ps = connection.prepareStatement(optedOut
                ? "INSERT IGNORE INTO opt_out (uuid) VALUES (?)"
                : "DELETE FROM opt_out WHERE uuid = ?"
            )) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getSLF4JLogger().warn("Failed to update opt out status to {} for {}", optedOut, uuid, e);
            }
        });
    }

    public void loadOptOut() {
        loadLegacyOptOuts();

        try (final Connection connection = plugin.getDatabase().getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM opt_out"); final ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final UUID uuid;
                try {
                    uuid = UUID.fromString(rs.getString("uuid"));
                } catch (IllegalArgumentException e) {
                    plugin.getSLF4JLogger().warn("Invalid uuid format '{}' for value in row of table opt_out", rs.getString("uuid"));
                    continue;
                }

                optedOut.add(uuid);
            }
        } catch (SQLException e) {
            plugin.getSLF4JLogger().warn("Failed to load opted out players", e);
        }
    }

    private void loadLegacyOptOuts() {
        final Path file = plugin.getDataPath().resolve(LEGACY_OPT_OUT_FILE);
        if (!Files.exists(file)) {
            return;
        }

        if (!plugin.getDatabase().ready()) {
            plugin.getSLF4JLogger().warn("Found an existing {} to migrate, but the database is not configured properly yet.", LEGACY_OPT_OUT_FILE);
            return;
        }

        try {
            Files.readAllLines(file).forEach(playerStr -> {
                try {
                    optedOut.add(UUID.fromString(playerStr));
                } catch (IllegalArgumentException ignored) {}
            });
        } catch (IOException e) {
            plugin.getSLF4JLogger().error("Failed to migrate legacy {} file", LEGACY_OPT_OUT_FILE, e);
            return;
        }

        try (final Connection conn = plugin.getDatabase().getConnection(); final PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO opt_out (uuid) VALUES(?)")) {
            for (final UUID uuid : optedOut) {
                ps.setString(1, uuid.toString());
                ps.addBatch();
            }

            ps.executeBatch();
        } catch (SQLException e) {
            plugin.getSLF4JLogger().warn("Failed to insert legacy opt outs", e);
            return;
        }

        try {
            Files.delete(file);
        } catch (IOException e) {
            plugin.getSLF4JLogger().warn("Failed to delete {} after successful migration", LEGACY_OPT_OUT_FILE, e);
        }
    }
}
