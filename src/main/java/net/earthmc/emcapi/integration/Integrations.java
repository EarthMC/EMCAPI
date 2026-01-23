package net.earthmc.emcapi.integration;

import net.earthmc.emcapi.EMCAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Integrations implements Listener {
    private final EMCAPI plugin;

    private final Map<String, Integration> integrations = new ConcurrentHashMap<>();
    private final DiscordIntegration discordIntegration;
    private final QuartersIntegration quartersIntegration;
    private final SuperbVoteIntegration superbVoteIntegration;
    private final MysteryMasterIntegration mysteryMasterIntegration;

    public Integrations(final EMCAPI plugin) {
        this.plugin = plugin;

        this.discordIntegration = addIntegration(new DiscordIntegration());
        this.quartersIntegration = addIntegration(new QuartersIntegration());
        this.superbVoteIntegration = addIntegration(new SuperbVoteIntegration());
        this.mysteryMasterIntegration = addIntegration(new MysteryMasterIntegration());
    }

    private <T extends Integration> T addIntegration(final T integration) {
        integrations.put(integration.name(), integration);

        integration.setEnabled(plugin.getServer().getPluginManager().isPluginEnabled(integration.name()));
        return integration;
    }

    public DiscordIntegration discordIntegration() {
        return this.discordIntegration;
    }

    public QuartersIntegration quartersIntegration() {
        return this.quartersIntegration;
    }

    public SuperbVoteIntegration superbVoteIntegration() {
        return this.superbVoteIntegration;
    }

    public MysteryMasterIntegration mysteryMasterIntegration() {
        return this.mysteryMasterIntegration;
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent event) {
        final Integration integration = integrations.get(event.getPlugin().getName());
        if (integration != null) {
            integration.setEnabled(true);
        }
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent event) {
        final Integration integration = integrations.get(event.getPlugin().getName());
        if (integration != null) {
            integration.setEnabled(false);
        }
    }
}
