package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.util.MathUtil;
import io.javalin.http.BadRequestResponse;
import kotlin.Pair;
import net.earthmc.emcapi.object.nearby.NearbyContext;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.object.nearby.NearbyType;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.JSONUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class NearbyEndpoint extends PostEndpoint<NearbyContext> {

    @Override
    public NearbyContext getObjectOrNull(JsonElement element) {
        JsonObject jsonObject = JSONUtil.getJsonElementAsJsonObjectOrNull(element);
        if (jsonObject == null) throw new BadRequestResponse("Your query contains a value that is not a JSON object");

        String targetTypeString = JSONUtil.getJsonElementAsStringOrNull(jsonObject.get("target_type"));
        String searchTypeString = JSONUtil.getJsonElementAsStringOrNull(jsonObject.get("search_type"));
        if (targetTypeString == null || searchTypeString == null) throw new BadRequestResponse("You did not specify a target or search type");

        NearbyType targetType;
        NearbyType searchType;
        try {
            targetType = NearbyType.valueOf(targetTypeString);
            searchType = NearbyType.valueOf(searchTypeString);
        } catch (IllegalArgumentException e) {
            throw new BadRequestResponse("Your target or search type is invalid");
        }

        Integer radius = JSONUtil.getJsonElementAsIntegerOrNull(jsonObject.get("radius"));
        if (radius == null) throw new BadRequestResponse("You did not specify a radius");

        JsonElement targetElement = jsonObject.get("target");
        if (targetType.equals(NearbyType.COORDINATE)) {
            JsonArray jsonArray = JSONUtil.getJsonElementAsJsonArrayOrNull(targetElement);
            if (jsonArray == null) throw new BadRequestResponse("Your target is not a valid JSON array");

            Pair<Integer, Integer> pair = new Pair<>(jsonArray.get(0).getAsInt(), jsonArray.get(1).getAsInt());

            return new NearbyContext(targetType, pair, searchType, radius);
        } else if (targetType.equals(NearbyType.TOWN)) {
            String target = JSONUtil.getJsonElementAsStringOrNull(targetElement);
            if (target == null) throw new BadRequestResponse("Your target is not a valid string");

            return new NearbyContext(targetType, target, searchType, radius);
        }

        return null;
    }

    @Override
    public JsonElement getJsonElement(NearbyContext context) {
        NearbyType targetType = context.getTargetType();
        int radius = context.getRadius();
        switch (targetType) {
            case COORDINATE -> {
                Pair<Integer, Integer> pair = context.getTargetCoordinate();

                return lookupNearbyCoordinate(pair.getFirst(), pair.getSecond(), radius);
            }
            case TOWN -> {
                String townName = context.getTargetString();

                return lookupNearbyTown(townName, radius);
            }
        };

        return null;
    }

    public JsonArray lookupNearbyCoordinate(Integer x, Integer z, Integer radius) {
        if (x == null || z == null) throw new BadRequestResponse("Invalid coordinates provided");
        if (radius == null || radius < 0) throw new BadRequestResponse("Invalid radius provided");

        Location location = new Location(Bukkit.getWorlds().get(0), x, 0, z);

        return getJsonArrayOfNearbyTowns(WorldCoord.parseWorldCoord(location), radius, null);
    }

    public JsonElement lookupNearbyTown(String townString, Integer radius) {
        if (townString == null) throw new BadRequestResponse("Invalid town provided");

        Town town = TownyAPI.getInstance().getTown(townString);
        if (town == null) throw new BadRequestResponse(townString + " is not a real town");

        if (radius == null || radius < 0) throw new BadRequestResponse("Invalid radius provided");

        TownBlock homeBlock = town.getHomeBlockOrNull();
        if (homeBlock == null) throw new BadRequestResponse("The specified town has no homeblock");

        return getJsonArrayOfNearbyTowns(homeBlock.getWorldCoord(), radius, town);
    }

    private JsonArray getJsonArrayOfNearbyTowns(WorldCoord worldCoord, int radius, Town town) {
        List<Town> towns = new ArrayList<>();

        for (Town otherTown : TownyAPI.getInstance().getTowns()) {
            if (town != null && town == otherTown) continue; // Don't add the town we are looking nearby

            TownBlock homeBlock = otherTown.getHomeBlockOrNull();
            if (homeBlock == null) continue;

            WorldCoord homeBlockWorldCoord = homeBlock.getWorldCoord();

            if (MathUtil.distance(worldCoord, homeBlockWorldCoord) * TownySettings.getTownBlockSize() <= radius) towns.add(otherTown);
        }

        return EndpointUtils.getTownArray(towns);
    }
}
