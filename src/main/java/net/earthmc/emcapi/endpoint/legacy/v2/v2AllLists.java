package net.earthmc.emcapi.endpoint.legacy.v2;

import com.google.gson.JsonArray;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

public class v2AllLists {
    public static String allResidentsList() {
        JsonArray json = new JsonArray();

        for (Resident resident : TownyUniverse.getInstance().getResidents()) {
            json.add(resident.getName());
        }

        return json.toString();
    }

    public static String allTownsList() {
        JsonArray json = new JsonArray();

        for (Town town : TownyUniverse.getInstance().getTowns()) {
            json.add(town.getName());
        }

        return json.toString();
    }

    public static String allNationsList() {
        JsonArray json = new JsonArray();

        for (Nation nation : TownyUniverse.getInstance().getNations()) {
            json.add(nation.getName());
        }

        return json.toString();
    }
}
