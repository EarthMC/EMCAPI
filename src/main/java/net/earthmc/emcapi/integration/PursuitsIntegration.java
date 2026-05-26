package net.earthmc.emcapi.integration;

import net.earthmc.lynchpin.api.pursuits.Pursuit;
import net.earthmc.lynchpin.api.pursuits.PursuitsProvider;

import java.util.Map;

public class PursuitsIntegration extends Integration {
    private PursuitsProvider pursuits;

    public PursuitsIntegration() {
        super("Lynchpin");
        try {
            pursuits = PursuitsProvider.instance();
        } catch (Throwable ignored) {
            plugin.getSLF4JLogger().warn("Not loading pursuits integration due to the module not being present/enabled");
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && pursuits != null && pursuits.isEnabled();
    }

    @Override
    public void register() {
        Integrations.addIntegration("lynchpin-pursuits", this);
    }

    public Pursuit getPursuit(Pursuit.Type type) {
        return pursuits.getPursuit(type);
    }

    public Map<Pursuit.Type, Pursuit> getPursuits() {
        return pursuits.getPursuits();
    }
}
