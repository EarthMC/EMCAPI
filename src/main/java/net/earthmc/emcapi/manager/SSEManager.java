package net.earthmc.emcapi.manager;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.sse.SseClient;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class SSEManager {

    private final Javalin javalin;
    private final static Queue<SseClient> clients = new ConcurrentLinkedQueue<>();

    public SSEManager(Javalin javalin) {
        this.javalin = javalin;
    }

    public void loadSSE() {
        javalin.sse("/events", client -> {
            client.keepAlive();
            client.sendEvent("connected", "Connected to the EarthMC API.");
            client.onClose(() -> clients.remove(client));
            clients.add(client);
        });
    }

    public static void broadcastMessage(String event, JsonObject data) {
        int timestamp = Math.toIntExact(System.currentTimeMillis() / 1000);
        data.addProperty("timestamp", timestamp);

        for (SseClient client : clients) {
            client.sendEvent(event, data.toString());
        }
    }
}

