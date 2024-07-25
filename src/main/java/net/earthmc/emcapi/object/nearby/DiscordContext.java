package net.earthmc.emcapi.object.nearby;

public class DiscordContext {

    private final DiscordType type;
    private final String target;

    public DiscordContext(DiscordType type, String target) {
        this.type = type;
        this.target = target;
    }

    public DiscordType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }
}
