package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;

public class DocumentationEndpoint {

    public String lookup() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("documentation", "https://earthmc.net/docs/api");

        JsonObject authorObject = new JsonObject();
        JsonObject discordObject = new JsonObject();
        discordObject.addProperty("username", "fruitloopins");
        discordObject.addProperty("id", "160374716928884736");
        authorObject.add("discord", discordObject);

        authorObject.addProperty("github", "https://github.com/Fruitloopins");
        authorObject.addProperty("note", "Feel free to get in contact if you need any help with using the API, or message in the Official API Discussion thread (https://discord.com/channels/219863747248914433/1218363271367622717)");
        jsonObject.add("author", authorObject);

        jsonObject.addProperty("fish", "><>");

        return jsonObject.toString();
    }
}
