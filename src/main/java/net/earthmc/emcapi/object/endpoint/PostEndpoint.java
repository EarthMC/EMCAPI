package net.earthmc.emcapi.object.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.earthmc.emcapi.EMCAPI;
import java.util.Map;

public abstract class PostEndpoint<T> {

    public String lookup(JsonArray queryArray, JsonObject template) {
        JsonArray jsonArray = new JsonArray();

        int numLoops = Math.min(EMCAPI.instance.getConfig().getInt("behaviour.max_lookup_size"), queryArray.size());
        for (int i = 0; i < numLoops; i++) {
            JsonElement element = queryArray.get(i);
            T object = getObjectOrNull(element);

            JsonElement innerObject;
            if (object == null) {
                continue;
            } else {
                innerObject = getTemplateJsonElement(object, template);
            }

            jsonArray.add(innerObject);
        }

        return jsonArray.toString();
    }

    public abstract T getObjectOrNull(JsonElement element);

    public abstract JsonElement getJsonElement(T object);

    public JsonElement getTemplateJsonElement(T object, JsonObject template) {
        JsonElement fullJson = getJsonElement(object);

        if (!(fullJson instanceof JsonObject) || template == null || template.entrySet().isEmpty()) {
            return fullJson;
        }

        JsonObject fullJsonObject = fullJson.getAsJsonObject();
        JsonObject filteredJson = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : template.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue().getAsBoolean() && fullJsonObject.has(key)) {
                filteredJson.add(key, fullJsonObject.get(key));
            }
        }

        return filteredJson;
    }
}
