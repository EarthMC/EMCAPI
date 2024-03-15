package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.earthmc.emcapi.util.EndpointUtils;
import net.milkbowl.vault.economy.Economy;

public class PlayersEndpoint {
    private static Economy economy = null;

    public PlayersEndpoint(Economy economy) {
        PlayersEndpoint.economy = economy;
    }

    public String lookup(String query) {
        return EndpointUtils.lookup(query, EndpointUtils::getResidentOrNull, "is not a real player");
    }

    public static JsonObject getPlayerObject(Resident resident) {
        JsonObject playerObject = new JsonObject();

        Town town = resident.getTownOrNull();
        Nation nation = resident.getNationOrNull();

        playerObject.addProperty("name", resident.getName());
        playerObject.addProperty("uuid", resident.getUUID().toString());
        playerObject.addProperty("title", resident.getTitle().isEmpty() ? null : resident.getTitle());
        playerObject.addProperty("surname", resident.getSurname().isEmpty() ? null : resident.getSurname());
        playerObject.addProperty("formattedName", resident.getFormattedName());
        playerObject.addProperty("about", resident.getAbout().isEmpty() ? null : resident.getAbout());
        playerObject.addProperty("town", town == null ? null : town.getName());
        playerObject.addProperty("nation", nation == null ? null : nation.getName());

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("registered", resident.getRegistered());
        timestampsObject.addProperty("joinedTownAt", resident.hasTown() ? resident.getJoinedTownAt() : null);
        timestampsObject.addProperty("lastOnline", resident.getLastOnline() != 0 ? resident.getLastOnline() : null);
        playerObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("isOnline", resident.isOnline());
        statusObject.addProperty("isNPC", resident.isNPC());
        statusObject.addProperty("isMayor", resident.isMayor());
        statusObject.addProperty("isKing", resident.isKing());
        statusObject.addProperty("hasTown", resident.hasTown());
        statusObject.addProperty("hasNation", resident.hasNation());
        playerObject.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("balance", resident.getPlayer() != null ? economy.getBalance(resident.getPlayer()) : TownyEconomyHandler.isActive() ? resident.getAccount().getHoldingBalance() : 0.0);
        statsObject.addProperty("numFriends", resident.getFriends().size());
        playerObject.add("stats", statsObject);

        playerObject.add("perms", EndpointUtils.getPermsObject(resident.getPermissions()));

        JsonObject ranksObject = new JsonObject();
        JsonArray townRanksArray = new JsonArray();
        for (String rank : resident.getTownRanks()) {
            townRanksArray.add(rank);
        }
        ranksObject.add("townRanks", townRanksArray.isEmpty() ? null : townRanksArray);

        JsonArray nationRanksArray = new JsonArray();
        for (String rank : resident.getNationRanks()) {
            nationRanksArray.add(rank);
        }
        ranksObject.add("nationRanks", nationRanksArray.isEmpty() ? null : nationRanksArray);
        playerObject.add("ranks", ranksObject);

        JsonArray friendsArray = new JsonArray();
        for (Resident friend : resident.getFriends()) {
            friendsArray.add(friend.getName());
        }
        playerObject.add("friends", friendsArray.isEmpty() ? null : friendsArray);

        return playerObject;
    }
}
