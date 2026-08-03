package net.earthmc.emcapi.endpoint;

import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.BadRequestResponse;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.integration.McMMOIntegration;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.object.optout.OptOutSettings;
import net.earthmc.emcapi.util.CooldownUtil;
import net.earthmc.emcapi.util.HttpExceptions;
import net.earthmc.emcapi.util.JSONUtil;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@OpenApi(
    path = "/v4/mcmmo",
    methods = HttpMethod.POST,
    summary = "Query mcMMO data",
    requestBody = @OpenApiRequestBody(
        description = "Specify a player UUID to query and include a valid API key",
        required = true,
        content = {
            @OpenApiContent(
                from = ContentTypes.UUIDKey.class,
                mimeType = ContentType.JSON,
                example = """
                    {
                      "query": "5b8274bf-b162-4336-85a0-48f9d5380a78",
                      "key": "<key>"
                    }
                    """
            )
        }
    ),
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.McMMO[].class,
                    mimeType = ContentType.JSON,
                    example = """
                        [
                          {
                            "name": "Veyronity",
                            "ACROBATICS": 21,
                            "ALCHEMY": 0,
                            "ARCHERY": 0,
                            "AXES": 0,
                            "CROSSBOWS": 0,
                            "EXCAVATION": 0,
                            "FISHING": 0,
                            "HERBALISM": 0,
                            "MACES": 0,
                            "MINING": 1,
                            "REPAIR": 0,
                            "SALVAGE": 0,
                            "SMELTING": 0,
                            "SPEARS": 0,
                            "SWORDS": 0,
                            "TAMING": 0,
                            "TRIDENTS": 0,
                            "UNARMED": 0,
                            "WOODCUTTING": 50
                          }
                        ]
                        """
                )
            }
        ),
        @OpenApiResponse(
            status = "401",
            description = "If you don't specify an API key or the key doesn't match a known player"
        ),
        @OpenApiResponse(
            status = "403",
            description = "If the owner of the API key doesn't match with the queried player, and the queried player has their information private"
        ),
        @OpenApiResponse(
            status = "404",
            description = "If the item in your query returns null, a NotFound error is thrown instead of returning an empty array"
        ),
        @OpenApiResponse(
            status = "429",
            description = "This endpoint has a cooldown of 1 hour"
        )
    }
)
public class McMMOEndpoint extends PostEndpoint<PlayerProfile> {
    private final McMMOIntegration integration;
    private static final long COOLDOWN_SECONDS = 3600;

    public McMMOEndpoint(EMCAPI plugin) {
        super(plugin);
        this.integration = Integrations.getIntegration("mcMMO");
    }

    @Override
    public PlayerProfile getObjectOrNull(@NotNull JsonElement element, @Nullable String key) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw HttpExceptions.NOT_A_STRING;

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
        OptOutSettings settings = plugin.getOptOut().getPlayerSettings(player);
        if (!player.equals(keyOwner) && (settings == null || settings.mcmmo())) {
            throw HttpExceptions.FORBIDDEN;
        }

        CooldownUtil.checkAndAddCooldownOrThrow("mcmmo", keyOwner.toString(), COOLDOWN_SECONDS);
        return integration.getPlayerProfile(player);
    }

    @Override
    public JsonElement getJsonElement(@NotNull PlayerProfile object, @Nullable String key) {
        JsonObject json = new JsonObject();
        json.addProperty("name", object.getPlayerName());
        for (PrimarySkillType skill : PrimarySkillType.values()) {
            json.addProperty(skill.name(), object.getSkillLevel(skill));
        }

        return json;
    }
}
