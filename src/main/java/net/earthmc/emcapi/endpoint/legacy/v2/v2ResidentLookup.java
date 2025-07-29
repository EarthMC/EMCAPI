package net.earthmc.emcapi.endpoint.legacy.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyPermission;
import io.javalin.http.NotFoundResponse;

import java.util.UUID;

public class v2ResidentLookup {
    public static JsonObject getResidentData(Resident resident, boolean useCache) {
        JsonObject json = new JsonObject();

        json.addProperty("name", resident.getName());
        json.addProperty("uuid", resident.getUUID().toString());
        if (!resident.getTitle().isEmpty()) {
            json.addProperty("title", resident.getTitle());
        }
        if (!resident.getSurname().isEmpty()) {
            json.addProperty("surname", resident.getSurname());
        }
        if (!resident.getAbout().isEmpty()) {
            json.addProperty("about", resident.getAbout());
        }
        if (resident.getTownOrNull() != null) {
            json.addProperty("town", resident.getTownOrNull().getName());
            if (resident.getNationOrNull() != null) {
                json.addProperty("nation", resident.getNationOrNull().getName());
            }
        }

        JsonObject timestampObject = new JsonObject();
        timestampObject.addProperty("registered", resident.getRegistered());
        if (resident.getTownOrNull() != null) {
            timestampObject.addProperty("joinedTownAt", resident.getJoinedTownAt());
        }
        if (!resident.isNPC()) {
            timestampObject.addProperty("lastOnline", resident.getLastOnline());
        }
        json.add("timestamps", timestampObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("isOnline", resident.isOnline());
        statusObject.addProperty("isNPC", resident.isNPC());
        json.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        if (TownyEconomyHandler.isActive()) {
            statsObject.addProperty("balance", resident.isOnline() && !useCache ? resident.getAccount().getHoldingBalance() : resident.getAccount().getCachedBalance());
        } else {
            statsObject.addProperty("balance", 0);
        }
        json.add("stats", statsObject);

        JsonObject permsObject = new JsonObject();
        JsonObject rnaoPermsObject = new JsonObject();
        JsonArray buildPermsArray = new JsonArray();
        buildPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.BUILD));
        buildPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.BUILD));
        buildPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.BUILD));
        buildPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.BUILD));
        rnaoPermsObject.add("buildPerms", buildPermsArray);

        JsonArray destroyPermsArray = new JsonArray();
        destroyPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.DESTROY));
        destroyPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.DESTROY));
        destroyPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.DESTROY));
        destroyPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.DESTROY));
        rnaoPermsObject.add("destroyPerms", destroyPermsArray);

        JsonArray switchPermsArray = new JsonArray();
        switchPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.SWITCH));
        switchPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.SWITCH));
        switchPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.SWITCH));
        switchPermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.SWITCH));
        rnaoPermsObject.add("switchPerms", switchPermsArray);

        JsonArray itemUsePermsArray = new JsonArray();
        itemUsePermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.RESIDENT, TownyPermission.ActionType.ITEM_USE));
        itemUsePermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.NATION, TownyPermission.ActionType.ITEM_USE));
        itemUsePermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.ALLY, TownyPermission.ActionType.ITEM_USE));
        itemUsePermsArray.add(resident.getPermissions().getPerm(TownyPermission.PermLevel.OUTSIDER, TownyPermission.ActionType.ITEM_USE));
        rnaoPermsObject.add("itemUsePerms", itemUsePermsArray);
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
        if (!nationRanksArray.isEmpty()) {
            ranksObject.add("nationRanks", nationRanksArray);
        }

        JsonArray townRanksArray = new JsonArray();
        for (String rank : resident.getTownRanks()) {
            townRanksArray.add(rank);
        }
        if (!townRanksArray.isEmpty()) {
            ranksObject.add("townRanks", townRanksArray);
        }
        if (!ranksObject.asMap().isEmpty()) {
            json.add("ranks", ranksObject);
        }

        JsonArray friendsArray = new JsonArray();
        for (Resident friend : resident.getFriends()) {
            friendsArray.add(friend.getName());
        }
        if (!resident.getFriends().isEmpty()) {
            json.add("friends", friendsArray);
        }

        return json;
    }

    public static String allResidentsBulk() {
        JsonArray json = new JsonArray();

        for (Resident resident : TownyUniverse.getInstance().getResidents()) {
            json.add(getResidentData(resident, true));
        }

        return json.toString();
    }

    public static String residentLookup(String name) {
        Resident resident = null;

        try {
            resident = TownyAPI.getInstance().getResident(UUID.fromString(name));
        } catch (IllegalArgumentException e) {
            resident = TownyAPI.getInstance().getResident(name);
        }

        if (resident != null) {
            return getResidentData(resident, false).toString();
        } else {
            throw new NotFoundResponse();
        }
    }
}
