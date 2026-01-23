package net.earthmc.emcapi.endpoint.towny.list;

import com.google.gson.JsonArray;
import net.earthmc.emcapi.integration.QuartersIntegration;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;

public class QuartersListEndpoint extends GetEndpoint {
    private final QuartersIntegration quartersIntegration;

    public QuartersListEndpoint(QuartersIntegration quartersIntegration) {
        this.quartersIntegration = quartersIntegration;
    }

    @Override
    public String lookup() {
        return getJsonElement().toString();
    }

    @Override
    public JsonArray getJsonElement() {
        return quartersIntegration.getAllQuartersArray();
    }
}
