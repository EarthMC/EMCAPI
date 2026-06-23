package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.util.MathUtil;
import io.javalin.http.BadRequestResponse;
import kotlin.Pair;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.object.nearby.NearbyContext;
import net.earthmc.emcapi.object.nearby.NearbyType;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.JSONUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NearbyEndpoint extends PostEndpoint<NearbyContext> {

    public NearbyEndpoint(final EMCAPI plugin) {
        super(plugin);
    }

    @Override
    public NearbyContext getObjectOrNull(JsonElement element, @Nullable String key) {
        JsonObject jsonObject = JSONUtil.getJsonElementAsJsonObjectOrNull(element);
        if (jsonObject == null) throw new BadRequestResponse("Your query contains a value that is not a JSON object");

        String targetTypeString = JSONUtil.getJsonElementAsStringOrNull(jsonObject.get("target_type"));
        String searchTypeString = JSONUtil.getJsonElementAsStringOrNull(jsonObject.get("search_type"));
        if (targetTypeString == null || searchTypeString == null) throw new BadRequestResponse("You did not specify a target or search type");

        NearbyType targetType;
        NearbyType searchType;
        try {
            targetType = NearbyType.valueOf(targetTypeString);
        } catch (IllegalArgumentException ignored) {
            throw new BadRequestResponse("Invalid target type");
        }
        try {
            searchType = NearbyType.valueOf(searchTypeString);
            if (searchType == NearbyType.COORDINATE) {
                throw new BadRequestResponse("Search type cannot be coordinate. Select town or nation.");
            }
        } catch (IllegalArgumentException ignored) {
            throw new BadRequestResponse("Invalid search type");
        }

        Integer radius = JSONUtil.getJsonElementAsIntegerOrNull(jsonObject.get("radius"));
        if (radius == null || radius < 0) throw new BadRequestResponse("Invalid radius provided");

        boolean strict = Objects.requireNonNullElse(JSONUtil.getJsonElementAsBooleanOrNull(jsonObject.get("strict")), false);
        JsonElement targetElement = jsonObject.get("target");
        return switch (targetType) {
            case COORDINATE -> {
                JsonArray jsonArray = JSONUtil.getJsonElementAsJsonArrayOrNull(targetElement);
                if (jsonArray == null) throw new BadRequestResponse("Your target is not a valid JSON array");

                Pair<Integer, Integer> pair = new Pair<>(jsonArray.get(0).getAsInt(), jsonArray.get(1).getAsInt());

                yield new NearbyContext(targetType, pair, searchType, radius, strict);
            }
            case TOWN, NATION -> {
                String target = JSONUtil.getJsonElementAsStringOrNull(targetElement);
                if (target == null) throw new BadRequestResponse("Your target is not a valid string");

                yield new NearbyContext(targetType, target, searchType, radius, strict);
            }
        };
    }

    @Override
    public JsonElement getJsonElement(NearbyContext context, @Nullable String key) {
        NearbyType targetType = context.getTargetType();
        int radius = context.getRadius();
        NearbyType searchType = context.getSearchType();
        boolean strict = context.isStrict();

        return switch (targetType) {
            case COORDINATE -> {
                Pair<Integer, Integer> pair = context.getTargetCoordinate();

                yield lookupNearCoordinates(pair.getFirst(), pair.getSecond(), radius, strict, searchType);
            }
            case TOWN -> lookupNearTown(context.getTargetString(), radius, strict, searchType);
            case NATION -> lookupNearNation(context.getTargetString(), radius, strict, searchType);
        };
    }

    private JsonArray lookupNearCoordinates(int x, int z, int radius, boolean strict,  NearbyType searchType) {
        Location location = new Location(Bukkit.getWorlds().getFirst(), x, 0, z);

        WorldCoord worldCoord = WorldCoord.parseWorldCoord(location);
        return searchType == NearbyType.TOWN ? getJsonArrayOfNearbyTowns(worldCoord, radius, strict, null) : getNearbyNations(worldCoord, radius, strict, null);
    }

    private JsonElement lookupNearTown(String townString, int radius, boolean strict, NearbyType searchType) {
        if (townString == null) throw new BadRequestResponse("Invalid town provided");

        Town town = TownyAPI.getInstance().getTown(townString);
        if (town == null) throw new BadRequestResponse(townString + " is not a real town");

        TownBlock homeBlock = town.getHomeBlockOrNull();
        if (homeBlock == null) throw new BadRequestResponse("The specified town has no homeblock");

        return searchType == NearbyType.TOWN ? getJsonArrayOfNearbyTowns(homeBlock.getWorldCoord(), radius, strict, town) : getNearbyNations(homeBlock.getWorldCoord(), radius, strict, town.isCapital() ? town.getNationOrNull() : null);
    }

    private JsonElement lookupNearNation(String nationName, int radius, boolean strict, NearbyType searchType) {
        if (nationName == null) throw new BadRequestResponse("Invalid nation provided");

        Nation nation = TownyAPI.getInstance().getNation(nationName);
        if (nation == null) throw new BadRequestResponse(nationName + " is not a real nation");

        Town capital = nation.getCapital();
        if (capital == null) throw new BadRequestResponse("The specified nation has no capital");

        TownBlock homeBlock = capital.getHomeBlockOrNull();
        if (homeBlock == null) throw new BadRequestResponse("The specified nation's capital has no homeblock");

        return searchType == NearbyType.TOWN ? getJsonArrayOfNearbyTowns(homeBlock.getWorldCoord(), radius, strict, capital) : getNearbyNations(homeBlock.getWorldCoord(), radius, strict, nation);
    }

    private JsonArray getJsonArrayOfNearbyTowns(WorldCoord center, int radius, boolean strict, Town excludeTown) {
        List<Town> towns = new ArrayList<>();

        for (Town town : TownyAPI.getInstance().getTowns()) {
            if (excludeTown != null && excludeTown.equals(town)) continue;

            if (isTownInRange(town, center, radius, strict)) {
                towns.add(town);
            }
        }

        return EndpointUtils.getGovernmentArray(towns);
    }

    private JsonArray getNearbyNations(WorldCoord center, int radius, boolean strict, Nation excludeNation) {
        List<Nation> nations = new ArrayList<>();

        for (Nation nation : TownyAPI.getInstance().getNations()) {
            if (excludeNation != null && excludeNation.equals(nation)) continue;

            Town capital = nation.getCapital();
            if (capital == null) continue;

            if (isTownInRange(capital, center, radius, strict)) {
                nations.add(nation);
            }
        }

        return EndpointUtils.getGovernmentArray(nations);
    }

    private boolean isTownInRange(Town town, WorldCoord center, int radius, boolean strict) {
        TownBlock homeBlock = town.getHomeBlockOrNull();
        if (homeBlock == null) return false;
        WorldCoord homeBlockWorldCoord = homeBlock.getWorldCoord();
        double distance = MathUtil.distance(center, homeBlockWorldCoord) * TownySettings.getTownBlockSize();
        if (distance <= radius) {
            return true;
        }
        if (!strict && distance <= radius + 300) { // Homeblock is less than 300 blocks out of reach, check all the town's chunks
            for (TownBlock townBlock : town.getTownBlocks()) {
                if (MathUtil.distance(center, townBlock.getWorldCoord()) * TownySettings.getTownBlockSize() <= radius) {
                    return true;
                }
            }
        }
        return false;
    }
}
