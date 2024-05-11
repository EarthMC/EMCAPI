package net.earthmc.emcapi.manager;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;

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
}
