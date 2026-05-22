package net.earthmc.emcapi.integration;

import net.earthmc.lynchpin.api.advancements.AdvancementEntry;
import net.earthmc.lynchpin.api.advancements.AdvancementsProvider;

import java.util.Set;

public class AdvancementsIntegration extends Integration {
    private AdvancementsProvider module;

    public AdvancementsIntegration() {
        super("Lynchpin");
        try {
            module = AdvancementsProvider.instance();
        } catch (Exception ignored) {
            plugin.getLogger().warning("Not loading advancements integration due to the module not being present/enabled");
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && module != null && module.isEnabled();
    }

    @Override
    public void register() {
        Integrations.addIntegration("lynchpin-advancements", this);
    }

    public Set<AdvancementEntry> getAdvancements() {
        return module.getAdvancements();
    }
}
