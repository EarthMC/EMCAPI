package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import org.jetbrains.annotations.NotNull;

public class AuthGetEndpoint extends GetEndpoint {

    @Override
    public String lookup(Context ctx) {
        String value = super.lookup(ctx);
        ctx.header("Cache-Control", "private, max-age=30");
        return value;
    }

    @Override
    public JsonElement getJsonElement(@NotNull String key) {
        JsonObject json = new JsonObject();
        json.addProperty("authorized", String.valueOf(KeyManager.getKeyOwner(key)));
        return json;
    }
}
