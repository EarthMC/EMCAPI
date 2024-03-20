package net.earthmc.emcapi.endpoint.legacy.v1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import io.javalin.http.NotFoundResponse;

public class v1TownLookup {
    public static String townLookup(String name) throws NotRegisteredException {
        JsonObject json = new JsonObject();
        Town town = TownyAPI.getInstance().getTown(name);

        if (town != null) {

            JsonObject stringsObject = new JsonObject();
            stringsObject.addProperty("town", town.getName());
            stringsObject.addProperty("board", town.getBoard());

            if (town.hasMayor())
                stringsObject.addProperty("mayor", town.getMayor().getName());

            stringsObject.addProperty("founder", town.getFounder());
            stringsObject.addProperty("mapColorHexCode", town.getMapColorHexCode());
            json.add("strings", stringsObject);

            JsonObject affiliationObject = new JsonObject();
            if (town.hasNation())
                affiliationObject.addProperty("nation", town.getNationOrNull().getName());

            json.add("affiliation", affiliationObject);

            JsonObject timestampObject = new JsonObject();
            timestampObject.addProperty("registered", town.getRegistered());
            timestampObject.addProperty("joinedNationAt", town.getJoinedNationAt());
            json.add("timestamps", timestampObject);

            JsonObject statusObject = new JsonObject();
            statusObject.addProperty("isPublic", town.isPublic());
            statusObject.addProperty("isOpen", town.isOpen());
            statusObject.addProperty("isNeutral", town.isNeutral());
            statusObject.addProperty("isCapital", town.isCapital());
            statusObject.addProperty("isOverClaimed", town.isOverClaimed());
            statusObject.addProperty("isRuined", town.isRuined());
            json.add("status", statusObject);

            JsonObject statsObject = new JsonObject();
            statsObject.addProperty("numTownBlocks", town.getNumTownBlocks());
            statsObject.addProperty("maxTownBlocks", town.getMaxTownBlocks());
            statsObject.addProperty("numResidents", town.getNumResidents());
            statsObject.addProperty("balance", TownyEconomyHandler.isActive() ? town.getAccount().getHoldingBalance() : 0);
            json.add("stats", statsObject);

            JsonObject permsObject = new JsonObject();
            JsonObject rnaoPermsObject = new JsonObject();
            JsonObject buildPermsObject = new JsonObject();
            buildPermsObject.addProperty("resident", town.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.BUILD));
            buildPermsObject.addProperty("nation", town.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.BUILD));
            buildPermsObject.addProperty("ally", town.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.BUILD));
            buildPermsObject.addProperty("outsider", town.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.BUILD));
            rnaoPermsObject.add("buildPerms", buildPermsObject);

            JsonObject destroyPermsObject = new JsonObject();
            destroyPermsObject.addProperty("resident", town.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.DESTROY));
            destroyPermsObject.addProperty("nation", town.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.DESTROY));
            destroyPermsObject.addProperty("ally", town.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.DESTROY));
            destroyPermsObject.addProperty("outsider", town.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.DESTROY));
            rnaoPermsObject.add("destroyPerms", destroyPermsObject);

            JsonObject switchPermsObject = new JsonObject();
            switchPermsObject.addProperty("resident", town.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.SWITCH));
            switchPermsObject.addProperty("nation", town.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.SWITCH));
            switchPermsObject.addProperty("ally", town.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.SWITCH));
            switchPermsObject.addProperty("outsider", town.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.SWITCH));
            rnaoPermsObject.add("switchPerms", switchPermsObject);

            JsonObject itemUsePermsObject = new JsonObject();
            itemUsePermsObject.addProperty("resident", town.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.ITEM_USE));
            itemUsePermsObject.addProperty("nation", town.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.ITEM_USE));
            itemUsePermsObject.addProperty("ally", town.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.ITEM_USE));
            itemUsePermsObject.addProperty("outsider", town.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.ITEM_USE));
            rnaoPermsObject.add("itemUsePerms", itemUsePermsObject);
            permsObject.add("rnaoPerms", rnaoPermsObject);

            JsonObject flagPermsObject = new JsonObject();
            flagPermsObject.addProperty("pvp", town.getPermissions().pvp);
            flagPermsObject.addProperty("explosion", town.getPermissions().explosion);
            flagPermsObject.addProperty("fire", town.getPermissions().fire);
            flagPermsObject.addProperty("mobs", town.getPermissions().mobs);
            permsObject.add("flagPerms", flagPermsObject);
            json.add("perms", permsObject);

            JsonObject spawnObject = new JsonObject();
            if (town.hasSpawn()) {
                spawnObject.addProperty("x", town.getSpawnOrNull().getX());
                spawnObject.addProperty("y", town.getSpawnOrNull().getY());
                spawnObject.addProperty("z", town.getSpawnOrNull().getZ());
            }
            json.add("spawn", spawnObject);

            JsonObject homeObject = new JsonObject();
            if (town.hasHomeBlock()) {
                homeObject.addProperty("x", town.getHomeBlockOrNull().getX());
                homeObject.addProperty("z", town.getHomeBlockOrNull().getZ());
            }
            json.add("home", homeObject);

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
                ranksObject.add(rank, currentRankArray);
            }
            json.add("ranks", ranksObject);

            JsonArray trustedArray = new JsonArray();
            for (Resident resident : town.getTrustedResidents()) {
                trustedArray.add(resident.getName());
            }
            json.add("trusted", trustedArray);

            JsonArray outlawsArray = new JsonArray();
            for (Resident outlaw : town.getOutlaws()) {
                outlawsArray.add(outlaw.getName());
            }
            json.add("outlaws", outlawsArray);

        } else {
            throw new NotFoundResponse();
        }
        return json.toString();
    }
}
