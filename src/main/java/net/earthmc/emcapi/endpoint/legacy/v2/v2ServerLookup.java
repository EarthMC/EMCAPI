package net.earthmc.emcapi.endpoint.legacy.v2;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.Bukkit;

public class v2ServerLookup {
    public static String serverLookup() {
        JsonObject json = new JsonObject();
        String overworld = Bukkit.getWorlds().get(0).getName();

        JsonObject worldObject = new JsonObject();
        worldObject.addProperty("hasStorm", Bukkit.getWorld(overworld).hasStorm());
        worldObject.addProperty("isThundering", Bukkit.getWorld(overworld).isThundering());
        worldObject.addProperty("time", Bukkit.getWorld(overworld).getTime());
        worldObject.addProperty("fullTime", Bukkit.getWorld(overworld).getFullTime());
        json.add("world", worldObject);

        JsonObject playersObject = new JsonObject();
        playersObject.addProperty("maxPlayers", Bukkit.getMaxPlayers());
        playersObject.addProperty("numOnlineTownless", EndpointUtils.getNumOnlineNomads());
        playersObject.addProperty("numOnlinePlayers", Bukkit.getOnlinePlayers().size());
        json.add("players", playersObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("numResidents", TownyUniverse.getInstance().getResidents().size());
        statsObject.addProperty("numTownless", TownyAPI.getInstance().getResidentsWithoutTown().size());
        statsObject.addProperty("numTowns", TownyUniverse.getInstance().getTowns().size());
        statsObject.addProperty("numNations", TownyUniverse.getInstance().getNations().size());
        statsObject.addProperty("numTownBlocks", TownyUniverse.getInstance().getTownBlocks().size());
        json.add("stats", statsObject);

        return json.toString();
    }
}
