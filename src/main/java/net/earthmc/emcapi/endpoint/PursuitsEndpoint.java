package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.integration.PursuitsIntegration;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.CooldownUtil;
import net.earthmc.emcapi.util.HttpExceptions;
import net.earthmc.emcapi.util.JSONUtil;
import net.earthmc.lynchpin.api.pursuits.Pursuit;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Map;

public class PursuitsEndpoint extends PostEndpoint<PursuitsEndpoint.PursuitsLeaderboard> {
    private static final long COOLDOWN_SECONDS = 60;
    private final PursuitsIntegration integration;

    public PursuitsEndpoint(EMCAPI plugin) {
        super(plugin);
        integration = Integrations.getIntegration("lynchpin-pursuits");
    }

    @Override
    public PursuitsLeaderboard getObjectOrNull(JsonElement element, @Nullable String key) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw HttpExceptions.NOT_A_STRING;

        UUID keyOwner = KeyManager.getKeyOwner(key);
        if (keyOwner == null) {
            throw HttpExceptions.MISSING_API_KEY;
        }

        Pursuit.Type specifiedType;
        if (string.equalsIgnoreCase("all")) {
            specifiedType = null;
        } else {
            try {
                specifiedType = Pursuit.Type.valueOf(string.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                throw new BadRequestResponse("Invalid Pursuit Type specified. ");
            }
        }

        CooldownUtil.checkAndAddCooldownOrThrow("pursuits", keyOwner.toString(), COOLDOWN_SECONDS);
        if (specifiedType != null) {
            Pursuit pursuit = integration.getPursuit(specifiedType);
            if (pursuit == null) {
                throw new NotFoundResponse("No pursuit found for type " + specifiedType.name());
            }
            return new PursuitsLeaderboard(List.of(pursuit));
        }

        List<Pursuit> pursuits = new ArrayList<>(integration.getPursuits().values());
        if (pursuits.isEmpty()) {
            throw new NotFoundResponse("No pursuits found");
        }
        return new PursuitsLeaderboard(pursuits);
    }

    @Override
    public JsonElement getJsonElement(PursuitsLeaderboard object, @Nullable String key) {
        JsonObject json = new JsonObject();
        for (Pursuit pursuit : object.pursuits) {
            json.add(pursuit.type().name(), formatPursuit(pursuit));
        }

        return json;
    }

    private JsonObject formatPursuit(Pursuit pursuit) {
        JsonObject json = new JsonObject();

        json.addProperty("name", pursuit.name());
        json.addProperty("isActive", pursuit.isActive());

        json.add("top", formatTop(pursuit));
        return json;
    }

    private JsonObject formatTop(Pursuit pursuit) {
        JsonObject top = new JsonObject();
        int index = 1;
        String typeName = pursuit.type().name().toLowerCase();
        for (Map.Entry<UUID, Double> entry : pursuit.top(10).entrySet()) {
            JsonObject element = new JsonObject();
            element.addProperty(typeName, entry.getKey().toString());
            element.addProperty("score", entry.getValue());

            top.add(String.valueOf(index++), element);
        }
        return top;
    }

    public record PursuitsLeaderboard(List<Pursuit> pursuits) {}
}
