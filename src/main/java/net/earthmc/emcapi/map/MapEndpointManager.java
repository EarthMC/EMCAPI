package net.earthmc.emcapi.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownySettings;
import io.javalin.config.RoutesConfig;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.map.endpoint.NationRangeEndpoint;
import net.earthmc.emcapi.map.endpoint.StatusEndpoint;
import net.earthmc.emcapi.map.endpoint.TownMapEndpoint;
import net.earthmc.emcapi.util.Headers;
import net.earthmc.emcapi.util.JSONUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MapEndpointManager implements Listener {
    static final String MAP_ENDPOINT_VERSION = "1";
    private static final long UPDATE_INTERVAL = 1L;
    private static final long PLAYER_UPDATE_INTERVAL = UPDATE_INTERVAL * 20;

    private final Map<UUID, PlayerTracker> PLAYER_TRACKERS = new ConcurrentHashMap<>();
    private final EMCAPI plugin;

    private int maxNameLength = TownySettings.getMaxNameLength();

    private String formattedPlayers;

    public MapEndpointManager(final EMCAPI plugin) {
        this.plugin = plugin;
        formatPlayers();

        final NamespacedKey key = Objects.requireNonNull(NamespacedKey.fromString("emcapi:max_name_length"));

        TownySettings.removeReloadListener(key);
        TownySettings.addReloadListener(key, () -> this.maxNameLength = TownySettings.getMaxNameLength());
    }

    public void register(final RoutesConfig routes) {
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

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task -> formatPlayers(), UPDATE_INTERVAL, UPDATE_INTERVAL, TimeUnit.SECONDS);
    }

    private void formatPlayers() {
        final JsonObject root = new JsonObject();

        final JsonArray playerArray = new JsonArray();
        for (final PlayerTracker tracker : PLAYER_TRACKERS.values()) {
            final LocationDataSnapshot location = tracker.locationDataSnapshot();

            if (location.hidden()) {
                continue;
            }

            final JsonObject playerObject = new JsonObject();
            playerObject.addProperty("world", location.world());
            playerObject.addProperty("name", tracker.displayName());
            playerObject.addProperty("x", location.x());
            playerObject.addProperty("y", 64);
            playerObject.addProperty("z", location.z());
            playerObject.addProperty("uuid", tracker.uuidString());
            playerObject.addProperty("yaw", location.yaw());
            if (tracker.isPremium()) {
                playerObject.addProperty("premium", true);
            }

            playerArray.add(playerObject);
        }

        root.addProperty("max", plugin.getServer().getMaxPlayers());
        root.addProperty("current", PLAYER_TRACKERS.size());
        root.add("players", playerArray);
        this.formattedPlayers = JSONUtil.GSON.toJson(root);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        final PlayerTracker tracker = PlayerTracker.initialize(player);
        PLAYER_TRACKERS.put(player.getUniqueId(), tracker);

        player.getScheduler().runAtFixedRate(plugin, _ -> {
            tracker.update(player);
        }, () -> PLAYER_TRACKERS.remove(player.getUniqueId()), PLAYER_UPDATE_INTERVAL, PLAYER_UPDATE_INTERVAL);
    }

    public int maxTownyNameLength() {
        return this.maxNameLength;
    }
}
