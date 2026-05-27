package net.earthmc.emcapi.integration;

import com.palmergames.bukkit.towny.object.Nation;
import net.earthmc.lynchpin.api.towny.TownyProvider;
import net.earthmc.lynchpin.api.towny.pacts.Pact;
import net.earthmc.lynchpin.api.towny.pacts.TownyPacts;

import java.util.List;

public class PactsIntegration extends Integration {
    private TownyPacts module;

    public PactsIntegration() {
        super("Lynchpin");
        try {
            this.module = TownyProvider.instance().pacts();
        } catch (Throwable ignored) {
            plugin.getSLF4JLogger().warn("Not loading towny-pacts integration due to the module not being present/enabled");
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && module != null && module.isEnabled();
    }

    @Override
    public void register() {
        Integrations.addIntegration("lynchpin-towny-pacts", this);
    }

    public List<Pact> getActivePacts(Nation nation) {
        return module.getActivePacts(nation);
    }

    public List<Pact> getPendingPacts(Nation nation) {
        return module.getPendingPacts(nation);
    }
}
