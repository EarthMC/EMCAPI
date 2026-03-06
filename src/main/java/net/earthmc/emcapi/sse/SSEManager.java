package net.earthmc.emcapi.sse;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.sse.SseClient;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.util.EndpointUtils;

import javax.annotation.Nullable;
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
    private static final Map<UUID, ClientData> clientsMap = new ConcurrentHashMap<>();
    private static final Set<SseClient> clients = ConcurrentHashMap.newKeySet();
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
                ctx.status(401).result("Missing API key");
                client.close();
                return;
            }

            UUID key;
            try {
                key = UUID.fromString(auth.substring("Bearer ".length()));
            } catch (IllegalArgumentException ignored) {
                ctx.status(401).result("Invalid API key format");
                client.close();
                return;
            }

            UUID owner = EndpointUtils.getKeyOwner(key);
            if (owner == null) {
                ctx.status(403).result("Invalid API key");
                client.close();
                return;
            }
            if (clientsMap.containsKey(key)) {
                ctx.status(403).result("This API key is already in use.");
                client.close();
                return;
            }

            Set<String> events = new HashSet<>();
            Set<String> invalid = new HashSet<>();

            String listenStr = ctx.queryParam("listen");
            if (listenStr != null) {
                for (String event : listenStr.split(",")) {
                    if (ALLOWED_EVENTS.contains(event)) {
                        events.add(event);
                    } else {
                        invalid.add(event);
                    }
                }
                if (events.isEmpty()) {
                    ctx.status(400).result("No valid events specified");
                    client.close();
                    return;
                }
            } else {
                events.addAll(new HashSet<>(ALLOWED_EVENTS));
            }
            ClientData data = new ClientData(client, events, owner);
            client.keepAlive();
            client.sendEvent("open", "Connected to the EarthMC API.");
            client.sendEvent("listening", "Listening to the following events: " + String.join(", ", events));
            if (!invalid.isEmpty()) {
                client.sendEvent("invalid", "The following events are invalid: " + String.join(", ", invalid));
            }
            client.onClose(() -> {
                clients.remove(client);
                clientsMap.remove(key, data);
            });

            clients.add(client);
            clientsMap.put(key, data);
        });
    }

    public void shutdown() {
        for (SseClient client : clients) {
            client.sendEvent("close", "EarthMC API shut down.");
            client.close();
        }
        clientsMap.clear();
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
            for (ClientData clientData : clientsMap.values()) {
                if (!clientData.events.contains(event)) {
                    continue;
                }
                if (predicate != null && !predicate.test(clientData))
                    continue;
                clientData.client.sendEvent(event, message);
            }
        });
    }

    public static void deleteKey(UUID uuid) {
        ClientData data = clientsMap.remove(uuid);
        if (data != null) {
            SseClient client = data.client;
            client.sendEvent("disconnected", "This API key was deleted by the owner");
            client.close();
        }
    }

    public record ClientData(SseClient client, Set<String> events, UUID playerID) {}
}
