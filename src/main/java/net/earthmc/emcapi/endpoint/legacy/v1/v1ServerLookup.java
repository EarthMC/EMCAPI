package net.earthmc.emcapi.endpoint.legacy.v1;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

public class v1ServerLookup {
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
        playersObject.addProperty("numOnlinePlayers", Bukkit.getOnlinePlayers().size());
        json.add("players", playersObject);

        return json.toString();
    }
}
