package net.earthmc.emcapi.manager;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NationMetadataManager {

    private static final String DEFAULT_DYNMAP_COLOUR = "3FB4FF";

    public static String getDynmapColour(Nation nation) {
        return Objects.requireNonNullElse(getStringField(nation, "dynmapColour"), DEFAULT_DYNMAP_COLOUR).toUpperCase();
    }

    public static String getDynmapOutline(Nation nation) {
        return Objects.requireNonNullElse(getStringField(nation, "dynmapOutline"), DEFAULT_DYNMAP_COLOUR).toUpperCase();
    }

    public static String getWikiURL(Nation nation) {
        return getStringField(nation, "wiki_url");
    }

    public static String getDiscordURL(Nation nation) {
        return getStringField(nation, "discord_url");
    }

    public static List<Resident> getNationOutlaws(Nation nation) {
        if (nation == null) return List.of();

        String value = getStringField(nation, "townycommandaddons_nation_outlaws");
        if (value == null || value.isEmpty()) return List.of();

        List<Resident> outlawedResidents = new ArrayList<>();
        for (String string : value.split(",")) {
            Resident resident = TownyAPI.getInstance().getResident(UUID.fromString(string));
            if (resident == null) continue;

            outlawedResidents.add(resident);
        }
        return outlawedResidents;
    }

    private static String getStringField(Nation nation, String field) {
        if (nation.getMetadata(field) instanceof StringDataField sdf) {
            return sdf.getValue();
        }

        return null;
    }
}
