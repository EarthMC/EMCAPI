package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import io.javalin.http.BadRequestResponse;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordEndpoint {

    public String lookupID(String query) {
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();

        UUID uuid;
        try {
            uuid = UUID.fromString(query);
        } catch (IllegalArgumentException e) {
            throw new BadRequestResponse("Invalid Minecraft UUID provided");
        }

        String discordId = alm.getDiscordId(uuid);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", discordId);

        return jsonObject.toString();
    }

    public String lookupUUID(String query) {
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();

        Pattern pattern = Pattern.compile("^\\d{17,19}$");
        Matcher matcher = pattern.matcher(query);

        if (!matcher.find()) throw new BadRequestResponse("Invalid Discord ID provided");

        UUID uuid = alm.getUuid(query);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uuid", uuid != null ? uuid.toString() : null);

        return jsonObject.toString();
    }
}
