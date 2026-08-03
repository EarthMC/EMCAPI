package net.earthmc.emcapi.endpoint;

import com.ghostchu.quickshop.api.shop.Shop;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.integration.Integrations;
import net.earthmc.emcapi.integration.QuickShopIntegration;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.object.endpoint.PostEndpoint;
import net.earthmc.emcapi.object.optout.AuthSettings;
import net.earthmc.emcapi.object.optout.OptOutSettings;
import net.earthmc.emcapi.util.CooldownUtil;
import net.earthmc.emcapi.util.EndpointUtils;
import net.earthmc.emcapi.util.HttpExceptions;
import net.earthmc.emcapi.util.JSONUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@OpenApi(
    path = "/v4/shop",
    methods = HttpMethod.POST,
    summary = "Query shop data",
    requestBody = @OpenApiRequestBody(
        description = "Specify a player UUID to query and include a valid API key",
        required = true,
        content = {
            @OpenApiContent(
                from = ContentTypes.UUIDKey.class,
                mimeType = ContentType.JSON,
                example = """
                    {
                      "query": "5b8274bf-b162-4336-85a0-48f9d5380a78",
                      "key": "<key>"
                    }
                    """
            )
        }
    ),
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.Shops[].class,
                    mimeType = ContentType.JSON,
                    example = """
                        [{
                          "1": {
                            "id": 120,
                            "item": "COPPER_BLOCK",
                            "price": 2,
                            "amount": 4,
                            "type": "selling",
                            "stock": 5
                          }
                        }]
                        """
                )
            }
        ),
        @OpenApiResponse(
            status = "401",
            description = "If you don't specify an API key or the key doesn't match a known player"
        ),
        @OpenApiResponse(
            status = "403",
            description = "If the owner of the API key doesn't match with the queried player, and the queried player has their information private"
        ),
        @OpenApiResponse(
            status = "404",
            description = "If the item in your query returns null, a NotFound error is thrown instead of returning an empty array"
        ),
        @OpenApiResponse(
            status = "429",
            description = "This endpoint has a cooldown of 1 hour for loading information, and 60 seconds for reading cached information"
        ),
        @OpenApiResponse(
            status = "500",
            description = "If the target player's shops could not be loaded due to an internal error"
        )
    }
)
public class ShopEndpoint extends PostEndpoint<ShopEndpoint.ShopData> {
    private final QuickShopIntegration integration;
    private static final int LOAD_COOLDOWN_SECONDS = 3600;
    private static final int CACHE_COOLDOWN_SECONDS = 60;
    private final LoadingCache<UUID, ShopData> shopCache = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofHours(1))
        .build(new CacheLoader<>() {
            @Override
            public @NotNull ShopData load(@NotNull UUID uuid) {
                List<Shop> shops = integration.getPlayerShops(uuid);

                return new ShopData(getShopsJson(shops));
            }
        });

    public ShopEndpoint(EMCAPI plugin) {
        super(plugin);
        this.integration = Integrations.getIntegration("QuickShop-Hikari");
    }

    @Override
    public ShopData getObjectOrNull(@NotNull JsonElement element, @Nullable String key) {
        String string = JSONUtil.getJsonElementAsStringOrNull(element);
        if (string == null) throw HttpExceptions.NOT_A_STRING;

        UUID player;
        try {
            player = UUID.fromString(string);
        } catch (IllegalArgumentException ignored) {
            throw HttpExceptions.NOT_A_UUID;
        }

        UUID keyOwner = KeyManager.getKeyOwner(key);
        if (keyOwner == null) {
            throw HttpExceptions.MISSING_API_KEY;
        }
        OptOutSettings settings = plugin.getOptOut().getPlayerSettings(player);
        boolean publicData = settings != null && !settings.quickShops();
        boolean authorized = plugin.getAuth().authorize(player, AuthSettings.Type.SHOP_QUERY, keyOwner);
        if (!player.equals(keyOwner) && !publicData && !authorized) {
            throw HttpExceptions.FORBIDDEN;
        }
        // Side effect: Loading shop data would still throw 429 if the cache cooldown is violated. Not much of an issue considering it's only 1 minute
        CooldownUtil.checkAndAddCooldownOrThrow("shop_cache", keyOwner.toString(), CACHE_COOLDOWN_SECONDS);

        ShopData data = shopCache.getIfPresent(player);
        if (data != null) {
            return data;
        }
        CooldownUtil.checkAndAddCooldownOrThrow("shop_load", keyOwner.toString(), LOAD_COOLDOWN_SECONDS);
        try {
            return shopCache.get(player);
        } catch (ExecutionException e) {
            plugin.getSLF4JLogger().warn("ExecutionException while fetching shop cache for {}", player, e);
            CooldownUtil.remove("shop_load", keyOwner.toString());
            throw new InternalServerErrorResponse("Unexpected exception while loading " + player + "'s shops");
        }
    }

    @Override
    public JsonElement getJsonElement(@NotNull ShopData object, @Nullable String ignored) {
        return object.json;
    }

    public record ShopData(JsonElement json) {} // Wrapper to clarify what this endpoint returns

    private JsonElement getShopsJson(List<Shop> object) {
        if (object.isEmpty()) {
            return null;
        }

        final JsonObject shopsObject = new JsonObject();
        int counter = 0;
        for (final Shop shop : object) {
            shopsObject.add(String.valueOf(counter++), EndpointUtils.getShopObject(shop, true));
        }

        return shopsObject;
    }
}
