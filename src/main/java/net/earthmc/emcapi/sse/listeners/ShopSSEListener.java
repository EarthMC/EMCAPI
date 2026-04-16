package net.earthmc.emcapi.sse.listeners;

import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import net.earthmc.emcapi.sse.SSEManager;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.maxgamer.quickshop.api.event.ShopSuccessPurchaseEvent;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.UUID;

public class ShopSSEListener extends AbstractSSEListener {

    public ShopSSEListener(SSEManager sseManager) {
        super(sseManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopPurchase(ShopSuccessPurchaseEvent event) {
        Shop shop = event.getShop();
        UUID owner = shop.getOwner();

        boolean isSelling = shop.isSelling();
        String purchaser = getPlayerName(event.getPurchaser());
        JsonObject saleMessage = new JsonObject();
        saleMessage.add("shop", EndpointUtils.getShopObject(shop));
        saleMessage.addProperty("amount", event.getAmount());
        if (isSelling) {
            saleMessage.addProperty("buyer", purchaser);
            sse.sendEvent("ShopSoldItem", saleMessage, owner);
        } else {
            saleMessage.addProperty("seller", purchaser);
            sse.sendEvent("ShopBoughtItem", saleMessage, owner);
        }

        checkOwnerBalance(owner);
        checkShopOut(shop);
    }

    private void checkOwnerBalance(UUID owner) {
        Resident res = TownyAPI.getInstance().getResident(owner);
        if (res == null || res.getAccount().getHoldingBalance() > 0) return;

        sse.sendEvent("ShopOutOfGold", new JsonObject(), owner);
    }

    private void checkShopOut(Shop shop) {
        boolean isSelling = shop.isSelling();
        if (isSelling && shop.getRemainingStock() > 0 || !isSelling && shop.getRemainingSpace() > 0) {
            return;
        }
        JsonObject alertMessage = new JsonObject();
        alertMessage.addProperty("action", isSelling ? "out_of_stock" : "out_of_space");
        alertMessage.add("shop", EndpointUtils.getShopObject(shop));
        sse.sendEvent("ShopOutOf" + (isSelling ? "Stock " : "Space"), alertMessage, shop.getOwner());
    }

    private String getPlayerName(UUID uuid) {
        Resident res = TownyAPI.getInstance().getResident(uuid);
        if (res != null) {
            return res.getName();
        } else {
            return "Unknown player `(" + uuid + ")`";
        }
    }
}
