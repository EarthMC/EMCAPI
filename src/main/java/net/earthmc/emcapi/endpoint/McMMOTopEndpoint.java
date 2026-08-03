package net.earthmc.emcapi.endpoint;

import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.skills.SkillTools;
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
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.CooldownUtil;
import net.earthmc.emcapi.util.HttpExceptions;
import net.earthmc.emcapi.util.JSONUtil;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@OpenApi(
    path = "/v4/mcmmo-top",
    methods = HttpMethod.POST,
    summary = "Query mcMMO leaderboard data. Note that the information is cached for 15 minutes",
    requestBody = @OpenApiRequestBody(
        description = "Specify a player UUID to query and include a valid API key",
        required = true,
        content = {
            @OpenApiContent(
                from = ContentTypes.McMMOTopQuery.class,
                mimeType = ContentType.JSON,
                example = """
                    {
                      "query": "MINING",
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
                    from = ContentTypes.McMMOTop[].class,
                    mimeType = ContentType.JSON,
                    example = """
                        [
                          {
                            "skill": "power",
                            "1": {
                              "player": "K1kimor",
                              "level": 12078
                            },
                            "2": {
                              "player": "Veyronity",
                              "level": 72
                            },
                            "3": {
                              "player": "Andre1098",
                              "level": 29
                            },
                            "lastUpdated": 1779619278
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
            status = "429",
            description = "This endpoint has a cooldown of 2 minutes"
        )
    }
)
public class McMMOTopEndpoint extends PostEndpoint<McMMOTopEndpoint.McMMOLeaderboard> {
    private static final Map<String, List<PlayerStat>> CACHE = new ConcurrentHashMap<>();
    private static final long COOLDOWN_SECONDS = 120;
    private Long lastUpdated;

    public McMMOTopEndpoint(EMCAPI plugin) {
        super(plugin);
        if (Integrations.getIntegration("mcMMO").isEnabled()) {
            plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, t -> {
                for (PrimarySkillType skill : PrimarySkillType.values()) {
                    if (SkillTools.isChildSkill(skill)) continue;
                    loadSkill(skill);
                }
                loadSkill(null); // Main power
                lastUpdated = Instant.now().getEpochSecond();
            }, 1, 15, TimeUnit.MINUTES);
        }
    }

    private static List<PlayerStat> loadSkill(@Nullable PrimarySkillType skill) {
        List<PlayerStat> data = mcMMO.getDatabaseManager().readLeaderboard(skill, 1, 25);
        CACHE.put(skillName(skill), data);
        return data;
    }

    @Override
    public McMMOLeaderboard getObjectOrNull(@NotNull JsonElement element, @Nullable String key) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw HttpExceptions.NOT_A_STRING;

        UUID keyOwner = KeyManager.getKeyOwner(key);
        if (keyOwner == null) {
            throw HttpExceptions.MISSING_API_KEY;
        }

        PrimarySkillType skill;
        if (string.equalsIgnoreCase("power")) {
            skill = null;
        } else {
            try {
                skill = PrimarySkillType.valueOf(string.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                throw new BadRequestResponse("Invalid skill name queried.");
            }
            if (SkillTools.isChildSkill(skill)) {
                throw new BadRequestResponse(skill.name() + " is a child skill and does not have a leaderboard!");
            }
        }
        CooldownUtil.checkAndAddCooldownOrThrow("mcmmo-top", keyOwner.toString(), COOLDOWN_SECONDS);

        String skillName = skillName(skill);
        if (CACHE.containsKey(skillName)) {
            return new McMMOLeaderboard(skill, CACHE.get(skillName));
        }

        return new McMMOLeaderboard(skill, loadSkill(skill));
    }

    @Override
    public JsonElement getJsonElement(@NotNull McMMOLeaderboard object, @Nullable String key) {
        JsonObject json = new JsonObject();
        json.addProperty("skill", skillName(object.skill()));

        int index = 1;
        for (PlayerStat entry : object.data()) {
            JsonObject element = new JsonObject();
            element.addProperty("player", entry.playerName());
            element.addProperty("level", entry.value());

            json.add(String.valueOf(index++), element);
        }

        json.addProperty("lastUpdated", lastUpdated);
        return json;
    }

    private static String skillName(@Nullable PrimarySkillType skill) {
        return skill != null ? skill.name() : "power";
    }

    public record McMMOLeaderboard(@Nullable PrimarySkillType skill, List<PlayerStat> data) {}
}
