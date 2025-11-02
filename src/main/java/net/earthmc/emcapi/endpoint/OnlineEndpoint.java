package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonElement;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class OnlineEndpoint extends GetEndpoint {

    @Override
    public String lookup() {
        return getJsonElement().toString();
    }

    @Override
    public JsonElement getJsonElement() {
        return EndpointUtils.getOnlinePlayerArray(new ArrayList<>(Bukkit.getOnlinePlayers()));
    }
}
