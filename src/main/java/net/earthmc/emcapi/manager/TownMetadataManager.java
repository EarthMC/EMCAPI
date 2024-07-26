package net.earthmc.emcapi.manager;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;

public class TownMetadataManager {

    public static boolean hasOverclaimShield(Town town) {
        BooleanDataField bdf = (BooleanDataField) town.getMetadata("os_hasShield");
        if (bdf == null)
            return false;

        return bdf.getValue();
    }

    public static boolean getCanOutsidersSpawn(Town town) {
        BooleanDataField bdf = (BooleanDataField) town.getMetadata("bspawn_canoutsidersspawn");
        if (bdf == null)
            return false;

        return bdf.getValue();
    }

    public static String getWikiURL(Town town) {
        StringDataField sdf = (StringDataField) town.getMetadata("wiki_url");
        if (sdf == null)
            return null;

        return sdf.getValue();
    }
}
