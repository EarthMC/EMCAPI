package net.earthmc.emcapi.object.nearby;

import kotlin.Pair;

public class NearbyContext {

    private final NearbyType targetType;
    private final String targetString;
    private final Pair<Integer, Integer> targetCoordinate;
    private final NearbyType searchType;
    private final int radius;

    public NearbyContext(NearbyType targetType, String target, NearbyType searchType, int radius) {
        this.targetType = targetType;
        this.targetString = target;
        this.targetCoordinate = null;
        this.searchType = searchType;
        this.radius = radius;
    }

    public NearbyContext(NearbyType targetType, Pair<Integer, Integer> targetCoordinate, NearbyType searchType, int radius) {
        this.targetType = targetType;
        this.targetString = null;
        this.targetCoordinate = targetCoordinate;
        this.searchType = searchType;
        this.radius = radius;
    }

    public NearbyType getTargetType() {
        return targetType;
    }

    public String getTargetString() {
        return targetString;
    }

    public Pair<Integer, Integer> getTargetCoordinate() {
        return targetCoordinate;
    }

    public NearbyType getSearchType() {
        return searchType;
    }

    public int getRadius() {
        return radius;
    }
}
