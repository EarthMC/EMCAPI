package net.earthmc.emcapi.integration;

import net.earthmc.emcapi.util.HttpExceptions;

/**
 * Represents an integration with another plugin.
 */
public abstract class Integration {
    private final String name;
    private boolean enabled;

    protected Integration(final String name) {
        this.name = name;
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
        if (!this.enabled) {
            throw HttpExceptions.MISSING_PLUGIN;
        }
    }
}
