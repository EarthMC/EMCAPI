package net.earthmc.emcapi.sse;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.sse.SseClient;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.util.JSONUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SSEManager {
    private final EMCAPI plugin;
    private final Javalin javalin;
    private static final Map<String, ClientData> CLIENTS = new ConcurrentHashMap<>();
    private static final Map<String, Set<ClientData>> CLIENTS_BY_EVENT = new ConcurrentHashMap<>();
    private static final Map<UUID, ClientData> CLIENTS_BY_UUID = new ConcurrentHashMap<>();

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
                client.sendEvent("error", msg("Missing API key"));
                client.close();
                return;
            }

            String key = auth.substring("Bearer ".length());
            UUID owner = KeyManager.getKeyOwner(key);
            if (owner == null) {
                client.sendEvent("error", msg("Invalid API key"));
                client.close();
                return;
            }

            final ClientData existingClient = CLIENTS.get(key);
            if (existingClient != null) {
                // check if the other client is still active
                // prevent sending more than one keepalive per second
                final long now = System.currentTimeMillis();
                final long lastKeepAlive = existingClient.lastManualKeepAlive.getAndUpdate(prev ->
                    (prev == 0 || now - prev > 1000) ? now : prev
                );

                final boolean sendKeepAlive = lastKeepAlive == 0 || now - lastKeepAlive > 1000;

                if (sendKeepAlive) {
                    existingClient.client.sendComment("keepalive");
                }

                if (!sendKeepAlive || !existingClient.client.terminated()) {
                    client.sendEvent("error", msg("This API key is already in use."));
                    client.close();
                    return;
                }
            }

            Set<String> events = new HashSet<>();
            Set<String> invalid = new HashSet<>();

            String listenStr = ctx.queryParam("listen");
            if (listenStr != null) {
                if (listenStr.length() > 10_000) {
                    client.sendEvent("error", msg("Attempted to listen to too many events."));
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
                client.sendEvent("error", msg("No valid events specified through the 'listen' query param."));
                client.close();
                return;
            }

            ClientData data = new ClientData(client, Set.copyOf(events), owner);
            client.keepAlive();
            client.sendEvent("open", msg("Connected to the EarthMC API."));

            final JsonObject listening = new JsonObject();
            listening.add("valid", JSONUtil.toJsonArray(events));
            listening.add("invalid", JSONUtil.toJsonArray(invalid));
            client.sendEvent("listening", listening.toString());

            client.onClose(() -> {
                CLIENTS.remove(key, data);
                CLIENTS_BY_UUID.remove(data.playerID);

                for (final String event : data.events()) {
                    final Set<ClientData> dataSet = CLIENTS_BY_EVENT.get(event);

                    if (dataSet != null) {
                        dataSet.remove(data);
                    }
                }
            });

            CLIENTS.put(key, data);
            CLIENTS_BY_UUID.put(owner, data);

            for (final String event : events) {
                CLIENTS_BY_EVENT.computeIfAbsent(event, k -> ConcurrentHashMap.newKeySet()).add(data);
            }
        });

        // when a client disconnects, we don't really know about it until the next time we try to send something to them.
        // so to keep the list of clients tidy, we can periodically send a keepalive event.
        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task -> {
            for (final ClientData data : CLIENTS.values()) {
                data.client.sendComment("keepalive");
            }
        }, 5L, 5L, TimeUnit.MINUTES);
    }

    public void shutdown() {
        for (ClientData data : CLIENTS.values()) {
            data.client.sendEvent("close", msg("EarthMC API shutting down."));
            data.client.close();
        }

        CLIENTS.clear();
        CLIENTS_BY_EVENT.clear();
        CLIENTS_BY_UUID.clear();
    }

    public void sendEvent(String event, JsonObject data) {
        sendEvent(event, data, null);
    }

    public void sendEvent(String event, JsonObject data, @Nullable UUID targetPlayerId) {
        final ClientData targetClient = targetPlayerId != null ? CLIENTS_BY_UUID.get(targetPlayerId) : null;
        if (targetPlayerId != null && (targetClient == null || !targetClient.events.contains(event))) {
            return; // No client is active to hear it
        }

        data.addProperty("timestamp", Instant.now().getEpochSecond());
        String message = data.toString();

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            if (targetClient != null) {
                targetClient.client.sendEvent(event, message);
                return;
            }

            final Set<ClientData> listeningClients = CLIENTS_BY_EVENT.getOrDefault(event, Set.of());

            for (ClientData clientData : listeningClients) {
                clientData.client.sendEvent(event, message);
            }
        });
    }

    public static void deleteKey(String key) {
        ClientData data = CLIENTS.remove(key);
        if (data != null) {
            SseClient client = data.client;
            client.sendEvent("close", msg("This API key was deleted by the owner"));
            client.close();
        }
    }

    private static String msg(final String message) {
        final JsonObject object = new JsonObject();
        object.addProperty("message", message);
        return object.toString();
    }

    public record ClientData(SseClient client, @Unmodifiable Set<String> events, UUID playerID, AtomicLong lastManualKeepAlive) {
        public ClientData(final SseClient client, final Set<String> events, final UUID playerID) {
            this(client, events, playerID, new AtomicLong());
        }

        public ClientData {
            events = Set.copyOf(events);
        }
    }
}
