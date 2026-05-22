package net.earthmc.emcapi.endpoint;

import com.ghostchu.quickshop.api.shop.Shop;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.BadRequestResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.integration.QuickShopIntegration;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.util.CooldownUtil;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.HttpExceptions;
import net.earthmc.emcapi.util.JSONUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ShopEndpoint extends PostEndpoint<List<Shop>> {
    private final QuickShopIntegration integration;
    private static final int COOLDOWN_SECONDS = 3600;

    public ShopEndpoint(EMCAPI plugin) {
        super(plugin);
        this.integration = Integrations.getIntegration("QuickShop-Hikari");
    }

    @Override
    public List<Shop> getObjectOrNull(JsonElement element, @Nullable String key) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw new BadRequestResponse("Your query contains a value that is not a string");

        UUID player;
        try {
            player = UUID.fromString(string);
        } catch (IllegalArgumentException ignored) {
            throw new BadRequestResponse("Your query contains an invalid UUID");
        }

        UUID keyOwner = KeyManager.getKeyOwner(key);
        if (keyOwner == null) {
            throw HttpExceptions.MISSING_API_KEY;
        }
        if (!player.equals(KeyManager.getKeyOwner(key))) {
            throw HttpExceptions.FORBIDDEN;
        }
        CooldownUtil.checkAndAddCooldownOrThrow("shop", keyOwner.toString(), COOLDOWN_SECONDS);
        return integration.getPlayerShops(player);
    }

    @Override
    public JsonElement getJsonElement(List<Shop> object, @Nullable String key) {
        final Map<String, JsonElement> shops = new ConcurrentHashMap<>();
        int counter = 0;
        UUID keyOwner = KeyManager.getKeyOwner(key);
        if (keyOwner == null) {
            throw HttpExceptions.MISSING_API_KEY;
        }
        if (object.isEmpty()) {
            return null;
        }

        final List<CompletableFuture<Void>> shopFutures = new ArrayList<>();

        for (Shop shop : object) {
            if (!keyOwner.equals(shop.getOwner().getUniqueId())) continue;

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
