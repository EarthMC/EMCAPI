package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.javalin.http.BadRequestResponse;
import kotlin.Pair;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.JSONUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationEndpoint extends PostEndpoint<Pair<Integer, Integer>> {

    @Override
    public Pair<Integer, Integer> getObjectOrNull(JsonElement element) {
        JsonArray jsonArray = JSONUtil.getJsonElementAsJsonArrayOrNull(element);
        if (jsonArray == null) throw new BadRequestResponse("Your query contains a value that is not a JSON array");

        int x;
        int z;
        try {
            JsonElement xElement = jsonArray.get(0);
            JsonElement zElement = jsonArray.get(1);

            Integer xInner = JSONUtil.getJsonElementAsIntegerOrNull(xElement);
            Integer zInner = JSONUtil.getJsonElementAsIntegerOrNull(zElement);
            if (xInner == null || zInner == null) throw new BadRequestResponse("A JSON array in your query contained a value that was not an int");

            x = xInner;
            z = zInner;
        } catch (IndexOutOfBoundsException oobe) {
            throw new BadRequestResponse("A JSON array in your query did not contain two values");
        }

        return new Pair<>(x, z);
    }

    @Override
    public JsonElement getJsonElement(Pair<Integer, Integer> pair) {
        int x = pair.getFirst();
        int z = pair.getSecond();

        Location location = new Location(Bukkit.getWorlds().get(0), x, 0, z);
        TownyAPI townyAPI = TownyAPI.getInstance();
        Town town = townyAPI.getTown(location);

        JsonObject jsonObject = new JsonObject();
        JsonObject locationObject = new JsonObject();
        locationObject.addProperty("x", x);
        locationObject.addProperty("z", z);
        jsonObject.add("location", locationObject);

        jsonObject.addProperty("isWilderness", townyAPI.isWilderness(location));

        jsonObject.add("town", EndpointUtils.getTownJsonObject(town));
        jsonObject.add("nation", EndpointUtils.getNationJsonObject(town == null ? null : town.getNationOrNull()));

        return jsonObject;
    }
}
