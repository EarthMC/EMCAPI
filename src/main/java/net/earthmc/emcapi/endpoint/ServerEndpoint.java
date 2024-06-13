package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import io.minimum.minecraft.superbvote.SuperbVote;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.quarters.api.QuartersAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.LocalTime;

public class ServerEndpoint {

    public String lookup() {
        JsonObject jsonObject = new JsonObject();

        TownyAPI townyAPI = TownyAPI.getInstance();
        QuartersAPI quartersAPI = QuartersAPI.getInstance();
        World overworld = Bukkit.getWorlds().get(0);

        jsonObject.addProperty("version", Bukkit.getMinecraftVersion());
        jsonObject.addProperty("moonPhase", overworld.getMoonPhase().toString());

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("newDayTime", TownySettings.getNewDayTime());
        timestampsObject.addProperty("serverTimeOfDay", LocalTime.now().toSecondOfDay());
        jsonObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("hasStorm", overworld.hasStorm());
        statusObject.addProperty("isThundering", overworld.hasStorm());
        jsonObject.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("time", overworld.getTime());
        statsObject.addProperty("fullTime", overworld.getFullTime());
        statsObject.addProperty("maxPlayers", Bukkit.getMaxPlayers());
        statsObject.addProperty("numOnlinePlayers", Bukkit.getOnlinePlayers().size());
        statsObject.addProperty("numOnlineNomads", EndpointUtils.getNumOnlineNomads());
        statsObject.addProperty("numResidents", townyAPI.getResidents().size());
        statsObject.addProperty("numNomads", townyAPI.getResidentsWithoutTown().size());
        statsObject.addProperty("numTowns", townyAPI.getTowns().size());
        statsObject.addProperty("numTownBlocks", townyAPI.getTownBlocks().size());
        statsObject.addProperty("numNations", townyAPI.getNations().size());
        statsObject.addProperty("numQuarters", quartersAPI.getAllQuarters().size());
        statsObject.addProperty("numCuboids", quartersAPI.getAllQuarters().stream().mapToInt(quarter -> quarter.getCuboids().size()).sum());
        jsonObject.add("stats", statsObject);

        JsonObject votePartyObject = new JsonObject();
        votePartyObject.addProperty("target", SuperbVote.getPlugin().getVoteParty().votesNeeded());
        votePartyObject.addProperty("numRemaining", SuperbVote.getPlugin().getVoteParty().getCurrentVotes());
        jsonObject.add("voteParty", votePartyObject);

        return jsonObject.toString();
    }
}
