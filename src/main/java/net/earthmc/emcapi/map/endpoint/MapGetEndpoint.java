package net.earthmc.emcapi.map.endpoint;

import io.javalin.http.Context;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;

/**
 * Represents a GET endpoint for /map/ routes that don't require an API key.
 */
public abstract class MapGetEndpoint extends GetEndpoint {
    @Override
    public String lookup(Context ctx) {
        return getJsonElement(ctx, "").toString();
    }
}
