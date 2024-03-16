package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.util.MathUtil;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class NearbyEndpoint {

    public String lookupNearbyCoordinate(Integer x, Integer z, Integer radius, boolean betweenHomeBlocks) {
        if (x == null || z == null) throw new BadRequestResponse("Invalid coordinates provided");
        if (radius == null || radius < 0) throw new BadRequestResponse("Invalid radius provided");

        Location location = new Location(Bukkit.getWorlds().get(0), x, 0, z);

        return getJsonArrayOfNearbyTowns(location, radius, null, betweenHomeBlocks).toString();
    }

    public String lookupNearbyTown(String townString, Integer radius, boolean betweenHomeBlocks) {
        if (townString == null) throw new BadRequestResponse("Invalid town provided");

        Town town = EndpointUtils.getTownOrNull(townString);
        if (town == null) throw new BadRequestResponse(townString + " is not a real town");

        if (radius == null || radius < 0) throw new BadRequestResponse("Invalid radius provided");

        TownBlock homeBlock = town.getHomeBlockOrNull();
        if (homeBlock == null) throw new BadRequestResponse("The specified town has no homeblock");

        return getJsonArrayOfNearbyTowns(homeBlock.getWorldCoord().getLowerMostCornerLocation(), radius, town, betweenHomeBlocks).toString();
    }

    private JsonArray getJsonArrayOfNearbyTowns(Location location, int radius, Town town, boolean betweenHomeBlocks) {
        List<Town> towns = new ArrayList<>();

        for (Town otherTown : TownyAPI.getInstance().getTowns()) {
            if (town != null && town == otherTown) continue; // Don't add the town we are looking nearby

            TownBlock homeBlock = otherTown.getHomeBlockOrNull();
            if (homeBlock == null) continue;

            Location homeBlockLocation = homeBlock.getWorldCoord().getLowerMostCornerLocation();

            if (betweenHomeBlocks) {
                if (homeBlockLocation.distance(location) <= radius) towns.add(otherTown);
            } else {
                // Skip towns that have a homeblock over 64 townblocks away as they are very unlikely to be near enough and not worth checking every townblock
                // Side effect of this is in very rare cases a nearby "outpost" could be ignored
                if (homeBlockLocation.distance(location) > radius + (TownySettings.getTownBlockSize() * 64))
                    continue;

                for (TownBlock townBlock : otherTown.getTownBlocks()) {
                    if (townBlock.getWorldCoord().getLowerMostCornerLocation().distance(location) <= radius) {
                        towns.add(otherTown);
                        break;
                    }
                }
            }
        }

        if (!towns.isEmpty()) {
            return EndpointUtils.getTownArray(towns);
        } else {
            return new JsonArray();
        }
    }
}
