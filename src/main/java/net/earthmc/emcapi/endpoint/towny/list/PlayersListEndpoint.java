package net.earthmc.emcapi.endpoint.towny.list;

import com.google.gson.JsonArray;
import com.palmergames.bukkit.towny.TownyAPI;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;

public class PlayersListEndpoint extends GetEndpoint {

    @Override
    public JsonArray getJsonElement() {
        return EndpointUtils.getResidentArray(TownyAPI.getInstance().getResidents());
    }
}
