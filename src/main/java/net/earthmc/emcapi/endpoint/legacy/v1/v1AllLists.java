package net.earthmc.emcapi.endpoint.legacy.v1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

public class v1AllLists {
    public static String allResidents() {
        JsonObject json = new JsonObject();
        json.addProperty("numResidents", TownyUniverse.getInstance().getResidents().size());

        JsonArray allResidentsArray = new JsonArray();
        for (Resident resident : TownyUniverse.getInstance().getResidents()) {
            allResidentsArray.add(resident.getName());
        }
        json.add("allResidents", allResidentsArray);

        return json.toString();
    }

    public static String allTowns() {
        JsonObject json = new JsonObject();
        json.addProperty("numTowns", TownyUniverse.getInstance().getTowns().size());

        JsonArray allTownsArray = new JsonArray();
        for (Town town : TownyUniverse.getInstance().getTowns()) {
            allTownsArray.add(town.getName());
        }
        json.add("allTowns", allTownsArray);

        return json.toString();
    }

    public static String allNations() {
        JsonObject json = new JsonObject();
        json.addProperty("numNations", TownyUniverse.getInstance().getNations().size());

        JsonArray allNationsArray = new JsonArray();
        for (Nation nation : TownyUniverse.getInstance().getNations()) {
            allNationsArray.add(nation.getName());
        }
        json.add("allNations", allNationsArray);

        return json.toString();
    }
}
