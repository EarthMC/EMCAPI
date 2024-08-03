package net.earthmc.emcapi.listeners;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.manager.SSEManager;


public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        JsonObject message = new JsonObject();
        message.add("player", EndpointUtils.generateNameUUIDJsonObject(player.getName(), player.getUniqueId()));
        SSEManager.broadcastMessage("PlayerJoin", message);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        JsonObject message = new JsonObject();
        message.add("player", EndpointUtils.generateNameUUIDJsonObject(player.getName(), player.getUniqueId()));
        SSEManager.broadcastMessage("PlayerQuit", message);
    }

}
