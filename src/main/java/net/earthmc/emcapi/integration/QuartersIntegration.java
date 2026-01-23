package net.earthmc.emcapi.integration;

import au.lupine.quarters.api.manager.QuarterManager;
import au.lupine.quarters.object.entity.Quarter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.object.Town;

import java.util.Collection;
import java.util.List;

public class QuartersIntegration extends Integration {
    public QuartersIntegration() {
        super("Quarters");
    }

    public JsonArray getQuartersArrayForTown(final Town town) {
        return formatQuartersArray(QuarterManager.getInstance().getQuarters(town));
    }

    public JsonArray getAllQuartersArray() {
        return formatQuartersArray(QuarterManager.getInstance().getAllQuarters());
    }

    private JsonArray formatQuartersArray(final Collection<Quarter> quarters) {
        final JsonArray array = new JsonArray();

        for (final Quarter quarter : quarters) {
            final JsonObject object = new JsonObject();

            object.addProperty("name", quarter.getName());
            object.addProperty("uuid", quarter.getUUID().toString());

            array.add(object);
        }

        return array;
    }

    public QuarterStatistics retrieveQuarterStatistics() {
        if (!isEnabled()) {
            return QuarterStatistics.ZERO;
        }

        List<Quarter> quarters = QuarterManager.getInstance().getAllQuarters();

        final int quartersCount = quarters.size();
        final int cuboidsCount = quarters.stream().mapToInt(quarter -> quarter.getCuboids().size()).sum();
        return new QuarterStatistics(quartersCount, cuboidsCount);
    }

    public record QuarterStatistics(int totalQuarters, int totalCuboids) {
        public static final QuarterStatistics ZERO = new QuarterStatistics(0, 0);
    }
}
