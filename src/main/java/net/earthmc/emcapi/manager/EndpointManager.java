package net.earthmc.emcapi.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.endpoint.LocationEndpoint;
import net.earthmc.emcapi.endpoint.MysteryMasterEndpoint;
import net.earthmc.emcapi.endpoint.NearbyEndpoint;
import net.earthmc.emcapi.endpoint.OnlineEndpoint;
import net.earthmc.emcapi.endpoint.ServerEndpoint;
import net.earthmc.emcapi.endpoint.ShopEndpoint;
import net.earthmc.emcapi.endpoint.towny.NationsEndpoint;
import net.earthmc.emcapi.endpoint.towny.PlayersEndpoint;
import net.earthmc.emcapi.endpoint.towny.QuartersEndpoint;
import net.earthmc.emcapi.endpoint.towny.TownsEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.NationsListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.PlayersListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.QuartersListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.TownsListEndpoint;
import net.earthmc.emcapi.integration.MysteryMasterIntegration;
import net.earthmc.emcapi.integration.QuartersIntegration;
import net.earthmc.emcapi.integration.QuickShopIntegration;
import net.earthmc.emcapi.util.JSONUtil;
import org.jetbrains.annotations.Nullable;

public class EndpointManager {

    private final EMCAPI plugin;
    private final Javalin javalin;
    private final String URLPath;

    public EndpointManager(EMCAPI plugin) {
        this.plugin = plugin;
        this.javalin = plugin.getJavalin();
        this.URLPath = plugin.getURLPath();
    }

    public void loadEndpoints() {
        ServerEndpoint serverEndpoint = new ServerEndpoint(plugin);
        javalin.get(URLPath, ctx -> ctx.json(serverEndpoint.lookup()));

        loadPlayersEndpoint();
        loadTownsEndpoint();
        loadNationsEndpoint();
        loadQuartersEndpoint();
        loadLocationEndpoint();
        loadNearbyEndpoint();
        loadOnlinePlayersEndpoint();
        loadMysteryMasterEndpoint();
        loadShopsEndpoint();
    }

    private QueryBody parseBody(String body) {
        JsonObject jsonObject = JSONUtil.getJsonObjectFromString(body);

        JsonElement queryElement = jsonObject.get("query");
        if (queryElement == null) throw new BadRequestResponse("No query array provided");
        if (!queryElement.isJsonArray()) throw new BadRequestResponse("Provided query is not an array");
        JsonArray queryArray = queryElement.getAsJsonArray();

        JsonElement templateElement = jsonObject.get("template");
        JsonObject templateObject = templateElement != null && templateElement.isJsonObject() ? templateElement.getAsJsonObject() : null;

        JsonElement keyElement = jsonObject.get("key");
        String key = keyElement != null && keyElement.isJsonPrimitive() ? keyElement.getAsString() : null;

        return new QueryBody(queryArray, templateObject, key);
    }

    private record QueryBody(JsonArray query, @Nullable JsonObject template, @Nullable String key) {}

    private void loadPlayersEndpoint() {
        PlayersListEndpoint ple = new PlayersListEndpoint();
        javalin.get(URLPath + "/players", ctx -> ctx.json(ple.lookup()));

        PlayersEndpoint playersEndpoint = new PlayersEndpoint(plugin);
        javalin.post(URLPath + "/players", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(playersEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadTownsEndpoint() {
        TownsListEndpoint tle = new TownsListEndpoint();
        javalin.get(URLPath + "/towns", ctx -> ctx.json(tle.lookup()));

        TownsEndpoint townsEndpoint = new TownsEndpoint(plugin);
        javalin.post(URLPath + "/towns", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(townsEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadNationsEndpoint() {
        NationsListEndpoint nle = new NationsListEndpoint();
        javalin.get(URLPath + "/nations", ctx -> ctx.json(nle.lookup()));

        NationsEndpoint nationsEndpoint = new NationsEndpoint(plugin);
        javalin.post(URLPath + "/nations", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(nationsEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
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
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(quartersEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadLocationEndpoint() {
        LocationEndpoint locationEndpoint = new LocationEndpoint(plugin);
        javalin.post(URLPath + "/location", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(locationEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadNearbyEndpoint() {
        NearbyEndpoint nearbyEndpoint = new NearbyEndpoint(plugin);
        javalin.post(URLPath + "/nearby", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(nearbyEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadOnlinePlayersEndpoint() {
        OnlineEndpoint onlineEndpoint = new OnlineEndpoint();
        javalin.get(URLPath + "/online", ctx -> ctx.json(onlineEndpoint.lookup()));
    }

    private void loadMysteryMasterEndpoint() {
        MysteryMasterEndpoint mysteryMasterEndpoint = new MysteryMasterEndpoint(plugin);
        MysteryMasterIntegration mysteryMasterIntegration = plugin.integrations().mysteryMasterIntegration();
        javalin.get(URLPath + "/mm", ctx -> {
            mysteryMasterIntegration.throwIfDisabled();

            ctx.json(mysteryMasterEndpoint.lookup());
        });
    }

    private void loadShopsEndpoint() {
        ShopEndpoint shopEndpoint = new ShopEndpoint(plugin);
        QuickShopIntegration quickShopIntegration = plugin.integrations().quickShopIntegration();;
        javalin.post(URLPath + "/shop", ctx -> {
            quickShopIntegration.throwIfDisabled();

            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(shopEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }
}
