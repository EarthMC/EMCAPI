package net.earthmc.emcapi.endpoint;

import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.integration.McMMOIntegration;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.CooldownUtil;
import net.earthmc.emcapi.util.HttpExceptions;
import net.earthmc.emcapi.util.JSONUtil;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class McMMOEndpoint extends PostEndpoint<PlayerProfile> {
    private final McMMOIntegration integration;
    private static final long COOLDOWN_SECONDS = 3600;

    public McMMOEndpoint(EMCAPI plugin) {
        super(plugin);
        this.integration = Integrations.getIntegration("mcMMO");
    }

    @Override
    public PlayerProfile getObjectOrNull(JsonElement element, @Nullable String key) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw new BadRequestResponse("Your query contains a value that is not a string");

        UUID player;
        try {
            player = UUID.fromString(string);
        } catch (IllegalArgumentException ignored) {
            throw new BadRequestResponse("Your query contains an invalid UUID");
        }

        UUID keyOwner = KeyManager.getKeyOwner(key);
        if (keyOwner == null) {
            throw HttpExceptions.MISSING_API_KEY;
        }
        if (!player.equals(keyOwner)) {
            throw HttpExceptions.FORBIDDEN;
        }

        CooldownUtil.checkAndAddCooldownOrThrow("mcmmo", keyOwner.toString(), COOLDOWN_SECONDS);
        return integration.getPlayerProfile(player);
    }

    @Override
    public JsonElement getJsonElement(PlayerProfile object, @Nullable String key) {
        JsonObject json = new JsonObject();
        json.addProperty("name", object.getPlayerName());
        for (PrimarySkillType skill : PrimarySkillType.values()) {
            json.addProperty(skill.name(), object.getSkillLevel(skill));
        }

        return json;
    }
}
