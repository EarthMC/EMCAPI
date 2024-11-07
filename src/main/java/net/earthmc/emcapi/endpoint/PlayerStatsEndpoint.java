package net.earthmc.emcapi.endpoint;

import com.google.gson.JsonObject;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.earthmc.emcapi.EMCAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerStatsEndpoint {
    private final EMCAPI plugin;
    private final Path statsFilePath;
    private ScheduledTask task = null;
    private String latestStats = createEmptyStatistics().toString(); // Create empty statistics to return until we can load the actual ones

    public PlayerStatsEndpoint(EMCAPI plugin) {
        this.plugin = plugin;
        this.statsFilePath = plugin.getDataFolder().toPath().resolve("player-stats.json");
    }

    public void initialize() {
        // Load cached stats from disk and start the task to gather up-to-date statistics
        loadStatsFromDisk();
        startGatherStatisticsTask();
    }

    // Unused, but may be useful eventually
    public void shutdown() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    public void startGatherStatisticsTask() {
        this.task = Bukkit.getServer().getAsyncScheduler().runAtFixedRate(plugin, task -> {
            gatherStatistics();
        }, 15L, 30L, TimeUnit.MINUTES);
    }

    private void gatherStatistics() {
        // a long should be *long* enough to hold all results
        Map<Statistic, Long> statistics = new HashMap<>();

        for (final OfflinePlayer offlinePlayer : Bukkit.getServer().getOfflinePlayers()) {
            for (final Statistic statistic : Registry.STATISTIC) {
                if (statistic.getType() == Statistic.Type.UNTYPED)
                    statistics.merge(statistic, (long) offlinePlayer.getStatistic(statistic), Long::sum);
            }
        }

        JsonObject json = new JsonObject();
        statistics.forEach((statistic, value) -> json.addProperty(fixStatisticKey(statistic), value));

        this.latestStats = json.toString();

        // Save stats to disk
        try {
            Files.writeString(this.statsFilePath, this.latestStats, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            plugin.getSLF4JLogger().warn("Failed to save cached player stats to disk", e);
        }
    }

    @NotNull
    public String latestCachedStatistics() {
        return this.latestStats;
    }

    private void loadStatsFromDisk() {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                if (!Files.exists(this.statsFilePath)) {
                    return;
                }

                this.latestStats = Files.readString(this.statsFilePath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                plugin.getSLF4JLogger().warn("Failed to read cached player stats file from disk", e);
            }
        });
    }

    // time taken: too long
    private String fixStatisticKey(final Statistic statistic) {
        final String minimal = statistic.getKey().asMinimalString();

        return switch (minimal) {
            case "play_one_minute" -> "play_time";
            case "armor_cleaned" -> "clean_armor";
            case "banner_cleaned" -> "clean_banner";
            case "cake_slices_eaten" -> "eat_cake_slice";
            case "cauldron_used" -> "use_cauldron";
            case "cauldron_filled" -> "fill_cauldron";
            case "chest_opened" -> "open_chest";
            case "dispenser_inspected" -> "inspect_dispenser";
            case "dropper_inspected" -> "inspect_dropper";
            case "hopper_inspected" -> "inspect_hopper";
            case "enderchest_opened" -> "open_enderchest";
            case "furnace_interaction" -> "interact_with_furnace";
            case "crafting_table_interaction" -> "interact_with_crafting_table";
            case "beacon_interaction" -> "interact_with_beacon";
            case "brewingstand_interaction" -> "interact_with_brewingstand";
            case "record_played" -> "play_record";
            case "noteblock_played" -> "play_noteblock";
            case "noteblock_tuned" -> "tune_noteblock";
            case "flower_potted" -> "pot_flower";
            case "shulker_box_opened" -> "open_shulker_box";
            case "item_enchanted" -> "enchant_item";
            case "trapped_chest_triggered" -> "trigger_trapped_chest";
            default -> minimal;
        };
    }

    private JsonObject createEmptyStatistics() {
        JsonObject stats = new JsonObject();

        for (final Statistic stat : Registry.STATISTIC) {
            stats.addProperty(fixStatisticKey(stat), 0);
        }

        return stats;
    }
}
