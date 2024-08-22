package net.earthmc.emcapi.endpoint.towny;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.JSONUtil;
import au.lupine.quarters.api.manager.QuarterManager;
import au.lupine.quarters.object.entity.Cuboid;
import au.lupine.quarters.object.entity.Quarter;
import org.bukkit.Location;

import java.awt.Color;
import java.util.UUID;

public class QuartersEndpoint extends PostEndpoint<Quarter> {

    @Override
    public Quarter getObjectOrNull(JsonElement element) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw new BadRequestResponse("Your query contains a value that is not a string");

        UUID uuid;
        try {
            uuid = UUID.fromString(string);
        } catch (IllegalArgumentException e) {
            return null;
        }

        return QuarterManager.getInstance().getQuarter(uuid);
    }

    @Override
    public JsonElement getJsonElement(Quarter quarter) {
        JsonObject quarterObject = new JsonObject();

        quarterObject.addProperty("uuid", quarter.getUUID().toString());
        quarterObject.addProperty("type", quarter.getType().toString());

        quarterObject.add("owner", EndpointUtils.getResidentJsonObject(quarter.getOwnerResident()));
        quarterObject.add("town", EndpointUtils.getTownJsonObject(quarter.getTown()));

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("registered", quarter.getRegistered());
        timestampsObject.addProperty("claimedAt", quarter.getClaimedAt());
        quarterObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("isEmbassy", quarter.isEmbassy());
        quarterObject.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("price", quarter.getPrice());
        statsObject.addProperty("volume", quarter.getVolume());
        statsObject.addProperty("numCuboids", quarter.getCuboids().size());
        quarterObject.add("stats", statsObject);

        JsonArray colourArray = new JsonArray();
        Color color = quarter.getColour();
        colourArray.add(color.getRed());
        colourArray.add(color.getGreen());
        colourArray.add(color.getBlue());
        quarterObject.add("colour", colourArray);

        quarterObject.add("trusted", EndpointUtils.getResidentArray(quarter.getTrustedResidents()));

        JsonArray cuboidsArray = new JsonArray();
        for (Cuboid cuboid : quarter.getCuboids()) {
            JsonObject cuboidObject = getCuboidObject(cuboid);

            cuboidsArray.add(cuboidObject);
        }
        quarterObject.add("cuboids", cuboidsArray);

        return quarterObject;
    }

    private static JsonObject getCuboidObject(Cuboid cuboid) {
        JsonObject cuboidObject = new JsonObject();

        JsonArray cornerOne = new JsonArray();
        Location pos1 = cuboid.getCornerOne();
        cornerOne.add(pos1.getBlockX());
        cornerOne.add(pos1.getBlockY());
        cornerOne.add(pos1.getBlockZ());

        JsonArray cornerTwo = new JsonArray();
        Location pos2 = cuboid.getCornerTwo();
        cornerTwo.add(pos2.getBlockX());
        cornerTwo.add(pos2.getBlockY());
        cornerTwo.add(pos2.getBlockZ());

        cuboidObject.add("pos1", cornerOne);
        cuboidObject.add("pos2", cornerTwo);

        return cuboidObject;
    }
}
