package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.object.Resident;
import io.javalin.http.NotFoundResponse;
import net.earthmc.quarters.api.QuartersAPI;
import net.earthmc.quarters.object.Cuboid;
import net.earthmc.quarters.object.Quarter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class QuartersEndpoint {
    private final FileConfiguration config;

    public QuartersEndpoint(FileConfiguration config) {
        this.config = config;
    }

    public String lookup(String query) {
        String[] split = query.split(",");

        if (split.length == 1) {
            String uuidString = split[0];
            UUID uuid;

            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                throw new NotFoundResponse(uuidString + " is not a real quarter");
            }

            Quarter quarter = QuartersAPI.getInstance().getQuarter(uuid);

            return getQuarterObject(quarter).toString();
        } else {
            JsonArray jsonArray = new JsonArray();
            for (int i = 0; i < Math.min(config.getInt("behaviour.max_lookup_size"), split.length); i++) {
                String uuidString = split[i];
                UUID uuid;

                try {
                    uuid = UUID.fromString(uuidString);
                } catch (IllegalArgumentException e) {
                    throw new NotFoundResponse(uuidString + " is not a real quarter");
                }

                Quarter quarter = QuartersAPI.getInstance().getQuarter(uuid);

                jsonArray.add(getQuarterObject(quarter));
            }

            return jsonArray.toString();
        }
    }

    public JsonObject getQuarterObject(Quarter quarter) {
        JsonObject quarterObject = new JsonObject();

        quarterObject.addProperty("uuid", quarter.getUUID().toString());
        quarterObject.addProperty("owner", quarter.getOwnerResident() == null ? null : quarter.getOwnerResident().getName());
        quarterObject.addProperty("town", quarter.getTown().getName());
        quarterObject.addProperty("type", quarter.getType().toString());

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

        JsonArray cuboidsArray = new JsonArray();
        for (Cuboid cuboid : quarter.getCuboids()) {
            JsonObject cuboidObject = getCuboidObject(cuboid);

            cuboidsArray.add(cuboidObject);
        }
        quarterObject.add("cuboids", cuboidsArray);

        JsonArray trustedArray = new JsonArray();
        for (Resident trusted : quarter.getTrustedResidents()) {
            trustedArray.add(trusted.getName());
        }
        quarterObject.add("trusted", trustedArray.isEmpty() ? null : trustedArray);

        return quarterObject;
    }

    private JsonObject getCuboidObject(Cuboid cuboid) {
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
