package net.earthmc.emcapi.manager;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.sse.SseClient;
import net.milkbowl.vault.economy.Economy;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class SSEManager {

    private final Javalin javalin;
    private final Economy economy;
    private final static Queue<SseClient> clients = new ConcurrentLinkedQueue<>();

    public SSEManager(Javalin javalin, Economy economy) {
        this.javalin = javalin;
        this.economy = economy;
    }

    public void loadSSE() {
        javalin.sse("/events", client -> {
            client.keepAlive();
            client.sendEvent("connected", "Connected to the EarthMC API.");
            client.onClose(() -> clients.remove(client));
            clients.add(client);
        });

        // Testing purposes
        javalin.get("/broadcast", ctx -> {
            JsonObject message = new JsonObject();
            message.addProperty("text", "Hello to all connected clients!");
            broadcastMessage("Hello", message);
            ctx.result("Message broadcasted to all %s clients".formatted(clients.size()));
        });
        // Testing purposes
    }

    public static void broadcastMessage(String type, JsonObject data) {
        int timestamp = Math.toIntExact(System.currentTimeMillis() / 1000);
        data.addProperty("timestamp", timestamp);

        for (SseClient client : clients) {
            client.sendEvent(type, data.toString());
        }
    }
}

