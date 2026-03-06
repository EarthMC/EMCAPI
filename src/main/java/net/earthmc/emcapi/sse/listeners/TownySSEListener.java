package net.earthmc.emcapi.sse.listeners;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.NewNationEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.PreDeleteNationEvent;
import com.palmergames.bukkit.towny.event.RenameNationEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.event.nation.NationKingChangeEvent;
import com.palmergames.bukkit.towny.event.nation.NationMergeEvent;
import com.palmergames.bukkit.towny.event.town.TownMayorChangedEvent;
import com.palmergames.bukkit.towny.event.town.TownMergeEvent;
import com.palmergames.bukkit.towny.event.town.TownPreRuinedEvent;
import com.palmergames.bukkit.towny.event.town.TownReclaimedEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import net.earthmc.emcapi.sse.SSEManager;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class TownySSEListener extends AbstractSSEListener {

    public TownySSEListener(SSEManager sse) {
        super(sse);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNewDay(NewDayEvent event) {
        sse.sendEvent("NewDay", new JsonObject());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNewNation(NewNationEvent event) {
        JsonObject message = new JsonObject();
        Nation nation = event.getNation();
        message.add("nation", EndpointUtils.getNationJsonObject(nation));
        message.add("king", EndpointUtils.getResidentJsonObject(nation.getKing()));
        message.add("capital", EndpointUtils.getTownJsonObject(nation.getCapital()));
        sse.sendEvent("NationCreated", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeleteNation(PreDeleteNationEvent event) {
        JsonObject message = new JsonObject();
        Nation nation = event.getNation();
        message.add("nation", EndpointUtils.generateNameUUIDJsonObject(nation.getName(), nation.getUUID()));
        message.add("king", EndpointUtils.getResidentJsonObject(nation.getKing()));
        message.add("capital", EndpointUtils.getTownJsonObject(nation.getCapital()));
        sse.sendEvent("NationDeleted", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRenameNation(RenameNationEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNationJsonObject(event.getNation()));
        message.addProperty("oldName", event.getOldName());
        sse.sendEvent("NationRenamed", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
        sse.sendEvent("NationKingChanged", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNationMerge(NationMergeEvent event) {
        JsonObject message = new JsonObject();
        message.add("oldNation", EndpointUtils.getNationJsonObject(event.getNation()));
        message.add("remainingNation", EndpointUtils.getNationJsonObject(event.getRemainingnation()));
        sse.sendEvent("NationMerged", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNewTown(NewTownEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.add("mayor", EndpointUtils.getResidentJsonObject(event.getTown().getMayor()));
        sse.sendEvent("TownCreated", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeleteTown(DeleteTownEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.generateNameUUIDJsonObject(event.getTownName(), event.getTownUUID()));
        message.add("mayor", EndpointUtils.getResidentJsonObject(event.getMayor()));
        sse.sendEvent("TownDeleted", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRenameTown(RenameTownEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.addProperty("oldName", event.getOldName());
        sse.sendEvent("TownRenamed", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownMayorChanged(TownMayorChangedEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.add("newMayor", EndpointUtils.getResidentJsonObject(event.getNewMayor()));
        message.add("oldMayor", EndpointUtils.getResidentJsonObject(event.getOldMayor()));
        sse.sendEvent("TownMayorChanged", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownMerge(TownMergeEvent event) {
        JsonObject message = new JsonObject();
        message.add("oldTown", EndpointUtils.generateNameUUIDJsonObject(event.getSuccumbingTownName(), event.getSuccumbingTownUUID()));
        message.add("remainingTown", EndpointUtils.getTownJsonObject(event.getRemainingTown()));
        sse.sendEvent("TownMerged", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownRuined(TownPreRuinedEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.add("oldMayor", EndpointUtils.getResidentJsonObject(event.getTown().getMayor()));
        sse.sendEvent("TownRuined", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownReclaimed(TownReclaimedEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        message.add("newMayor", EndpointUtils.getResidentJsonObject(event.getResident()));
        sse.sendEvent("TownReclaimed", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownJoin(NationAddTownEvent event) {
        Nation nation = event.getNation();
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNationJsonObject(nation));
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        sse.sendEvent("TownJoinedNation", message, nation.getKing().getUUID());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownLeave(NationRemoveTownEvent event) {
        Nation nation = event.getNation();
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNationJsonObject(nation));
        message.add("town", EndpointUtils.getTownJsonObject(event.getTown()));
        sse.sendEvent("TownLeftNation", message, nation.getKing().getUUID());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownJoin(TownAddResidentEvent event) {
        Town town = event.getTown();
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(town));
        message.add("resident", EndpointUtils.getResidentJsonObject(event.getResident()));
        sse.sendEvent("ResidentJoinedTown", message, town.getMayor().getUUID());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownLeave(TownRemoveResidentEvent event) {
        Town town = event.getTown();
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getTownJsonObject(town));
        message.add("resident", EndpointUtils.getResidentJsonObject(event.getResident()));
        sse.sendEvent("ResidentLeftTown", message, town.getMayor().getUUID());
    }
}
