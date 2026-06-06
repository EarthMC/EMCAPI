package net.earthmc.emcapi.integration;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AccountLinkedEvent;
import github.scarsz.discordsrv.api.events.AccountUnlinkedEvent;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscordIntegration extends Integration {
    private static final Map<UUID, String> UUID_TO_DISCORD = new ConcurrentHashMap<>();
    private static final Map<String, UUID> DISCORD_TO_UUID = new ConcurrentHashMap<>();

    public DiscordIntegration() {
        super("DiscordSRV");
    }

    @Override
    public void register() {
        super.register();
        if (isEnabled()) {
            plugin.getServer().getAsyncScheduler().runDelayed(plugin, t -> {
                Set<UUID> uuids = TownyAPI.getInstance().getResidents().stream().filter(res -> !res.isNPC()).map(Resident::getUUID).collect(Collectors.toSet());

                Map<UUID, String> result = DiscordSRV.getPlugin().getAccountLinkManager().getManyDiscordIds(uuids);
                UUID_TO_DISCORD.putAll(result);

                for (Map.Entry<UUID, String> entry : result.entrySet()) {
                    DISCORD_TO_UUID.put(entry.getValue(), entry.getKey());
                }
            }, 30, TimeUnit.SECONDS);
        }
    }

    public UUID getUUID(String discordId) {
        return DISCORD_TO_UUID.get(discordId);
    }

    public String getDiscord(UUID uuid) {
        return UUID_TO_DISCORD.get(uuid);
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onDiscordLink(AccountLinkedEvent event) {
        OfflinePlayer player = event.getPlayer();
        String discordId = event.getUser().getId();

        UUID_TO_DISCORD.put(player.getUniqueId(), discordId);
        DISCORD_TO_UUID.put(discordId, player.getUniqueId());
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onDiscordUnlink(AccountUnlinkedEvent event) {
        OfflinePlayer player = event.getPlayer();
        String discordId = event.getDiscordId();

        UUID_TO_DISCORD.remove(player.getUniqueId());
        DISCORD_TO_UUID.remove(discordId);
    }
}
