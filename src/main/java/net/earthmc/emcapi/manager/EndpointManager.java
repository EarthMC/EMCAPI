package net.earthmc.emcapi.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.config.RoutesConfig;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.endpoint.LocationEndpoint;
import net.earthmc.emcapi.endpoint.MysteryMasterEndpoint;
import net.earthmc.emcapi.endpoint.NearbyEndpoint;
import net.earthmc.emcapi.endpoint.OnlineEndpoint;
import net.earthmc.emcapi.endpoint.ServerEndpoint;
import net.earthmc.emcapi.endpoint.ShopEndpoint;
import net.earthmc.emcapi.endpoint.McMMOEndpoint;
import net.earthmc.emcapi.endpoint.McMMOTopEndpoint;
import net.earthmc.emcapi.endpoint.PursuitsEndpoint;
import net.earthmc.emcapi.endpoint.AdvancementsEndpoint;
import net.earthmc.emcapi.endpoint.towny.NationsEndpoint;
import net.earthmc.emcapi.endpoint.towny.PlayersEndpoint;
import net.earthmc.emcapi.endpoint.towny.QuartersEndpoint;
import net.earthmc.emcapi.endpoint.towny.TownsEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.NationsListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.PlayersListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.QuartersListEndpoint;
import net.earthmc.emcapi.endpoint.towny.list.TownsListEndpoint;
import net.earthmc.emcapi.integration.*;
import net.earthmc.emcapi.util.JSONUtil;
import org.jetbrains.annotations.Nullable;

public class EndpointManager {

    private final EMCAPI plugin;
    private final String URLPath;

    public EndpointManager(EMCAPI plugin) {
        this.plugin = plugin;
        this.URLPath = plugin.getURLPath();
    }

    public void loadEndpoints(final RoutesConfig routes) {
        new SuperbVoteIntegration().register(); // Register integrations for usage in ServerEndpoint
        new QuartersIntegration().register();
        ServerEndpoint serverEndpoint = new ServerEndpoint(plugin);
        routes.get(URLPath, ctx -> ctx.json(serverEndpoint.lookup()));

        loadPlayersEndpoint(routes);
        loadTownsEndpoint(routes);
        loadNationsEndpoint(routes);
        loadQuartersEndpoint(routes);
        loadLocationEndpoint(routes);
        loadNearbyEndpoint(routes);
        loadOnlinePlayersEndpoint(routes);
        loadMysteryMasterEndpoint(routes);
        loadShopsEndpoint(routes);
        loadmcMMoEndpoint(routes);
        loadPursuitsEndpoint(routes);
        loadAdvancementsEndpoint(routes);
    }

    private static final BadRequestResponse NO_QUERY_ARRAY = new BadRequestResponse("No query array provided");
    private static final BadRequestResponse INVALID_QUERY_ARRAY = new BadRequestResponse("Provided query is not an array");

    private QueryBody parseBody(String body) {
        JsonObject jsonObject = JSONUtil.getJsonObjectFromString(body);

        JsonElement queryElement = jsonObject.get("query");
        if (queryElement == null) throw NO_QUERY_ARRAY;
        if (!queryElement.isJsonArray()) throw INVALID_QUERY_ARRAY;
        JsonArray queryArray = queryElement.getAsJsonArray();

        JsonElement templateElement = jsonObject.get("template");
        JsonObject templateObject = templateElement != null && templateElement.isJsonObject() ? templateElement.getAsJsonObject() : null;

        JsonElement keyElement = jsonObject.get("key");
        String key = keyElement != null && keyElement.isJsonPrimitive() ? keyElement.getAsString() : null;

        return new QueryBody(queryArray, templateObject, key);
    }

    private record QueryBody(JsonArray query, @Nullable JsonObject template, @Nullable String key) {}

    private void loadPlayersEndpoint(RoutesConfig routes) {
        PlayersListEndpoint ple = new PlayersListEndpoint();
        routes.get(URLPath + "/players", ctx -> ctx.json(ple.lookup()));

        new DiscordIntegration().register(); // Load the discord integration to check if DiscordSRV is enabled - checked when including discord for player
        PlayersEndpoint playersEndpoint = new PlayersEndpoint(plugin);
        routes.post(URLPath + "/players", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(playersEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadTownsEndpoint(RoutesConfig routes) {
        TownsListEndpoint tle = new TownsListEndpoint();
        routes.get(URLPath + "/towns", ctx -> ctx.json(tle.lookup()));

        new WarpsIntegration().register();
        TownsEndpoint townsEndpoint = new TownsEndpoint(plugin);
        routes.post(URLPath + "/towns", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(townsEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadNationsEndpoint(RoutesConfig routes) {
        NationsListEndpoint nle = new NationsListEndpoint();
        routes.get(URLPath + "/nations", ctx -> ctx.json(nle.lookup()));

        new EmbargoesIntegration().register();
        new PactsIntegration().register();
        NationsEndpoint nationsEndpoint = new NationsEndpoint(plugin);
        routes.post(URLPath + "/nations", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(nationsEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadQuartersEndpoint(RoutesConfig routes) {
        QuartersIntegration quartersIntegration = Integrations.getIntegration("Quarters");
        QuartersListEndpoint qle = new QuartersListEndpoint(quartersIntegration);

        routes.get(URLPath + "/quarters", ctx -> {
            quartersIntegration.throwIfDisabled();
            ctx.json(qle.lookup());
        });

        QuartersEndpoint quartersEndpoint = new QuartersEndpoint(plugin);
        routes.post(URLPath + "/quarters", ctx -> {
            quartersIntegration.throwIfDisabled();
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(quartersEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadLocationEndpoint(RoutesConfig routes) {
        LocationEndpoint locationEndpoint = new LocationEndpoint(plugin);
        routes.post(URLPath + "/location", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(locationEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadNearbyEndpoint(RoutesConfig routes) {
        NearbyEndpoint nearbyEndpoint = new NearbyEndpoint(plugin);
        routes.post(URLPath + "/nearby", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(nearbyEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadOnlinePlayersEndpoint(RoutesConfig routes) {
        OnlineEndpoint onlineEndpoint = new OnlineEndpoint();
        routes.get(URLPath + "/online", ctx -> ctx.json(onlineEndpoint.lookup()));
    }

    private void loadMysteryMasterEndpoint(RoutesConfig routes) {
        MysteryMasterIntegration mysteryMasterIntegration = new MysteryMasterIntegration();
        mysteryMasterIntegration.register();
        MysteryMasterEndpoint mysteryMasterEndpoint = new MysteryMasterEndpoint(plugin);
        routes.get(URLPath + "/mm", ctx -> {
            mysteryMasterIntegration.throwIfDisabled();

            ctx.json(mysteryMasterEndpoint.lookup());
        });
    }

    private void loadShopsEndpoint(RoutesConfig routes) {
        QuickShopIntegration quickShopIntegration = new QuickShopIntegration();
        quickShopIntegration.register();
        ShopEndpoint shopEndpoint = new ShopEndpoint(plugin);
        routes.post(URLPath + "/shop", ctx -> {
            quickShopIntegration.throwIfDisabled();

            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(shopEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadmcMMoEndpoint(RoutesConfig routes) {
        McMMOIntegration mcMMOIntegration = new McMMOIntegration();
        mcMMOIntegration.register();
        McMMOEndpoint mcMMOEndpoint = new McMMOEndpoint(plugin);
        routes.post(URLPath + "/mcmmo", ctx -> {
           mcMMOIntegration.throwIfDisabled();

           QueryBody parsedBody = parseBody(ctx.body());
           ctx.json(mcMMOEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });

        McMMOTopEndpoint mcMMOTopEndpoint = new McMMOTopEndpoint(plugin);
        routes.post(URLPath + "/mcmmo-top", ctx -> {
            mcMMOIntegration.throwIfDisabled();

            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(mcMMOTopEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadPursuitsEndpoint(RoutesConfig routes) {
        PursuitsIntegration pursuitsIntegration = new PursuitsIntegration();
        pursuitsIntegration.register();
        PursuitsEndpoint pursuitsEndpoint = new PursuitsEndpoint(plugin);
        routes.post(URLPath + "/pursuits", ctx -> {
            pursuitsIntegration.throwIfDisabled();

            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(pursuitsEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadAdvancementsEndpoint(RoutesConfig routes) {
        AdvancementsIntegration advancementsIntegration = new AdvancementsIntegration();
        advancementsIntegration.register();
        AdvancementsEndpoint advancementsEndpoint = new AdvancementsEndpoint();
        routes.get(URLPath + "/advancements", ctx -> {
            advancementsIntegration.throwIfDisabled();

            ctx.json(advancementsEndpoint.lookup());
        });
    }
}
