package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import io.javalin.http.BadRequestResponse;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordEndpoint {

    public String lookup(String query) {
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();

        Pattern pattern = Pattern.compile("^\\d{17,19}$");
        Matcher matcher = pattern.matcher(query);

        String response;
        if (matcher.find()) {
            response = alm.getUuid(query).toString();
        } else {
            UUID uuid;
            try {
                uuid = UUID.fromString(query);
            } catch (IllegalArgumentException e) {
                throw new BadRequestResponse("Invalid Discord ID or Minecraft UUID provided");
            }

            response = alm.getDiscordId(uuid);
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("response", response);

        return jsonObject.toString();
    }
}
