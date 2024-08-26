package net.earthmc.emcapi.util;

import au.lupine.quarters.object.entity.Quarter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyPermission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class EndpointUtils {

    public static int getNumOnlineNomads() {
        int numOnlineNomads = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Resident resident = TownyAPI.getInstance().getResident(player);
            if (resident != null && !resident.hasTown() && resident.isOnline()) {
                numOnlineNomads++;
            }
        }

        return numOnlineNomads;
    }

    public static JsonObject getPermsObject(TownyPermission permissions) {
        JsonObject permsObject = new JsonObject();

        permsObject.add("build", getPermArray(permissions, TownyPermission.ActionType.BUILD));
        permsObject.add("destroy", getPermArray(permissions, TownyPermission.ActionType.DESTROY));
        permsObject.add("switch", getPermArray(permissions, TownyPermission.ActionType.SWITCH));
        permsObject.add("itemUse", getPermArray(permissions, TownyPermission.ActionType.ITEM_USE));

        JsonObject flagsObject = new JsonObject();
        flagsObject.addProperty("pvp", permissions.pvp);
        flagsObject.addProperty("explosion", permissions.explosion);
        flagsObject.addProperty("fire", permissions.fire);
        flagsObject.addProperty("mobs", permissions.mobs);
        permsObject.add("flags", flagsObject);

        return permsObject;
    }

    private static JsonArray getPermArray(TownyPermission permissions, TownyPermission.ActionType actionType) {
        JsonArray jsonArray = new JsonArray();

        jsonArray.add(permissions.getPerm(TownyPermission.PermLevel.RESIDENT, actionType));
        jsonArray.add(permissions.getPerm(TownyPermission.PermLevel.NATION, actionType));
        jsonArray.add(permissions.getPerm(TownyPermission.PermLevel.ALLY, actionType));
        jsonArray.add(permissions.getPerm(TownyPermission.PermLevel.OUTSIDER, actionType));

        return jsonArray;
    }

    public static JsonObject getCoordinatesObject(@Nullable Location location) {
        JsonObject coordinatesObject = new JsonObject();

        JsonObject spawnObject = new JsonObject();
        spawnObject.addProperty("world", location == null ? null : location.getWorld().getName());
        spawnObject.addProperty("x", location == null ? null : location.getX());
        spawnObject.addProperty("y", location == null ? null : location.getY());
        spawnObject.addProperty("z", location == null ? null : location.getZ());
        spawnObject.addProperty("pitch", location == null ? null : location.getPitch());
        spawnObject.addProperty("yaw", location == null ? null : location.getYaw());

        coordinatesObject.add("spawn", spawnObject);

        return coordinatesObject;
    }

    public static List<Resident> getNationRank(Nation nation, String rank) {
        List<Resident> residentsWithRank = new ArrayList<>();

        for (Resident resident : nation.getResidents()) {
            if (resident.hasNationRank(rank))
                residentsWithRank.add(resident);
        }

        return Collections.unmodifiableList(residentsWithRank);
    }

    public static JsonArray getResidentArray(List<Resident> residents) {
        JsonArray jsonArray = new JsonArray();

        for (Resident resident : residents) {
            jsonArray.add(getResidentJsonObject(resident));
        }

        return jsonArray;
    }

    public static JsonObject getResidentJsonObject(Resident resident) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", resident == null ? null : resident.getName());
        jsonObject.addProperty("uuid", resident == null ? null : resident.getUUID().toString());

        return jsonObject;
    }

    public static JsonArray getTownArray(List<Town> towns) {
        JsonArray jsonArray = new JsonArray();

        for (Town town : towns) {
            jsonArray.add(getTownJsonObject(town));
        }

        return jsonArray;
    }

    public static JsonObject getTownJsonObject(Town town) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", town == null ? null : town.getName());
        jsonObject.addProperty("uuid", town == null ? null : town.getUUID().toString());

        return jsonObject;
    }

    public static JsonArray getNationArray(List<Nation> nations) {
        JsonArray jsonArray = new JsonArray();

        for (Nation nation : nations) {
            jsonArray.add(getNationJsonObject(nation));
        }

        return jsonArray;
    }

    public static JsonObject getNationJsonObject(Nation nation) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", nation == null ? null : nation.getName());
        jsonObject.addProperty("uuid", nation == null ? null : nation.getUUID().toString());

        return jsonObject;
    }

    public static JsonArray getQuarterArray(List<Quarter> quarters) {
        JsonArray jsonArray = new JsonArray();

        for (Quarter quarter : quarters) {
            jsonArray.add(getQuarterObject(quarter));
        }

        return jsonArray;
    }

    public static JsonObject getQuarterObject(Quarter quarter) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", quarter.getName());
        jsonObject.addProperty("uuid", quarter.getUUID().toString());

        return jsonObject;
    }

    public static JsonObject generateNameUUIDJsonObject(String name, UUID uuid) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("uuid", uuid.toString());
        return jsonObject;
    }
}
