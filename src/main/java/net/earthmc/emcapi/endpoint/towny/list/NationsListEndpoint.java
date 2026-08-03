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
    path = "/v4/nations",
    methods = HttpMethod.GET,
    summary = "Get a list of all nations and their UUIDs",
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.NameUUID[].class,
                    example = """
                        [{"name":"North_America","uuid":"851d8de8-e401-4685-8ca2-87ad030f2695"},{"name":"Japan","uuid":"32d05375-2745-48c7-99df-418d27f00d34"}]
                        """
                )
            }
        )
    }
)
public class NationsListEndpoint extends GetEndpoint {

    @Override
    public JsonArray getJsonElement() {
        return EndpointUtils.getGovernmentArray(TownyAPI.getInstance().getNations());
    }
}
