package net.earthmc.emcapi.endpoint.legacy.v1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyPermission;
import io.javalin.http.NotFoundResponse;

import java.util.UUID;

public class v1ResidentLookup {
    public static String residentLookup(String name) {
        JsonObject json = new JsonObject();
        Resident resident = null;

        try {
            UUID uuid = UUID.fromString(name);
            resident = TownyAPI.getInstance().getResident(uuid);
        } catch (Exception e) {
            resident = TownyAPI.getInstance().getResident(name);
        }

        if (resident != null) {

            JsonObject stringsObject = new JsonObject();
            stringsObject.addProperty("title", resident.getTitle());
            stringsObject.addProperty("username", resident.getName());
            stringsObject.addProperty("surname", resident.getSurname());
            json.add("strings", stringsObject);

            JsonObject affiliationObject = new JsonObject();

            if (resident.hasTown())
                affiliationObject.addProperty("town", resident.getTownOrNull().getName());

            if (resident.hasNation())
                affiliationObject.addProperty("nation", resident.getNationOrNull().getName());

            json.add("affiliation", affiliationObject);

            JsonObject timestampObject = new JsonObject();
            timestampObject.addProperty("joinedTownAt", resident.getJoinedTownAt());
            timestampObject.addProperty("registered", resident.getRegistered());
            timestampObject.addProperty("lastOnline", resident.getLastOnline());
            json.add("timestamps", timestampObject);

            JsonObject statusObject = new JsonObject();
            statusObject.addProperty("isOnline", resident.isOnline());
            json.add("status", statusObject);

            JsonObject statsObject = new JsonObject();
            statsObject.addProperty("balance", TownyEconomyHandler.isActive() ? (resident.isOnline() ? resident.getAccount().getHoldingBalance() : resident.getAccount().getCachedBalance()) : 0);
            json.add("stats", statsObject);

            JsonObject permsObject = new JsonObject();
            JsonObject rnaoPermsObject = new JsonObject();
            JsonObject buildPermsObject = new JsonObject();
            buildPermsObject.addProperty("friend", resident.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.BUILD));
            buildPermsObject.addProperty("town", resident.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.BUILD));
            buildPermsObject.addProperty("ally", resident.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.BUILD));
            buildPermsObject.addProperty("outsider", resident.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.BUILD));
            rnaoPermsObject.add("buildPerms", buildPermsObject);

            JsonObject destroyPermsObject = new JsonObject();
            destroyPermsObject.addProperty("friend", resident.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.DESTROY));
            destroyPermsObject.addProperty("town", resident.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.DESTROY));
            destroyPermsObject.addProperty("ally", resident.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.DESTROY));
            destroyPermsObject.addProperty("outsider", resident.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.DESTROY));
            rnaoPermsObject.add("destroyPerms", destroyPermsObject);

            JsonObject switchPermsObject = new JsonObject();
            switchPermsObject.addProperty("friend", resident.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.SWITCH));
            switchPermsObject.addProperty("town", resident.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.SWITCH));
            switchPermsObject.addProperty("ally", resident.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.SWITCH));
            switchPermsObject.addProperty("outsider", resident.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.SWITCH));
            rnaoPermsObject.add("switchPerms", switchPermsObject);

            JsonObject itemUsePermsObject = new JsonObject();
            itemUsePermsObject.addProperty("friend", resident.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.ITEM_USE));
            itemUsePermsObject.addProperty("town", resident.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.ITEM_USE));
            itemUsePermsObject.addProperty("ally", resident.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.ITEM_USE));
            itemUsePermsObject.addProperty("outsider", resident.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.ITEM_USE));
            rnaoPermsObject.add("itemUsePerms", itemUsePermsObject);
            permsObject.add("rnaoPerms", rnaoPermsObject);

            JsonObject flagPermsObject = new JsonObject();
            flagPermsObject.addProperty("pvp", resident.getPermissions().pvp);
            flagPermsObject.addProperty("explosion", resident.getPermissions().explosion);
            flagPermsObject.addProperty("fire", resident.getPermissions().fire);
            flagPermsObject.addProperty("mobs", resident.getPermissions().mobs);
            permsObject.add("flagPerms", flagPermsObject);
            json.add("perms", permsObject);

            JsonObject ranksObject = new JsonObject();
            JsonArray nationRanksArray = new JsonArray();
            for (String rank : resident.getNationRanks()) {
                nationRanksArray.add(rank);
            }
            ranksObject.add("nationRanks", nationRanksArray);

            JsonArray townRanksArray = new JsonArray();
            for (String rank : resident.getTownRanks()) {
                townRanksArray.add(rank);
            }
            ranksObject.add("townRanks", townRanksArray);
            json.add("ranks", ranksObject);

            JsonArray friendsArray = new JsonArray();
            for (Resident friend : resident.getFriends()) {
                friendsArray.add(friend.getName());
            }
            json.add("friends", friendsArray);

        } else {
            throw new NotFoundResponse();
        }
        return json.toString();
    }
}
