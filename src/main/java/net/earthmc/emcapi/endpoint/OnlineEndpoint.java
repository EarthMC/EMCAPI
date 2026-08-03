package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonElement;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.Bukkit;

import java.util.ArrayList;

@OpenApi(
    path = "/v4/online",
    methods = HttpMethod.GET,
    summary = "Get a count & list of online players, excluding those who have opted out",
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.Online.class,
                    mimeType = ContentType.JSON,
                    example = """
                        {"count":720,"players":[{"name":"Czipsu35","uuid":"12a19eee-6539-4634-89bc-4398ab8de870"}]}
                        """
                )
            }
        )
    }
)
public class OnlineEndpoint extends GetEndpoint {

    @Override
    public JsonElement getJsonElement() {
        return EndpointUtils.getOnlinePlayerArray(new ArrayList<>(Bukkit.getOnlinePlayers()));
    }
}
