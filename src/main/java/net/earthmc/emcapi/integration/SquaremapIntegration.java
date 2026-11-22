package net.earthmc.emcapi.integration;

import org.bukkit.entity.Player;
import xyz.jpenilla.squaremap.api.SquaremapProvider;

public class SquaremapIntegration extends Integration {
    public SquaremapIntegration() {
        super("squaremap");
    }

    public boolean isHidden(final Player player) {
        return SquaremapProvider.get().playerManager().hidden(player.getUniqueId());
    }
}
