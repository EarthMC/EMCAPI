package net.earthmc.emcapi.integration;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;

import java.util.List;
import java.util.UUID;

public class QuickShopIntegration extends Integration {

    public QuickShopIntegration() {
        super("QuickShop-Hikari");
    }

    public List<Shop> getPlayerShops(UUID player) {
        return QuickShopAPI.getInstance().getShopManager().getAllShops(player);
    }
}
