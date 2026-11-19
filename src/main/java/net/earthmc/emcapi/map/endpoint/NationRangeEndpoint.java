package net.earthmc.emcapi.map.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import io.javalin.http.Context;
import net.earthmc.emcapi.map.MapEndpointManager;
import net.earthmc.emcapi.util.HttpExceptions;

public class NationRangeEndpoint extends MapGetEndpoint {
    private final MapEndpointManager manager;

    public NationRangeEndpoint(MapEndpointManager manager) {
        this.manager = manager;
    }

    @Override
    public JsonElement getJsonElement(Context ctx, String key) {
        final String name = ctx.pathParam("nation");
        if (name.isEmpty() || name.length() > manager.maxTownyNameLength()) {
            throw HttpExceptions.NOT_FOUND;
        }

        final Nation nation = TownyUniverse.getInstance().getNation(name);
        if (nation == null) {
            throw HttpExceptions.NOT_FOUND;
        }

        final JsonObject object = new JsonObject();

        final Town capital = nation.getCapital();
        object.addProperty("capital", capital != null ? capital.getName() : null);

        final JsonArray townsArray = new JsonArray(nation.getNumTowns());
        for (final Town town : nation.getTowns()) {
            final JsonObject townObject = new JsonObject();

            final TownBlock homeBlock = town.getHomeBlockOrNull();
            if (homeBlock != null) {
                final JsonObject homeObject = new JsonObject();

                homeObject.addProperty("x", homeBlock.getX());
                homeObject.addProperty("z", homeBlock.getZ());
                townObject.add("home", homeObject);
            } else {
                townObject.add("home", null);
            }

            townsArray.add(townObject);
        }

        object.add("towns", townsArray);

        return object;
    }
}
