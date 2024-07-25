package net.earthmc.emcapi.manager;

import com.google.gson.*;
import io.javalin.Javalin;
import net.earthmc.emcapi.endpoint.*;
import net.earthmc.emcapi.endpoint.legacy.v1.*;
import net.earthmc.emcapi.endpoint.legacy.v2.*;
import net.earthmc.emcapi.endpoint.towny.*;
import net.earthmc.emcapi.endpoint.towny.list.NationsListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.PlayersListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.QuartersListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.TownsListEndpoint;
import net.earthmc.emcapi.util.JSONUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;

public class EndpointManager {

    private final Javalin javalin;
    private final Economy economy;
    private final String v1URLPath;
    private final String v2URLPath;
    private final String v3URLPath;

    public EndpointManager(Javalin javalin, FileConfiguration config, Economy economy) {
        this.javalin = javalin;
        this.economy = economy;
        this.v1URLPath = "v1/" + config.getString("networking.url_path");
        this.v2URLPath = "v2/" + config.getString("networking.url_path");
        this.v3URLPath = "v3/" + config.getString("networking.url_path");
    }

    public void loadEndpoints() {
        DocumentationEndpoint documentationEndpoint = new DocumentationEndpoint();
        javalin.get("/", ctx -> ctx.json(documentationEndpoint.lookup()));

        ServerEndpoint serverEndpoint = new ServerEndpoint();
        javalin.get(v3URLPath, ctx -> ctx.json(serverEndpoint.lookup()));

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
    }

    private void loadPlayersEndpoint() {
        PlayersListEndpoint ple = new PlayersListEndpoint();
        javalin.get(v3URLPath + "/players", ctx -> ctx.json(ple.lookup()));

        PlayersEndpoint playersEndpoint = new PlayersEndpoint(economy);
        javalin.post(v3URLPath + "/players", ctx -> {
            JsonObject jsonObject = JSONUtil.getJsonObjectFromString(ctx.body());

            JsonArray queryArray = jsonObject.get("query").getAsJsonArray();

            ctx.json(playersEndpoint.lookup(queryArray));
        });
    }

    private void loadTownsEndpoint() {
        TownsListEndpoint tle = new TownsListEndpoint();
        javalin.get(v3URLPath + "/towns", ctx -> ctx.json(tle.lookup()));

        TownsEndpoint townsEndpoint = new TownsEndpoint();
        javalin.post(v3URLPath + "/towns", ctx -> {
            JsonObject jsonObject = JSONUtil.getJsonObjectFromString(ctx.body());

            JsonArray queryArray = jsonObject.get("query").getAsJsonArray();

            ctx.json(townsEndpoint.lookup(queryArray));
        });
    }

    private void loadNationsEndpoint() {
        NationsListEndpoint nle = new NationsListEndpoint();
        javalin.get(v3URLPath + "/nations", ctx -> ctx.json(nle.lookup()));

        NationsEndpoint nationsEndpoint = new NationsEndpoint();
        javalin.post(v3URLPath + "/nations", ctx -> {
            JsonObject jsonObject = JSONUtil.getJsonObjectFromString(ctx.body());

            JsonArray queryArray = jsonObject.get("query").getAsJsonArray();

            ctx.json(nationsEndpoint.lookup(queryArray));
        });
    }

    private void loadQuartersEndpoint() {
        QuartersListEndpoint qle = new QuartersListEndpoint();
        javalin.get(v3URLPath + "/quarters", ctx -> ctx.json(qle.lookup()));

        QuartersEndpoint quartersEndpoint = new QuartersEndpoint();
        javalin.post(v3URLPath + "/quarters", ctx -> {
            JsonObject jsonObject = JSONUtil.getJsonObjectFromString(ctx.body());

            JsonArray queryArray = jsonObject.get("query").getAsJsonArray();

            ctx.json(quartersEndpoint.lookup(queryArray));
        });
    }

    private void loadLocationEndpoint() {
        LocationEndpoint locationEndpoint = new LocationEndpoint();
        javalin.get(v3URLPath + "/location", ctx -> {
            JsonObject jsonObject = JSONUtil.getJsonObjectFromString(ctx.body());

            JsonArray queryArray = jsonObject.get("query").getAsJsonArray();

            ctx.json(locationEndpoint.lookup(queryArray));
        });
    }

    private void loadNearbyEndpoint() {
        NearbyEndpoint nearbyEndpoint = new NearbyEndpoint();
        javalin.post(v3URLPath + "/nearby", ctx -> {
            JsonObject jsonObject = JSONUtil.getJsonObjectFromString(ctx.body());

            JsonArray queryArray = jsonObject.get("query").getAsJsonArray();

            ctx.json(nearbyEndpoint.lookup(queryArray));
        });
    }

    private void loadDiscordEndpoint() {
        DiscordEndpoint discordEndpoint = new DiscordEndpoint();
        javalin.post(v3URLPath + "/discord", ctx -> {
            JsonObject jsonObject = JSONUtil.getJsonObjectFromString(ctx.body());

            JsonArray queryArray = jsonObject.get("query").getAsJsonArray();

            ctx.json(discordEndpoint.lookup(queryArray));
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
