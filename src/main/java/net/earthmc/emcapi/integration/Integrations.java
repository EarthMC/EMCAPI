package net.earthmc.emcapi.integration;

import net.earthmc.emcapi.EMCAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Integrations implements Listener {
    private static final Map<String, Integration> INTEGRATIONS = new ConcurrentHashMap<>();
    private static final Map<String, Integration> PLUGIN_INTEGRATION_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Integration> T getIntegration(String name) {
        return (T) INTEGRATIONS.get(name);
    }

    public static void addIntegration(String identifier, Integration integration) {
        INTEGRATIONS.put(identifier, integration);
        PLUGIN_INTEGRATION_MAP.put(integration.name(), integration);

        integration.setEnabled(EMCAPI.instance.getServer().getPluginManager().isPluginEnabled(integration.name()));
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent event) {
        final Integration integration = PLUGIN_INTEGRATION_MAP.get(event.getPlugin().getName());
        if (integration != null) {
            integration.setEnabled(true);
        }
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent event) {
        final Integration integration = PLUGIN_INTEGRATION_MAP.get(event.getPlugin().getName());
        if (integration != null) {
            integration.setEnabled(false);
        }
    }
}
