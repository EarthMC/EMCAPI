package net.earthmc.emcapi.endpoint.legacy.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import io.javalin.http.NotFoundResponse;

import java.util.UUID;

public class v2TownLookup {
    public static JsonObject getTownData(Town town, boolean useCache) {
        JsonObject json = new JsonObject();

        json.addProperty("name", town.getName());
        json.addProperty("uuid", town.getUUID().toString());
        if (town.hasMayor()) {
            json.addProperty("mayor", town.getMayor().getName());
        }
        if (town.getBoard().isEmpty()) {
            json.addProperty("board", town.getBoard());
        }
        json.addProperty("founder", town.getFounder());
        if (town.getNationOrNull() != null) {
            json.addProperty("nation", town.getNationOrNull().getName());
        }
        if (!town.getMapColorHexCode().isEmpty()) {
            json.addProperty("mapColorHexCode", town.getMapColorHexCode());
        }

        JsonObject timestampObject = new JsonObject();
        timestampObject.addProperty("registered", town.getRegistered());
        if (town.getNationOrNull() != null) {
            timestampObject.addProperty("joinedNationAt", town.getJoinedNationAt());
        }
        json.add("timestamps", timestampObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("isPublic", town.isPublic());
        statusObject.addProperty("isOpen", town.isOpen());
        statusObject.addProperty("isNeutral", town.isNeutral());
        statusObject.addProperty("isCapital", town.isCapital());
        statusObject.addProperty("isOverClaimed", town.isOverClaimed());
        statusObject.addProperty("isRuined", town.isRuined());
        statusObject.addProperty("isForSale", town.isForSale());
        json.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("numTownBlocks", town.getNumTownBlocks());
        statsObject.addProperty("maxTownBlocks", town.getMaxTownBlocks());
        statsObject.addProperty("numResidents", town.getNumResidents());
        if (TownyEconomyHandler.isActive()) {
            statsObject.addProperty("balance", useCache ? town.getAccount().getCachedBalance() : town.getAccount().getHoldingBalance());
        } else {
            statsObject.addProperty("balance", 0);
        }
        if (town.isForSale()) {
            statsObject.addProperty("forSalePrice", town.getForSalePrice());
        }
        json.add("stats", statsObject);

        JsonObject permsObject = new JsonObject();
        JsonObject rnaoPermsObject = new JsonObject();
        JsonArray buildPermsArray = new JsonArray();
        buildPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.BUILD));
        buildPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.BUILD));
        buildPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.BUILD));
        buildPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.BUILD));
        rnaoPermsObject.add("buildPerms", buildPermsArray);

        JsonArray destroyPermsArray = new JsonArray();
        destroyPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.DESTROY));
        destroyPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.DESTROY));
        destroyPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.DESTROY));
        destroyPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.DESTROY));
        rnaoPermsObject.add("destroyPerms", destroyPermsArray);

        JsonArray switchPermsArray = new JsonArray();
        switchPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.SWITCH));
        switchPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.SWITCH));
        switchPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.SWITCH));
        switchPermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.SWITCH));
        rnaoPermsObject.add("switchPerms", switchPermsArray);

        JsonArray itemUsePermsArray = new JsonArray();
        itemUsePermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.ITEM_USE));
        itemUsePermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.ITEM_USE));
        itemUsePermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.ITEM_USE));
        itemUsePermsArray.add(town.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.ITEM_USE));
        rnaoPermsObject.add("itemUsePerms", itemUsePermsArray);
        permsObject.add("rnaoPerms", rnaoPermsObject);

        JsonObject flagPermsObject = new JsonObject();
        flagPermsObject.addProperty("pvp", town.getPermissions().pvp);
        flagPermsObject.addProperty("explosion", town.getPermissions().explosion);
        flagPermsObject.addProperty("fire", town.getPermissions().fire);
        flagPermsObject.addProperty("mobs", town.getPermissions().mobs);
        permsObject.add("flagPerms", flagPermsObject);
        json.add("perms", permsObject);

        JsonObject coordinatesObject = new JsonObject();
        if (town.hasSpawn()) {
            coordinatesObject.add("spawn", v2NationLookup.serializeLocation(town.getSpawnOrNull()));
        }

        if (town.hasHomeBlock()) {
            JsonArray homeArray = new JsonArray();
            homeArray.add(town.getHomeBlockOrNull().getX());
            homeArray.add(town.getHomeBlockOrNull().getZ());
            coordinatesObject.add("home", homeArray);
        }

        if (!town.getTownBlocks().isEmpty()) {
            JsonObject townBlocksObject = new JsonObject();
            JsonArray xArray = new JsonArray();
            JsonArray zArray = new JsonArray();
            for (TownBlock townBlock : town.getTownBlocks()) {
                xArray.add(townBlock.getX());
                zArray.add(townBlock.getZ());
            }
            townBlocksObject.add("x", xArray);
            townBlocksObject.add("z", zArray);
            coordinatesObject.add("townBlocks", townBlocksObject);
        }
        if (!coordinatesObject.asMap().isEmpty()) {
            json.add("coordinates", coordinatesObject);
        }

        JsonArray residentsArray = new JsonArray();
        for (Resident resident : town.getResidents()) {
            residentsArray.add(resident.getName());
        }
        json.add("residents", residentsArray);

        JsonObject ranksObject = new JsonObject();
        for (String rank : TownyPerms.getTownRanks()) {
            JsonArray currentRankArray = new JsonArray();
            for (Resident resident : town.getRank(rank)) {
                currentRankArray.add(resident.getName());
            }

            if (!currentRankArray.isEmpty()) {
                ranksObject.add(rank, currentRankArray);
            }
        }
        if (!ranksObject.asMap().isEmpty()) {
            json.add("ranks", ranksObject);
        }

        JsonArray trustedArray = new JsonArray();
        for (Resident resident : town.getTrustedResidents()) {
            trustedArray.add(resident.getName());
        }

        if (!town.getTrustedResidents().isEmpty()) {
            json.add("trusted", trustedArray);
        }

        JsonArray outlawsArray = new JsonArray();
        for (Resident outlaw : town.getOutlaws()) {
            outlawsArray.add(outlaw.getName());
        }
        if (!town.getOutlaws().isEmpty()) {
            json.add("outlaws", outlawsArray);
        }

        return json;
    }

    public static String allTownsBulk() { // /routeRoot/towns/
        JsonArray json = new JsonArray();

        for (Town town : TownyUniverse.getInstance().getTowns()) {
            json.add(getTownData(town, true));
        }

        return json.toString();
    }

    public static String townLookup(String name) { // /routeRoot/towns/{name}/
        Town town = null;

        try {
            town = TownyAPI.getInstance().getTown(UUID.fromString(name));
        } catch (IllegalArgumentException e) {
            town = TownyAPI.getInstance().getTown(name);
        }

        if (town != null) {
            return getTownData(town, false).toString();
        } else {
            throw new NotFoundResponse();
        }
    }
}
