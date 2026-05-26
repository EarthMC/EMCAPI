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
    private final Javalin javalin;
    private final String URLPath;

    public EndpointManager(EMCAPI plugin) {
        this.plugin = plugin;
        this.javalin = plugin.getJavalin();
        this.URLPath = plugin.getURLPath();
    }

    public void loadEndpoints() {
        new SuperbVoteIntegration().register(); // Register integrations for usage in ServerEndpoint
        new QuartersIntegration().register();
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
        loadmcMMoEndpoint();
        loadPursuitsEndpoint();
        loadAdvancementsEndpoint();
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

        new DiscordIntegration().register(); // Load the discord integration to check if DiscordSRV is enabled - checked when including discord for player
        PlayersEndpoint playersEndpoint = new PlayersEndpoint(plugin);
        javalin.post(URLPath + "/players", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(playersEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadTownsEndpoint() {
        TownsListEndpoint tle = new TownsListEndpoint();
        javalin.get(URLPath + "/towns", ctx -> ctx.json(tle.lookup()));

        new WarpsIntegration().register();
        TownsEndpoint townsEndpoint = new TownsEndpoint(plugin);
        javalin.post(URLPath + "/towns", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(townsEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadNationsEndpoint() {
        NationsListEndpoint nle = new NationsListEndpoint();
        javalin.get(URLPath + "/nations", ctx -> ctx.json(nle.lookup()));

        new EmbargoesIntegration().register();
        new PactsIntegration().register();
        NationsEndpoint nationsEndpoint = new NationsEndpoint(plugin);
        javalin.post(URLPath + "/nations", ctx -> {
            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(nationsEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadQuartersEndpoint() {
        QuartersIntegration quartersIntegration = Integrations.getIntegration("Quarters");
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
        MysteryMasterIntegration mysteryMasterIntegration = new MysteryMasterIntegration();
        mysteryMasterIntegration.register();
        MysteryMasterEndpoint mysteryMasterEndpoint = new MysteryMasterEndpoint(plugin);
        javalin.get(URLPath + "/mm", ctx -> {
            mysteryMasterIntegration.throwIfDisabled();

            ctx.json(mysteryMasterEndpoint.lookup());
        });
    }

    private void loadShopsEndpoint() {
        QuickShopIntegration quickShopIntegration = new QuickShopIntegration();
        quickShopIntegration.register();
        ShopEndpoint shopEndpoint = new ShopEndpoint(plugin);
        javalin.post(URLPath + "/shop", ctx -> {
            quickShopIntegration.throwIfDisabled();

            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(shopEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadmcMMoEndpoint() {
        McMMOIntegration mcMMOIntegration = new McMMOIntegration();
        mcMMOIntegration.register();
        McMMOEndpoint mcMMOEndpoint = new McMMOEndpoint(plugin);
        javalin.post(URLPath + "/mcmmo", ctx -> {
           mcMMOIntegration.throwIfDisabled();

           QueryBody parsedBody = parseBody(ctx.body());
           ctx.json(mcMMOEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });

        McMMOTopEndpoint mcMMOTopEndpoint = new McMMOTopEndpoint(plugin);
        javalin.post(URLPath + "/mcmmo-top", ctx -> {
            mcMMOIntegration.throwIfDisabled();

            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(mcMMOTopEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadPursuitsEndpoint() {
        PursuitsIntegration pursuitsIntegration = new PursuitsIntegration();
        pursuitsIntegration.register();
        PursuitsEndpoint pursuitsEndpoint = new PursuitsEndpoint(plugin);
        javalin.post(URLPath + "/pursuits", ctx -> {
            pursuitsIntegration.throwIfDisabled();

            QueryBody parsedBody = parseBody(ctx.body());
            ctx.json(pursuitsEndpoint.lookup(parsedBody.query, parsedBody.template, parsedBody.key));
        });
    }

    private void loadAdvancementsEndpoint() {
        AdvancementsIntegration advancementsIntegration = new AdvancementsIntegration();
        advancementsIntegration.register();
        AdvancementsEndpoint advancementsEndpoint = new AdvancementsEndpoint();
        javalin.get(URLPath + "/advancements", ctx -> {
            advancementsIntegration.throwIfDisabled();

            ctx.json(advancementsEndpoint.lookup());
        });
    }
}
