package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Resident;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.integration.QuartersIntegration;
import net.earthmc.emcapi.integration.SuperbVoteIntegration;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

@OpenApi(
    path = "/v4",
    methods = HttpMethod.GET,
    summary = "Get live information about the server",
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.Server.class,
                    mimeType = ContentType.JSON,
                    example = """
                        {"version":"1.21.11","moonPhase":"LAST_QUARTER","timestamps":{"newDayTime":36000,"serverTimeOfDay":60073},"status":{"hasStorm":false,"isThundering":false},"stats":{"time":12648,"fullTime":180924648,"maxPlayers":800,"numOnlinePlayers":715,"numOnlineNomads":26,"numResidents":72913,"numNomads":29605,"numTowns":5563,"numTownBlocks":368175,"numNations":199,"numQuarters":14824,"numCuboids":21000},"voteParty":{"target":5000,"numRemaining":4967}}
                        """
                )
            }
        ),
        @OpenApiResponse(
            status = "502",
            description = "If the game server is down or the API is disabled"
        )
    }
)
public class ServerEndpoint extends GetEndpoint {

    private final EMCAPI plugin;

    private int quartersCount;
    private int cuboidsCount;

    public ServerEndpoint(final EMCAPI plugin) {
        this.plugin = plugin;

        final QuartersIntegration quartersIntegration = Integrations.getIntegration("Quarters");

        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task -> {
            final QuartersIntegration.QuarterStatistics statistics = quartersIntegration.retrieveQuarterStatistics();

            this.quartersCount = statistics.totalQuarters();
            this.cuboidsCount = statistics.totalCuboids();
        }, 0L, 1L, TimeUnit.HOURS);
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
        statsObject.addProperty("maxPlayers", plugin.getServer().getMaxPlayers());
        statsObject.addProperty("numOnlinePlayers", plugin.getServer().getOnlinePlayers().size());
        statsObject.addProperty("numOnlineNomads", getNumOnlineNomads());
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

        final SuperbVoteIntegration superbVote = Integrations.getIntegration("SuperbVote");
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

    private static int getNumOnlineNomads() {
        int numOnlineNomads = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Resident resident = TownyAPI.getInstance().getResident(player);
            if (resident == null || !resident.hasTown()) {
                numOnlineNomads++;
            }
        }

        return numOnlineNomads;
    }
}
