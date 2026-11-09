package net.earthmc.emcapi.object.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

// TODO: Make #lookup final again, remove #getJsonElement
// Once all endpoints require authentication
public abstract class GetEndpoint {

    public String lookup(Context ctx) {
        return getJsonElement().toString();
    }

    public JsonElement getJsonElement() {
        return JsonNull.INSTANCE;
    }

    public JsonElement getJsonElement(@NotNull String key) {
        return getJsonElement();
    }
}
