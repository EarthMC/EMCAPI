package net.earthmc.emcapi.endpoint.towny.list;

import com.google.gson.JsonArray;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;

import java.util.List;

public class PlayersListEndpoint extends GetEndpoint {

    @Override
    public JsonArray getJsonElement() {
        List<Resident> residents = EndpointUtils.filterActiveResidents(TownyAPI.getInstance().getResidents());
        return EndpointUtils.getResidentArray(residents);
    }
}
