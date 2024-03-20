package net.earthmc.emcapi.endpoint.legacy.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import io.javalin.http.NotFoundResponse;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class v2NationLookup {
    public static JsonObject getNationData(Nation nation, boolean useCache) {
        JsonObject json = new JsonObject();

        json.addProperty("name", nation.getName());
        json.addProperty("uuid", nation.getUUID().toString());
        if (nation.hasKing()) {
            json.addProperty("king", nation.getKing().getName());
        }
        if (!nation.getBoard().isEmpty()) {
            json.addProperty("board", nation.getBoard());
        }
        json.addProperty("capital", nation.getCapital().getName());
        if (!nation.getMapColorHexCode().isEmpty()) {
            json.addProperty("mapColorHexCode", nation.getMapColorHexCode());
        }

        JsonObject timestampObject = new JsonObject();
        timestampObject.addProperty("registered", nation.getRegistered());
        json.add("timestamps", timestampObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("isPublic", nation.isPublic());
        statusObject.addProperty("isOpen", nation.isOpen());
        statusObject.addProperty("isNeutral", nation.isNeutral());
        json.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("numTownBlocks", nation.getNumTownblocks());
        statsObject.addProperty("numResidents", nation.getNumResidents());
        statsObject.addProperty("numTowns", nation.getNumTowns());
        if (TownyEconomyHandler.isActive()) {
            statsObject.addProperty("balance", useCache ? nation.getAccount().getCachedBalance() : nation.getAccount().getHoldingBalance());
        } else {
            statsObject.addProperty("balance", 0);
        }
        json.add("stats", statsObject);

        if (nation.hasSpawn()) {
            json.add("spawn", serializeLocation(nation.getSpawnOrNull()));
        }

        JsonArray residentsArray = new JsonArray();
        for (Resident resident : nation.getResidents()) {
            residentsArray.add(resident.getName());
        }
        json.add("residents", residentsArray);

        JsonObject ranksObject = new JsonObject();
        for (String rank : TownyPerms.getNationRanks()) {
            JsonArray currentRankArray = new JsonArray();
            for (Resident resident : EndpointUtils.getNationRank(nation, rank)) {
                currentRankArray.add(resident.getName());
            }

            if (!currentRankArray.isEmpty()) {
                ranksObject.add(rank, currentRankArray);
            }
        }
        if (!ranksObject.asMap().isEmpty()) {
            json.add("ranks", ranksObject);
        }

        JsonArray townsArray = new JsonArray();
        for (Town town : nation.getTowns()) {
            townsArray.add(town.getName());
        }
        json.add("towns", townsArray);

        JsonArray alliesArray = new JsonArray();
        for (Nation ally : nation.getAllies()) {
            alliesArray.add(ally.getName());
        }
        if (!alliesArray.isEmpty()) {
            json.add("allies", alliesArray);
        }

        JsonArray enemiesArray = new JsonArray();
        for (Nation enemy : nation.getEnemies()) {
            enemiesArray.add(enemy.getName());
        }
        if (!enemiesArray.isEmpty()) {
            json.add("enemies", enemiesArray);
        }

        return json;
    }

    public static String allNationsBulk() {
        JsonArray json = new JsonArray();

        for (Nation nation : TownyUniverse.getInstance().getNations()) {
            json.add(getNationData(nation, true));
        }

        return json.toString();
    }

    public static String nationLookup(String name) {
        Nation nation = null;

        try {
            nation = TownyAPI.getInstance().getNation(UUID.fromString(name));
        } catch (IllegalArgumentException e) {
            nation = TownyAPI.getInstance().getNation(name);
        }

        if (nation != null) {
            return getNationData(nation, false).toString();
        } else {
            throw new NotFoundResponse();
        }
    }

    public static JsonObject serializeLocation(@NotNull Location location) {
        JsonObject spawn = new JsonObject();
        spawn.addProperty("world", location.getWorld().getName());
        spawn.addProperty("x", location.getX());
        spawn.addProperty("y", location.getY());
        spawn.addProperty("z", location.getZ());
        spawn.addProperty("pitch", location.getPitch());
        spawn.addProperty("yaw", location.getYaw());

        return spawn;
    }
}
