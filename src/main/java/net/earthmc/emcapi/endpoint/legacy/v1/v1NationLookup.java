package net.earthmc.emcapi.endpoint.legacy.v1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import io.javalin.http.NotFoundResponse;
import net.earthmc.emcapi.util.EndpointUtils;

public class v1NationLookup {
    public static String nationLookup(String name) {
        JsonObject json = new JsonObject();
        Nation nation = TownyAPI.getInstance().getNation(name);

        if (nation != null) {

            JsonObject stringsObject = new JsonObject();
            stringsObject.addProperty("nation", nation.getName());
            stringsObject.addProperty("board", nation.getBoard());
            stringsObject.addProperty("king", nation.getKing().getName());
            stringsObject.addProperty("capital", nation.getCapital().getName());
            stringsObject.addProperty("mapColorHexCode", nation.getMapColorHexCode());
            json.add("strings", stringsObject);

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
            statsObject.addProperty("balance", TownyEconomyHandler.isActive() ? nation.getAccount().getHoldingBalance() : 0);
            json.add("stats", statsObject);

            JsonObject spawnObject = new JsonObject();
            if (nation.hasSpawn()) {
                spawnObject.addProperty("x", nation.getSpawnOrNull().getX());
                spawnObject.addProperty("y", nation.getSpawnOrNull().getY());
                spawnObject.addProperty("z", nation.getSpawnOrNull().getZ());
            }
            json.add("spawn", spawnObject);

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
                ranksObject.add(rank, currentRankArray);
            }
            json.add("ranks", ranksObject);

            JsonArray townsArray = new JsonArray();
            for (Town town : nation.getTowns()) {
                townsArray.add(town.getName());
            }
            json.add("towns", townsArray);

            JsonArray alliesArray = new JsonArray();
            for (Nation ally : nation.getAllies()) {
                alliesArray.add(ally.getName());
            }
            json.add("allies", alliesArray);

            JsonArray enemiesArray = new JsonArray();
            for (Nation enemy : nation.getEnemies()) {
                enemiesArray.add(enemy.getName());
            }
            json.add("enemies", enemiesArray);

        } else {
            throw new NotFoundResponse();
        }
        return json.toString();
    }
}
