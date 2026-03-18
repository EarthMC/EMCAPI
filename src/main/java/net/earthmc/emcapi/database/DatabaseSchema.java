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
}
