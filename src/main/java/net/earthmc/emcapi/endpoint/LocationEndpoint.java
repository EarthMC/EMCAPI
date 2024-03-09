package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.javalin.http.BadRequestResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationEndpoint {

    public String lookup(Integer x, Integer z) {
        if (x == null || z == null) throw new BadRequestResponse("Invalid coordinates provided");

        JsonObject jsonObject = new JsonObject();

        Location location = new Location(Bukkit.getWorlds().get(0), x, 0, z);
        TownyAPI townyAPI = TownyAPI.getInstance();
        Town town = townyAPI.getTown(location);

        jsonObject.addProperty("isWilderness", townyAPI.isWilderness(location));

        if (town != null) {
            jsonObject.addProperty("town", town.getName());
            jsonObject.addProperty("nation", town.getNationOrNull() == null ? null : town.getNationOrNull().getName());
        } else {
            jsonObject.add("town", null);
            jsonObject.add("nation", null);
        }

        return jsonObject.toString();
    }
}
