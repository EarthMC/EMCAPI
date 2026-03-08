package net.earthmc.emcapi.command;

import net.earthmc.emcapi.manager.KeyManager;
import net.earthmc.emcapi.sse.SSEManager;
import net.earthmc.emcapi.util.EndpointUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ApiCommand implements TabExecutor {
    private static final Component infoMessage = Component.text()
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

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players may use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(infoMessage);
            return true;
        }
        String action = args[0].toLowerCase();
        switch (action) {
            case "opt-in" -> {
                player.sendMessage(Component.text("You have opted back in for your information being public on the API.", NamedTextColor.GREEN));
                EndpointUtils.setOptedOut(player.getUniqueId(), false);
            }
            case "opt-out" -> {
                player.sendMessage(Component.text("You have opted out of your information being public on the API", NamedTextColor.RED));
                EndpointUtils.setOptedOut(player.getUniqueId(), true);
            }
            case "key" -> handleKey(player, args);
            default -> player.sendMessage(Component.text("Usage: /api [opt-in|opt-out|key]", NamedTextColor.RED));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("opt-in", "opt-out", "key").filter(str -> str.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("key")) {
            return Stream.of("create", "delete", "copy").filter(str -> str.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }

        return List.of();
    }

    private void handleKey(Player player, String[] args) {
        if (!player.hasPermission("emcapi.key")) {
            player.sendMessage(Component.text("You do not have permission to use this command", NamedTextColor.RED));
            return;
        }

        UUID playerID = player.getUniqueId();
        Long key;
        if (args.length == 1) {
            key = KeyManager.getPlayerKey(playerID);
            if (key != null) {
                player.sendMessage(Component.text("Click to copy your API key.", NamedTextColor.GREEN).clickEvent(ClickEvent.copyToClipboard(key.toString())));
            } else {
                player.sendMessage(Component.text("You do not have an API key. Use /api key create to create one.", NamedTextColor.RED));
            }
            return;
        }
        String action = args[1].toLowerCase();
        switch (action) {
            case "create" -> {
                if (KeyManager.getPlayerKey(playerID) != null) {
                    player.sendMessage(Component.text("You already have an API key! Use /api key to get it.", NamedTextColor.RED));
                } else {
                    key = KeyManager.createApiKey(playerID);
                    player.sendMessage(Component.text("Key created! Click to copy.", NamedTextColor.GREEN).clickEvent(ClickEvent.copyToClipboard(key.toString())));
                }
            }
            case "delete" -> {
                key = KeyManager.getPlayerKey(playerID);
                if (key != null) {
                    SSEManager.deleteKey(key);
                    KeyManager.deletePlayerKey(playerID);
                    player.sendMessage(Component.text("Successfully deleted your API key", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("You do not have an API key.", NamedTextColor.RED));
                }
            }
            case "copy" -> {
                key = KeyManager.getPlayerKey(playerID);
                if (key != null) {
                    player.sendMessage(Component.text("Click to copy your API key.", NamedTextColor.GREEN).clickEvent(ClickEvent.copyToClipboard(key.toString())));
                } else {
                    player.sendMessage(Component.text("You do not have an API key. Use /api key create to create one.", NamedTextColor.RED));
                }
            }
            default -> player.sendMessage(Component.text("Usage: /api key <create|delete|copy>", NamedTextColor.RED));
        }
    }
}
