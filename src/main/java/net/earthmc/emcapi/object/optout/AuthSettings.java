package net.earthmc.emcapi.object.optout;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public record AuthSettings(Map<Type, Set<UUID>> authorised) {

    public static AuthSettings getNew() {
        return new AuthSettings(new ConcurrentHashMap<>());
    }

    public static AuthSettings parse(Map<Type, String> map) {
        AuthSettings settings = new AuthSettings(new ConcurrentHashMap<>());
        for (Map.Entry<Type, String> entry : map.entrySet()) {
            Set<UUID> uuids = parseUUIDs(entry.getValue());
            if (uuids.isEmpty()) continue;
            settings.authorised.put(entry.getKey(), uuids);
        }

        return settings;
    }

    private static Set<UUID> parseUUIDs(String string) {
        final String[] split = string.split(",");

        Set<UUID> uuids = ConcurrentHashMap.newKeySet(split.length);
        for (String str : split) {
            try {
                uuids.add(UUID.fromString(str));
            } catch (IllegalArgumentException ignored) {}
        }
        return uuids;
    }

    public boolean authorize(Type type, UUID uuid) {
        final Set<UUID> uuids = authorised.get(type);
        return uuids != null && uuids.contains(uuid);
    }

    public AuthSettings add(Type type, UUID uuid) {
        authorised.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(uuid);
        return this;
    }

    public AuthSettings remove(Type type, UUID uuid) {
        final Set<UUID> authorizedUUIDs = authorised.get(type);
        if (authorizedUUIDs != null && authorizedUUIDs.remove(uuid) && authorizedUUIDs.isEmpty()) {
            authorised.remove(type);
        }
        return this;
    }

    public AuthSettings clear(Type type) {
        authorised.remove(type);
        return this;
    }

    public int size(Type type) {
        final Set<UUID> set = authorised.get(type);
        return set == null ? 0 : set.size();
    }

    public boolean isRedundant() {
        return authorised.isEmpty();
    }

    public Set<UUID> getAuthorizedForType(Type type) {
        return authorised.getOrDefault(type, Set.of());
    }

    public String getStringForType(Type type) {
        final Set<UUID> uuids = authorised.get(type);
        if (uuids == null) {
            return "";
        }

        return uuids.stream().map(UUID::toString).collect(Collectors.joining(","));
    }

    public enum Type {
        SHOP_SSE,
        SHOP_QUERY,
        RES_QUERY
    }
}
