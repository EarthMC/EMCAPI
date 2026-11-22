package net.earthmc.emcapi.map;

import com.palmergames.bukkit.towny.TownySettings;
import io.javalin.config.RoutesConfig;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.SquaremapIntegration;
import net.earthmc.emcapi.map.endpoint.NationRangeEndpoint;
import net.earthmc.emcapi.map.endpoint.StatusEndpoint;
import net.earthmc.emcapi.map.endpoint.TownMapEndpoint;
import net.earthmc.emcapi.map.player.PlayerManager;
import net.earthmc.emcapi.util.Headers;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static net.earthmc.emcapi.map.player.PlayerManager.UPDATE_INTERVAL;

public class MapEndpointManager implements Listener {
    static final String MAP_ENDPOINT_VERSION = "1";

    private final EMCAPI plugin;
    private final PlayerManager playerManager;
    private final SquaremapIntegration squaremapIntegration = new SquaremapIntegration();

    private int maxNameLength = 16;

    private String formattedPlayers;

    public MapEndpointManager(final EMCAPI plugin) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager(plugin, squaremapIntegration);
        this.formattedPlayers = playerManager.formatPlayers();

        final NamespacedKey key = Objects.requireNonNull(NamespacedKey.fromString("emcapi:max_name_length"));

        TownySettings.removeReloadListener(key);
        TownySettings.addReloadListener(key, () -> this.maxNameLength = TownySettings.getMaxNameLength());
    }

    public void register(final RoutesConfig routes) {
        this.maxNameLength = TownySettings.getMaxNameLength();
        this.squaremapIntegration.register();

        routes.get("map/v" + MAP_ENDPOINT_VERSION + "/players", ctx -> {
            ctx.header(Headers.CACHE_CONTROL, "public, max-age=1, must-revalidate");
            ctx.json(formattedPlayers);
        });

        TownMapEndpoint towns = new TownMapEndpoint(this);
        routes.get("map/v" + MAP_ENDPOINT_VERSION + "/town/{town}", ctx -> {
            ctx.header(Headers.CACHE_CONTROL, "public, max-age=180, stale-while-revalidate=180");
            ctx.json(towns.lookup(ctx));
        });

        final NationRangeEndpoint nationRange = new NationRangeEndpoint(this);
        routes.get("map/v" + MAP_ENDPOINT_VERSION + "/nation-range/{nation}", ctx -> {
            ctx.header(Headers.CACHE_CONTROL, "public, max-age=300, stale-while-revalidate=300");
            ctx.json(nationRange.lookup(ctx));
        });

        final StatusEndpoint status = new StatusEndpoint(this.plugin.getServer());
        routes.get("map/v" + MAP_ENDPOINT_VERSION + "/status", ctx -> {
            ctx.header(Headers.CACHE_CONTROL, "public, max-age=120, stale-while-revalidate=60");
            ctx.json(status.lookup(ctx));
        });

        plugin.getServer().getPluginManager().registerEvents(this.playerManager, plugin);
        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, _ -> this.formattedPlayers = this.playerManager.formatPlayers(), UPDATE_INTERVAL, UPDATE_INTERVAL, TimeUnit.SECONDS);
    }

    public int maxTownyNameLength() {
        return this.maxNameLength;
    }
}
