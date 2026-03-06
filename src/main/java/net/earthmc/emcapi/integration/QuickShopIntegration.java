package net.earthmc.emcapi.integration;

import net.earthmc.emcapi.util.EndpointUtils;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.List;
import java.util.UUID;

public class QuickShopIntegration extends Integration {

    public QuickShopIntegration() {
        super("QuickShop");
    }

    public List<Shop> getPlayerShops(UUID player, UUID key) {
        if (!isEnabled()) {
            return List.of();
        }
        if (!player.equals(EndpointUtils.getKeyOwner(key))) {
            return List.of();
        }
        return QuickShop.getInstance().getShopManager().getPlayerAllShops(player);
    }
}
