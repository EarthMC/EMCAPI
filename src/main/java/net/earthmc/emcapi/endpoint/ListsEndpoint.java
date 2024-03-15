package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.palmergames.bukkit.towny.TownyAPI;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.quarters.api.QuartersAPI;
import net.earthmc.quarters.object.Quarter;

public class ListsEndpoint {

    public String listPlayers() {
        JsonArray jsonArray = EndpointUtils.getResidentArray(TownyAPI.getInstance().getResidents());

        return jsonArray == null ? null : jsonArray.toString();
    }

    public String listTowns() {
        JsonArray jsonArray = EndpointUtils.getTownArray(TownyAPI.getInstance().getTowns());

        return jsonArray == null ? null : jsonArray.toString();
    }

    public String listNations() {
        JsonArray jsonArray = EndpointUtils.getNationArray(TownyAPI.getInstance().getNations());

        return jsonArray == null ? null : jsonArray.toString();
    }

    public String listQuarters() {
        JsonArray jsonArray = new JsonArray();

        for (Quarter quarter : QuartersAPI.getInstance().getAllQuarters()) {
            jsonArray.add(quarter.getUUID().toString());
        }

        return jsonArray.toString();
    }
}
