package net.earthmc.emcapi.manager;

import io.javalin.Javalin;
import net.earthmc.emcapi.endpoint.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;

public class EndpointManager {
    Javalin javalin;
    FileConfiguration config;
    Economy economy;
    String urlPath;

    public EndpointManager(Javalin javalin, FileConfiguration config, Economy economy) {
        this.javalin = javalin;
        this.config = config;
        this.economy = economy;
        this.urlPath = config.getString("networking.url_path");
    }

    public void loadEndpoints() {
        DocumentationEndpoint documentationEndpoint = new DocumentationEndpoint();
        javalin.get(urlPath, ctx -> ctx.json(documentationEndpoint.lookup()));

        ServerEndpoint serverEndpoint = new ServerEndpoint();
        javalin.get(urlPath + "/server", ctx -> ctx.json(serverEndpoint.lookup()));

        ListsEndpoint listsEndpoint = new ListsEndpoint();
        PlayersEndpoint playersEndpoint = new PlayersEndpoint(economy);
        javalin.get(urlPath + "/players", ctx -> {
            String query = ctx.queryParamAsClass("query", String.class).getOrDefault(null);

            if (query == null) {
                ctx.json(listsEndpoint.listPlayers());
                return;
            }

            ctx.json(playersEndpoint.lookup(query));
        });

        TownsEndpoint townsEndpoint = new TownsEndpoint();
        javalin.get(urlPath + "/towns", ctx -> {
            String query = ctx.queryParamAsClass("query", String.class).getOrDefault(null);

            if (query == null) {
                ctx.json(listsEndpoint.listTowns());
                return;
            }

            ctx.json(townsEndpoint.lookup(query));
        });

        NationsEndpoint nationsEndpoint = new NationsEndpoint();
        javalin.get(urlPath + "/nations", ctx -> {
            String query = ctx.queryParamAsClass("query", String.class).getOrDefault(null);

            if (query == null) {
                ctx.json(listsEndpoint.listNations());
                return;
            }

            ctx.json(nationsEndpoint.lookup(query));
        });

        QuartersEndpoint quartersEndpoint = new QuartersEndpoint();
        javalin.get(urlPath + "/quarters", ctx -> {
            String query = ctx.queryParamAsClass("query", String.class).getOrDefault(null);

            if (query == null) {
                ctx.json(listsEndpoint.listQuarters());
                return;
            }

            ctx.json(quartersEndpoint.lookup(query));
        });

        LocationEndpoint locationEndpoint = new LocationEndpoint();
        javalin.get(urlPath + "/location", ctx -> {
            Integer x = ctx.queryParamAsClass("x", Integer.class).getOrDefault(null);
            Integer z = ctx.queryParamAsClass("z", Integer.class).getOrDefault(null);

            ctx.json(locationEndpoint.lookup(x, z));
        });

        NearbyEndpoint nearbyEndpoint = new NearbyEndpoint();
        javalin.get(urlPath + "/nearby/coordinate", ctx -> {
            Integer x = ctx.queryParamAsClass("x", Integer.class).getOrDefault(null);
            Integer z = ctx.queryParamAsClass("z", Integer.class).getOrDefault(null);
            Integer radius = ctx.queryParamAsClass("radius", Integer.class).getOrDefault(null);

            ctx.json(nearbyEndpoint.lookupNearbyCoordinate(x, z, radius));
        });

        javalin.get(urlPath + "/nearby/town", ctx -> {
            String town = ctx.queryParamAsClass("town", String.class).getOrDefault(null);
            Integer radius = ctx.queryParamAsClass("radius", Integer.class).getOrDefault(null);

            ctx.json(nearbyEndpoint.lookupNearbyTown(town, radius));
        });

        DiscordEndpoint discordEndpoint = new DiscordEndpoint();
        javalin.get(urlPath + "/discord/id/{query}", ctx -> ctx.json(discordEndpoint.lookupID(ctx.pathParam("query"))));

        javalin.get(urlPath + "/discord/uuid/{query}", ctx -> ctx.json(discordEndpoint.lookupUUID(ctx.pathParam("query"))));
    }
}
