package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.ServiceUnavailableResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.mysterymaster.api.MysteryMasterAPI;
import net.earthmc.mysterymaster.api.MysteryMasterProvider;
import net.earthmc.mysterymaster.api.MysteryPlayer;

import java.util.List;

public class MysteryMasterEndpoint extends GetEndpoint {

    private MysteryMasterAPI api = null;
    private final ServiceUnavailableResponse UNAVAILABLE = new ServiceUnavailableResponse("Mystery Master details are not available currently");

    public MysteryMasterEndpoint(final EMCAPI plugin) {
        try {
            api = MysteryMasterProvider.api();
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().warning("Not loading mystery master endpoint due to the plugin not being present");
        }
    }

    @Override
    public String lookup() {
        if (api == null) throw UNAVAILABLE;
        return getJsonElement().toString();
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
