package net.earthmc.emcapi.manager;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;

public class NationMetadataManager {

    public static String getDynmapColour(Nation nation) {
        StringDataField sdf = (StringDataField) nation.getMetadata("dynmapColour");
        if (sdf == null) return null;

        return sdf.getValue();
    }

    public static String getDynmapOutline(Nation nation) {
        StringDataField sdf = (StringDataField) nation.getMetadata("dynmapOutline");
        if (sdf == null) return null;

        return sdf.getValue();
    }
}
