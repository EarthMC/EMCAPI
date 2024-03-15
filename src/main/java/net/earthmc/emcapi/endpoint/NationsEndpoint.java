package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import net.earthmc.emcapi.manager.NationMetadataManager;
import net.earthmc.emcapi.util.EndpointUtils;

public class NationsEndpoint {

    public String lookup(String query) {
        return EndpointUtils.lookup(query, EndpointUtils::getNationOrNull, "is not a real nation");
    }

    public static JsonObject getNationObject(Nation nation) {
        JsonObject nationObject = new JsonObject();

        nationObject.addProperty("name", nation.getName());
        nationObject.addProperty("uuid", nation.getUUID().toString());
        nationObject.addProperty("king", nation.getKing().getName());
        nationObject.addProperty("board", nation.getBoard().isEmpty() ? null : nation.getBoard());
        nationObject.addProperty("capital", nation.getCapital().getName());
        nationObject.addProperty("dynmapColour", NationMetadataManager.getDynmapColour(nation));
        nationObject.addProperty("dynmapOutline", NationMetadataManager.getDynmapOutline(nation));

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("registered", nation.getRegistered());
        nationObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("isPublic", nation.isPublic());
        statusObject.addProperty("isOpen", nation.isOpen());
        statusObject.addProperty("isNeutral", nation.isNeutral());
        nationObject.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("numTownBlocks", nation.getNumTownblocks());
        statsObject.addProperty("numResidents", nation.getNumResidents());
        statsObject.addProperty("numTowns", nation.getNumTowns());
        statsObject.addProperty("numAllies", nation.getAllies().size());
        statsObject.addProperty("numEnemies", nation.getEnemies().size());
        statsObject.addProperty("balance", TownyEconomyHandler.isActive() ? nation.getAccount().getHoldingBalance() : 0);
        nationObject.add("stats", statsObject);

        nationObject.add("coordinates", EndpointUtils.getCoordinatesObject(nation.getSpawnOrNull()));
        nationObject.add("residents", EndpointUtils.getResidentArray(nation.getResidents()));
        nationObject.add("towns", EndpointUtils.getTownArray(nation.getTowns()));
        nationObject.add("allies", EndpointUtils.getNationArray(nation.getAllies()));
        nationObject.add("enemies", EndpointUtils.getNationArray(nation.getEnemies()));

        JsonObject ranksObject = new JsonObject();
        for (String rank : TownyPerms.getNationRanks()) {
            JsonArray rankArray = new JsonArray();
            for (Resident resident : EndpointUtils.getNationRank(nation, rank)) {
                rankArray.add(resident.getName());
            }
            ranksObject.add(rank, rankArray.isEmpty() ? null : rankArray);
        }
        nationObject.add("ranks", ranksObject);

        return nationObject;
    }
}
