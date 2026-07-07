package net.earthmc.emcapi.database;

import net.earthmc.emcapi.manager.KeyManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseSchema {
    public static void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createApiKeys());

            for (String column : getApiKeysColumns()) {
                try {
                    statement.executeUpdate("alter table api_keys add column " + column);
                } catch (SQLException ignored) {}
            }

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS opt_out(`uuid` CHAR(36) NOT NULL, PRIMARY KEY (`uuid`))");
            for (String column : getOptOutColumns()) {
                try {
                    statement.executeUpdate("alter table opt_out add column " + column);
                } catch (SQLException ignored) {}
            }
        }
    }

    private static String createApiKeys() {
        return "CREATE TABLE IF NOT EXISTS api_keys(" +
            "`uuid` CHAR(36) NOT NULL," +
            "PRIMARY KEY (`uuid`)" +
            ")";
    }

    private static List<String> getApiKeysColumns() {
        return List.of(
            "`api_key` VARCHAR(" + KeyManager.MAX_KEY_LENGTH + ") NOT NULL"
        );
    }

    private static List<String> getOptOutColumns() {
        return List.of(
            "`override_all` BOOLEAN DEFAULT TRUE", // If true, the player is deemed to have opted out of all the options, without checking each one
            "`towny_resident` BOOLEAN DEFAULT TRUE",
            "`online_status` BOOLEAN DEFAULT TRUE",
            "`quickshops` BOOLEAN DEFAULT TRUE",
            "`mcmmo_stats` BOOLEAN DEFAULT TRUE"
        );
    }
}
