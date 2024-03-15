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
import net.earthmc.quarters.object.Quarter;

import java.util.Collection;
import java.util.List;

public class TownsEndpoint {

    public String lookup(String query) {
        return EndpointUtils.lookup(query, EndpointUtils::getTownOrNull, "is not a real town");
    }

    public static JsonObject getTownObject(Town town) {
        JsonObject townObject = new JsonObject();

        townObject.addProperty("name", town.getName());
        townObject.addProperty("uuid", town.getUUID().toString());
        townObject.addProperty("mayor", town.getMayor().getName());
        townObject.addProperty("board", town.getBoard().isEmpty() ? null : town.getBoard());
        townObject.addProperty("founder", town.getFounder());
        townObject.addProperty("nation", town.hasNation() ? town.getNationOrNull().getName() : null);

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("registered", town.getRegistered());
        timestampsObject.addProperty("joinedNationAt", town.hasNation() ? town.getJoinedNationAt() : null);
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
        statsObject.addProperty("numResidents", town.getNumResidents());
        statsObject.addProperty("numTrusted", town.getTrustedResidents().size());
        statsObject.addProperty("numOutlaws", town.getOutlaws().size());
        statsObject.addProperty("balance", TownyEconomyHandler.isActive() ? town.getAccount().getHoldingBalance() : 0);
        statsObject.addProperty("forSalePrice", town.getForSalePrice());
        townObject.add("stats", statsObject);

        townObject.add("perms", EndpointUtils.getPermsObject(town.getPermissions()));

        JsonObject coordinatesObject = EndpointUtils.getCoordinatesObject(town.getSpawnOrNull());
        JsonArray homeBlockArray = new JsonArray();
        TownBlock homeBlock = town.getHomeBlockOrNull();
        if (homeBlock != null) {
            homeBlockArray.add(homeBlock.getX());
            homeBlockArray.add(homeBlock.getZ());
            coordinatesObject.add("homeBlock", homeBlockArray);
        } else {
            coordinatesObject.add("homeBlock", null);
        }

        JsonObject townBlocksObject = new JsonObject();
        Collection<TownBlock> townBlocks = town.getTownBlocks();
        if (!townBlocks.isEmpty()) {
            JsonArray xArray = new JsonArray();
            JsonArray zArray = new JsonArray();

            for (TownBlock townBlock : town.getTownBlocks()) {
                xArray.add(townBlock.getX());
                zArray.add(townBlock.getZ());
            }

            townBlocksObject.add("x", xArray);
            townBlocksObject.add("z", zArray);
            coordinatesObject.add("townBlocks", townBlocksObject);
        } else {
            coordinatesObject.add("townBlocks", null);
        }

        JsonArray residentsArray = new JsonArray();
        for (Resident resident : town.getResidents()) {
            residentsArray.add(resident.getName());
        }
        townObject.add("residents", residentsArray);

        JsonObject ranksObject = new JsonObject();
        for (String rank : TownyPerms.getTownRanks()) {
            JsonArray rankArray = new JsonArray();
            for (Resident resident : town.getRank(rank)) {
                rankArray.add(resident.getName());
            }
            ranksObject.add(rank, rankArray.isEmpty() ? null : rankArray);
        }
        townObject.add("ranks", ranksObject);

        JsonArray trustedArray = new JsonArray();
        for (Resident resident : town.getTrustedResidents()) {
            trustedArray.add(resident.getName());
        }
        townObject.add("trusted", trustedArray.isEmpty() ? null : trustedArray);

        JsonArray outlawsArray = new JsonArray();
        for (Resident resident : town.getOutlaws()) {
            outlawsArray.add(resident.getName());
        }
        townObject.add("outlaws", outlawsArray.isEmpty() ? null : outlawsArray);

        List<Quarter> quartersList = QuartersAPI.getInstance().getQuartersTown(town).getQuarters();
        if (quartersList != null) {
            JsonArray quartersArray = new JsonArray();
            for (Quarter quarter : quartersList) {
                quartersArray.add(quarter.getUUID().toString());
            }
            townObject.add("quarters", quartersArray);
        }

        return townObject;
    }
}
