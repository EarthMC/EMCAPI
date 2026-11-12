package net.earthmc.emcapi.object.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import io.javalin.http.Context;
import net.earthmc.emcapi.util.EndpointUtils;
import org.jetbrains.annotations.NotNull;

public abstract class GetEndpoint {

    public String lookup(Context ctx) {
        String key = EndpointUtils.verifyKey(ctx);
        return getJsonElement(key).toString();
    }

    public JsonElement getJsonElement() {
        return JsonNull.INSTANCE;
    }

    public JsonElement getJsonElement(@NotNull String key) {
        return getJsonElement();
    }
}
