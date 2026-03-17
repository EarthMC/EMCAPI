package net.earthmc.emcapi.object.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.earthmc.emcapi.EMCAPI;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class PostEndpoint<T> {
    protected final EMCAPI plugin;

    protected PostEndpoint(final EMCAPI plugin) {
        this.plugin = plugin;
    }

    public String lookup(JsonArray queryArray, @Nullable JsonObject template, @Nullable String key) {
        JsonArray jsonArray = new JsonArray();

        int numLoops = Math.min(EMCAPI.instance.getConfig().getInt("behaviour.max_lookup_size"), queryArray.size());
        for (int i = 0; i < numLoops; i++) {
            JsonElement element = queryArray.get(i);
            T object = getObjectOrNull(element, key);

            if (object == null) {
                continue;
            }
            jsonArray.add(getTemplateJsonElement(object, template, key));
        }

        return jsonArray.toString();
    }

    public abstract T getObjectOrNull(JsonElement element, @Nullable String key);

    public abstract JsonElement getJsonElement(T object, @Nullable String key);

    public JsonElement getTemplateJsonElement(T object, JsonObject template, @Nullable String key) {
        JsonElement fullJson = getJsonElement(object, key);

        if (!(fullJson instanceof JsonObject) || template == null || template.entrySet().isEmpty()) {
            return fullJson;
        }

        JsonObject fullJsonObject = fullJson.getAsJsonObject();
        JsonObject filteredJson = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : template.entrySet()) {
            String entryKey = entry.getKey();
            if (entry.getValue() instanceof JsonPrimitive primitive && primitive.getAsBoolean() && fullJsonObject.has(entryKey)) {
                filteredJson.add(entryKey, fullJsonObject.get(entryKey));
            }
        }

        return filteredJson;
    }
}
