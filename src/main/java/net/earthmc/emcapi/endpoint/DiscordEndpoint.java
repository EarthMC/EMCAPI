package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.EMCAPI;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordEndpoint {

    public String lookup(String query) {
        if (query == null) throw new BadRequestResponse("No query provided");

        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        String[] split = query.split(",");

        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < Math.min(EMCAPI.instance.getConfig().getInt("behaviour.max_lookup_size"), split.length); i++) {
            String string = split[i];
            UUID uuid;
            String discordId;

            try {
                uuid = UUID.fromString(string);
                discordId = alm.getDiscordId(uuid);
            } catch (IllegalArgumentException e) {
                Pattern pattern = Pattern.compile("^\\d{17,19}$");
                Matcher matcher = pattern.matcher(string);

                if (!matcher.find()) throw new BadRequestResponse(string + " is not a valid UUID or Discord ID");

                uuid = alm.getUuid(string);
                discordId = string;
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uuid", uuid == null ? null : uuid.toString());
            jsonObject.addProperty("id", discordId);

            jsonArray.add(jsonObject);
        }

        return jsonArray.toString();
    }
}
