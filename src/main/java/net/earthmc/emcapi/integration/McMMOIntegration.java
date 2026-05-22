package net.earthmc.emcapi.integration;

import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.mcMMO;

import java.util.UUID;

public class McMMOIntegration extends Integration {

    public McMMOIntegration() {
        super("mcMMO");
    }

    public PlayerProfile getPlayerProfile(UUID uuid) {
        return mcMMO.getDatabaseManager().loadPlayerProfile(uuid);
    }
}
