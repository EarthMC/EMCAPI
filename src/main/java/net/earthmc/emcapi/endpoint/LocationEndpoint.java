package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.javalin.http.BadRequestResponse;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import kotlin.Pair;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.JSONUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OpenApi(
    path = "/v4/location",
    methods = HttpMethod.POST,
    summary = "Query location data",
    requestBody = @OpenApiRequestBody(
        description = "Specify a pair of x & z coordinates",
        required = true,
        content = {
            @OpenApiContent(
                from = int[].class,
                mimeType = ContentType.JSON,
                example = "[0, 0]"
            )
        }
    ),
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.Location[].class,
                    mimeType = ContentType.JSON,
                    example = """
                        [{
                            "location": {
                              "x":0,
                              "z":0
                            },
                            "isWilderness":false,
                            "town": {
                              "name":"Jyväskylä",
                              "uuid":"0b69c00d-c112-4ca0-a16c-ce551120e464"
                            },
                            "nation": {
                              "name":"Finland",
                              "uuid":"ae16c3c0-f8ab-4715-8553-019168008c49"
                            }
                        }]
                        """
                )
            }
        ),
        @OpenApiResponse(
            status = "400",
            description = "BadRequestResponse is thrown if your query is invalid. One 'pair' (array) of coordinates (E.g. [0, 0]) or an array of pairs (E.g. [[0, 0], [100, 100] is expected."
        )
    }
)
public class LocationEndpoint extends PostEndpoint<Pair<Integer, Integer>> {

    public LocationEndpoint(final EMCAPI plugin) {
        super(plugin);
    }

    @Override
    public Pair<Integer, Integer> getObjectOrNull(@NotNull JsonElement element, @Nullable String key) {
        JsonArray jsonArray = JSONUtil.getJsonElementAsJsonArrayOrNull(element);
        if (jsonArray == null) throw new BadRequestResponse("Your query contains a value that is not a JSON array");

        int x;
        int z;
        try {
            JsonElement xElement = jsonArray.get(0);
            JsonElement zElement = jsonArray.get(1);

            Integer xInner = JSONUtil.getJsonElementAsIntegerOrNull(xElement);
            Integer zInner = JSONUtil.getJsonElementAsIntegerOrNull(zElement);
            if (xInner == null || zInner == null) throw new BadRequestResponse("A JSON array in your query contained a value that was not an int");

            x = xInner;
            z = zInner;
        } catch (IndexOutOfBoundsException oobe) {
            throw new BadRequestResponse("A JSON array in your query did not contain two values");
        }

        return new Pair<>(x, z);
    }

    @Override
    public JsonElement getJsonElement(@NotNull Pair<Integer, Integer> pair, @Nullable String key) {
        int x = pair.getFirst();
        int z = pair.getSecond();

        Location location = new Location(Bukkit.getWorlds().getFirst(), x, 0, z);
        TownyAPI townyAPI = TownyAPI.getInstance();
        Town town = townyAPI.getTown(location);

        JsonObject jsonObject = new JsonObject();
        JsonObject locationObject = new JsonObject();
        locationObject.addProperty("x", x);
        locationObject.addProperty("z", z);
        jsonObject.add("location", locationObject);

        jsonObject.addProperty("isWilderness", townyAPI.isWilderness(location));

        jsonObject.add("town", EndpointUtils.getNameAndIdObject(town));
        jsonObject.add("nation", EndpointUtils.getNameAndIdObject(town == null ? null : town.getNationOrNull()));

        return jsonObject;
    }
}
