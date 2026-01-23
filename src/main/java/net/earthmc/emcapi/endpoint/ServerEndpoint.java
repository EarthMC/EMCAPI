package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.QuartersIntegration;
import net.earthmc.emcapi.integration.SuperbVoteIntegration;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class ServerEndpoint extends GetEndpoint {

    private final EMCAPI plugin;

    private int quartersCount;
    private int cuboidsCount;

    public ServerEndpoint(final EMCAPI plugin) {
        this.plugin = plugin;

        final QuartersIntegration quartersIntegration = plugin.integrations().quartersIntegration();

        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task -> {
            final QuartersIntegration.QuarterStatistics statistics = quartersIntegration.retrieveQuarterStatistics();

            this.quartersCount = statistics.totalQuarters();
            this.cuboidsCount = statistics.totalCuboids();
        }, 0L, 1L, TimeUnit.HOURS);
    }

    @Override
    public String lookup() {
        return getJsonElement().toString();
    }

    @Override
    public JsonObject getJsonElement() {
        JsonObject serverObject = new JsonObject();

        TownyAPI townyAPI = TownyAPI.getInstance();
        World overworld = plugin.getServer().getWorlds().getFirst();

        serverObject.addProperty("version", plugin.getServer().getMinecraftVersion());
        serverObject.addProperty("moonPhase", overworld.getMoonPhase().toString());

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("newDayTime", TownySettings.getNewDayTime());
        timestampsObject.addProperty("serverTimeOfDay", LocalTime.now().toSecondOfDay());
        serverObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("hasStorm", overworld.hasStorm());
        statusObject.addProperty("isThundering", overworld.isThundering());
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

        statsObject.addProperty("numQuarters", quartersCount);
        statsObject.addProperty("numCuboids", cuboidsCount);

        serverObject.add("stats", statsObject);

        int target;
        int currentVotes;

        final SuperbVoteIntegration superbVote = plugin.integrations().superbVoteIntegration();
        if (superbVote.isEnabled()) {
            target = superbVote.votesNeeded();
            currentVotes = superbVote.currentVotes();
        } else {
            target = 0;
            currentVotes = 0;
        }

        JsonObject votePartyObject = new JsonObject();
        votePartyObject.addProperty("target", target);
        votePartyObject.addProperty("numRemaining", target - currentVotes);
        serverObject.add("voteParty", votePartyObject);

        return serverObject;
    }
}
