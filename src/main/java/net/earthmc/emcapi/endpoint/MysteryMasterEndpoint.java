package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.ServiceUnavailableResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.mysterymaster.MysteryMasterPlugin;
import net.earthmc.mysterymaster.data.MysteryPlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.List;

public class MysteryMasterEndpoint extends GetEndpoint {

    private static MysteryMasterPlugin mmp;

    public MysteryMasterEndpoint() {
        PluginManager pm = EMCAPI.instance.getServer().getPluginManager();

        Plugin plugin = pm.getPlugin("MysteryMaster");
        if (plugin == null) {
            mmp = null;
        } else {
            mmp = (MysteryMasterPlugin) plugin;
        }
    }

    @Override
    public String lookup() {
        if (mmp == null) throw new ServiceUnavailableResponse("Mystery Master details are not available currently");
        return getJsonElement().toString();
    }

    @Override
    public JsonElement getJsonElement() {
        JsonArray jsonArray = new JsonArray();

        List<MysteryPlayer> players = mmp.getRewardTask().getCurrentTopPlayers();
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
        return switch (indexChange) {
            case -1 -> "UP";
            case 1 -> "DOWN";
            default -> "UNCHANGED";
        };
    }
}
