package net.earthmc.emcapi.command;

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
import java.util.stream.Stream;

public class OptOutCommand implements TabExecutor {
    private static final Component infoMessage = Component.text("The API provides real-time information about players, towns, and nations. The API can be accessed ", NamedTextColor.BLUE)
            .append(Component.text("here", NamedTextColor.BLUE, TextDecoration.UNDERLINED).clickEvent(ClickEvent.openUrl("https://api.earthmc.net/v3/aurora/")))
            .appendNewline()
            .append(Component.text("Read the docs ", NamedTextColor.GREEN))
            .append(Component.text("here", NamedTextColor.GREEN, TextDecoration.UNDERLINED).clickEvent(ClickEvent.openUrl("https://earthmc.net/docs/api")))
            .appendNewline()
            .append(Component.text("If you'd like to opt out of your information being public on the API, you can use /api opt-out", NamedTextColor.DARK_RED));

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players may use this command.", NamedTextColor.RED));
            return true;
        }

        if (strings.length < 1) {
            player.sendMessage(infoMessage);
            return true;
        }
        if (strings[0].equalsIgnoreCase("opt-in")) {
            player.sendMessage(Component.text("You have opted back in for your information being public on the API.", NamedTextColor.GREEN));
            EndpointUtils.setOptedOut(player.getUniqueId(), false);
        } else if (strings[0].equalsIgnoreCase("opt-out")){
            player.sendMessage(Component.text("You have opted out of your information being public on the API", NamedTextColor.RED));
            EndpointUtils.setOptedOut(player.getUniqueId(), true);
        } else {
            player.sendMessage(Component.text("Usage: /api [opt-in|opt-out]", NamedTextColor.RED));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return Stream.of("opt-in", "opt-out").filter(str -> str.startsWith(strings[0])).toList();
        }

        return List.of();
    }
}
