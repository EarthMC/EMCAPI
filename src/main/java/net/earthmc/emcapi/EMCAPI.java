package net.earthmc.emcapi;

import com.zaxxer.hikari.HikariConfig;
import io.javalin.Javalin;
import io.javalin.util.JavalinLogger;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.earthmc.emcapi.database.APIDatabase;
import net.earthmc.emcapi.database.DatabaseSchema;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.manager.EndpointManager;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.manager.LegacyEndpointManager;
import net.earthmc.emcapi.manager.OptOut;
import net.earthmc.emcapi.sse.SSEManager;
import net.earthmc.emcapi.sse.listeners.ShopSSEListener;
import net.earthmc.emcapi.sse.listeners.TownySSEListener;
import net.earthmc.emcapi.command.ApiCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public final class EMCAPI extends JavaPlugin {

    public static EMCAPI instance;
    private Javalin javalin;
    private Integrations pluginIntegrations;
    private SSEManager sseManager;
    private final APIDatabase database = new APIDatabase();
    private final OptOut optOut = new OptOut(this);

    @Override
    public void onLoad() {
        JavalinLogger.enabled = getConfig().getBoolean("behaviour.developer_mode");
        JavalinLogger.startupInfo = false;
    }

    @Override
    public void onEnable() {
        instance = this;

        loadConfig();
        loadDatabase();
        initialiseJavalin();

        this.pluginIntegrations = new Integrations(this);
        getServer().getPluginManager().registerEvents(this.pluginIntegrations, this);

        if (getConfig().getBoolean("behaviour.load_legacy")) {
            new LegacyEndpointManager(this).loadEndpoints(); // Load retired endpoints and still serve current endpoints at /v3/aurora/
        }
        new EndpointManager(this).loadEndpoints();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(ApiCommand.create(this), "Allows you to opt in or out of your information being visible in the API."));

        optOut.loadOptOut();

        sseManager = new SSEManager(this);
        sseManager.loadSSE();
        PluginManager pm = getServer().getPluginManager();
        if (pm.isPluginEnabled("Towny")) {
            pm.registerEvents(new TownySSEListener(sseManager), this);
        }
        if (pm.isPluginEnabled("QuickShop")) {
            pm.registerEvents(new ShopSSEListener(sseManager), this);
        }

        try {
            KeyManager.loadApiKeys(this);
        } catch (SQLException e) {
            getSLF4JLogger().warn("exception while loading API keys: ", e);
        }
    }

    @Override
    public void onDisable() {
        if (this.sseManager != null) {
            sseManager.shutdown();
        }

        javalin.stop();
        database.close();
    }

    private void initialiseJavalin() {
        javalin = Javalin.create(config -> {
            config.jetty.modifyServer(server -> {
                disableServerVersionHeader(server);

                WebAppContext context = new WebAppContext();
                context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
                context.setErrorHandler(new ErrorHandler());
                context.setWar(EMCAPI.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm());

                server.setHandler(context);
            });
        });

        javalin.start(getConfig().getString("networking.host"), getConfig().getInt("networking.port"));
    }

    private void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
    }

    private void loadDatabase() {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + getConfig().getString("database.host") + ":" + getConfig().getString("database.port") + "/" + getConfig().getString("database.name") + getConfig().getString("database.flags"));
        final String username = getConfig().getString("database.username");
        final String password = getConfig().getString("database.password");

        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(Math.min(1, getConfig().getInt("database.max-pool-size", 1)));
        config.setMinimumIdle(Math.min(0, getConfig().getInt("database.min-pool-size", 0)));
        config.setPoolName("EMCAPI");

        try {
            database.start(config);

            try (final Connection connection = database.getConnection()) {
                DatabaseSchema.createTables(connection);
            } catch (SQLException e) {
                getSLF4JLogger().warn("Failed to create default tables", e);
            }
        } catch (SQLException e) {
            if (!"root".equals(username) || !"".equals(password)) {
                getSLF4JLogger().warn("Failed to start datasource", e);
            }
        } catch (ReflectiveOperationException e) {
            getSLF4JLogger().warn("Failed to find embedded sql driver", e);
        }
    }

    private void disableServerVersionHeader(final Server server) {
        for (Connector conn : server.getConnectors()) {
            conn.getConnectionFactories().stream()
                    .filter(cf -> cf instanceof HttpConnectionFactory)
                    .forEach(cf -> ((HttpConnectionFactory) cf)
                            .getHttpConfiguration().setSendServerVersion(false));
        }
    }

    private static class ErrorHandler extends ErrorPageErrorHandler {

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.getWriter()
                    .append("{\"status\":\"ERROR\",\"message\":\"HTTP ")
                    .append(String.valueOf(response.getStatus()))
                    .append("\"}");
        }
    }

    @NotNull
    public Javalin getJavalin() {
        return javalin;
    }

    public Integrations integrations() {
        return this.pluginIntegrations;
    }

    public String getURLPath() {
        String version = getConfig().getString("networking.api_version", "3");
        return "v" + version;
    }

    public APIDatabase getDatabase() {
        return database;
    }

    public OptOut getOptOut() {
        return optOut;
    }
}
