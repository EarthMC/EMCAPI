package net.earthmc.emcapi.manager;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;

public class TownMetadataManager {

    public static boolean hasOverclaimShield(Town town) {
        BooleanDataField bdf = (BooleanDataField) town.getMetadata("os_hasShield");
        if (bdf == null)
            return false;

        return bdf.getValue();
    }
}
