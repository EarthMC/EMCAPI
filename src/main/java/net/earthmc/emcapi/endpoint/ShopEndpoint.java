package net.earthmc.emcapi.endpoint;

import com.ghostchu.quickshop.api.shop.Shop;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.InternalServerErrorResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.integration.QuickShopIntegration;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.object.optout.AuthSettings;
import net.earthmc.emcapi.object.optout.OptOutSettings;
import net.earthmc.emcapi.util.CooldownUtil;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.HttpExceptions;
import net.earthmc.emcapi.util.JSONUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class ShopEndpoint extends PostEndpoint<ShopEndpoint.ShopData> {
    private final QuickShopIntegration integration;
    private static final int COOLDOWN_SECONDS = 3600;
    private final LoadingCache<UUID, ShopData> shopCache = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofHours(1))
        .build(new CacheLoader<>() {
            @Override
            public @NotNull ShopData load(@NotNull UUID uuid) {
                List<Shop> shops = integration.getPlayerShops(uuid);

                return new ShopData(getShopsJson(shops));
            }
        });

    public ShopEndpoint(EMCAPI plugin) {
        super(plugin);
        this.integration = Integrations.getIntegration("QuickShop-Hikari");
    }

    @Override
    public ShopData getObjectOrNull(JsonElement element, @Nullable String key) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw HttpExceptions.NOT_A_STRING;

        UUID player;
        try {
            player = UUID.fromString(string);
        } catch (IllegalArgumentException ignored) {
            throw HttpExceptions.NOT_A_UUID;
        }

        UUID keyOwner = KeyManager.getKeyOwner(key);
        if (keyOwner == null) {
            throw HttpExceptions.MISSING_API_KEY;
        }
        OptOutSettings settings = plugin.getOptOut().getPlayerSettings(player);
        if (!player.equals(keyOwner) && (settings == null || settings.quickShops() && !plugin.getAuth().authorize(player, AuthSettings.Type.SHOP_QUERY, keyOwner))) {
            throw HttpExceptions.FORBIDDEN;
        }
        CooldownUtil.checkAndAddCooldownOrThrow("shop", keyOwner.toString(), COOLDOWN_SECONDS);

        ShopData data = shopCache.getIfPresent(player);
        if (data != null) {
            return data;
        }
        try {
            return shopCache.get(player);
        } catch (ExecutionException e) {
            plugin.getSLF4JLogger().warn("ExecutionException while fetching shop cache for {}", player, e);
            CooldownUtil.remove("shop", keyOwner.toString());
            throw new InternalServerErrorResponse("Unexpected exception while loading " + player + "'s shops");
        }
    }

    @Override
    public JsonElement getJsonElement(ShopData object, @Nullable String ignored) {
        return object.json;
    }

    public record ShopData(JsonElement json) {} // Wrapper to clarify what this endpoint returns

    private JsonElement getShopsJson(List<Shop> object) {
        final Map<String, JsonElement> shops = new ConcurrentHashMap<>();
        int counter = 0;
        if (object.isEmpty()) {
            return null;
        }

        final List<CompletableFuture<Void>> shopFutures = new ArrayList<>();

        for (Shop shop : object) {
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
