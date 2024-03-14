package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import io.javalin.http.BadRequestResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class NearbyEndpoint {

    public String lookupNearbyCoordinate(Integer x, Integer z, Integer radius) {
        if (x == null || z == null) throw new BadRequestResponse("Invalid coordinates provided");
        if (radius == null || radius < 0) throw new BadRequestResponse("Invalid radius provided");

        Location location = new Location(Bukkit.getWorlds().get(0), x, 0, z);

        return getJsonArrayOfNearbyTowns(location, radius).toString();
    }

    public String lookupNearbyTown(String townString, Integer radius) {
        if (townString == null) throw new BadRequestResponse("Invalid town provided");

        Town town = TownyAPI.getInstance().getTown(townString);
        if (town == null) throw new BadRequestResponse(townString + " is not a real town");

        if (radius == null || radius < 0) throw new BadRequestResponse("Invalid radius provided");

        TownBlock homeBlock = town.getHomeBlockOrNull();
        if (homeBlock == null) throw new BadRequestResponse("The specified town has no homeblock");

        return getJsonArrayOfNearbyTowns(homeBlock.getWorldCoord().getLowerMostCornerLocation(), radius).toString();
    }

    private JsonArray getJsonArrayOfNearbyTowns(Location location, int radius) {
        JsonArray jsonArray = new JsonArray();

        for (Town town : TownyAPI.getInstance().getTowns()) {
            TownBlock homeBlock = town.getHomeBlockOrNull();
            if (homeBlock == null) continue;

            if (homeBlock.getWorldCoord().getUpperMostCornerLocation().distance(location) <= radius) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", town.getName());
                jsonObject.addProperty("uuid", town.getUUID().toString());

                jsonArray.add(jsonObject);
            }
        }

        return jsonArray;
    }
}
