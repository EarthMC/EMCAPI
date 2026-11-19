package net.earthmc.emcapi.object.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import io.javalin.http.Context;
import net.earthmc.emcapi.util.EndpointUtils;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class GetEndpoint {

    public String lookup(Context ctx) {
        String key = EndpointUtils.verifyKey(ctx);
        return getJsonElement(ctx, key).toString();
    }

    public JsonElement getJsonElement() {
        return JsonNull.INSTANCE;
    }

    public JsonElement getJsonElement(Context ctx, String key) {
        return getJsonElement();
    }
}
