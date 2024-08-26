package net.earthmc.emcapi.endpoint.towny.list;

import au.lupine.quarters.api.manager.QuarterManager;
import com.google.gson.JsonArray;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;

public class QuartersListEndpoint extends GetEndpoint {

    @Override
    public String lookup() {
        return getJsonElement().toString();
    }

    @Override
    public JsonArray getJsonElement() {
        return EndpointUtils.getQuarterArray(QuarterManager.getInstance().getAllQuarters());
    }
}
