package net.earthmc.emcapi.manager;

import io.javalin.config.RoutesConfig;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.endpoint.AuthGetEndpoint;
import net.earthmc.emcapi.endpoint.AuthPostEndpoint;
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
import net.earthmc.emcapi.util.EndpointUtils;

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
        routes.get(URLPath, ctx -> ctx.json(serverEndpoint.lookup(ctx)));

        loadAuthTestEndpoints(routes);
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

    private void loadAuthTestEndpoints(RoutesConfig routes) {
        AuthGetEndpoint authGet = new AuthGetEndpoint();
        routes.get(URLPath + "/authtest", ctx -> ctx.json(authGet.lookup(ctx)));

        AuthPostEndpoint authPost = new AuthPostEndpoint(plugin);
        routes.post(URLPath + "/authtest", ctx -> {
            EndpointUtils.verifyKey(ctx);
            ctx.json(authPost.lookup(ctx));
        });
    }

    private void loadPlayersEndpoint(RoutesConfig routes) {
        PlayersListEndpoint ple = new PlayersListEndpoint();
        routes.get(URLPath + "/players", ctx -> ctx.json(ple.lookup(ctx)));

        new DiscordIntegration().register(); // Load the discord integration to check if DiscordSRV is enabled - checked when including discord for player
        PlayersEndpoint playersEndpoint = new PlayersEndpoint(plugin);
        routes.post(URLPath + "/players", ctx -> {
            ctx.json(playersEndpoint.lookup(ctx));
        });
    }

    private void loadTownsEndpoint(RoutesConfig routes) {
        TownsListEndpoint tle = new TownsListEndpoint();
        routes.get(URLPath + "/towns", ctx -> ctx.json(tle.lookup(ctx)));

        new WarpsIntegration().register();
        TownsEndpoint townsEndpoint = new TownsEndpoint(plugin);
        routes.post(URLPath + "/towns", ctx -> {
            ctx.json(townsEndpoint.lookup(ctx));
        });
    }

    private void loadNationsEndpoint(RoutesConfig routes) {
        NationsListEndpoint nle = new NationsListEndpoint();
        routes.get(URLPath + "/nations", ctx -> ctx.json(nle.lookup(ctx)));

        new EmbargoesIntegration().register();
        new PactsIntegration().register();
        NationsEndpoint nationsEndpoint = new NationsEndpoint(plugin);
        routes.post(URLPath + "/nations", ctx -> {
            ctx.json(nationsEndpoint.lookup(ctx));
        });
    }

    private void loadQuartersEndpoint(RoutesConfig routes) {
        QuartersIntegration quartersIntegration = Integrations.getIntegration("Quarters");
        QuartersListEndpoint qle = new QuartersListEndpoint(quartersIntegration);

        routes.get(URLPath + "/quarters", ctx -> {
            quartersIntegration.throwIfDisabled();
            ctx.json(qle.lookup(ctx));
        });

        QuartersEndpoint quartersEndpoint = new QuartersEndpoint(plugin);
        routes.post(URLPath + "/quarters", ctx -> {
            quartersIntegration.throwIfDisabled();
            ctx.json(quartersEndpoint.lookup(ctx));
        });
    }

    private void loadLocationEndpoint(RoutesConfig routes) {
        LocationEndpoint locationEndpoint = new LocationEndpoint(plugin);
        routes.post(URLPath + "/location", ctx -> {
            ctx.json(locationEndpoint.lookup(ctx));
        });
    }

    private void loadNearbyEndpoint(RoutesConfig routes) {
        NearbyEndpoint nearbyEndpoint = new NearbyEndpoint(plugin);
        routes.post(URLPath + "/nearby", ctx -> {
            ctx.json(nearbyEndpoint.lookup(ctx));
        });
    }

    private void loadOnlinePlayersEndpoint(RoutesConfig routes) {
        OnlineEndpoint onlineEndpoint = new OnlineEndpoint();
        routes.get(URLPath + "/online", ctx -> ctx.json(onlineEndpoint.lookup(ctx)));
    }

    private void loadMysteryMasterEndpoint(RoutesConfig routes) {
        MysteryMasterIntegration mysteryMasterIntegration = new MysteryMasterIntegration();
        mysteryMasterIntegration.register();
        MysteryMasterEndpoint mysteryMasterEndpoint = new MysteryMasterEndpoint(plugin);
        routes.get(URLPath + "/mm", ctx -> {
            mysteryMasterIntegration.throwIfDisabled();
            ctx.json(mysteryMasterEndpoint.lookup(ctx));
        });
    }

    private void loadShopsEndpoint(RoutesConfig routes) {
        QuickShopIntegration quickShopIntegration = new QuickShopIntegration();
        quickShopIntegration.register();
        ShopEndpoint shopEndpoint = new ShopEndpoint(plugin);
        routes.post(URLPath + "/shop", ctx -> {
            quickShopIntegration.throwIfDisabled();
            ctx.json(shopEndpoint.lookup(ctx));
        });
    }

    private void loadmcMMoEndpoint(RoutesConfig routes) {
        McMMOIntegration mcMMOIntegration = new McMMOIntegration();
        mcMMOIntegration.register();
        McMMOEndpoint mcMMOEndpoint = new McMMOEndpoint(plugin);
        routes.post(URLPath + "/mcmmo", ctx -> {
           mcMMOIntegration.throwIfDisabled();
           ctx.json(mcMMOEndpoint.lookup(ctx));
        });

        McMMOTopEndpoint mcMMOTopEndpoint = new McMMOTopEndpoint(plugin);
        routes.post(URLPath + "/mcmmo-top", ctx -> {
            mcMMOIntegration.throwIfDisabled();
            ctx.json(mcMMOTopEndpoint.lookup(ctx));
        });
    }

    private void loadPursuitsEndpoint(RoutesConfig routes) {
        PursuitsIntegration pursuitsIntegration = new PursuitsIntegration();
        pursuitsIntegration.register();
        PursuitsEndpoint pursuitsEndpoint = new PursuitsEndpoint(plugin);
        routes.post(URLPath + "/pursuits", ctx -> {
            pursuitsIntegration.throwIfDisabled();
            ctx.json(pursuitsEndpoint.lookup(ctx));
        });
    }

    private void loadAdvancementsEndpoint(RoutesConfig routes) {
        AdvancementsIntegration advancementsIntegration = new AdvancementsIntegration();
        advancementsIntegration.register();
        AdvancementsEndpoint advancementsEndpoint = new AdvancementsEndpoint();
        routes.get(URLPath + "/advancements", ctx -> {
            advancementsIntegration.throwIfDisabled();
            ctx.json(advancementsEndpoint.lookup(ctx));
        });
    }
}
