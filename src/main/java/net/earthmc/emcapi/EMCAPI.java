package net.earthmc.emcapi;

import io.javalin.Javalin;
import io.javalin.util.JavalinLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.earthmc.emcapi.manager.EndpointManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.IOException;

public final class EMCAPI extends JavaPlugin {

    public static EMCAPI instance;
    private Javalin javalin;
    private Economy economy;

    @Override
    public void onLoad() {
        JavalinLogger.enabled = getConfig().getBoolean("behaviour.developer_mode");
        JavalinLogger.startupInfo = false;
    }

    @Override
    public void onEnable() {
        instance = this;

        loadConfig();
        setupEconomy();
        initialiseJavalin();

        EndpointManager endpointManager = new EndpointManager(javalin, getConfig(), economy);
        endpointManager.loadEndpoints();

        if (getConfig().getBoolean("behaviour.enable_legacy_endpoints"))
            endpointManager.loadLegacyEndpoints();
    }

    @Override
    public void onDisable() {
        javalin.stop();
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

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) return;

        economy = rsp.getProvider();
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
}
