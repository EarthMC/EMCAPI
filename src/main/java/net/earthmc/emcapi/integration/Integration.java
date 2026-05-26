package net.earthmc.emcapi.integration;

import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.util.HttpExceptions;

/**
 * Represents an integration with another plugin.
 */
public abstract class Integration {
    private final String name;
    private boolean enabled;
    protected final EMCAPI plugin = EMCAPI.instance;

    protected Integration(final String name) {
        this.name = name;
    }

    /**
     * Allow overriding to use a custom identifier. {@code name()} will still be used for plugin names.
     */
    public void register() {
        Integrations.addIntegration(name(), this);
    }

    /**
     * {@return the name of the plugin that this is integrating with}
     */
    public String name() {
        return this.name;
    }

    /**
     * {@return whether the plugin for this integration is currently enabled}
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void throwIfDisabled() {
        if (!isEnabled()) {
            throw HttpExceptions.MISSING_PLUGIN;
        }
    }
}
