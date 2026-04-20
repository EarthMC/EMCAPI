package net.earthmc.emcapi.integration;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import net.earthmc.emcapi.manager.KeyManager;

import java.util.List;
import java.util.UUID;

public class QuickShopIntegration extends Integration {

    public QuickShopIntegration() {
        super("QuickShop-Hikari");
    }

    public List<Shop> getPlayerShops(UUID player, String key) {
        if (!isEnabled()) {
            return List.of();
        }
        if (!player.equals(KeyManager.getKeyOwner(key))) {
            return List.of();
        }
        return QuickShopAPI.getInstance().getShopManager().getAllShops(player);
    }
}
