package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.QuickShopIntegration;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.JSONUtil;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Shop;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ShopEndpoint extends PostEndpoint<List<Shop>> {
    private final QuickShopIntegration integration;
    private static final Map<UUID, Long> LAST_QUERY_MAP = new ConcurrentHashMap<>();
    private static final int COOLDOWN_SECONDS = 3600;

    public ShopEndpoint(EMCAPI plugin) {
        super(plugin);
        this.integration = plugin.integrations().quickShopIntegration();
        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, t ->
                LAST_QUERY_MAP.entrySet().removeIf(entry -> entry.getValue() < Instant.now().getEpochSecond() - COOLDOWN_SECONDS),
                1,
                1,
                TimeUnit.MINUTES // Check more often
        );
    }

    @Override
    public List<Shop> getObjectOrNull(JsonElement element, @Nullable String key) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw new BadRequestResponse("Your query contains a value that is not a string");

        UUID player;
        try {
            player = UUID.fromString(string);
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        integration.throwIfDisabled();
        return integration.getPlayerShops(player, key);
    }

    @Override
    public JsonElement getJsonElement(List<Shop> object, @Nullable String key) {
        final Map<String, JsonElement> shops = new ConcurrentHashMap<>();
        int counter = 0;
        UUID keyOwner = KeyManager.getKeyOwner(key);
        if (keyOwner == null) {
            return null;
        }
        if (LAST_QUERY_MAP.containsKey(keyOwner) && LAST_QUERY_MAP.get(keyOwner) > Instant.now().getEpochSecond() - COOLDOWN_SECONDS) {
            return null;
        }
        if (object.isEmpty()) {
            return null;
        } else {
            LAST_QUERY_MAP.put(keyOwner, Instant.now().getEpochSecond());
        }

        final List<CompletableFuture<Void>> shopFutures = new ArrayList<>();

        for (Shop shop : object) {
            if (!shop.getOwner().equals(keyOwner)) continue;

            final CompletableFuture<Void> shopFuture = new CompletableFuture<>();
            shopFutures.add(shopFuture);

            final int count = counter++;

            plugin.getServer().getRegionScheduler().execute(plugin, shop.getLocation(), () -> {
                shop.getLocation().getWorld().getChunkAtAsync(shop.getLocation()).thenAccept(chunk -> {
                    try {
                        shops.put(String.valueOf(count), EndpointUtils.getShopObject(shop));
                        shopFuture.complete(null);
                    } catch (Throwable throwable) {
                        shopFuture.completeExceptionally(throwable);
                    }
                });
            });
        }

        CompletableFuture.allOf(shopFutures.toArray(new CompletableFuture[]{})).join();
        if (shops.isEmpty()) {
            return null;
        }

        final JsonObject shopsObject = new JsonObject();
        for (final Map.Entry<String, JsonElement> entry : shops.entrySet()) {
            shopsObject.add(entry.getKey(), entry.getValue());
        }

        return shopsObject;
    }
}
