package net.earthmc.emcapi.object.optout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record AuthSettings(Map<Type, List<UUID>> authorised) {

    public static AuthSettings getNew() {
        return new AuthSettings(new HashMap<>());
    }

    public static AuthSettings parse(Map<Type, String> map) {
        AuthSettings settings = new AuthSettings(new HashMap<>());
        for (Map.Entry<Type, String> entry : map.entrySet()) {
            List<UUID> uuids = parseUUIDs(entry.getValue());
            if (uuids.isEmpty()) continue;
            settings.authorised.put(entry.getKey(), uuids);
        }

        return settings;
    }

    private static List<UUID> parseUUIDs(String string) {
        List<UUID> list = new ArrayList<>();
        for (String str : string.split(",")) {
            try {
                list.add(UUID.fromString(str));
            } catch (IllegalArgumentException ignored) {}
        }
        return list;
    }

    public boolean authorize(Type type, UUID uuid) {
        return authorised.containsKey(type) && authorised.get(type).contains(uuid);
    }

    public AuthSettings add(Type type, UUID uuid) {
        authorised.computeIfAbsent(type, k -> new ArrayList<>()).add(uuid);
        return this;
    }

    public AuthSettings remove(Type type, UUID uuid) {
        if (authorised.containsKey(type)) {
            authorised.get(type).remove(uuid);
            if (authorised.get(type).isEmpty()) {
                authorised.remove(type);
            }
        }
        return this;
    }

    public AuthSettings clear(Type type) {
        authorised.remove(type);
        return this;
    }

    public int size(Type type) {
        return authorised.containsKey(type) ? authorised.get(type).size() : 0;
    }

    public boolean isRedundant() {
        return authorised.isEmpty();
    }

    public String getStringForType(Type type) {
        return authorised.containsKey(type) ? authorised.get(type).stream().map(UUID::toString).collect(Collectors.joining(",")) : "";
    }

    public enum Type {
        SHOP_SSE,
        SHOP_QUERY
    }
}
