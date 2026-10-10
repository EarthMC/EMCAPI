package net.earthmc.emcapi.endpoint.map;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

import static org.bukkit.util.NumberConversions.floor;

public class PlayerTracker {
    private final UUID uuid;
    private final String uuidString;
    private final String displayName;

    private World lastWorld;

    private LocationDataSnapshot snapshot;

    private PlayerTracker(final Player player) {
        this.uuid = player.getUniqueId();
        this.uuidString = uuid.toString().replace("-", "");
        this.displayName = player.getName();
        update(player);
    }

    public static PlayerTracker initialize(final Player player) {
        return new PlayerTracker(player);
    }

    public void update(final Player player) {
        final double x = player.getX();
        final double z = player.getZ();
        final World world = player.getWorld();
        final float yaw = player.getYaw();
        final boolean hidden = player.isSneaking() || !player.getWorld().hasSkyLight() || player.getEyeLocation().getBlock().getLightFromSky() < 15 || !player.isVisibleByDefault() || player.isInvisible() || player.getGameMode() == GameMode.SPECTATOR;

        String worldName;
        if (world == lastWorld) {
            worldName = snapshot.world();
        } else {
            worldName = world.getKey().asString().replace(':', '_');
            lastWorld = world;
        }

        this.snapshot = new LocationDataSnapshot(floor(x), floor(z), floor(yaw), worldName, hidden);
    }

    public UUID uuid() {
        return this.uuid;
    }

    public String uuidString() {
        return this.uuidString;
    }

    public String displayName() {
        return this.displayName;
    }

    public LocationDataSnapshot locationDataSnapshot() {
        return this.snapshot;
    }
}
