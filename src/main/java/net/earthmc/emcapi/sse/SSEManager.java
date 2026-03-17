package net.earthmc.emcapi.sse;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.sse.SseClient;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.manager.KeyManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class SSEManager {
    private final EMCAPI plugin;
    private final Javalin javalin;
    private static final Map<String, ClientData> CLIENTS = new ConcurrentHashMap<>();
    private static final Map<String, Set<ClientData>> CLIENTS_BY_EVENT = new ConcurrentHashMap<>();

    private static final Set<String> ALLOWED_EVENTS = Set.of(
        "NewDay",
        "NationCreated", "NationDeleted", "NationRenamed", "NationKingChanged", "NationMerged",
        "TownCreated", "TownDeleted", "TownRenamed", "TownMayorChanged", "TownMerged", "TownRuined", "TownReclaimed",
        "TownJoinedNation", "TownLeftNation",
        "ResidentJoinedTown", "ResidentLeftTown",
        "ShopSoldItem", "ShopBoughtItem", "ShopOutOfStock", "ShopOutOfSpace", "ShopOutOfGold"
    );

    public SSEManager(EMCAPI plugin) {
        this.plugin = plugin;
        this.javalin = plugin.getJavalin();
    }

    public void loadSSE() {
        javalin.sse(plugin.getURLPath() + "/events", client -> {
            Context ctx = client.ctx();
            String auth = ctx.header("Authorization");

            if (auth == null || !auth.startsWith("Bearer ")) {
                client.sendEvent("error", "Missing API key");
                client.close();
                return;
            }

            String key = auth.substring("Bearer ".length());
            UUID owner = KeyManager.getKeyOwner(key);
            if (owner == null) {
                client.sendEvent("error", "Invalid API key");
                client.close();
                return;
            }

            if (CLIENTS.containsKey(key)) {
                client.sendEvent("error", "This API key is already in use.");
                client.close();
                return;
            }

            Set<String> events = new HashSet<>();
            Set<String> invalid = new HashSet<>();

            String listenStr = ctx.queryParam("listen");
            if (listenStr != null) {
                if (listenStr.length() > 10_000) {
                    client.sendEvent("error", "Attempted to listen to too many events.");
                    client.close();
                    return;
                }

                for (String event : listenStr.split(",")) {
                    if (ALLOWED_EVENTS.contains(event)) {
                        events.add(event);
                    } else {
                        invalid.add(event);
                    }
                }
            }

            if (events.isEmpty()) {
                client.sendEvent("error", "No valid events specified through the 'listen' query param.");
                client.close();
                return;
            }

            ClientData data = new ClientData(client, Set.copyOf(events), owner);
            client.keepAlive();
            client.sendEvent("open", "Connected to the EarthMC API.");
            client.sendEvent("listening", "Listening to the following events: " + String.join(", ", events));

            if (!invalid.isEmpty()) {
                client.sendEvent("invalid", "The following events are invalid: " + String.join(", ", invalid));
            }

            client.onClose(() -> {
                CLIENTS.remove(key, data);

                for (final String event : data.events()) {
                    final Set<ClientData> dataSet = CLIENTS_BY_EVENT.get(event);

                    if (dataSet != null) {
                        dataSet.remove(data);
                    }
                }
            });

            CLIENTS.put(key, data);

            for (final String event : events) {
                CLIENTS_BY_EVENT.computeIfAbsent(event, k -> ConcurrentHashMap.newKeySet()).add(data);
            }
        });
    }

    public void shutdown() {
        for (ClientData data : CLIENTS.values()) {
            data.client.sendEvent("close", "EarthMC API shutting down.");
            data.client.close();
        }

        CLIENTS.clear();
        CLIENTS_BY_EVENT.clear();
    }

    public void sendEvent(String event, JsonObject data) {
        sendEvent(event, data, (Predicate<ClientData>) null);
    }

    public void sendEvent(String event, JsonObject data, UUID targetPlayerID) {
        sendEvent(event, data, client -> client.playerID.equals(targetPlayerID));
    }

    public void sendEvent(String event, JsonObject data, @Nullable Predicate<ClientData> predicate) {
        long timestamp = Instant.now().getEpochSecond();
        data.addProperty("timestamp", timestamp);
        String message = data.toString();

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            final Set<ClientData> listeningClients = CLIENTS_BY_EVENT.getOrDefault(event, Set.of());

            for (ClientData clientData : listeningClients) {
                if (predicate != null && !predicate.test(clientData)) {
                    continue;
                }

                clientData.client.sendEvent(event, message);
            }
        });
    }

    public static void deleteKey(String key) {
        ClientData data = CLIENTS.remove(key);
        if (data != null) {
            SseClient client = data.client;
            client.sendEvent("close", "This API key was deleted by the owner");
            client.close();
        }
    }

    public record ClientData(SseClient client, @Unmodifiable Set<String> events, UUID playerID) {}
}
