package net.earthmc.emcapi.endpoint.towny;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.manager.NationMetadataManager;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.JSONUtil;

import java.util.UUID;

public class NationsEndpoint extends PostEndpoint<Nation> {

    @Override
    public Nation getObjectOrNull(JsonElement element) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw new BadRequestResponse("Your query contains a value that is not a string");

        Nation nation;
        try {
            nation = TownyAPI.getInstance().getNation(UUID.fromString(string));
        } catch (IllegalArgumentException e) {
            nation = TownyAPI.getInstance().getNation(string);
        }

        return nation;
    }

    @Override
    public JsonElement getJsonElement(Nation nation) {
        JsonObject nationObject = new JsonObject();

        nationObject.addProperty("name", nation.getName());
        nationObject.addProperty("uuid", nation.getUUID().toString());
        nationObject.addProperty("board", nation.getBoard().isEmpty() ? null : nation.getBoard());
        nationObject.addProperty("dynmapColour", NationMetadataManager.getDynmapColour(nation));
        nationObject.addProperty("dynmapOutline", NationMetadataManager.getDynmapOutline(nation));
        nationObject.addProperty("wiki", NationMetadataManager.getWikiURL(nation));

        nationObject.add("king", EndpointUtils.getResidentJsonObject(nation.getKing()));
        nationObject.add("capital", EndpointUtils.getTownJsonObject(nation.getCapital()));

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
        statsObject.addProperty("numOutlaws", NationMetadataManager.getNumOutlaws(nation));
        statsObject.addProperty("numAllies", nation.getAllies().size());
        statsObject.addProperty("numEnemies", nation.getEnemies().size());
        statsObject.addProperty("balance", TownyEconomyHandler.isActive() ? nation.getAccount().getHoldingBalance() : 0);
        nationObject.add("stats", statsObject);

        nationObject.add("coordinates", EndpointUtils.getCoordinatesObject(nation.getSpawnOrNull()));
        nationObject.add("residents", EndpointUtils.getResidentArray(nation.getResidents()));
        nationObject.add("outlaws", EndpointUtils.getResidentArray(NationMetadataManager.getNationOutlaws(nation)));
        nationObject.add("towns", EndpointUtils.getTownArray(nation.getTowns()));
        nationObject.add("allies", EndpointUtils.getNationArray(nation.getAllies()));
        nationObject.add("enemies", EndpointUtils.getNationArray(nation.getEnemies()));
        nationObject.add("sanctioned", EndpointUtils.getTownArray(nation.getSanctionedTowns()));

        JsonObject ranksObject = new JsonObject();
        for (String rank : TownyPerms.getNationRanks()) {
            ranksObject.add(rank, EndpointUtils.getResidentArray(EndpointUtils.getNationRank(nation, rank)));
        }
        nationObject.add("ranks", ranksObject);

        return nationObject;
    }
}
