package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import net.earthmc.emcapi.manager.TownMetadataManager;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.quarters.api.QuartersAPI;

public class TownsEndpoint {

    public String lookup(String query) {
        return EndpointUtils.lookup(query, EndpointUtils::getTownOrNull, "is not a real town");
    }

    public static JsonObject getTownObject(Town town) {
        JsonObject townObject = new JsonObject();

        townObject.addProperty("name", town.getName());
        townObject.addProperty("uuid", town.getUUID().toString());
        townObject.addProperty("board", town.getBoard().isEmpty() ? null : town.getBoard());
        townObject.addProperty("founder", town.getFounder());
        townObject.addProperty("wiki", TownMetadataManager.getWikiURL(town));

        townObject.add("mayor", EndpointUtils.getResidentJsonObject(town.getMayor()));
        townObject.add("nation", EndpointUtils.getNationJsonObject(town.getNationOrNull()));

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("registered", town.getRegistered());
        timestampsObject.addProperty("joinedNationAt", town.hasNation() ? town.getJoinedNationAt() : null);
        timestampsObject.addProperty("ruinedAt", town.isRuined() ? town.getRuinedTime() : null);
        townObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("isPublic", town.isPublic());
        statusObject.addProperty("isOpen", town.isOpen());
        statusObject.addProperty("isNeutral", town.isNeutral());
        statusObject.addProperty("isCapital", town.isCapital());
        statusObject.addProperty("isOverClaimed", town.isOverClaimed());
        statusObject.addProperty("isRuined", town.isRuined());
        statusObject.addProperty("isForSale", town.isForSale());
        statusObject.addProperty("hasNation", town.hasNation());
        statusObject.addProperty("hasOverclaimShield", TownMetadataManager.hasOverclaimShield(town));
        townObject.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("numTownBlocks", town.getNumTownBlocks());
        statsObject.addProperty("maxTownBlocks", town.getMaxTownBlocks());
        statsObject.addProperty("bonusBlocks", town.getBonusBlocks());
        statsObject.addProperty("numResidents", town.getNumResidents());
        statsObject.addProperty("numTrusted", town.getTrustedResidents().size());
        statsObject.addProperty("numOutlaws", town.getOutlaws().size());
        statsObject.addProperty("balance", TownyEconomyHandler.isActive() ? town.getAccount().getHoldingBalance() : 0);
        statsObject.addProperty("forSalePrice", !town.isForSale() ? null : town.getForSalePrice());
        townObject.add("stats", statsObject);

        townObject.add("perms", EndpointUtils.getPermsObject(town.getPermissions()));

        JsonObject coordinatesObject = EndpointUtils.getCoordinatesObject(town.getSpawnOrNull());
        JsonArray homeBlockArray = new JsonArray();
        TownBlock homeBlock = town.getHomeBlockOrNull();
        homeBlockArray.add(homeBlock == null ? null : homeBlock.getX());
        homeBlockArray.add(homeBlock == null ? null : homeBlock.getZ());
        coordinatesObject.add("homeBlock", homeBlockArray);

        JsonArray townBlocksArray = new JsonArray();
        for (TownBlock townBlock : town.getTownBlocks()) {
            JsonArray townBlockArray = new JsonArray();
            townBlockArray.add(townBlock.getX());
            townBlockArray.add(townBlock.getZ());

            townBlocksArray.add(townBlockArray);
        }
        coordinatesObject.add("townBlocks", townBlocksArray);

        townObject.add("coordinates", coordinatesObject);

        townObject.add("residents", EndpointUtils.getResidentArray(town.getResidents()));
        townObject.add("trusted", EndpointUtils.getResidentArray(town.getTrustedResidents().stream().toList()));
        townObject.add("outlaws", EndpointUtils.getResidentArray(town.getOutlaws().stream().toList()));

        JsonArray quartersArray = EndpointUtils.getQuarterArray(QuartersAPI.getInstance().getQuartersTown(town).getQuarters());
        townObject.add("quarters", quartersArray);

        JsonObject ranksObject = new JsonObject();
        for (String rank : TownyPerms.getTownRanks()) {
            JsonArray rankArray = new JsonArray();
            for (Resident resident : town.getRank(rank)) {
                rankArray.add(resident.getName());
            }
            ranksObject.add(rank, rankArray);
        }
        townObject.add("ranks", ranksObject);

        return townObject;
    }
}
