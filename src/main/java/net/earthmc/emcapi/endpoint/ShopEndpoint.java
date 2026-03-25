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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ShopEndpoint extends PostEndpoint<List<Shop>> {
    private final QuickShopIntegration integration;

    public ShopEndpoint(EMCAPI plugin) {
        super(plugin);
        this.integration = plugin.integrations().quickShopIntegration();
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
        JsonObject shopsObject = new JsonObject();
        int counter = 0;
        UUID keyOwner = KeyManager.getKeyOwner(key);

        final List<CompletableFuture<Void>> shopFutures = new ArrayList<>();

        for (Shop shop : object) {
            if (!shop.getOwner().equals(keyOwner)) continue;

            final CompletableFuture<Void> shopFuture = new CompletableFuture<>();
            shopFutures.add(shopFuture);

            final int count = counter++;

            plugin.getServer().getRegionScheduler().execute(plugin, shop.getLocation(), () -> {
                shop.getLocation().getWorld().getChunkAtAsync(shop.getLocation()).thenAccept(chunk -> {
                    try {
                        shopsObject.add(String.valueOf(count), EndpointUtils.getShopObject(shop));
                        shopFuture.complete(null);
                    } catch (Throwable throwable) {
                        shopFuture.completeExceptionally(throwable);
                    }
                });
            });
        }

        CompletableFuture.allOf(shopFutures.toArray(new CompletableFuture[]{})).join();

        return shopsObject;
    }
}
