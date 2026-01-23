package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.scarsz.discordsrv.DiscordSRV;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.object.nearby.DiscordContext;
import net.earthmc.emcapi.object.nearby.DiscordType;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.HttpExceptions;
import net.earthmc.emcapi.util.JSONUtil;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordEndpoint extends PostEndpoint<DiscordContext> {
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d{17,19}$");
    private static final BadRequestResponse MISSING_TYPE_TARGET = new BadRequestResponse("Your JSON query is missing a type or target");
    private static final BadRequestResponse INVALID_TYPE_TARGET = new BadRequestResponse("Your JSON query has an invalid type or target");

    @Override
    public DiscordContext getObjectOrNull(JsonElement element) {
        JsonObject jsonObject = JSONUtil.getJsonElementAsJsonObjectOrNull(element);
        if (jsonObject == null) {
            throw HttpExceptions.NOT_A_JSON_OBJECT;
        }

        JsonElement typeElement = jsonObject.get("type");
        JsonElement targetElement = jsonObject.get("target");
        if (typeElement == null || targetElement == null) {
            throw MISSING_TYPE_TARGET;
        }

        String typeString = JSONUtil.getJsonElementAsStringOrNull(typeElement);
        String target = JSONUtil.getJsonElementAsStringOrNull(targetElement);
        if (typeString == null || target == null) {
            throw INVALID_TYPE_TARGET;
        }

        UUID uuid = null;
        try {
            uuid = getUUIDFromStr(target);
        } catch (BadRequestResponse ignored) {
            try {
                uuid = getUUIDFromDiscordId(target);
            } catch (BadRequestResponse ignored1) {}
        }
        if (uuid != null && EndpointUtils.playerOptedOut(uuid)) {
            return null;
        }

        try {
            DiscordType type = DiscordType.valueOf(typeString.toUpperCase());
            return new DiscordContext(type, target);
        } catch (IllegalArgumentException e) {
            throw new BadRequestResponse("Specified type is not valid");
        }
    }

    @Override
    public JsonElement getJsonElement(DiscordContext context) {
        DiscordType type = context.getType();
        String target = context.getTarget();

        JsonObject discordObject = new JsonObject();
        if (type == DiscordType.DISCORD) {
            UUID uuid = getUUIDFromDiscordId(target);
            discordObject.addProperty("id", target);
            discordObject.addProperty("uuid", uuid == null ? null : uuid.toString());
        } else if (type == DiscordType.MINECRAFT) {
            UUID uuid = getUUIDFromStr(target);
            if (uuid == null) {
                throw new BadRequestResponse(target + " is not a valid Minecraft UUID");
            }

            discordObject.addProperty("id", DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(uuid));
            discordObject.addProperty("uuid", uuid.toString());
        }

        return discordObject;
    }

    private UUID getUUIDFromStr(String uuidStr) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return uuid;
    }

    private UUID getUUIDFromDiscordId(String discordId) {
        Matcher matcher = ID_PATTERN.matcher(discordId);

        if (!matcher.find()) return null;

        return DiscordSRV.getPlugin().getAccountLinkManager().getUuid(discordId);
    }
}
