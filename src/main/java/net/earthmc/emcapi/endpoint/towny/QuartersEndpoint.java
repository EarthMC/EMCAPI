package net.earthmc.emcapi.endpoint.towny;

import au.lupine.quarters.api.manager.QuarterManager;
import au.lupine.quarters.object.entity.Cuboid;
import au.lupine.quarters.object.entity.Quarter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.HttpExceptions;
import net.earthmc.emcapi.util.JSONUtil;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.UUID;

@OpenApi(
    path = "/v4/quarters",
    methods = HttpMethod.POST,
    summary = "Query quarters data",
    requestBody = @OpenApiRequestBody(
        description = "Specify a quarter UUID to query, optionally include API key",
        required = true,
        content = {
            @OpenApiContent(
                from = ContentTypes.UUIDKey.class,
                mimeType = ContentType.JSON,
                example = """
                    {
                      "query": "5fb3b17a-c67e-476e-b8ad-f030955ef8ea",
                      "key": "<key>"
                    }
                    """
            )
        }
    ),
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.Quarter[].class,
                    mimeType = ContentType.JSON
                )
            },
            description = "When using multi-querying with an array, some may fail silently, as not to disrupt the other successful queries"
        ),
        @OpenApiResponse(
            status = "404",
            description = "If the item(s) in your query (all) return null, a NotFound error is thrown instead of returning an empty array"
        )
    }
)
public class QuartersEndpoint extends PostEndpoint<Quarter> {

    public QuartersEndpoint(final EMCAPI plugin) {
        super(plugin);
    }

    @Override
    public Quarter getObjectOrNull(@NotNull JsonElement element, @Nullable String key) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw HttpExceptions.NOT_A_STRING;;

        UUID uuid;
        try {
            uuid = UUID.fromString(string);
        } catch (IllegalArgumentException e) {
            return null;
        }

        return QuarterManager.getInstance().getQuarter(uuid);
    }

    @Override
    public JsonElement getJsonElement(@NotNull Quarter quarter, @Nullable String key) {
        JsonObject quarterObject = new JsonObject();

        quarterObject.addProperty("name", quarter.getName());
        quarterObject.addProperty("uuid", quarter.getUUID().toString());
        quarterObject.addProperty("type", quarter.getType().toString());
        quarterObject.addProperty("creator", quarter.getCreator() != null ? quarter.getCreator().toString() : null);

        quarterObject.add("owner", EndpointUtils.getResidentJsonObject(quarter.getOwnerResident()));
        quarterObject.add("town", EndpointUtils.getNameAndIdObject(quarter.getTown()));
        quarterObject.add("nation", EndpointUtils.getNameAndIdObject(quarter.getNation()));

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("registered", quarter.getRegistered());
        timestampsObject.addProperty("claimedAt", quarter.getClaimedAt());
        quarterObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("isEmbassy", quarter.isEmbassy());
        statusObject.addProperty("isForSale", quarter.isForSale());
        quarterObject.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("price", quarter.getPrice());
        statsObject.addProperty("volume", quarter.getVolume());
        statsObject.addProperty("numCuboids", quarter.getCuboids().size());
        statsObject.addProperty("particleSize", quarter.getParticleSize());
        quarterObject.add("stats", statsObject);

        JsonArray colourArray = new JsonArray();
        Color colour = quarter.getColour();
        colourArray.add(colour.getRed());
        colourArray.add(colour.getGreen());
        colourArray.add(colour.getBlue());
        colourArray.add(colour.getAlpha());
        quarterObject.add("colour", colourArray);

        quarterObject.add("trusted", EndpointUtils.getResidentArray(quarter.getTrustedResidents()));

        JsonArray cuboidsArray = new JsonArray();
        for (Cuboid cuboid : quarter.getCuboids()) {
            JsonObject cuboidObject = getCuboidObject(cuboid);

            cuboidsArray.add(cuboidObject);
        }
        quarterObject.add("cuboids", cuboidsArray);

        return quarterObject;
    }

    private static JsonObject getCuboidObject(Cuboid cuboid) {
        JsonObject cuboidObject = new JsonObject();

        JsonArray pos1Array = new JsonArray();
        Location pos1 = cuboid.getCornerOne();
        pos1Array.add(pos1.getBlockX());
        pos1Array.add(pos1.getBlockY());
        pos1Array.add(pos1.getBlockZ());

        JsonArray pos2Array = new JsonArray();
        Location pos2 = cuboid.getCornerTwo();
        pos2Array.add(pos2.getBlockX());
        pos2Array.add(pos2.getBlockY());
        pos2Array.add(pos2.getBlockZ());

        cuboidObject.add("cornerOne", pos1Array);
        cuboidObject.add("cornerTwo", pos2Array);

        return cuboidObject;
    }
}
