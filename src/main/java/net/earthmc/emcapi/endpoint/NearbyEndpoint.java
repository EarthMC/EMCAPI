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

    public String lookupNearbyCoordinate(Integer x, Integer z, Integer radius) {
        if (x == null || z == null) throw new BadRequestResponse("Invalid coordinates provided");
        if (radius == null || radius < 0) throw new BadRequestResponse("Invalid radius provided");

        Location location = new Location(Bukkit.getWorlds().get(0), x, 0, z);

        return getJsonArrayOfNearbyTowns(WorldCoord.parseWorldCoord(location), radius, null).toString();
    }

    public String lookupNearbyTown(String townString, Integer radius) {
        if (townString == null) throw new BadRequestResponse("Invalid town provided");

        Town town = EndpointUtils.getTownOrNull(townString);
        if (town == null) throw new BadRequestResponse(townString + " is not a real town");

        if (radius == null || radius < 0) throw new BadRequestResponse("Invalid radius provided");

        TownBlock homeBlock = town.getHomeBlockOrNull();
        if (homeBlock == null) throw new BadRequestResponse("The specified town has no homeblock");

        return getJsonArrayOfNearbyTowns(homeBlock.getWorldCoord(), radius, town).toString();
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
