package net.earthmc.emcapi.integration;

import com.palmergames.bukkit.towny.object.Nation;
import net.earthmc.lynchpin.api.towny.TownyProvider;
import net.earthmc.lynchpin.api.towny.embargoes.Embargo;
import net.earthmc.lynchpin.api.towny.embargoes.TownyEmbargo;

import java.util.Set;

public class EmbargoesIntegration extends Integration {
    private TownyEmbargo module;

    public EmbargoesIntegration() {
        super("Lynchpin");
        try {
            this.module = TownyProvider.instance().embargoes();
        } catch (Exception ignored) {
            plugin.getLogger().warning("Not loading towny-embargoes integration due to the module not being present/enabled");
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && module != null && module.isEnabled();
    }

    @Override
    public void register() {
        Integrations.addIntegration("lynchpin-towny-embargoes", this);
    }

    public Set<Embargo> getEmbargoes(Nation nation) {
        return module.getEmbargoes(nation);
    }
}
