package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.ServiceUnavailableResponse;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.service.mysterymaster.MysteryMasterService;
import net.earthmc.emcapi.service.mysterymaster.MysteryPlayer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.ServiceLoader;

public class MysteryMasterEndpoint extends GetEndpoint {

    private final MysteryMasterService service;

    public MysteryMasterEndpoint() {
        final Plugin mm = Bukkit.getPluginManager().getPlugin("MysteryMaster");
        if (mm == null) {
            service = null;
            return;
        }

        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(mm.getClass().getClassLoader());
            service = ServiceLoader.load(MysteryMasterService.class).findFirst().orElse(null);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Override
    public String lookup() {
        if (service == null) throw new ServiceUnavailableResponse("Mystery Master details are not available currently");
        return getJsonElement().toString();
    }

    @Override
    public JsonElement getJsonElement() {
        JsonArray jsonArray = new JsonArray();

        List<? extends MysteryPlayer> players = service.getCurrentTopPlayers();
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
