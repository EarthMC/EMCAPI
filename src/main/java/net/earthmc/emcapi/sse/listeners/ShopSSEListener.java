package net.earthmc.emcapi.sse.listeners;

import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.management.ShopDeleteEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import net.earthmc.emcapi.sse.SSEManager;
import net.earthmc.emcapi.util.EndpointUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ShopSSEListener extends AbstractSSEListener {

    public ShopSSEListener(SSEManager sseManager) {
        super(sseManager);
    }

    private void sendShopEvent(String eventName, JsonObject message, Shop shop) {
        UUID owner = shop.getOwner().getUniqueId();
        if (owner != null) {
            sse.sendEvent(eventName, message, owner);
        }
        shop.getPermissionAudiences().entrySet().stream()
                .filter((e) -> e.getValue().equals(BuiltInShopPermissionGroup.STAFF.getNamespacedNode()))
                .map(Map.Entry::getKey)
                .filter(Objects::nonNull)
                .filter((uuid) -> !uuid.equals(owner))
                .forEach((uuid) -> sse.sendEvent(eventName, message, uuid));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopPurchase(ShopSuccessPurchaseEvent event) {
        Shop shop = event.getShop();
        UUID owner = shop.getOwner().getUniqueId();
        if (owner == null) {
            return;
        }

        boolean isSelling = shop.isSelling();
        String purchaser = getPlayerName(event.getPurchaser());
        JsonObject saleMessage = new JsonObject();
        saleMessage.add("shop", EndpointUtils.getShopObject(shop));
        saleMessage.addProperty("amount", event.getAmount());
        if (isSelling) {
            saleMessage.addProperty("buyer", purchaser);
            sendShopEvent("ShopSoldItem", saleMessage, shop);
        } else {
            saleMessage.addProperty("seller", purchaser);
            sendShopEvent("ShopBoughtItem", saleMessage, shop);
        }

        checkOwnerBalance(owner, shop);
        checkShopOut(shop);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopCreate(ShopCreateEvent event) {

        if (!event.isPhase(Phase.POST)) {
            return;
        }

        Shop shop = event.shop().orElse(null);
        if (shop == null) {
            return;
        }
        UUID owner = shop.getOwner().getUniqueId();
        if (owner == null ) {
            return;
        }

        JsonObject message = new JsonObject();
        message.add("shop", EndpointUtils.getShopObject(shop));
        sse.sendEvent("ShopCreated", message, owner);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopDelete(ShopDeleteEvent event) {
        Shop shop = event.shop().orElse(null);
        if (shop == null) {
            return;
        }
        if (!event.isPhase(Phase.POST)) {
            return;
        }
        UUID owner = shop.getOwner().getUniqueId();
        if (owner == null) return;

        JsonObject message = new JsonObject();
        message.add("shop", EndpointUtils.getShopObject(shop));
        sse.sendEvent("ShopDeleted", message, owner);
    }

    private void checkOwnerBalance(UUID owner, Shop shop) {
        Resident res = TownyAPI.getInstance().getResident(owner);
        if (res == null || res.getAccount().getHoldingBalance() > 0) return;

        sendShopEvent("ShopOutOfGold", new JsonObject(), shop);
    }

    private void checkShopOut(Shop shop) {
        boolean isSelling = shop.isSelling();
        if (isSelling && shop.getRemainingStock() > 0 || !isSelling && shop.getRemainingSpace() > 0) {
            return;
        }
        JsonObject alertMessage = new JsonObject();
        alertMessage.addProperty("action", isSelling ? "out_of_stock" : "out_of_space");
        alertMessage.add("shop", EndpointUtils.getShopObject(shop));
        sendShopEvent("ShopOutOf" + (isSelling ? "Stock" : "Space"), alertMessage, shop);
    }

    private String getPlayerName(QUser user) {
        String name = user.getUsername();
        if (name != null) {
            return name;
        }
        UUID uuid = user.getUniqueId();
        if (uuid == null) {
            return "Unknown player";
        }
        Resident res = TownyAPI.getInstance().getResident(uuid);
        if (res != null) {
            return res.getName();
        } else {
            return "Unknown player (" + uuid + ")";
        }
    }
}
