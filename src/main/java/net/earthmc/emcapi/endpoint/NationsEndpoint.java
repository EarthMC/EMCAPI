package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import io.javalin.http.NotFoundResponse;
import net.earthmc.emcapi.manager.NationMetadataManager;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.configuration.file.FileConfiguration;

public class NationsEndpoint {
    private final FileConfiguration config;

    public NationsEndpoint(FileConfiguration config) {
        this.config = config;
    }

    public String lookup(String query) {
        String[] split = query.split(",");

        if (split.length == 1) {
            String name = split[0];
            Nation nation = EndpointUtils.getNationOrNull(name);

            if (nation != null) {
                return getNationObject(nation).toString();
            } else {
                throw new NotFoundResponse(name + " is not a real nation");
            }
        } else {
            JsonArray jsonArray = new JsonArray();
            for (int i = 0; i < Math.min(config.getInt("behaviour.max_lookup_size"), split.length); i++) {
                String name = split[i];
                Nation nation = EndpointUtils.getNationOrNull(name);

                if (nation != null) {
                    jsonArray.add(getNationObject(nation));
                } else {
                    throw new NotFoundResponse(name + " is not a real nation");
                }
            }

            return jsonArray.toString();
        }
    }

    private JsonObject getNationObject(Nation nation) {
        JsonObject nationObject = new JsonObject();

        nationObject.addProperty("name", nation.getName());
        nationObject.addProperty("uuid", nation.getUUID().toString());
        nationObject.addProperty("king", nation.getKing().getName());
        nationObject.addProperty("board", nation.getBoard().isEmpty() ? null : nation.getBoard());
        nationObject.addProperty("capital", nation.getCapital().getName());
        nationObject.addProperty("dynmapColour", NationMetadataManager.getDynmapColour(nation));
        nationObject.addProperty("dynmapOutline", NationMetadataManager.getDynmapOutline(nation));

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("registered", nation.getRegistered());
        nationObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("isPublic", nation.isPublic());
        statusObject.addProperty("isOpen", nation.isOpen());
        statusObject.addProperty("isNeutral", nation.isNeutral());
        nationObject.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("numTownBlocks", nation.getNumTownblocks());
        statsObject.addProperty("numResidents", nation.getNumResidents());
        statsObject.addProperty("numTowns", nation.getNumTowns());
        statsObject.addProperty("numAllies", nation.getAllies().size());
        statsObject.addProperty("numEnemies", nation.getEnemies().size());
        statsObject.addProperty("balance", TownyEconomyHandler.isActive() ? nation.getAccount().getHoldingBalance() : 0);
        nationObject.add("stats", statsObject);

        nationObject.add("coordinates", EndpointUtils.getCoordinatesObject(nation.getSpawnOrNull()));

        JsonArray residentsArray = new JsonArray();
        for (Resident resident : nation.getResidents()) {
            residentsArray.add(resident.getName());
        }
        nationObject.add("residents", residentsArray);

        JsonObject ranksObject = new JsonObject();
        for (String rank : TownyPerms.getNationRanks()) {
            JsonArray rankArray = new JsonArray();
            for (Resident resident : EndpointUtils.getNationRank(nation, rank)) {
                rankArray.add(resident.getName());
            }
            ranksObject.add(rank, rankArray.isEmpty() ? null : rankArray);
        }
        nationObject.add("ranks", ranksObject);

        JsonArray townsArray = new JsonArray();
        for (Town town : nation.getTowns()) {
            townsArray.add(town.getName());
        }
        nationObject.add("towns", townsArray);

        JsonArray alliesArray = new JsonArray();
        for (Nation ally : nation.getAllies()) {
            alliesArray.add(ally.getName());
        }
        nationObject.add("allies", alliesArray.isEmpty() ? null : alliesArray);

        JsonArray enemiesArray = new JsonArray();
        for (Nation enemy : nation.getEnemies()) {
            enemiesArray.add(enemy.getName());
        }
        nationObject.add("enemies", enemiesArray.isEmpty() ? null : enemiesArray);

        return nationObject;
    }
}
