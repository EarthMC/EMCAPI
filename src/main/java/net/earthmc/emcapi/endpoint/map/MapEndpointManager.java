package net.earthmc.emcapi.endpoint.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.config.RoutesConfig;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.util.JSONUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MapEndpointManager implements Listener {
    static final String MAP_ENDPOINT_VERSION = "1";
    private static final long UPDATE_INTERVAL = 1L;
    private static final long PLAYER_UPDATE_INTERVAL = UPDATE_INTERVAL * 20;

    private final Map<UUID, PlayerTracker> PLAYER_TRACKERS = new ConcurrentHashMap<>();
    private final EMCAPI plugin;

    private String formattedPlayers;

    public MapEndpointManager(final EMCAPI plugin) {
        this.plugin = plugin;
        formatPlayers();
    }

    public void register(final RoutesConfig routes) {
        routes.get("map/v" + MAP_ENDPOINT_VERSION + "/players", ctx -> {
            ctx.json(formattedPlayers);
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

        player.getScheduler().runAtFixedRate(plugin, task -> {
            tracker.update(player);
        }, () -> PLAYER_TRACKERS.remove(player.getUniqueId()), PLAYER_UPDATE_INTERVAL, PLAYER_UPDATE_INTERVAL);
    }
}
