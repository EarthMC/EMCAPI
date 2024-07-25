package net.earthmc.emcapi.util;

import com.google.gson.*;
import io.javalin.http.BadRequestResponse;

public class JSONUtil {

    public static JsonObject getJsonObjectFromString(String string) {
        Gson gson = new Gson();

        try {
            return gson.fromJson(string, JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new BadRequestResponse("Invalid JSON body provided");
        }
    }

    public static String getJsonElementAsStringOrNull(JsonElement element) {
        if (!element.isJsonPrimitive()) return null;

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isString()) return null;

        return primitive.getAsString();
    }

    public static Integer getJsonElementAsIntegerOrNull(JsonElement element) {
        if (!element.isJsonPrimitive()) return null;

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isNumber()) return null;

        return primitive.getAsInt();
    }

    public static JsonArray getJsonElementAsJsonArrayOrNull(JsonElement element) {
        if (!element.isJsonArray()) return null;
        return element.getAsJsonArray();
    }

    public static JsonObject getJsonElementAsJsonObjectOrNull(JsonElement element) {
        if (!element.isJsonObject()) return null;
        return element.getAsJsonObject();
    }
}
