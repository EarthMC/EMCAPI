package net.earthmc.emcapi.endpoint.towny.list;

import com.google.gson.JsonArray;
import com.palmergames.bukkit.towny.TownyAPI;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;

@OpenApi(
    path = "/v4/towns",
    methods = HttpMethod.GET,
    summary = "Get a list of all towns and their UUIDs",
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.NameUUID[].class,
                    example = """
                        [{"name":"Bern","uuid":"d170e904-17d7-403d-9523-b036d3bd88bf"},{"name":"Fiji","uuid":"2778efd7-8637-40db-bfe6-6353446e28f3"}]
                        """
                )
            }
        )
    }
)
public class TownsListEndpoint extends GetEndpoint {

    @Override
    public JsonArray getJsonElement() {
        return EndpointUtils.getGovernmentArray(TownyAPI.getInstance().getTowns());
    }
}
