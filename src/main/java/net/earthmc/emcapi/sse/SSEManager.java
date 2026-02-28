package net.earthmc.emcapi.sse;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.sse.SseClient;
import net.earthmc.emcapi.EMCAPI;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SSEManager {
    private final EMCAPI plugin;
    private final Javalin javalin;
    private static final Set<SseClient> clients = ConcurrentHashMap.newKeySet();

    public SSEManager(EMCAPI plugin) {
        this.plugin = plugin;
        this.javalin = plugin.getJavalin();
    }

    public void loadSSE() {
        javalin.sse(plugin.getURLPath() + "/events", client -> {
            client.keepAlive();
            client.sendEvent("open", "Connected to the EarthMC API.");
            client.onClose(() -> clients.remove(client));
            clients.add(client);
        });
    }

    public void shutdown() {
        for (SseClient client : clients) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
               client.sendEvent("close", "Disconnected from the EarthMC API.");
               client.close();
            });
        }
    }

    public void sendEvent(String event, JsonObject data) {
        long timestamp = Instant.now().getEpochSecond();
        data.addProperty("timestamp", timestamp);
        String message = data.toString();

        for (SseClient client : clients) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, t -> client.sendEvent(event, message));
        }
    }
}
