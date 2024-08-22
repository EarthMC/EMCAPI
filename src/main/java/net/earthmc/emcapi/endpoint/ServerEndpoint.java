package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import io.minimum.minecraft.superbvote.SuperbVote;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import au.lupine.quarters.api.manager.QuarterManager;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.LocalTime;

public class ServerEndpoint extends GetEndpoint {

    @Override
    public String lookup() {
        return getJsonElement().toString();
    }

    @Override
    public JsonObject getJsonElement() {
        JsonObject serverObject = new JsonObject();

        TownyAPI townyAPI = TownyAPI.getInstance();
        QuarterManager quartersAPI = QuarterManager.getInstance();
        World overworld = Bukkit.getWorlds().get(0);

        serverObject.addProperty("version", Bukkit.getMinecraftVersion());
        serverObject.addProperty("moonPhase", overworld.getMoonPhase().toString());

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("newDayTime", TownySettings.getNewDayTime());
        timestampsObject.addProperty("serverTimeOfDay", LocalTime.now().toSecondOfDay());
        serverObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("hasStorm", overworld.hasStorm());
        statusObject.addProperty("isThundering", overworld.hasStorm());
        serverObject.add("status", statusObject);

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
        serverObject.add("stats", statsObject);

        int target = SuperbVote.getPlugin().getVoteParty().votesNeeded();
        int currentVotes = SuperbVote.getPlugin().getVoteParty().getCurrentVotes();

        JsonObject votePartyObject = new JsonObject();
        votePartyObject.addProperty("target", target);
        votePartyObject.addProperty("numRemaining", target - currentVotes);
        serverObject.add("voteParty", votePartyObject);

        return serverObject;
    }
}
