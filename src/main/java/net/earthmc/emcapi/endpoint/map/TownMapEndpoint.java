package net.earthmc.emcapi.endpoint.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
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
        json.addProperty("nation", nation != null ? nation.getName() : null);
        json.addProperty("board", town.getBoard());
        json.addProperty("discord", TownMetadataManager.getDiscordURL(town));
        json.addProperty("wiki", TownMetadataManager.getWikiURL(town));

        json.add("residents", formatResidents(town.getResidents()));
        json.add("councillors", formatResidents(town.getRank("Councillor")));
        json.addProperty("founded", town.getRegistered());
        json.addProperty("claims", town.getNumTownBlocks());
        json.addProperty("balance", TownyEconomyHandler.isActive() ? town.getAccount().getCachedBalance() : 0);
        json.addProperty("pvp", town.isPVP());
        json.addProperty("public", town.isPublic());

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
