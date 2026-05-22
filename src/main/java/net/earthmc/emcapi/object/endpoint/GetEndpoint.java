package net.earthmc.emcapi.object.endpoint;

import com.google.gson.JsonElement;

public abstract class GetEndpoint {

    public final String lookup() {
        return getJsonElement().toString();
    }

    public abstract JsonElement getJsonElement();
}
