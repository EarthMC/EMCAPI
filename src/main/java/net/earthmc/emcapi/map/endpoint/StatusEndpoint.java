package net.earthmc.emcapi.map.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyUniverse;
import io.javalin.http.Context;
import org.bukkit.Server;

/**
 * Used on the website's front page/navbar.
 */
public class StatusEndpoint extends MapGetEndpoint {
    private final Server server;
    private final TownyUniverse townyUniverse;

    public StatusEndpoint(Server server) {
        this.server = server;
        this.townyUniverse = TownyUniverse.getInstance();
    }

    @Override
    public JsonElement getJsonElement(Context ctx, String key) {
        ctx.header("Cache-Control", "public, max-age=60");

        final JsonObject object = new JsonObject();
        object.addProperty("onlinePlayers", server.getOnlinePlayers().size());
        object.addProperty("numTowns", townyUniverse.getTowns().size());

        return object;
    }
}
