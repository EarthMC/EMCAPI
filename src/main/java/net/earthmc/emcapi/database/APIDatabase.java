package net.earthmc.emcapi.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class APIDatabase implements Closeable {
    private HikariDataSource dataSource;

    /**
     * Attempts to register drivers and starts the datasource.
     * @param config The config to start the data source with.
     * @throws ReflectiveOperationException If the driver class could not be found/instantiated.
     * @throws SQLException If a connection to the datasource couldn't be established.
     */
    public void start(final HikariConfig config) throws ReflectiveOperationException, SQLException {
        DriverManager.registerDriver((Driver) Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance());

        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Attempts to close the datasource if it's running.
     */
    @Override
    public void close() {
        if (this.dataSource != null) {
            try {
                this.dataSource.close();
            } finally {
                this.dataSource = null;
            }
        }
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public boolean ready() {
        return this.dataSource != null && this.dataSource.isRunning();
    }
}
