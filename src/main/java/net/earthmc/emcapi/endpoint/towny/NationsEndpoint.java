package net.earthmc.emcapi.endpoint.towny;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.EmbargoesIntegration;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.integration.PactsIntegration;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.manager.NationMetadataManager;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.HttpExceptions;
import net.earthmc.emcapi.util.JSONUtil;
import net.earthmc.lynchpin.api.towny.embargoes.Embargo;
import net.earthmc.lynchpin.api.towny.pacts.Pact;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class NationsEndpoint extends PostEndpoint<Nation> {

    public NationsEndpoint(final EMCAPI plugin) {
        super(plugin);
    }

    @Override
    public Nation getObjectOrNull(JsonElement element, @Nullable String key) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw HttpExceptions.NOT_A_STRING;;

        Nation nation;
        try {
            nation = TownyAPI.getInstance().getNation(UUID.fromString(string));
        } catch (IllegalArgumentException e) {
            nation = TownyAPI.getInstance().getNation(string);
        }

        return nation;
    }

    @Override
    public JsonElement getJsonElement(Nation nation, @Nullable String key) {
        JsonObject nationObject = new JsonObject();

        nationObject.addProperty("name", nation.getName());
        nationObject.addProperty("uuid", nation.getUUID().toString());
        nationObject.addProperty("board", nation.getBoard().isEmpty() ? null : nation.getBoard());
        nationObject.addProperty("dynmapColour", NationMetadataManager.getDynmapColour(nation));
        nationObject.addProperty("dynmapOutline", NationMetadataManager.getDynmapOutline(nation));
        nationObject.addProperty("wiki", NationMetadataManager.getWikiURL(nation));
        nationObject.addProperty("discord", NationMetadataManager.getDiscordURL(nation));

        nationObject.add("king", EndpointUtils.getResidentJsonObject(nation.getKing()));
        nationObject.add("capital", EndpointUtils.getNameAndIdObject(nation.getCapital()));

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("registered", nation.getRegistered());
        nationObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("isPublic", nation.isPublic());
        statusObject.addProperty("isOpen", nation.isOpen());
        statusObject.addProperty("isNeutral", nation.isNeutral());
        nationObject.add("status", statusObject);

        List<Resident> nationOutlawedResidents = NationMetadataManager.getNationOutlaws(nation);
        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("nationBonus", TownySettings.getNationBonusBlocks(nation));
        statsObject.addProperty("numTownBlocks", nation.getNumTownblocks());
        statsObject.addProperty("numResidents", nation.getNumResidents());
        statsObject.addProperty("numTowns", nation.getNumTowns());
        statsObject.addProperty("numOutlaws", nationOutlawedResidents.size());
        statsObject.addProperty("numAllies", nation.getAllies().size());
        statsObject.addProperty("numEnemies", nation.getEnemies().size());
        statsObject.addProperty("balance", TownyEconomyHandler.isActive() ? nation.getAccount().getHoldingBalance() : 0);
        nationObject.add("stats", statsObject);

        nationObject.add("coordinates", EndpointUtils.getCoordinatesObject(nation.getSpawnOrNull()));
        nationObject.add("residents", EndpointUtils.getResidentArray(nation.getResidents()));
        nationObject.add("towns", EndpointUtils.getGovernmentArray(nation.getTowns()));
        nationObject.add("outlaws", EndpointUtils.getResidentArray(nationOutlawedResidents));
        nationObject.add("allies", EndpointUtils.getGovernmentArray(nation.getAllies()));
        nationObject.add("enemies", EndpointUtils.getGovernmentArray(nation.getEnemies()));
        nationObject.add("sanctioned", EndpointUtils.getGovernmentArray(nation.getSanctionedTowns()));

        JsonObject ranksObject = new JsonObject();
        for (String rank : TownyPerms.getNationRanks()) {
            ranksObject.add(rank, EndpointUtils.getResidentArray(EndpointUtils.getNationRank(nation, rank)));
        }
        nationObject.add("ranks", ranksObject);

        nationObject.add("embargoes", getEmbargoesObject(nation));
        nationObject.add("pacts", getPactsObject(nation));
        nationObject.add("bankhistory", getBankHistory(nation, key));

        return nationObject;
    }

    private JsonObject getEmbargoesObject(Nation nation) {
        JsonObject json = new JsonObject();
        EmbargoesIntegration integration = Integrations.getIntegration("lynchpin-towny-embargoes");
        if (integration == null || !integration.isEnabled()) {
            return json;
        }

        JsonArray byNation = new JsonArray();
        JsonArray againstNation = new JsonArray();
        for (Embargo embargo : integration.getEmbargoes(nation)) {
            if (nation.equals(embargo.getSender())) {
                Nation other = embargo.getAgainst();
                byNation.add(EndpointUtils.getNameAndIdObject(other));
            } else {
                Nation other = embargo.getSender();
                againstNation.add(EndpointUtils.getNameAndIdObject(other));
            }
        }
        json.add("own", byNation);
        json.add("against", againstNation);

        return json;
    }

    private JsonObject getPactsObject(Nation nation) {
        JsonObject json = new JsonObject();
        PactsIntegration integration = Integrations.getIntegration("lynchpin-towny-pacts");
        if (integration == null || !integration.isEnabled()) {
            return json;
        }

        JsonArray active = new JsonArray();
        JsonArray pending = new JsonArray();

        for (Pact pact : integration.getActivePacts(nation)) {
            active.add(EndpointUtils.getPactObject(pact));
        }

        for (Pact pact : integration.getPendingPacts(nation)) {
            pending.add(EndpointUtils.getPactObject(pact));
        }

        json.add("active", active);
        json.add("pending", pending);

        return json;
    }

    private JsonArray getBankHistory(Nation nation, @Nullable String key) {
        JsonArray empty = new JsonArray();
        if (!TownyEconomyHandler.isActive()) {
            return empty;
        }
        UUID keyOwner = KeyManager.getKeyOwner(key);
        if (keyOwner == null) {
            return empty;
        }
        Resident res = TownyAPI.getInstance().getResident(keyOwner);
        if (res == null || !nation.equals(res.getNationOrNull()) || !(nation.isKing(res) || res.hasNationRank("Chancellor") || res.hasNationRank("Treasurer"))) {
            return empty;
        }

        return EndpointUtils.getBankHistoryArray(nation);
    }
}
