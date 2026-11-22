package net.earthmc.emcapi.map.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.SquaremapIntegration;
import net.earthmc.emcapi.util.JSONUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager implements Listener {
    public static final long UPDATE_INTERVAL = 1L;
    private static final long PLAYER_UPDATE_INTERVAL = UPDATE_INTERVAL * 20;

    private final Map<UUID, PlayerTracker> playerTrackers = new ConcurrentHashMap<>();
    private final EMCAPI plugin;
    private final SquaremapIntegration squaremapIntegration;

    public PlayerManager(final EMCAPI plugin, final SquaremapIntegration squaremapIntegration) {
        this.plugin = plugin;
        this.squaremapIntegration = squaremapIntegration;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        final PlayerTracker tracker = PlayerTracker.initialize(player, this);
        playerTrackers.put(player.getUniqueId(), tracker);

        player.getScheduler().runAtFixedRate(this.plugin,_ -> tracker.update(player), () -> playerTrackers.remove(player.getUniqueId()), PLAYER_UPDATE_INTERVAL, PLAYER_UPDATE_INTERVAL);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGameModeChange(final PlayerGameModeChangeEvent event) {
        final PlayerTracker tracker = playerTrackers.get(event.getPlayer().getUniqueId());
        if (tracker != null) {
            tracker.spectator = event.getNewGameMode() == GameMode.SPECTATOR;
        }
    }

    public String formatPlayers() {
        final JsonObject root = new JsonObject();

        final JsonArray playerArray = new JsonArray();
        for (final PlayerTracker tracker : playerTrackers.values()) {
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
        root.addProperty("current", playerTrackers.size());
        root.add("players", playerArray);
        return JSONUtil.GSON.toJson(root);
    }

    public boolean isSquaremapHidden(final Player player) {
        if (!this.squaremapIntegration.isEnabled()) {
            return false;
        }

        return this.squaremapIntegration.isHidden(player);
    }
}
