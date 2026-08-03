package net.earthmc.emcapi.endpoint.towny.list;

import com.google.gson.JsonArray;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import net.earthmc.emcapi.integration.QuartersIntegration;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;

@OpenApi(
    path = "/v4/quarters",
    methods = HttpMethod.GET,
    summary = "Get a list of all quarters and their UUIDs",
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.NameUUID[].class,
                    example = """
                        [{"name":"Dull Residence","uuid":"53040e8f-c269-4514-a9a8-af2aac278e4e"},{"name":"Annoying Flat","uuid":"08150543-bfbd-489b-96a0-df95f9a1782f"}]
                        """
                )
            }
        )
    }
)
public class QuartersListEndpoint extends GetEndpoint {
    private final QuartersIntegration quartersIntegration;

    public QuartersListEndpoint(QuartersIntegration quartersIntegration) {
        this.quartersIntegration = quartersIntegration;
    }

    @Override
    public JsonArray getJsonElement() {
        return quartersIntegration.getAllQuartersArray();
    }
}
