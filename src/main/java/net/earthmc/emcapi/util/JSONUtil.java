package net.earthmc.emcapi.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.javalin.http.BadRequestResponse;

public class JSONUtil {

    public static JsonObject getJsonObjectFromString(String string) {
        try {
            return JsonParser.parseString(string).getAsJsonObject();
        } catch (Exception e) {
            throw new BadRequestResponse("Invalid JSON body provided");
        }
    }

    public static String getJsonElementAsStringOrNull(JsonElement element) {
        if (element == null) return null;

        if (!element.isJsonPrimitive()) return null;

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isString()) return null;

        return primitive.getAsString();
    }

    public static Integer getJsonElementAsIntegerOrNull(JsonElement element) {
        if (element == null) return null;

        if (!element.isJsonPrimitive()) return null;

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isNumber()) return null;

        return primitive.getAsInt();
    }

    public static JsonArray getJsonElementAsJsonArrayOrNull(JsonElement element) {
        if (element == null) return null;

        if (!element.isJsonArray()) return null;
        return element.getAsJsonArray();
    }

    public static JsonObject getJsonElementAsJsonObjectOrNull(JsonElement element) {
        if (element == null) return null;

        if (!element.isJsonObject()) return null;
        return element.getAsJsonObject();
    }

    public static JsonArray toJsonArray(final Iterable<?> collection) {
        final JsonArray array = new JsonArray();

        for (final Object element : collection) {
            switch (element) {
                case Boolean bool -> array.add(bool);
                case Number number -> array.add(number);
                case Character character -> array.add(character);
                case String string -> array.add(string);
                case JsonElement jsonElement -> array.add(jsonElement);
                case null -> array.add(JsonNull.INSTANCE);
                default -> throw new IllegalArgumentException("unsupported collection type '" + element.getClass().getName() + "'");
            }
        }

        return array;
    }
}
