package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.palmergames.bukkit.towny.TownyAPI;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.quarters.api.QuartersAPI;

public class ListsEndpoint {

    public String listPlayers() {
        JsonArray jsonArray = EndpointUtils.getResidentArray(TownyAPI.getInstance().getResidents());

        return jsonArray.toString();
    }

    public String listTowns() {
        JsonArray jsonArray = EndpointUtils.getTownArray(TownyAPI.getInstance().getTowns());

        return jsonArray.toString();
    }

    public String listNations() {
        JsonArray jsonArray = EndpointUtils.getNationArray(TownyAPI.getInstance().getNations());

        return jsonArray.toString();
    }

    public String listQuarters() {
        JsonArray jsonArray = EndpointUtils.getQuarterArray(QuartersAPI.getInstance().getAllQuarters());

        return jsonArray.toString();
    }
}
