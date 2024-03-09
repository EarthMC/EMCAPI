package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.earthmc.quarters.api.QuartersAPI;
import net.earthmc.quarters.object.Quarter;

public class ListsEndpoint {

    public String listPlayers() {
        JsonArray jsonArray = new JsonArray();

        for (Resident resident : TownyAPI.getInstance().getResidents()) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("name", resident.getName());
            jsonObject.addProperty("uuid", resident.getUUID().toString());

            jsonArray.add(jsonObject);
        }

        return jsonArray.toString();
    }

    public String listTowns() {
        JsonArray jsonArray = new JsonArray();

        for (Town town : TownyAPI.getInstance().getTowns()) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("name", town.getName());
            jsonObject.addProperty("uuid", town.getUUID().toString());

            jsonArray.add(jsonObject);
        }

        return jsonArray.toString();
    }

    public String listNations() {
        JsonArray jsonArray = new JsonArray();

        for (Nation nation : TownyAPI.getInstance().getNations()) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("name", nation.getName());
            jsonObject.addProperty("uuid", nation.getUUID().toString());

            jsonArray.add(jsonObject);
        }

        return jsonArray.toString();
    }

    public String listQuarters() {
        JsonArray jsonArray = new JsonArray();

        for (Quarter quarter : QuartersAPI.getInstance().getAllQuarters()) {
            jsonArray.add(quarter.getUUID().toString());
        }

        return jsonArray.toString();
    }
}
