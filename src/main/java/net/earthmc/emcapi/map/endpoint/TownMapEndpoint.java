package net.earthmc.emcapi.map.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.javalin.http.Context;
import net.earthmc.emcapi.manager.NationMetadataManager;
import net.earthmc.emcapi.manager.TownMetadataManager;
import net.earthmc.emcapi.map.MapEndpointManager;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.HttpExceptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TownMapEndpoint extends MapGetEndpoint {
    private final MapEndpointManager manager;

    public TownMapEndpoint(final MapEndpointManager manager) {
        this.manager = manager;
    }

    @Override
    public JsonElement getJsonElement(Context ctx, String key) {
        final String townName = ctx.pathParam("town");

        if (townName.isEmpty() || townName.length() > manager.maxTownyNameLength()) {
            throw HttpExceptions.NOT_FOUND;
        }

        final Town town = TownyAPI.getInstance().getTown(townName);
        if (town == null) {
            throw HttpExceptions.NOT_FOUND;
        }

        JsonObject json = new JsonObject();
        json.addProperty("name", town.getName());

        final Resident mayor = town.getMayor();
        json.addProperty("mayor", mayor != null ? mayor.getName() : "");
        Nation nation = town.getNationOrNull();
        if (nation == null) {
            json.add("nation", null);
        } else {
            JsonObject nationObject = new JsonObject();
            nationObject.addProperty("name", nation.getName());
            nationObject.addProperty("wiki", NationMetadataManager.getWikiURL(nation));
            json.add("nation", nationObject);
        }

        JsonObject links = new JsonObject();
        links.addProperty("board", town.getBoard());
        links.addProperty("discord", TownMetadataManager.getDiscordURL(town));
        links.addProperty("wiki", TownMetadataManager.getWikiURL(town));
        json.add("links", links);

        final long inactivityCutoff = System.currentTimeMillis() - (TownySettings.getDeleteTime() * 1000L);
        final List<Resident> inactiveResidents = new ArrayList<>();
        final List<Resident> activeResidents = new ArrayList<>();

        for (final Resident resident : town.getResidents()) {
            final boolean active = EndpointUtils.isResidentActive(resident, inactivityCutoff);
            (active ? activeResidents : inactiveResidents).add(resident);
        }

        json.add("residents", formatResidents(activeResidents));
        json.add("inactiveResidents", formatResidents(inactiveResidents));

        json.add("councillors", formatResidents(town.getRank("Councillor")));

        JsonObject stats = new JsonObject();
        stats.addProperty("founded", town.getRegistered());
        stats.addProperty("claims", town.getNumTownBlocks());
        stats.addProperty("balance", TownyEconomyHandler.isActive() ? town.getAccount().getCachedBalance() : 0);
        json.add("stats", stats);

        JsonObject status = new JsonObject();
        status.addProperty("pvp", town.isPVP());
        status.addProperty("public", town.isPublic());
        json.add("status", status);

        return json;
    }

    private JsonArray formatResidents(Collection<Resident> residents) {
        JsonArray array = new JsonArray();
        for (Resident resident : residents) {
            array.add(resident.getName());
        }
        return array;
    }
}
