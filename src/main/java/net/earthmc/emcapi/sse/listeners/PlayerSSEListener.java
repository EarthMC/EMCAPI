package net.earthmc.emcapi.sse.listeners;

import com.google.gson.JsonObject;
import net.earthmc.emcapi.sse.SSEManager;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerSSEListener extends AbstractSSEListener {

    public PlayerSSEListener(SSEManager sseManager) {
        super(sseManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = player.getKiller();

        if (killer == null || killer.equals(player)) {
            return;
        }
        JsonObject message = new JsonObject();
        message.add("player", EndpointUtils.generateNameUUIDJsonObject(player.getName(), player.getUniqueId()));
        message.add("killer", EndpointUtils.generateNameUUIDJsonObject(killer.getName(), killer.getUniqueId()));
        sse.sendEvent("PlayerDeathEvent", message);
    }
}
