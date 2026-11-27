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
import com.palmergames.bukkit.towny.event.economy.BankTransactionEvent;
import com.palmergames.bukkit.towny.event.economy.NationTransactionEvent;
import com.palmergames.bukkit.towny.event.economy.TownTransactionEvent;
import com.palmergames.bukkit.towny.event.nation.NationKingChangeEvent;
import com.palmergames.bukkit.towny.event.nation.NationMergeEvent;
import com.palmergames.bukkit.towny.event.town.TownMayorChangedEvent;
import com.palmergames.bukkit.towny.event.town.TownMergeEvent;
import com.palmergames.bukkit.towny.event.town.TownPreRuinedEvent;
import com.palmergames.bukkit.towny.event.town.TownReclaimedEvent;
import com.palmergames.bukkit.towny.event.town.TownSetForSaleEvent;
import com.palmergames.bukkit.towny.event.town.TownSetNotForSaleEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.economy.Account;
import com.palmergames.bukkit.towny.object.economy.transaction.Transaction;
import net.earthmc.emcapi.sse.SSEManager;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
        message.add("nation", EndpointUtils.getNameAndIdObject(nation));
        message.add("king", EndpointUtils.getResidentJsonObject(nation.getKing()));
        message.add("capital", EndpointUtils.getNameAndIdObject(nation.getCapital()));
        sse.sendEvent("NationCreated", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeleteNation(PreDeleteNationEvent event) {
        JsonObject message = new JsonObject();
        Nation nation = event.getNation();
        message.add("nation", EndpointUtils.generateNameUUIDJsonObject(nation.getName(), nation.getUUID()));
        message.add("king", EndpointUtils.getResidentJsonObject(nation.getKing()));
        message.add("capital", EndpointUtils.getNameAndIdObject(nation.getCapital()));
        sse.sendEvent("NationDeleted", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRenameNation(RenameNationEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNameAndIdObject(event.getNation()));
        message.addProperty("oldName", event.getOldName());
        sse.sendEvent("NationRenamed", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNationKingChange(NationKingChangeEvent event) {
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNameAndIdObject(event.getNation()));
        message.add("newKing", EndpointUtils.getResidentJsonObject(event.getNewKing()));
        message.add("oldKing", EndpointUtils.getResidentJsonObject(event.getOldKing()));
        message.addProperty("isCapitalChange", event.isCapitalChange());
        if (event.isCapitalChange()) {
            message.add("newCapital", EndpointUtils.getNameAndIdObject(event.getNewKing().getTownOrNull()));
            message.add("oldCapital", EndpointUtils.getNameAndIdObject(event.getOldKing().getTownOrNull()));
        }
        sse.sendEvent("NationKingChanged", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNationMerge(NationMergeEvent event) {
        JsonObject message = new JsonObject();
        message.add("oldNation", EndpointUtils.getNameAndIdObject(event.getNation()));
        message.add("remainingNation", EndpointUtils.getNameAndIdObject(event.getRemainingnation()));
        sse.sendEvent("NationMerged", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNewTown(NewTownEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getNameAndIdObject(event.getTown()));
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
        message.add("town", EndpointUtils.getNameAndIdObject(event.getTown()));
        message.addProperty("oldName", event.getOldName());
        sse.sendEvent("TownRenamed", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownMayorChanged(TownMayorChangedEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getNameAndIdObject(event.getTown()));
        message.add("newMayor", EndpointUtils.getResidentJsonObject(event.getNewMayor()));
        message.add("oldMayor", EndpointUtils.getResidentJsonObject(event.getOldMayor()));
        sse.sendEvent("TownMayorChanged", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownMerge(TownMergeEvent event) {
        JsonObject message = new JsonObject();
        message.add("oldTown", EndpointUtils.generateNameUUIDJsonObject(event.getSuccumbingTownName(), event.getSuccumbingTownUUID()));
        message.add("remainingTown", EndpointUtils.getNameAndIdObject(event.getRemainingTown()));
        sse.sendEvent("TownMerged", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownRuined(TownPreRuinedEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getNameAndIdObject(event.getTown()));
        message.add("oldMayor", EndpointUtils.getResidentJsonObject(event.getTown().getMayor()));
        sse.sendEvent("TownRuined", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownReclaimed(TownReclaimedEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getNameAndIdObject(event.getTown()));
        message.add("newMayor", EndpointUtils.getResidentJsonObject(event.getResident()));
        sse.sendEvent("TownReclaimed", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownSetForSale(TownSetForSaleEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getNameAndIdObject(event.getTown()));
        message.addProperty("price", event.getPrice());
        sse.sendEvent("TownSetForSale", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownSetNotForSale(TownSetNotForSaleEvent event) {
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getNameAndIdObject(event.getTown()));
        sse.sendEvent("TownSetNotForSale", message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownJoin(NationAddTownEvent event) {
        Nation nation = event.getNation();
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNameAndIdObject(nation));
        message.add("town", EndpointUtils.getNameAndIdObject(event.getTown()));
        try {
            boolean isNewNation = nation.getCapital() == null;
            UUID leader = !isNewNation
                ? nation.getKing().getUUID()
                : event.getTown().getMayor().getUUID(); // NationAddTown is fired when new nations are created, before the capital is set. In this case, this town will be the capital anyway.
            List<UUID> notify = isNewNation ? List.of(leader) : findResidentsToNotify(nation, false);
            sse.sendEvent("TownJoinedNation", message, notify);
        } catch (NullPointerException ignored) {}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownLeave(NationRemoveTownEvent event) {
        Nation nation = event.getNation();
        if (nation.getCapital() == null) {
            return;
        }
        List<UUID> notify = findResidentsToNotify(nation, false);

        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNameAndIdObject(nation));
        message.add("town", EndpointUtils.getNameAndIdObject(event.getTown()));
        sse.sendEvent("TownLeftNation", message, notify);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownJoin(TownAddResidentEvent event) {
        Town town = event.getTown();
        if (town.getMayor() == null) {
            return;
        }
        List<UUID> notify = findResidentsToNotify(town, false);

        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getNameAndIdObject(town));
        message.add("resident", EndpointUtils.getResidentJsonObject(event.getResident()));
        sse.sendEvent("ResidentJoinedTown", message, notify);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownLeave(TownRemoveResidentEvent event) {
        Town town = event.getTown();
        if (town.getMayor() == null) {
            return;
        }
        List<UUID> notify = findResidentsToNotify(town, false);

        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getNameAndIdObject(town));
        message.add("resident", EndpointUtils.getResidentJsonObject(event.getResident()));
        sse.sendEvent("ResidentLeftTown", message, notify);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownTransaction(TownTransactionEvent event) {
        List<UUID> notify = findResidentsToNotify(event.getTown(), true);
        JsonObject message = new JsonObject();
        message.add("town", EndpointUtils.getNameAndIdObject(event.getTown()));
        formatTransaction(message, event);
        sse.sendEvent("TownTransactionEvent", message, notify);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNationTransaction(NationTransactionEvent event) {
        List<UUID> notify = findResidentsToNotify(event.getNation(), true);
        JsonObject message = new JsonObject();
        message.add("nation", EndpointUtils.getNameAndIdObject(event.getNation()));
        formatTransaction(message, event);
        sse.sendEvent("NationTransactionEvent", message, notify);
    }

    private void formatTransaction(JsonObject message, BankTransactionEvent event) {
        Transaction transaction = event.getTransaction();
        message.addProperty("type", transaction.getType().name());
        message.addProperty("amount", transaction.getAmount());
        message.addProperty("sender", Optional.ofNullable(transaction.getSendingAccount()).map(Account::getName).orElse(null));
        message.addProperty("receiver", Optional.ofNullable(transaction.getReceivingAccount()).map(Account::getName).orElse(null));
    }

    private List<UUID> findResidentsToNotify(Town town, boolean bankTransaction) {
        List<UUID> list = new ArrayList<>();
        list.add(town.getMayor().getUUID());
        list.addAll(getUUIDs(town.getRank("Councillor")));
        if (bankTransaction) {
            list.addAll(getUUIDs(town.getRank("Treasurer")));
        }
        return list;
    }

    private List<UUID> findResidentsToNotify(Nation nation, boolean bankTransaction) {
        List<UUID> list = new ArrayList<>();
        list.add(nation.getKing().getUUID());

        for (Resident resident : nation.getResidents()) {
            if (resident.hasNationRank("Chancellor") || (bankTransaction && resident.hasNationRank("Treasurer"))) {
                list.add(resident.getUUID());
            }
        }
        return list;
    }

    private List<UUID> getUUIDs(List<Resident> residents) {
        return residents.stream().map(Resident::getUUID).toList();
    }
}
