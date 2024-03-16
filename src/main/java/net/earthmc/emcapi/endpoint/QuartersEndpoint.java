package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.quarters.object.Cuboid;
import net.earthmc.quarters.object.Quarter;
import org.bukkit.Location;

public class QuartersEndpoint {

    public String lookup(String query) {
        return EndpointUtils.lookup(query, EndpointUtils::getQuarterOrNull, "is not a real quarter");
    }

    public static JsonObject getQuarterObject(Quarter quarter) {
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
        int[] rgb = quarter.getRGB();
        colourArray.add(rgb[0]);
        colourArray.add(rgb[1]);
        colourArray.add(rgb[2]);
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

        JsonArray pos1Array = new JsonArray();
        Location pos1 = cuboid.getPos1();
        pos1Array.add(pos1.getBlockX());
        pos1Array.add(pos1.getBlockY());
        pos1Array.add(pos1.getBlockZ());

        JsonArray pos2Array = new JsonArray();
        Location pos2 = cuboid.getPos2();
        pos2Array.add(pos2.getBlockX());
        pos2Array.add(pos2.getBlockY());
        pos2Array.add(pos2.getBlockZ());

        cuboidObject.add("pos1", pos1Array);
        cuboidObject.add("pos2", pos2Array);

        return cuboidObject;
    }
}
