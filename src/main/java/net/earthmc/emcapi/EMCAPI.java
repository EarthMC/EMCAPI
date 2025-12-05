package net.earthmc.emcapi;

import io.javalin.Javalin;
import io.javalin.util.JavalinLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.earthmc.emcapi.manager.EndpointManager;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.command.OptOutCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class EMCAPI extends JavaPlugin {

    public static EMCAPI instance;
    private Javalin javalin;

    @Override
    public void onLoad() {
        JavalinLogger.enabled = getConfig().getBoolean("behaviour.developer_mode");
        JavalinLogger.startupInfo = false;
    }

    @Override
    public void onEnable() {
        instance = this;

        loadConfig();
        initialiseJavalin();

        EndpointManager endpointManager = new EndpointManager(this);
        endpointManager.loadEndpoints();

        PluginCommand apiCommand = getCommand("api");
        if (apiCommand == null) {
            getLogger().warning("API command not found.");
        } else {
            OptOutCommand cmd = new OptOutCommand();
            apiCommand.setExecutor(cmd);
            apiCommand.setTabCompleter(cmd);
        }
        try {
            EndpointUtils.loadOptOut(getDataFolder().toPath());
        } catch (IOException e) {
            getLogger().warning("IOException while loading opted-out players: " + e);
        }

        if (getConfig().getBoolean("behaviour.enable_legacy_endpoints"))
            endpointManager.loadLegacyEndpoints();
    }

    @Override
    public void onDisable() {
        javalin.stop();
        try {
            EndpointUtils.saveOptOut(getDataFolder().toPath());
        } catch (IOException e) {
            getLogger().warning("IOException while saving opted-out players: " + e);
        }
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
}
