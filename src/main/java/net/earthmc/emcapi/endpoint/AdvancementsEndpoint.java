package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import net.earthmc.emcapi.integration.AdvancementsIntegration;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.lynchpin.api.advancements.AdvancementEntry;

import java.text.SimpleDateFormat;

@OpenApi(
    path = "/v4/advancements",
    methods = HttpMethod.GET,
    summary = "Get a list of advancements completed on the server, along with the date it was completed on and the player that did it",
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.Advancements.class,
                    mimeType = ContentType.JSON,
                    example = """
                        {minecraft:adventure/totem_of_undying":{"player":"77b181f2-61da-49cb-93a7-17e0febf0511","date":"2026-04-18"}}
                        """
                )
            }
        ),
        @OpenApiResponse(
            status = "503",
            description = "If the advancements service is currently unavailable or disabled"
        )
    }
)
public class AdvancementsEndpoint extends GetEndpoint {
    private final AdvancementsIntegration integration;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public AdvancementsEndpoint() {
        integration = Integrations.getIntegration("lynchpin-advancements");
    }

    @Override
    public JsonElement getJsonElement() {
        JsonObject outer = new JsonObject();
        for (AdvancementEntry entry : integration.getAdvancements()) {
            JsonObject json = new JsonObject();
            json.addProperty("player", entry.player().toString());
            json.addProperty("date", formatter.format(entry.date()));

            outer.add(entry.advancement(), json);
        }

        return outer;
    }
}
