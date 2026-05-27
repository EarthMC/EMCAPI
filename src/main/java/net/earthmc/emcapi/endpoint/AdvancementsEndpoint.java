package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.earthmc.emcapi.integration.AdvancementsIntegration;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.lynchpin.api.advancements.AdvancementEntry;

import java.text.SimpleDateFormat;

public class AdvancementsEndpoint extends GetEndpoint {
    private final AdvancementsIntegration integration;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public AdvancementsEndpoint() {
        integration = Integrations.getIntegration("lynchpin-advancements");
    }

    @Override
    public JsonElement getJsonElement() {
        JsonObject outer = new JsonObject();
        for (AdvancementEntry entry : integration.getAdvancements()) {
            JsonObject json = new JsonObject();
            json.addProperty("player", entry.player().toString());
            json.addProperty("date", formatter.format(entry.date()));

            outer.add(entry.advancement(), json);
        }

        return outer;
    }
}
