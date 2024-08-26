package net.earthmc.emcapi.listeners;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.event.*;
import com.palmergames.bukkit.towny.event.nation.*;
import com.palmergames.bukkit.towny.event.town.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.earthmc.emcapi.manager.SSEManager;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.JSONUtil;


public class TownyListeners implements Listener {

    @EventHandler
    public void onNewDay(NewDayEvent event) {
        JsonObject message = new JsonObject();
        message.add("fallenTowns", JSONUtil.getJsonArrayFromStringList(event.getFallenTowns()));
        message.add("fallenNations", JSONUtil.getJsonArrayFromStringList(event.getFallenNations()));
        SSEManager.broadcastMessage("NewDay", message);
    }



    @EventHandler
    public void onNewNation(NewNationEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNationJsonObject(event.getNation()));
        message.add("king", EndpointUtils.getResidentJsonObject(event.getNation().getKing()));
        SSEManager.broadcastMessage("NewNation", message);
    }

    @EventHandler
    public void onDeleteNation(DeleteNationEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.generateNameUUIDJsonObject(event.getNationName(), event.getNationUUID()));
        message.add("king", EndpointUtils.getResidentJsonObject(event.getLeader()));
        SSEManager.broadcastMessage("DeleteNation", message);
    }

    @EventHandler
    public void onRenameNation(RenameNationEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNationJsonObject(event.getNation()));
        message.addProperty("oldName", event.getOldName());
        SSEManager.broadcastMessage("RenameNation", message);
    }

    @EventHandler
    public void onNationKingChange(NationKingChangeEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNationJsonObject(event.getNation()));
        message.add("newKing", EndpointUtils.getResidentJsonObject(event.getNewKing()));
        message.add("oldKing", EndpointUtils.getResidentJsonObject(event.getOldKing()));
        message.addProperty("isCapitalChange", event.isCapitalChange());
        if (event.isCapitalChange()) {
            message.add("newCapital", EndpointUtils.getTownJsonObject(event.getNewKing().getTownOrNull()));
            message.add("oldCapital", EndpointUtils.getTownJsonObject(event.getOldKing().getTownOrNull()));
        }
        SSEManager.broadcastMessage("NationKingChange", message);
    }

    @EventHandler
    public void onNationAddTown(NationAddTownEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNationJsonObject(event.getNation()));
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        SSEManager.broadcastMessage("NationAddTown", message);
    }

    @EventHandler
    public void onNationRemoveTown(NationRemoveTownEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNationJsonObject(event.getNation()));
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        SSEManager.broadcastMessage("NationRemoveTown", message);
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

    @EventHandler
    public void onRenameTown(RenameTownEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.addProperty("oldName", event.getOldName());
        SSEManager.broadcastMessage("RenameTown", message);
    }

    @EventHandler
    public void onTownMayorChanged(TownMayorChangedEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.add("newMayor", EndpointUtils.getResidentJsonObject(event.getNewMayor()));
        message.add("oldMayor", EndpointUtils.getResidentJsonObject(event.getOldMayor()));
        SSEManager.broadcastMessage("TownMayorChange", message);
    }

    @EventHandler
    public void onTownRuined(TownPreRuinedEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.add("oldMayor", EndpointUtils.getResidentJsonObject(event.getTown().getMayor()));
        SSEManager.broadcastMessage("TownRuined", message);
    }

    @EventHandler
    public void onTownReclaimed(TownReclaimedEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.add("newMayor", EndpointUtils.getResidentJsonObject(event.getResident()));
        SSEManager.broadcastMessage("TownReclaimed", message);
    }

    @EventHandler
    public void onTownAddResident(TownAddResidentEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.add("resident", EndpointUtils.getResidentJsonObject(event.getResident()));
        SSEManager.broadcastMessage("TownAddResident", message);
    }

    @EventHandler
    public void onTownRemoveResident(TownRemoveResidentEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.add("resident", EndpointUtils.getResidentJsonObject(event.getResident()));
        SSEManager.broadcastMessage("TownRemoveResident", message);
    }

}
