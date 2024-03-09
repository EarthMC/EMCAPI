package net.earthmc.emcapi.util;

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

    public static Resident getResidentOrNull(String name) {
        Resident resident;

        try {
            resident = TownyAPI.getInstance().getResident(UUID.fromString(name));
        } catch (IllegalArgumentException e) {
            resident = TownyAPI.getInstance().getResident(name);
        }

        return resident;
    }

    public static Town getTownOrNull(String name) {
        Town town;

        try {
            town = TownyAPI.getInstance().getTown(UUID.fromString(name));
        } catch (IllegalArgumentException e) {
            town = TownyAPI.getInstance().getTown(name);
        }

        return town;
    }

    public static Nation getNationOrNull(String name) {
        Nation nation;

        try {
            nation = TownyAPI.getInstance().getNation(UUID.fromString(name));
        } catch (IllegalArgumentException e) {
            nation = TownyAPI.getInstance().getNation(name);
        }

        return nation;
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
        if (location != null) {
            spawnObject.addProperty("world", location.getWorld().getName());
            spawnObject.addProperty("x", location.getX());
            spawnObject.addProperty("y", location.getY());
            spawnObject.addProperty("z", location.getZ());
            spawnObject.addProperty("pitch", location.getPitch());
            spawnObject.addProperty("yaw", location.getYaw());
            coordinatesObject.add("spawn", coordinatesObject);
        } else {
            coordinatesObject.add("spawn", null);
        }

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
}
