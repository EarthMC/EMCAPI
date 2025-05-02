package net.earthmc.emcapi.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import kotlin.Pair;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.endpoint.*;
import net.earthmc.emcapi.endpoint.legacy.v1.*;
import net.earthmc.emcapi.endpoint.legacy.v2.*;
import net.earthmc.emcapi.endpoint.towny.NationsEndpoint;
import net.earthmc.emcapi.endpoint.towny.PlayersEndpoint;
import net.earthmc.emcapi.endpoint.towny.QuartersEndpoint;
import net.earthmc.emcapi.endpoint.towny.TownsEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.NationsListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.PlayersListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.QuartersListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.TownsListEndpoint;
import net.earthmc.emcapi.util.JSONUtil;
import net.milkbowl.vault.economy.Economy;

public class EndpointManager {

    private final EMCAPI plugin;
    private final Javalin javalin;
    private final Economy economy;
    private final String v1URLPath;
    private final String v2URLPath;
    private final String v3URLPath;

    public EndpointManager(EMCAPI plugin) {
        this.plugin = plugin;
        this.javalin = plugin.getJavalin();
        this.economy = plugin.getEconomy();
        this.v1URLPath = "v1/" + plugin.getConfig().getString("networking.url_path");
        this.v2URLPath = "v2/" + plugin.getConfig().getString("networking.url_path");
        this.v3URLPath = "v3/" + plugin.getConfig().getString("networking.url_path");
    }

    public void loadEndpoints() {
        DocumentationEndpoint documentationEndpoint = new DocumentationEndpoint();
        javalin.get("/", ctx -> ctx.json(documentationEndpoint.lookup()));

        ServerEndpoint serverEndpoint = new ServerEndpoint();
        javalin.get(v3URLPath, ctx -> ctx.json(serverEndpoint.lookup()));

        // The endpoint class won't load without the plugin present
        if (plugin.getServer().getPluginManager().getPlugin("MysteryMaster") != null) {
            MysteryMasterEndpoint mysteryMasterEndpoint = new MysteryMasterEndpoint(plugin);
            javalin.get(v3URLPath + "/mm", ctx -> ctx.json(mysteryMasterEndpoint.lookup()));
        }

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
        loadPlayerStatsEndpoint();
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
        javalin.get(v3URLPath + "/players", ctx -> ctx.json(ple.lookup()));

        PlayersEndpoint playersEndpoint = new PlayersEndpoint(economy);
        javalin.post(v3URLPath + "/players", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(playersEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond()));
        });
    }

    private void loadTownsEndpoint() {
        TownsListEndpoint tle = new TownsListEndpoint();
        javalin.get(v3URLPath + "/towns", ctx -> ctx.json(tle.lookup()));

        TownsEndpoint townsEndpoint = new TownsEndpoint();
        javalin.post(v3URLPath + "/towns", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(townsEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond()));
        });
    }

    private void loadNationsEndpoint() {
        NationsListEndpoint nle = new NationsListEndpoint();
        javalin.get(v3URLPath + "/nations", ctx -> ctx.json(nle.lookup()));

        NationsEndpoint nationsEndpoint = new NationsEndpoint();
        javalin.post(v3URLPath + "/nations", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(nationsEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond()));
        });
    }

    private void loadQuartersEndpoint() {
        QuartersListEndpoint qle = new QuartersListEndpoint();
        javalin.get(v3URLPath + "/quarters", ctx -> ctx.json(qle.lookup()));

        QuartersEndpoint quartersEndpoint = new QuartersEndpoint();
        javalin.post(v3URLPath + "/quarters", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(quartersEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond()));
        });
    }

    private void loadLocationEndpoint() {
        LocationEndpoint locationEndpoint = new LocationEndpoint();
        javalin.post(v3URLPath + "/location", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(locationEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond()));
        });
    }

    private void loadNearbyEndpoint() {
        NearbyEndpoint nearbyEndpoint = new NearbyEndpoint();
        javalin.post(v3URLPath + "/nearby", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(nearbyEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond()));
        });
    }

    private void loadDiscordEndpoint() {
        DiscordEndpoint discordEndpoint = new DiscordEndpoint();
        javalin.post(v3URLPath + "/discord", ctx -> {
            Pair<JsonArray, JsonObject> parsedBody = parseBody(ctx.body());
            ctx.json(discordEndpoint.lookup(parsedBody.getFirst(), parsedBody.getSecond()));
        });
    }

    private void loadPlayerStatsEndpoint() {
        PlayerStatsEndpoint playerStatsEndpoint = new PlayerStatsEndpoint(this.plugin);
        playerStatsEndpoint.initialize();
        javalin.get(v3URLPath + "/player-stats", ctx -> {
            ctx.json(playerStatsEndpoint.latestCachedStatistics());
        });
    }

    public void loadLegacyEndpoints() {
        // v1
        javalin.get(v1URLPath, ctx -> ctx.json(v1ServerLookup.serverLookup()));
        javalin.get(v1URLPath + "/residents", ctx -> ctx.json(v1AllLists.allResidents()));
        javalin.get(v1URLPath + "/towns", ctx -> ctx.json(v1AllLists.allTowns()));
        javalin.get(v1URLPath + "/nations", ctx -> ctx.json(v1AllLists.allNations()));
        javalin.get(v1URLPath + "/residents/{name}", ctx -> ctx.json(v1ResidentLookup.residentLookup(ctx.pathParam("name"))));
        javalin.get(v1URLPath + "/towns/{name}", ctx -> ctx.json(v1TownLookup.townLookup(ctx.pathParam("name"))));
        javalin.get(v1URLPath + "/nations/{name}", ctx -> ctx.json(v1NationLookup.nationLookup(ctx.pathParam("name"))));

        // v2
        javalin.get(v2URLPath, ctx -> ctx.json(v2ServerLookup.serverLookup()));
        javalin.get(v2URLPath + "/residents", ctx -> ctx.json(v2ResidentLookup.allResidentsBulk()));
        javalin.get(v2URLPath + "/towns", ctx -> ctx.json(v2TownLookup.allTownsBulk()));
        javalin.get(v2URLPath + "/nations", ctx -> ctx.json(v2NationLookup.allNationsBulk()));
        javalin.get(v2URLPath + "/residents/{name}", ctx -> ctx.json(v2ResidentLookup.residentLookup(ctx.pathParam("name"))));
        javalin.get(v2URLPath + "/towns/{name}", ctx -> ctx.json(v2TownLookup.townLookup(ctx.pathParam("name"))));
        javalin.get(v2URLPath + "/nations/{name}", ctx -> ctx.json(v2NationLookup.nationLookup(ctx.pathParam("name"))));
        javalin.get(v2URLPath + "/lists/residents", ctx -> ctx.json(v2AllLists.allResidentsList()));
        javalin.get(v2URLPath + "/lists/towns", ctx -> ctx.json(v2AllLists.allTownsList()));
        javalin.get(v2URLPath + "/lists/nations", ctx -> ctx.json(v2AllLists.allNationsList()));
    }
}
