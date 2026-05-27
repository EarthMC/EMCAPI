package net.earthmc.emcapi.manager;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;

import java.util.Objects;

public class TownMetadataManager {

    public static boolean hasOverclaimShield(Town town) {
        return Objects.requireNonNullElse(getField(town, "os_hasShield", Boolean.class), false);
    }

    public static boolean getCanOutsidersSpawn(Town town) {
        return Objects.requireNonNullElse(getField(town, "bspawn_canoutsidersspawn", Boolean.class), false);
    }

    public static String getWikiURL(Town town) {
        return getField(town, "wiki_url", String.class);
    }

    public static String getDiscordURL(Town town) {
        return getField(town, "discord_url", String.class);
    }

    public static boolean getPassiveMobs(Town town) {
        return Objects.requireNonNullElse(getField(town, "passive_mobs", Boolean.class), true);
    }

    public static boolean getSnow(Town town) {
        return Objects.requireNonNullElse(getField(town, "accumulate_snow", Boolean.class), true);
    }

    public static boolean getFriendlyFire(Town town) {
        return Objects.requireNonNullElse(getField(town, "friendly_fire", Boolean.class), false);
    }

    private static <T> T getField(Town town, String field, Class<T> type) {
        CustomDataField<?> dataField = town.getMetadata(field);

        if (dataField == null)
            return null;

        Object value = dataField.getValue();
        if (!type.isInstance(value))
            return null;

        return type.cast(value);
    }
}
