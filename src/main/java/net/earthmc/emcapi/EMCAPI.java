package net.earthmc.emcapi;

import com.zaxxer.hikari.HikariConfig;
import io.javalin.Javalin;
import io.javalin.http.TooManyRequestsResponse;
import io.javalin.util.JavalinLogger;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.earthmc.emcapi.database.APIDatabase;
import net.earthmc.emcapi.database.DatabaseSchema;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.manager.EndpointManager;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.manager.OptOut;
import net.earthmc.emcapi.sse.SSEManager;
import net.earthmc.emcapi.sse.listeners.ShopSSEListener;
import net.earthmc.emcapi.sse.listeners.TownySSEListener;
import net.earthmc.emcapi.command.ApiCommand;
import net.earthmc.emcapi.util.CooldownUtil;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public final class EMCAPI extends JavaPlugin {

    public static EMCAPI instance;
    private Javalin javalin;
    private final SSEManager sseManager = new SSEManager(this);
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

        // Load keys & opt out before the webserver starts
        try {
            KeyManager.loadApiKeys(this);
        } catch (SQLException e) {
            getSLF4JLogger().warn("exception while loading API keys: ", e);
        }
        optOut.loadOptOut();

        initialiseJavalin();

        getServer().getPluginManager().registerEvents(new Integrations(), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(ApiCommand.create(this), "Allows you to opt in or out of your information being visible in the API."));

        PluginManager pm = getServer().getPluginManager();
        if (pm.isPluginEnabled("Towny")) {
            pm.registerEvents(new TownySSEListener(sseManager), this);
        }
        if (pm.isPluginEnabled("QuickShop-Hikari")) {
            pm.registerEvents(new ShopSSEListener(sseManager), this);
        }

        getServer().getAsyncScheduler().runAtFixedRate(this, t -> CooldownUtil.refresh(), 5, 5, TimeUnit.MINUTES);
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
        javalin = Javalin.start(config -> {
            config.jetty.modifyServer(this::disableServerVersionHeader);

            config.routes.exception(TooManyRequestsResponse.class, (e, ctx) -> {
                final String retryAfter = e.getDetails().get("retry");
                if (retryAfter != null) {
                    ctx.header("Retry-After", retryAfter);
                }

                ctx.status(HttpStatus.TOO_MANY_REQUESTS_429);
                ctx.result(e.getMessage()); // sending a json object may be nicer in the future
            });

            config.jetty.host = getConfig().getString("networking.host");
            config.jetty.port = getConfig().getInt("networking.port");

            new EndpointManager(this).loadEndpoints(config.routes);
            sseManager.loadSSE(config.routes);
        });
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

    @NotNull
    public Javalin getJavalin() {
        return javalin;
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
