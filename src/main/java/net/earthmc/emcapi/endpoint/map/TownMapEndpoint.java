package net.earthmc.emcapi.endpoint.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.earthmc.emcapi.manager.NationMetadataManager;
import net.earthmc.emcapi.manager.TownMetadataManager;
import net.earthmc.emcapi.util.HttpExceptions;

import java.util.Collection;

public class TownMapEndpoint {

    public String lookup(String townName) {
        if (townName == null || townName.isEmpty()) {
            throw HttpExceptions.NOT_FOUND;
        }

        Town town = TownyAPI.getInstance().getTown(townName);
        if (town == null) {
            throw HttpExceptions.NOT_FOUND;
        }

        return formatTown(town).toString();
    }

    private JsonObject formatTown(Town town) {
        JsonObject json = new JsonObject();
        json.addProperty("name", town.getName());
        json.addProperty("mayor", town.getMayor().getName());
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

        json.add("residents", formatResidents(town.getResidents()));
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
