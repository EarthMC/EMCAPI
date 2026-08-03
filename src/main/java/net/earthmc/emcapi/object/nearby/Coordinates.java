package net.earthmc.emcapi.object.nearby;

import kotlin.Pair;

public final class Coordinates {
    public final int x;
    public final int z;

    public Coordinates(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public Pair<Integer, Integer> toPair() {
        return new Pair<>(x, z);
    }
}
