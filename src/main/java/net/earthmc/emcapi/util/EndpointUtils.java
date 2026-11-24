package net.earthmc.emcapi.util;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.menu.browse.MarketUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.economy.BankTransaction;
import io.javalin.http.Context;
import io.papermc.paper.datacomponent.DataComponentTypes;
import kotlin.Pair;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.lynchpin.api.towny.pacts.Pact;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public class EndpointUtils {
    private static final JsonObject NULL_UUID_NAME = Make.make(new JsonObject(), object -> {
        object.add("name", null);
        object.add("uuid", null);
    });

    public static JsonObject getPermsObject(TownyPermission permissions) {
        JsonObject permsObject = new JsonObject();

        permsObject.add("build", getPermArray(permissions, TownyPermission.ActionType.BUILD));
        permsObject.add("destroy", getPermArray(permissions, TownyPermission.ActionType.DESTROY));
        permsObject.add("switch", getPermArray(permissions, TownyPermission.ActionType.SWITCH));
        permsObject.add("itemUse", getPermArray(permissions, TownyPermission.ActionType.ITEM_USE));

        JsonObject flagsObject = new JsonObject();
        flagsObject.addProperty("pvp", permissions.pvp);
        flagsObject.addProperty("explosion", permissions.explosion);
        flagsObject.addProperty("fire", permissions.fire);
        flagsObject.addProperty("mobs", permissions.mobs);
        permsObject.add("flags", flagsObject);

        return permsObject;
    }

    private static JsonArray getPermArray(TownyPermission permissions, TownyPermission.ActionType actionType) {
        JsonArray jsonArray = new JsonArray();

        jsonArray.add(permissions.getPerm(TownyPermission.PermLevel.RESIDENT, actionType));
        jsonArray.add(permissions.getPerm(TownyPermission.PermLevel.NATION, actionType));
        jsonArray.add(permissions.getPerm(TownyPermission.PermLevel.ALLY, actionType));
        jsonArray.add(permissions.getPerm(TownyPermission.PermLevel.OUTSIDER, actionType));

        return jsonArray;
    }

    public static JsonObject getCoordinatesObject(@Nullable Location location) {
        JsonObject coordinatesObject = new JsonObject();

        JsonObject spawnObject = new JsonObject();
        spawnObject.addProperty("world", location == null ? null : location.getWorld().getName());
        spawnObject.addProperty("x", location == null ? null : location.getX());
        spawnObject.addProperty("y", location == null ? null : location.getY());
        spawnObject.addProperty("z", location == null ? null : location.getZ());
        spawnObject.addProperty("pitch", location == null ? null : location.getPitch());
        spawnObject.addProperty("yaw", location == null ? null : location.getYaw());

        coordinatesObject.add("spawn", spawnObject);

        return coordinatesObject;
    }

    public static JsonObject formatGovernmentRanks(final Government government, final List<String> ranks, final Function<Resident, List<String>> rankGetter) {
        // the previous implementation would do a lot of unnecessary iterations due to Resident#hasTown/NationRank iterating over all ranks to match ranks case insensitively

        final Map<String, Collection<Resident>> residentsByRank = new HashMap<>();

        for (final Resident resident : government.getResidents()) {
            final List<String> residentRanks = rankGetter.apply(resident);

            for (final String residentRank : residentRanks) {
                residentsByRank.computeIfAbsent(residentRank, k -> new ArrayList<>()).add(resident);
            }
        }

        final JsonObject ranksObject = new JsonObject();

        // iterate over ranks instead of the map in order to
        // 1. keep the order the same based on ranks
        // 2. include ranks that don't have anyone as empty arrays
        for (final String rank : ranks) {
            final Collection<Resident> residents = residentsByRank.get(rank);

            ranksObject.add(rank, residents != null ? getResidentArray(residents) : new JsonArray(0));
        }

        return ranksObject;
    }

    public static JsonArray getResidentArray(Collection<Resident> residents) {
        JsonArray jsonArray = new JsonArray();

        for (Resident resident : filterActiveResidents(residents)) {
            jsonArray.add(getResidentJsonObject(resident));
        }

        return jsonArray;
    }

    public static JsonObject getResidentJsonObject(@Nullable Resident resident) {
        if (resident == null) {
            return NULL_UUID_NAME;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", resident.getName());
        jsonObject.addProperty("uuid", resident.hasUUID() ? resident.getUUID().toString() : null);

        return jsonObject;
    }

    /**
     * Creates an array of name and id objects for the given collection of governments.
     *
     * @param governments Governments to put into an array
     * @return A new JSON array
     */
    public static JsonArray getGovernmentArray(Iterable<? extends Government> governments) {
        JsonArray jsonArray = new JsonArray();

        for (Government government : governments) {
            if (government == null) {
                continue;
            }

            jsonArray.add(getNameAndIdObject(government));
        }

        return jsonArray;
    }

    public static JsonObject getNameAndIdObject(@Nullable Government government) {
        if (government == null) {
            return NULL_UUID_NAME;
        }

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", government.getName() == null ? null : government.getName());
        jsonObject.addProperty("uuid", government.getUUID() == null ? null : government.getUUID().toString());

        return jsonObject;
    }

    public static JsonObject generateNameUUIDJsonObject(String name, UUID uuid) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("uuid", uuid.toString());
        return jsonObject;
    }

    public static JsonObject getShopObject(Shop shop) {
        // There's a chance the cache might not be updated by the time the events that call this method are called, so continue directly getting the stock.
        // For the shop endpoint, using the cache is safe and means we won't have to schedule onto the region thread & load every chunk for every shop
        return getShopObject(shop, false);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static JsonObject getShopObject(Shop shop, boolean useCache) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", shop.getShopId());
        jsonObject.addProperty("owner", String.valueOf(shop.getOwner().getUniqueId()));

        final ItemStack item = shop.getItem();
        jsonObject.addProperty("item", item.getType().name());

        item.unsetData(DataComponentTypes.CUSTOM_NAME); // the item is a clone so this is fine
        jsonObject.addProperty("item_name", PlainTextComponentSerializer.plainText().serialize(item.effectiveName()));

        jsonObject.addProperty("price", shop.getPrice());
        jsonObject.addProperty("amount", item.getAmount());
        jsonObject.addProperty("type", shop.isSelling() ? "selling" : "buying");

        final int stock;
        if (useCache) {
            stock = shop.isSelling() ? MarketUtils.getStockFromCache(shop) : MarketUtils.getSpaceFromCache(shop);
        } else {
            stock = shop.isSelling() ? shop.getRemainingStock() : shop.getRemainingSpace();
        }

        jsonObject.addProperty("stock", stock);
        jsonObject.add("location", formatLocation(shop.getLocation()));

        return jsonObject;
    }

    public static JsonObject getPactObject(Pact pact) {
        JsonObject json = new JsonObject();

        json.addProperty("sender", pact.getSenderNation().getName());
        json.addProperty("receiver", pact.getReceivingNation().getName());
        json.addProperty("status", pact.status().name());

        JsonObject stats = new JsonObject();
        stats.addProperty("createdAt", pact.getCreatedAt());
        stats.addProperty("expiresAt", pact.getExpiresAt());
        stats.addProperty("duration", pact.getDuration());
        json.add("stats", stats);

        return json;
    }

    public static JsonObject formatLocation(final Location location) {
        JsonObject locationObject = new JsonObject();
        try {
            locationObject.addProperty("world", location.getWorld().getKey().toString());
        } catch (IllegalArgumentException e) {
            locationObject.addProperty("world", "earthmc:unloaded");
        }

        locationObject.addProperty("x", location.getBlockX());
        locationObject.addProperty("y", location.getBlockY());
        locationObject.addProperty("z", location.getBlockZ());

        return locationObject;
    }

    public static JsonArray getBankHistoryArray(final Government government, final @Nullable String apiKey, final Predicate<Resident> isAllowedToView) {
        JsonArray json = new JsonArray();

        if (!TownyEconomyHandler.isActive()) {
            return json;
        }

        UUID keyOwner = KeyManager.getKeyOwner(apiKey);
        if (keyOwner == null) {
            return json;
        }

        Resident res = TownyAPI.getInstance().getResident(keyOwner);
        if (res == null || !isAllowedToView.test(res)) {
            return json;
        }

        List<BankTransaction> history = government.getAccount().getAuditor().getTransactions();
        if (history.isEmpty()) {
            return json;
        }
        int start = Math.max(0, history.size() - 10);
        for (int i = history.size() - 1; i >= start; i--) {
            BankTransaction transaction = history.get(i);
            json.add(getBankTransactionObject(transaction));
        }

        return json;
    }

    private static JsonObject getBankTransactionObject(BankTransaction transaction) {
        JsonObject json = new JsonObject();

        json.addProperty("time", transaction.time());
        json.addProperty("type", transaction.getType().name());
        json.addProperty("amount", transaction.getAmount());
        json.addProperty("balance", transaction.getBalance());
        json.addProperty("reason", transaction.getReason());

        return json;
    }

    public static List<Resident> filterActiveResidents(Collection<Resident> residents) {
        long cutoff = System.currentTimeMillis() - (TownySettings.getDeleteTime() * 1000L);

        return residents.stream().filter(res -> isResidentActive(res, cutoff)).toList();
    }

    public static int getActiveResidentCount(Collection<Resident> residents) {
        return filterActiveResidents(residents).size();
    }

    public static boolean isResidentActive(Resident resident, long cutoff) {
        if (resident == null || resident.isNPC() || resident.isOnline() || resident.isMayor()) {
            return true;
        }
        return resident.getLastOnline() > cutoff;
    }

    public static @NotNull String verifyKey(Context ctx) {
        String auth = ctx.header("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw HttpExceptions.MISSING_API_KEY;
        }

        String key = auth.substring("Bearer ".length());
        UUID owner = KeyManager.getKeyOwner(key);
        if (owner == null) {
            throw HttpExceptions.INVALID_API_KEY;
        }
        return key;
    }

    public static Pair<JsonElement, JsonObject> parseBody(String body) {
        JsonObject jsonObject = JSONUtil.getJsonObjectFromString(body);

        JsonElement queryElement = jsonObject.get("query");
        if (queryElement == null) throw HttpExceptions.NO_QUERY;
        JsonElement templateElement = jsonObject.get("template");
        JsonObject templateObject = templateElement != null && templateElement.isJsonObject() ? templateElement.getAsJsonObject() : null;

        return new Pair<>(queryElement, templateObject);
    }
}
