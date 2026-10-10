package net.earthmc.emcapi.endpoint.map;

public record LocationDataSnapshot(int x, int z, int yaw, String world, boolean hidden) {
}
