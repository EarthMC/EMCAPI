package net.earthmc.emcapi.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.sse.SSEManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ApiCommand {
    private static final Cache<CommandCooldown, Instant> COOLDOWNS = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.MINUTES).build();

    private static final Component INFO_MESSAGE = Component.text()
        .append(Component.text("- The API provides real-time information about players, towns, and nations. The API can be accessed ", NamedTextColor.AQUA))
        .append(Component.text("here", NamedTextColor.AQUA, TextDecoration.UNDERLINED).clickEvent(ClickEvent.openUrl("https://api.earthmc.net/")))
        .appendNewline()
        .append(Component.text("- Read the docs ", NamedTextColor.GREEN))
        .append(Component.text("here", NamedTextColor.GREEN, TextDecoration.UNDERLINED).clickEvent(ClickEvent.openUrl("https://earthmc.net/docs/api")))
        .appendNewline()
        .append(Component.text("- If you'd like to connect to the API's Server-Sent-Events endpoint, please create an API key using /api key create", NamedTextColor.GREEN))
        .appendNewline()
        .append(Component.text("- If you'd like to opt out of your information being public on the API, you can use /api opt-out", NamedTextColor.RED))
        .build();

    private ApiCommand() {}

    public static LiteralCommandNode<CommandSourceStack> create(final EMCAPI plugin) {
        return Commands.literal("api")
            .executes(ctx -> {
                ctx.getSource().getSender().sendMessage(INFO_MESSAGE);
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.literal("opt-out")
                .requires(ctx -> ctx.getSender() instanceof Player)
                .executes(ctx -> {
                    final Player player = (Player) ctx.getSource().getSender();

                    if (plugin.getOptOut().playerOptedOut(player.getUniqueId())) {
                        player.sendMessage(Component.text("You have already opted out previously!", NamedTextColor.RED));
                    } else {
                        if (isOnCooldown(player, CooldownType.OPT_OUT_CHANGE)) {
                            return Command.SINGLE_SUCCESS;
                        }

                        player.sendMessage(Component.text("You have opted out of having your information being public on the API.", NamedTextColor.GREEN));
                        plugin.getOptOut().setOptedOut(player.getUniqueId(), true);
                    }
                    return Command.SINGLE_SUCCESS;
                }))
            .then(Commands.literal("opt-in")
                .requires(ctx -> ctx.getSender() instanceof Player)
                .executes(ctx -> {
                    final Player player = (Player) ctx.getSource().getSender();

                    if (plugin.getOptOut().playerOptedOut(player.getUniqueId())) {
                        if (isOnCooldown(player, CooldownType.OPT_OUT_CHANGE)) {
                            return Command.SINGLE_SUCCESS;
                        }

                        player.sendMessage(Component.text("You have opted back in to your information being public on the API.", NamedTextColor.GREEN));
                        plugin.getOptOut().setOptedOut(player.getUniqueId(), false);
                    } else {
                        player.sendMessage(Component.text("You are currently not opted out!", NamedTextColor.RED));
                    }
                    return Command.SINGLE_SUCCESS;
                }))
            .then(Commands.literal("key")
                .requires(ctx -> ctx.getSender() instanceof Player player && player.hasPermission("emcapi.key"))
                .executes(ApiCommand::copyKey)
                .then(Commands.literal("create")
                    .executes(ctx -> {
                        final Player player = (Player) ctx.getSource().getSender();
                        if (KeyManager.getPlayerKey(player.getUniqueId()) != null) {
                            player.sendMessage(Component.text("You already have an API key! Use /api key to view it.", NamedTextColor.RED));
                        } else {
                            if (isOnCooldown(player, CooldownType.MODIFY_KEY)) {
                                return Command.SINGLE_SUCCESS;
                            }

                            final String key = KeyManager.createApiKey(player.getUniqueId());
                            player.sendMessage(Component.text("Key created! Click to copy.", NamedTextColor.GREEN).clickEvent(ClickEvent.copyToClipboard(key)));
                        }
                        return Command.SINGLE_SUCCESS;
                    }))
                .then(Commands.literal("delete")
                    .executes(ctx -> {
                        final Player player = (Player) ctx.getSource().getSender();
                        final String key = KeyManager.getPlayerKey(player.getUniqueId());
                        if (key != null) {
                            if (isOnCooldown(player, CooldownType.MODIFY_KEY)) {
                                return Command.SINGLE_SUCCESS;
                            }

                            SSEManager.deleteKey(key);
                            KeyManager.deletePlayerKey(player.getUniqueId());
                            player.sendMessage(Component.text("Successfully deleted your API key", NamedTextColor.GREEN));
                        } else {
                            player.sendMessage(Component.text("You do not have an API key.", NamedTextColor.RED));
                        }

                        return Command.SINGLE_SUCCESS;
                    }))
                .then(Commands.literal("copy")
                    .executes(ApiCommand::copyKey)))
            .build();
    }

    private static int copyKey(final CommandContext<CommandSourceStack> ctx) {
        final Player player = (Player) ctx.getSource().getSender();
        final String key = KeyManager.getPlayerKey(player.getUniqueId());

        if (key != null) {
            player.sendMessage(Component.text("Click to copy your API key.", NamedTextColor.GREEN).clickEvent(ClickEvent.copyToClipboard(key)));
        } else {
            player.sendMessage(Component.text("You do not have an API key. Use /api key create to create one.", NamedTextColor.RED));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static boolean isOnCooldown(final Player player, final CooldownType type) {
        final Instant now = Instant.now();
        final Instant lastCommandUse = COOLDOWNS.asMap().putIfAbsent(new CommandCooldown(player.getUniqueId(), type), now);

        if (lastCommandUse != null) {
            final long seconds = Duration.between(now, lastCommandUse.plusSeconds(60)).getSeconds();

            if (seconds > 0) {
                player.sendMessage(Component.text("Please wait " + seconds + " more second" + (seconds == 1 ? "" : "s") + " before trying this command again.", NamedTextColor.RED));
                return true;
            }
        }

        return false;
    }

    private enum CooldownType {
        MODIFY_KEY,
        OPT_OUT_CHANGE
    }

    private record CommandCooldown(UUID uuid, CooldownType type) {}
}
