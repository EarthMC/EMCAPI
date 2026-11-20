package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.object.optout.OptOutType;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
        final List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray(players.size());

        for (Player player : players) {
            if (EMCAPI.instance.getOptOut().playerOptedOut(player.getUniqueId(), OptOutType.ONLINE_STATUS)) {
                continue;
            }

            jsonArray.add(EndpointUtils.generateNameUUIDJsonObject(player.getName(), player.getUniqueId()));
        }

        jsonObject.addProperty("max", Bukkit.getMaxPlayers());
        jsonObject.addProperty("count", players.size());
        jsonObject.add("players", jsonArray);

        return jsonObject;
    }
}
