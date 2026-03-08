package net.earthmc.emcapi.integration;

import net.earthmc.emcapi.manager.KeyManager;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.List;
import java.util.UUID;

public class QuickShopIntegration extends Integration {

    public QuickShopIntegration() {
        super("QuickShop");
    }

    public List<Shop> getPlayerShops(UUID player, String key) {
        if (!isEnabled()) {
            return List.of();
        }
        if (!player.equals(KeyManager.getKeyOwner(key))) {
            return List.of();
        }
        return QuickShop.getInstance().getShopManager().getPlayerAllShops(player);
    }
}
