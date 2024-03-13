package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.quarters.api.QuartersAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ServerEndpoint {

    public String lookup() {
        JsonObject jsonObject = new JsonObject();

        TownyAPI townyAPI = TownyAPI.getInstance();
        QuartersAPI quartersAPI = QuartersAPI.getInstance();

        JsonObject worldObject = new JsonObject();
        World overworld = Bukkit.getWorlds().get(0);
        worldObject.addProperty("hasStorm", overworld.hasStorm());
        worldObject.addProperty("isThundering", overworld.isThundering());
        worldObject.addProperty("time", overworld.getTime());
        worldObject.addProperty("fullTime", overworld.getFullTime());
        worldObject.addProperty("moonPhase", overworld.getMoonPhase().toString());
        jsonObject.add("world", worldObject);

        JsonObject playersObject = new JsonObject();
        playersObject.addProperty("maxPlayers", Bukkit.getMaxPlayers());
        playersObject.addProperty("numOnlinePlayers", Bukkit.getOnlinePlayers().size());
        playersObject.addProperty("numOnlineNomads", EndpointUtils.getNumOnlineNomads());
        jsonObject.add("players", playersObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("numResidents", townyAPI.getResidents().size());
        statsObject.addProperty("numNomads", townyAPI.getResidentsWithoutTown().size());
        statsObject.addProperty("numTowns", townyAPI.getTowns().size());
        statsObject.addProperty("numTownBlocks", townyAPI.getTownBlocks().size());
        statsObject.addProperty("numNations", townyAPI.getNations().size());
        statsObject.addProperty("numQuarters", quartersAPI.getAllQuarters().size());
        statsObject.addProperty("numCuboids", quartersAPI.getAllQuarters().stream().mapToInt(quarter -> quarter.getCuboids().size()).sum());
        statsObject.addProperty("version", Bukkit.getMinecraftVersion());
        jsonObject.add("stats", statsObject);

        JsonObject votePartyObject = new JsonObject();
        votePartyObject.addProperty("target", parsePlaceholderToIntOrNull("%voteparty_votes_required_total%"));
        votePartyObject.addProperty("numRemaining", parsePlaceholderToIntOrNull("%voteparty_votes_required_party%"));
        jsonObject.add("voteParty", votePartyObject);

        return jsonObject.toString();
    }

    private Integer parsePlaceholderToIntOrNull(String placeholder) {
        try {
            return Integer.parseInt(PlaceholderAPI.setPlaceholders(null, placeholder));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
