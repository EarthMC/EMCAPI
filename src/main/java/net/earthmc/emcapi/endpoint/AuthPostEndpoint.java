package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import org.jetbrains.annotations.NotNull;

public class AuthPostEndpoint extends PostEndpoint<Integer> {

    public AuthPostEndpoint(EMCAPI plugin) {
        super(plugin);
    }

    @Override
    protected Integer getObjectOrNull(@NotNull JsonElement element, String key) {
        return 0;
    }

    @Override
    protected JsonObject getJsonElement(@NotNull Integer object, String key) {
        JsonObject json = new JsonObject();
        json.addProperty("authorized", String.valueOf(KeyManager.getKeyOwner(key)));
        return json;
    }
}
