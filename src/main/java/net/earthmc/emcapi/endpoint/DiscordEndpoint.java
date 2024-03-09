package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import io.javalin.http.BadRequestResponse;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordEndpoint {
    AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();

    public String lookupID(String query) {
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
        Pattern pattern = Pattern.compile("^\\d{17,19}$");
        Matcher matcher = pattern.matcher(query);

        if (!matcher.find()) throw new BadRequestResponse("Invalid Discord ID provided");

        String uuid = alm.getUuid(query).toString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uuid", uuid);

        return jsonObject.toString();
    }
}
