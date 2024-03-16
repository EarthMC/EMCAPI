package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.configuration.file.FileConfiguration;

public class DocumentationEndpoint {
    private final String url;

    public DocumentationEndpoint(FileConfiguration config) {
        if (!config.getBoolean("behaviour.developer_mode")) {
            url = "https://api.earthmc.net/" + config.getString("networking.url_path") + "/";
        } else {
            url = "http://localhost:" + config.getInt("networking.port") + "/" + config.getString("networking.url_path") + "/";
        }
    }

    public String lookup() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("documentation", "https://earthmc.net/docs/api");

        JsonObject authorObject = new JsonObject();
        JsonObject discordObject = new JsonObject();
        discordObject.addProperty("username", "fruitloopins");
        discordObject.addProperty("id", "160374716928884736");
        authorObject.add("discord", discordObject);

        authorObject.addProperty("github", "https://github.com/Fruitloopins");
        authorObject.addProperty("note", "Feel free to get in contact if you need any help with using the API, or send a message in the Official API Discussion thread (https://discord.com/channels/219863747248914433/1218363271367622717)");
        jsonObject.add("author", authorObject);

        jsonObject.addProperty("fish", "><>");

        JsonObject examplesObject = new JsonObject();
        examplesObject.addProperty("server", url + "server");
        examplesObject.addProperty("location", url + "location?x=2304&z=-9743");

        JsonArray playersArray = new JsonArray();
        playersArray.add(url + "players");
        playersArray.add(url + "players?query=Fruitloopins,fed0ec4a-f1ad-4b97-9443-876391668b34");
        examplesObject.add("players", playersArray);

        JsonArray townsArray = new JsonArray();
        townsArray.add(url + "towns");
        townsArray.add(url + "towns?query=Berlin,82a1ecc6-d40c-4ee4-9bff-5b4acecb4e63");
        examplesObject.add("towns", townsArray);

        JsonArray nationsArray = new JsonArray();
        nationsArray.add(url + "nations");
        nationsArray.add(url + "nations?query=Germany,d7833c7a-2fe6-4f26-9120-00e0759c555f");
        examplesObject.add("nations", nationsArray);

        JsonArray quartersArray = new JsonArray();
        quartersArray.add(url + "quarters");
        quartersArray.add(url + "quarters?query=627d2fc1-d36a-4cf2-8d74-5f456924c47e");
        examplesObject.add("quarters", quartersArray);

        JsonArray discordArray = new JsonArray();
        discordArray.add(url + "discord/id/fed0ec4a-f1ad-4b97-9443-876391668b34");
        discordArray.add(url + "discord/uuid/160374716928884736");
        examplesObject.add("discord", discordArray);

        JsonArray nearbyArray = new JsonArray();
        nearbyArray.add(url + "nearby/coordinate?x=2304&z=-9743&radius=1000");
        nearbyArray.add(url + "nearby/town?town=82a1ecc6-d40c-4ee4-9bff-5b4acecb4e63&radius=1000");
        examplesObject.add("nearby", nearbyArray);
        jsonObject.add("examples", examplesObject);

        return jsonObject.toString();
    }
}
