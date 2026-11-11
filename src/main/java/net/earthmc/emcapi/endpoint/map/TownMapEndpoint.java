package net.earthmc.emcapi.endpoint.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.javalin.http.Context;
import net.earthmc.emcapi.manager.TownMetadataManager;
import net.earthmc.emcapi.util.HttpExceptions;
import org.bukkit.Location;

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
        Location spawn;
        if (nation != null && nation.isCapital(town) && (spawn = nation.getSpawnOrNull()) != null) {
            json.add("spawn", formatSpawn(spawn));
        }
        json.addProperty("board", town.getBoard());
        json.addProperty("discord", TownMetadataManager.getDiscordURL(town));
        json.addProperty("wiki", TownMetadataManager.getWikiURL(town));

        json.add("residents", formatResidents(town.getResidents()));
        json.add("councillors", formatResidents(town.getRank("Councillor")));
        json.addProperty("founded", town.getRegistered());
        json.addProperty("claims", town.getNumTownBlocks());
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

    private JsonObject formatSpawn(Location spawn) {
        JsonObject json = new JsonObject();
        json.addProperty("world", spawn.getWorld().getName());
        json.addProperty("x", spawn.getBlockX());
        json.addProperty("y", spawn.getBlockY());
        json.addProperty("z", spawn.getBlockZ());

        return json;
    }
}
