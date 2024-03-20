package net.earthmc.emcapi.manager;

import io.javalin.Javalin;
import net.earthmc.emcapi.endpoint.*;
import net.earthmc.emcapi.endpoint.legacy.v1.*;
import net.earthmc.emcapi.endpoint.legacy.v2.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;

public class EndpointManager {
    Javalin javalin;
    FileConfiguration config;
    Economy economy;
    String v1URLPath;
    String v2URLPath;
    String v3URLPath;

    public EndpointManager(Javalin javalin, FileConfiguration config, Economy economy) {
        this.javalin = javalin;
        this.config = config;
        this.economy = economy;
        this.v1URLPath = "v1/" + config.getString("networking.url_path");
        this.v2URLPath = "v2/" + config.getString("networking.url_path");
        this.v3URLPath = "v3/" + config.getString("networking.url_path");
    }

    public void loadEndpoints() {
        DocumentationEndpoint documentationEndpoint = new DocumentationEndpoint(config);
        javalin.get("/", ctx -> ctx.json(documentationEndpoint.lookup()));

        ServerEndpoint serverEndpoint = new ServerEndpoint();
        javalin.get(v3URLPath, ctx -> ctx.json(serverEndpoint.lookup()));

        ListsEndpoint listsEndpoint = new ListsEndpoint();
        PlayersEndpoint playersEndpoint = new PlayersEndpoint(economy);
        javalin.get(v3URLPath + "/players", ctx -> {
            String query = ctx.queryParamAsClass("query", String.class).getOrDefault(null);

            if (query == null) {
                ctx.json(listsEndpoint.listPlayers());
                return;
            }

            ctx.json(playersEndpoint.lookup(query));
        });

        TownsEndpoint townsEndpoint = new TownsEndpoint();
        javalin.get(v3URLPath + "/towns", ctx -> {
            String query = ctx.queryParamAsClass("query", String.class).getOrDefault(null);

            if (query == null) {
                ctx.json(listsEndpoint.listTowns());
                return;
            }

            ctx.json(townsEndpoint.lookup(query));
        });

        NationsEndpoint nationsEndpoint = new NationsEndpoint();
        javalin.get(v3URLPath + "/nations", ctx -> {
            String query = ctx.queryParamAsClass("query", String.class).getOrDefault(null);

            if (query == null) {
                ctx.json(listsEndpoint.listNations());
                return;
            }

            ctx.json(nationsEndpoint.lookup(query));
        });

        QuartersEndpoint quartersEndpoint = new QuartersEndpoint();
        javalin.get(v3URLPath + "/quarters", ctx -> {
            String query = ctx.queryParamAsClass("query", String.class).getOrDefault(null);

            if (query == null) {
                ctx.json(listsEndpoint.listQuarters());
                return;
            }

            ctx.json(quartersEndpoint.lookup(query));
        });

        LocationEndpoint locationEndpoint = new LocationEndpoint();
        javalin.get(v3URLPath + "/location", ctx -> {
            Integer x = ctx.queryParamAsClass("x", Integer.class).getOrDefault(null);
            Integer z = ctx.queryParamAsClass("z", Integer.class).getOrDefault(null);

            ctx.json(locationEndpoint.lookup(x, z));
        });

        NearbyEndpoint nearbyEndpoint = new NearbyEndpoint();
        javalin.get(v3URLPath + "/nearby/coordinate", ctx -> {
            Integer x = ctx.queryParamAsClass("x", Integer.class).getOrDefault(null);
            Integer z = ctx.queryParamAsClass("z", Integer.class).getOrDefault(null);
            Integer radius = ctx.queryParamAsClass("radius", Integer.class).getOrDefault(null);

            ctx.json(nearbyEndpoint.lookupNearbyCoordinate(x, z, radius));
        });

        javalin.get(v3URLPath + "/nearby/town", ctx -> {
            String town = ctx.queryParamAsClass("town", String.class).getOrDefault(null);
            Integer radius = ctx.queryParamAsClass("radius", Integer.class).getOrDefault(null);

            ctx.json(nearbyEndpoint.lookupNearbyTown(town, radius));
        });

        DiscordEndpoint discordEndpoint = new DiscordEndpoint();
        javalin.get(v3URLPath + "/discord", ctx -> {
            String query = ctx.queryParamAsClass("query", String.class).getOrDefault(null);

            ctx.json(discordEndpoint.lookup(query));
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
