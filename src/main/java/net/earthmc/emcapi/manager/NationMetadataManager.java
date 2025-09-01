package net.earthmc.emcapi.manager;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NationMetadataManager {

    private static final String DEFAULT_DYNMAP_COLOUR = "3FB4FF";

    public static String getDynmapColour(Nation nation) {
        StringDataField sdf = (StringDataField) nation.getMetadata("dynmapColour");
        if (sdf == null) return DEFAULT_DYNMAP_COLOUR;

        return sdf.getValue().toUpperCase();
    }

    public static String getDynmapOutline(Nation nation) {
        StringDataField sdf = (StringDataField) nation.getMetadata("dynmapOutline");
        if (sdf == null) return DEFAULT_DYNMAP_COLOUR;

        return sdf.getValue().toUpperCase();
    }

    public static String getWikiURL(Nation nation) {
        StringDataField sdf = (StringDataField) nation.getMetadata("wiki_url");
        if (sdf == null)
            return null;

        return sdf.getValue();
    }

    public static List<Resident> getNationOutlaws(Nation nation) {
        if (nation == null) return List.of();

        StringDataField sdf = (StringDataField) nation.getMetadata("townycommandaddons_nation_outlaws");
        if (sdf == null || sdf.getValue() == null || sdf.getValue().isEmpty()) return List.of();

        List<Resident> outlawedResidents = new ArrayList<>();
        for (String string : sdf.getValue().split(",")) {
            Resident resident = TownyAPI.getInstance().getResident(UUID.fromString(string));
            if (resident == null) continue;

            outlawedResidents.add(resident);
        }
        return outlawedResidents;
    }
}
