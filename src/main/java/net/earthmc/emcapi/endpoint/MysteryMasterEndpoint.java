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
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.mysterymaster.api.MysteryMasterAPI;
import net.earthmc.mysterymaster.api.MysteryMasterProvider;
import net.earthmc.mysterymaster.api.MysteryPlayer;

import java.util.List;

@OpenApi(
    path = "/v4/mm",
    methods = HttpMethod.GET,
    summary = "Query Mystery Master leaderboard data",
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.MysteryMasterEntry[].class,
                    mimeType = ContentType.JSON,
                    example = """
                        [{"name":"nahember","uuid":"31c426fb-70fe-4612-9122-d256af182d6f","change":"UP"},{"name":"FindItCooper","uuid":"e83a3e70-13d7-43b1-a91f-bef87bbd9fe9","change":"UP"}]
                        """
                )
            }
        ),
        @OpenApiResponse(
            status = "503",
            description = "If the Mystery Master service is currently unavailable or disabled"
        )
    }
)
public class MysteryMasterEndpoint extends GetEndpoint {

    private MysteryMasterAPI api = null;

    public MysteryMasterEndpoint(final EMCAPI plugin) {
        try {
            api = MysteryMasterProvider.api();
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().warning("Not loading mystery master endpoint due to the plugin not being present");
        }
    }

    @Override
    public JsonElement getJsonElement() {
        JsonArray jsonArray = new JsonArray();

        List<MysteryPlayer> players = api.getCurrentTopPlayers();
        for (int i = 0; i < Math.min(50, players.size()); i++) {
            MysteryPlayer player = players.get(i);
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("name", player.username());
            jsonObject.addProperty("uuid", player.uuid().toString());
            jsonObject.addProperty("change", getChange(player.indexChange()));

            jsonArray.add(jsonObject);
        }

        return jsonArray;
    }

    private String getChange(int indexChange) {
        if (indexChange == 0) return "UNCHANGED";

        // positive change means down, negative up
        return indexChange > 0 ? "DOWN" : "UP";
    }
}
