package net.earthmc.emcapi.sse;

import com.google.gson.JsonObject;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.http.sse.SseClient;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;
import jdk.jfr.consumer.EventStream;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.object.optout.AuthSettings;
import net.earthmc.emcapi.manager.Authorisation;
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

@OpenApi(
    path = "/v4/events",
    methods = HttpMethod.GET,
    summary = "Server-Sent-Events (SSE) endpoint, providing real time events as they happen on the server",
    queryParams = {
        @OpenApiParam(
            name = "listen",
            description = "Comma-separated list of the events you wish to listen to",
            example = "NewDay,TownCreated,NationCreated",
            required = true
        )
    },
    security = @OpenApiSecurity(name = "BearerAuth"),
    headers = @OpenApiParam(
        name = "Authorization",
        description = "You must include a valid EarthMC API key to connect, in the format 'Bearer <key>'",
        required = true
    ),
    responses = {
        @OpenApiResponse(
            status = "200",
            description = "SSE stream established",
            content = {
                @OpenApiContent(
                    from = EventStream.class,
                    mimeType = "text/event-stream"
                )
            }
        )
    }
)
public class SSEManager {
    private final EMCAPI plugin;
    private static final Map<String, ClientData> CLIENTS = new ConcurrentHashMap<>();
    private static final Map<String, Set<ClientData>> CLIENTS_BY_EVENT = new ConcurrentHashMap<>();
    private static final Map<UUID, ClientData> CLIENTS_BY_UUID = new ConcurrentHashMap<>();

    private static final Set<String> ALLOWED_EVENTS = Set.of(
        "NewDay",
        "NationCreated", "NationDeleted", "NationRenamed", "NationKingChanged", "NationMerged",
        "TownCreated", "TownDeleted", "TownRenamed", "TownMayorChanged", "TownMerged", "TownRuined", "TownReclaimed",
        "TownSetForSale", "TownSetNotForSale",
        "TownJoinedNation", "TownLeftNation",
        "ResidentJoinedTown", "ResidentLeftTown",
        "ShopSoldItem", "ShopBoughtItem", "ShopOutOfStock", "ShopOutOfSpace", "ShopOutOfGold", "ShopCreated", "ShopDeleted"
    );

    public SSEManager(EMCAPI plugin) {
        this.plugin = plugin;
    }

    public void loadSSE(final RoutesConfig routes) {
        routes.sse(plugin.getURLPath() + "/events", client -> {
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

            ClientData existingClient = CLIENTS.get(key);
            if (existingClient != null) {
                if (!existingClient.client.terminated()) {
                    existingClient.client.sendEvent("close", msg("Another client has connected with this API Key. If this was not you, revoke your API key."));
                }
                existingClient.client.close();
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
        }, 30, 30, TimeUnit.SECONDS);
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
        sendEvent(event, data, CLIENTS_BY_EVENT.getOrDefault(event, Set.of()));
    }

    public void sendEvent(String event, JsonObject data, @Nullable UUID targetPlayerId) {
        Set<ClientData> listeningClients = ConcurrentHashMap.newKeySet();

        final ClientData targetClient = CLIENTS_BY_UUID.get(targetPlayerId);
        if (targetClient != null && targetClient.events.contains(event)) {
            listeningClients.add(targetClient);
        }

        if (event.contains("Shop")) {
            Authorisation auth = plugin.getAuth();
            AuthSettings settings = auth.getAuthSettings(targetPlayerId);

            if (settings != null) {
                for (UUID authorizedPlayerId : settings.getAuthorizedForType(AuthSettings.Type.SHOP_SSE)) {
                    final ClientData authorizedClient = CLIENTS_BY_UUID.get(authorizedPlayerId);
                    if (authorizedClient != null && authorizedClient.events.contains(event)) {
                        listeningClients.add(authorizedClient);
                    }
                }
            }
        }

        sendEvent(event, data, listeningClients);
    }

    public void sendEvent(String event, JsonObject data, Set<ClientData> listeningClients) {
        if (listeningClients.isEmpty()) {
            return;
        }

        data.addProperty("timestamp", Instant.now().getEpochSecond());
        String message = data.toString();

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            for (final ClientData clientData : listeningClients) {
                if (clientData != null) {
                    clientData.client.sendEvent(event, message);
                }
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

    public record ClientData(SseClient client, @Unmodifiable Set<String> events, UUID playerID) {
        public ClientData {
            events = Set.copyOf(events);
        }
    }
}
