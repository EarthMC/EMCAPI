package net.earthmc.emcapi.integration;

import com.palmergames.bukkit.towny.object.Town;
import net.earthmc.lynchpin.api.towny.TownyProvider;
import net.earthmc.lynchpin.api.towny.warps.TownyWarps;
import net.earthmc.lynchpin.api.towny.warps.Warp;

import java.util.Set;

public class WarpsIntegration extends Integration {
    private TownyWarps module;

    public WarpsIntegration() {
        super("Lynchpin");
        try {
            this.module = TownyProvider.instance().warps();
        } catch (Exception ignored) {
            plugin.getLogger().warning("Not loading towny-warps integration due to the module not being present/enabled");
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && module != null && module.isEnabled();
    }

    @Override
    public void register() {
        Integrations.addIntegration("lynchpin-towny-warps", this);
    }

    public Set<Warp> getWarps(Town town) {
        return module.getWarps(town);
    }
}
