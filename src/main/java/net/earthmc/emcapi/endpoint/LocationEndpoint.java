package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationEndpoint {

    public String lookup(String query) {
        if (query == null) throw new BadRequestResponse("No query provided");

        String[] split = query.split(",");

        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < Math.min(EMCAPI.instance.getConfig().getInt("behaviour.max_lookup_size"), split.length); i++) {
            String[] coordinateSplit = split[i].split(";");

            int x, z;
            try {
                x = Integer.parseInt(coordinateSplit[0]);
                z = Integer.parseInt(coordinateSplit[1]);
            } catch (NumberFormatException nfe) {
                throw new BadRequestResponse("Invalid integer at index " + i);
            } catch (IndexOutOfBoundsException ioobe) {
                throw new BadRequestResponse("Coordinate at index " + i + " only contains one value");
            }

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
            jsonArray.add(jsonObject);
        }

        return jsonArray.toString();
    }
}
