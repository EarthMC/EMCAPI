package net.earthmc.emcapi.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import kotlin.Pair;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.endpoint.DiscordEndpoint;
import net.earthmc.emcapi.endpoint.legacy.DocumentationEndpoint;
import net.earthmc.emcapi.endpoint.LocationEndpoint;
import net.earthmc.emcapi.endpoint.legacy.MudkipEndpoint;
import net.earthmc.emcapi.endpoint.MysteryMasterEndpoint;
import net.earthmc.emcapi.endpoint.NearbyEndpoint;
import net.earthmc.emcapi.endpoint.OnlineEndpoint;
import net.earthmc.emcapi.endpoint.ServerEndpoint;
import net.earthmc.emcapi.endpoint.towny.NationsEndpoint;
import net.earthmc.emcapi.endpoint.towny.PlayersEndpoint;
import net.earthmc.emcapi.endpoint.towny.QuartersEndpoint;
import net.earthmc.emcapi.endpoint.towny.TownsEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.NationsListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.PlayersListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.QuartersListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.TownsListEndpoint;
import net.earthmc.emcapi.integration.DiscordIntegration;
import net.earthmc.emcapi.integration.QuartersIntegration;
import net.earthmc.emcapi.util.JSONUtil;

public class LegacyEndpointManager {

    private final EMCAPI plugin;
    private final Javalin javalin;
    private final String URLPath = "v3/aurora";

    public LegacyEndpointManager(EMCAPI plugin) {
        this.plugin = plugin;
        this.javalin = plugin.getJavalin();
    }

    public void loadEndpoints() {
        DocumentationEndpoint documentationEndpoint = new DocumentationEndpoint();
        javalin.get("/", ctx -> ctx.json(documentationEndpoint.lookup()));

        ServerEndpoint serverEndpoint = new ServerEndpoint(plugin);
        javalin.get(URLPath, ctx -> ctx.json(serverEndpoint.lookup()));

        MysteryMasterEndpoint mysteryMasterEndpoint = new MysteryMasterEndpoint(plugin);
        javalin.get(URLPath + "/mm", ctx -> {
            plugin.integrations().mysteryMasterIntegration().throwIfDisabled();
            ctx.json(mysteryMasterEndpoint.lookup());
        });

        MudkipEndpoint mudkipEndpoint = new MudkipEndpoint();
        javalin.get("/mudkip", ctx -> {
            ctx.contentType("text/plain; charset=UTF-8");
            ctx.result(mudkipEndpoint.lookup());
        });

        loadPlayersEndpoint();
        loadTownsEndpoint();
        loadNationsEndpoint();
        loadQuartersEndpoint();
        loadLocationEndpoint();
        loadNearbyEndpoint();
        loadDiscordEndpoint();
        loadOnlinePlayersEndpoint();
    }

    private Pair<JsonArray, JsonObject> parseBody(String body) {
        JsonObject jsonObject = JSONUtil.getJsonObjectFromString(body);

        JsonElement queryElement = jsonObject.get("query");
        if (queryElement == null) throw new BadRequestResponse("No query array provided");
        if (!queryElement.isJsonArray()) throw new BadRequestResponse("Provided query is not an array");
        JsonArray queryArray = queryElement.getAsJsonArray();

        JsonElement templateElement = jsonObject.get("template");
        JsonObject templateObject = templateElement != null && templateElement.isJsonObject()
                ? templateElement.getAsJsonObject()
                : null;

        return new Pair<>(queryArray, templateObject);
    }

    private void loadPlayersEndpoint() {
        PlayersListEndpoint ple = new PlayersListEndpoint();
        javalin.get(URLPath + "/players", ctx -> ctx.json(ple.lookup()));

        PlayersEndpoint playersEndpoint = new PlayersEndpoint(plugin);
        javalin.post(URLPath + "/players", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(playersEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond(), null));
        });
    }

    private void loadTownsEndpoint() {
        TownsListEndpoint tle = new TownsListEndpoint();
        javalin.get(URLPath + "/towns", ctx -> ctx.json(tle.lookup()));

        TownsEndpoint townsEndpoint = new TownsEndpoint(plugin);
        javalin.post(URLPath + "/towns", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(townsEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond(), null));
        });
    }

    private void loadNationsEndpoint() {
        NationsListEndpoint nle = new NationsListEndpoint();
        javalin.get(URLPath + "/nations", ctx -> ctx.json(nle.lookup()));

        NationsEndpoint nationsEndpoint = new NationsEndpoint(plugin);
        javalin.post(URLPath + "/nations", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(nationsEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond(), null));
        });
    }

    private void loadQuartersEndpoint() {
        QuartersIntegration quartersIntegration = plugin.integrations().quartersIntegration();
        QuartersListEndpoint qle = new QuartersListEndpoint(quartersIntegration);

        javalin.get(URLPath + "/quarters", ctx -> {
            quartersIntegration.throwIfDisabled();
            ctx.json(qle.lookup());
        });

        QuartersEndpoint quartersEndpoint = new QuartersEndpoint(plugin);
        javalin.post(URLPath + "/quarters", ctx -> {
            quartersIntegration.throwIfDisabled();
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(quartersEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond(), null));
        });
    }

    private void loadLocationEndpoint() {
        LocationEndpoint locationEndpoint = new LocationEndpoint(plugin);
        javalin.post(URLPath + "/location", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(locationEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond(), null));
        });
    }

    private void loadNearbyEndpoint() {
        NearbyEndpoint nearbyEndpoint = new NearbyEndpoint(plugin);
        javalin.post(URLPath + "/nearby", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(nearbyEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond(), null));
        });
    }

    private void loadDiscordEndpoint() {
        DiscordEndpoint discordEndpoint = new DiscordEndpoint(plugin);
        final DiscordIntegration discordIntegration = plugin.integrations().discordIntegration();

        javalin.post(URLPath + "/discord", ctx -> {
            discordIntegration.throwIfDisabled();

            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(discordEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond(), null));
        });
    }

    private void loadOnlinePlayersEndpoint() {
        OnlineEndpoint onlineEndpoint = new OnlineEndpoint();
        javalin.get(URLPath + "/online", ctx -> ctx.json(onlineEndpoint.lookup()));
    }
}
