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
        loadLookupEndpoints();
        loadListEndpoints();
    }

    private void loadLookupEndpoints() {
        ServerEndpoint serverEndpoint = new ServerEndpoint();
        javalin.get(urlPath, ctx -> ctx.json(serverEndpoint.lookup()));

        PlayersEndpoint playersEndpoint = new PlayersEndpoint(config, economy);
        javalin.get(urlPath + "/players/{query}", ctx -> ctx.json(playersEndpoint.lookup(ctx.pathParam("query"))));

        TownsEndpoint townsEndpoint = new TownsEndpoint(config);
        javalin.get(urlPath + "/towns/{query}", ctx -> ctx.json(townsEndpoint.lookup(ctx.pathParam("query"))));

        NationsEndpoint nationsEndpoint = new NationsEndpoint(config);
        javalin.get(urlPath + "/nations/{query}", ctx -> ctx.json(nationsEndpoint.lookup(ctx.pathParam("query"))));

        QuartersEndpoint quartersEndpoint = new QuartersEndpoint(config);
        javalin.get(urlPath + "/quarters/{query}", ctx -> ctx.json(quartersEndpoint.lookup(ctx.pathParam("query"))));

        LocationEndpoint locationEndpoint = new LocationEndpoint();
        javalin.get(urlPath + "/location", ctx -> {
            Integer x = ctx.queryParamAsClass("x", Integer.class).getOrDefault(null);
            Integer z = ctx.queryParamAsClass("z", Integer.class).getOrDefault(null);

            ctx.json(locationEndpoint.lookup(x, z));
        });

        DiscordEndpoint discordEndpoint = new DiscordEndpoint();
        javalin.get(urlPath + "/discord/id/{query}", ctx -> ctx.json(discordEndpoint.lookupID(ctx.pathParam("query"))));

        javalin.get(urlPath + "/discord/uuid/{query}", ctx -> ctx.json(discordEndpoint.lookupUUID(ctx.pathParam("query"))));
    }

    private void loadListEndpoints() {
        ListsEndpoint listsEndpoint = new ListsEndpoint();
        javalin.get(urlPath + "/players", ctx -> ctx.json(listsEndpoint.listPlayers()));
        javalin.get(urlPath + "/towns", ctx -> ctx.json(listsEndpoint.listTowns()));
        javalin.get(urlPath + "/nations", ctx -> ctx.json(listsEndpoint.listNations()));
        javalin.get(urlPath + "/quarters", ctx -> ctx.json(listsEndpoint.listQuarters()));
    }
}
