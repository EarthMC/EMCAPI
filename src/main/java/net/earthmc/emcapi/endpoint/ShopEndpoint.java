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

import java.util.List;
import java.util.UUID;

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
        int counter = 1;
        UUID keyOwner = KeyManager.getKeyOwner(key);
        for (Shop shop : object) {
            if (!shop.getOwner().equals(keyOwner)) continue;
            shopsObject.add(String.valueOf(counter++), EndpointUtils.getShopObject(shop));
        }

        return shopsObject;
    }
}
