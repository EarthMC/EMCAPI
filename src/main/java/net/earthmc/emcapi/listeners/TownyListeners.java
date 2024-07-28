package net.earthmc.emcapi.listeners;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.event.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.earthmc.emcapi.manager.SSEManager;
import net.earthmc.emcapi.util.EndpointUtils;


public class TownyListeners implements Listener {

    // Will add more events later

    @EventHandler
    public void onNewNation(NewNationEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNationJsonObject(event.getNation()));
        message.add("king", EndpointUtils.getResidentJsonObject(event.getNation().getKing()));
        SSEManager.broadcastMessage("NewNation", message);
    }

    @EventHandler
    public void onNationDelete(DeleteNationEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.generateNameUUIDJsonObject(event.getNationName(), event.getNationUUID()));
        message.add("king", EndpointUtils.getResidentJsonObject(event.getLeader()));
        SSEManager.broadcastMessage("DeleteNation", message);
    }


    @EventHandler
    public void onNewTown(NewTownEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.add("mayor", EndpointUtils.getResidentJsonObject(event.getTown().getMayor()));
        SSEManager.broadcastMessage("NewTown", message);
    }

    @EventHandler
    public void onDeleteTown(DeleteTownEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.generateNameUUIDJsonObject(event.getTownName(), event.getTownUUID()));
        message.add("mayor", EndpointUtils.getResidentJsonObject(event.getMayor()));
        SSEManager.broadcastMessage("DeleteTown", message);
    }

}
