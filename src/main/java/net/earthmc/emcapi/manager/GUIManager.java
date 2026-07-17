package net.earthmc.emcapi.manager;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import dev.warriorrr.inventories.event.input.StartAwaitingInputEvent;
import dev.warriorrr.inventories.gui.MenuInventory;
import dev.warriorrr.inventories.gui.MenuItem;
import dev.warriorrr.inventories.gui.action.ClickAction;
import dev.warriorrr.inventories.gui.input.response.InputResponse;
import dev.warriorrr.inventories.gui.slot.Slot;
import dev.warriorrr.inventories.gui.slot.anchor.HorizontalAnchor;
import dev.warriorrr.inventories.gui.slot.anchor.SlotAnchor;
import dev.warriorrr.inventories.gui.slot.anchor.VerticalAnchor;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import kotlin.Pair;
import net.earthmc.emcapi.EMCAPI;
import net.earthmc.emcapi.object.optout.AuthSettings;
import net.earthmc.emcapi.object.optout.OptOutSettings;
import net.earthmc.emcapi.object.optout.OptOutType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class GUIManager implements Listener {
    private final EMCAPI plugin;
    private final OptOut optOut;
    private final Authorisation auth;
    private final Set<UUID> authPending = ConcurrentHashMap.newKeySet();
    private final Set<UUID> optOutPending = ConcurrentHashMap.newKeySet();
    private final Set<UUID> signInputs = ConcurrentHashMap.newKeySet();

    public GUIManager(EMCAPI plugin) {
        this.plugin = plugin;
        this.optOut = plugin.getOptOut();
        this.auth = plugin.getAuth();
    }

    public MenuInventory createRoot(Player player) {
        signInputs.remove(player.getUniqueId()); // In case of any bugs, re-using the command from the beginning will allow a fresh start
        MenuInventory.Builder menu = MenuInventory.builder()
            .title(Component.text("API Help Menu", NamedTextColor.AQUA, TextDecoration.BOLD))
            .rows(3);

        MenuItem optOut = MenuItem.builder(Material.BARRIER)
            .name(Component.text("Manage your API privacy", NamedTextColor.GREEN, TextDecoration.BOLD))
            .lore(Component.text("Click to manage your API opt out settings", NamedTextColor.GRAY))
            .action(ClickAction.openSilent(() -> createOptOutMenu(player)))
            .slot(slot(1, 2))
            .withGlint()
            .build();

        MenuItem authorise = MenuItem.builder(Material.PLAYER_HEAD)
            .skullOwner(player.getUniqueId())
            .name(Component.text("Manage your player authorisation settings", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
            .lore(Component.text("Click to manage which players have what rights to your information", NamedTextColor.GRAY))
            .action(ClickAction.openSilent(() -> createAuthMenu(player)))
            .slot(slot(1, 4))
            .build();

        MenuItem help = MenuItem.builder(Material.COPPER_LANTERN)
            .name(Component.text("API Guide", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .lore(Component.text("Click to learn more about the API and your privacy", NamedTextColor.GRAY))
            .action(ClickAction.openSilent(() -> createHelpMenu(player)))
            .slot(slot(1, 6))
            .withGlint()
            .build();

        menu.addItem(optOut).addItem(authorise).addItem(help);
        return menu.build();
    }

    public MenuInventory createOptOutMenu(Player player) {
        OptOutSettings settings = optOut.opted.getOrDefault(player.getUniqueId(), OptOutSettings.DEFAULT);
        MenuInventory.Builder menu = MenuInventory.builder()
            .title(Component.text("Manage your API privacy", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
            .rows(3);

        boolean override = settings.override();
        Component name = override ? Component.text("Override: All data disabled.", NamedTextColor.RED, TextDecoration.BOLD)
            : Component.text("Override is disabled. Data is shared per-feature.", NamedTextColor.GREEN);
        Component lore = override ? Component.text("Click to disable your override settings", NamedTextColor.DARK_GREEN)
            : Component.text("Enabling this will override your other preferences", NamedTextColor.DARK_RED);

        menu.addItem(MenuItem.builder(Material.BARRIER)
            .slot(slot(0, 4))
            .name(name)
            .lore(lore)
            .withGlint(override)
            .action(ClickAction.openSilent(() -> {
                optOut.opted.put(player.getUniqueId(), settings.override(!override));
                optOutPending.add(player.getUniqueId());
                return createOptOutMenu(player);
            }))
            .build());

        boolean resident = settings.townyResident();
        name = Component.text("Resident Status", resident ? NamedTextColor.RED : NamedTextColor.GREEN);
        lore = resident ? Component.text("Click to show your resident data", NamedTextColor.DARK_GREEN)
            : Component.text("Click to hide your resident data", NamedTextColor.DARK_RED);

        menu.addItem(MenuItem.builder(Material.PLAYER_HEAD)
            .slot(slot(1, 1))
            .skullOwner(player.getUniqueId())
            .name(name)
            .lore(lore)
            .action(ClickAction.openSilent(() -> {
                optOut.opted.put(player.getUniqueId(), settings.update(OptOutType.TOWNY_RESIDENT, !resident));
                optOutPending.add(player.getUniqueId());
                return createOptOutMenu(player);
            }))
            .build());

        boolean online = settings.onlineStatus();
        name = Component.text("Online Status", online ? NamedTextColor.RED : NamedTextColor.GREEN);
        lore = online ? Component.text("Click to re-enable your online status", NamedTextColor.DARK_GREEN)
            : Component.text("Click to disable your online status", NamedTextColor.DARK_RED);

        menu.addItem(MenuItem.builder(Material.EMERALD)
            .slot(slot(1, 3))
            .name(name)
            .lore(lore)
            .action(ClickAction.openSilent(() -> {
                optOut.opted.put(player.getUniqueId(), settings.update(OptOutType.ONLINE_STATUS, !online));
                optOutPending.add(player.getUniqueId());
                return createOptOutMenu(player);
            }))
            .withGlint(online)
            .build());

        boolean shops = settings.quickShops();
        name = Component.text("QuickShops", shops ? NamedTextColor.RED : NamedTextColor.GREEN);
        lore = shops ? Component.text("Click to make your shop data public", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
            : Component.text("Click to make your shop data private, only accessible with your API key", NamedTextColor.DARK_RED, TextDecoration.BOLD);

        menu.addItem(MenuItem.builder(Material.CHEST)
            .slot(slot(1, 5))
            .name(name)
            .lore(lore)
            .action(ClickAction.openSilent(() -> {
                optOut.opted.put(player.getUniqueId(), settings.update(OptOutType.QUICKSHOPS, !shops));
                optOutPending.add(player.getUniqueId());
                return createOptOutMenu(player);
            }))
            .withGlint(shops)
            .build());

        boolean mcmmo = settings.mcmmo();
        name = Component.text("McMMO Stats", mcmmo ? NamedTextColor.RED : NamedTextColor.GREEN);
        lore = mcmmo ? Component.text("Click to make your mcMMO stats public", NamedTextColor.DARK_GREEN)
            : Component.text("Click to make your mcMMO stats private, only accessible with your API key", NamedTextColor.DARK_RED);

        menu.addItem(MenuItem.builder(Material.DIAMOND_AXE)
            .slot(slot(1, 7))
            .name(name)
            .lore(lore)
            .action(ClickAction.openSilent(() -> {
                optOut.opted.put(player.getUniqueId(), settings.update(OptOutType.MCMMO, !mcmmo));
                optOutPending.add(player.getUniqueId());
                return createOptOutMenu(player);
            }))
            .withGlint(mcmmo)
            .build());

        menu.addItem(createMainMenuButton(player));
        return menu.build();
    }

    @SuppressWarnings("UnstableApiUsage")
    public MenuInventory createAuthMenu(Player player) {
        AuthSettings settings = auth.authMap.containsKey(player.getUniqueId()) ? auth.authMap.get(player.getUniqueId()) : AuthSettings.getNew();
        MenuInventory.Builder menu = MenuInventory.builder()
            .title(Component.text("Manage authorisation", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
            .rows(5);

        MenuItem main = MenuItem.builder(Material.WRITTEN_BOOK)
            .name(Component.text("What's here", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .lore(Component.text("• You can authorise specific players to access your information", NamedTextColor.YELLOW))
            .lore(Component.text("• Currently, you can allow players to listen to your Shop Server-Sent-Events, or to query your shop data.", NamedTextColor.YELLOW))
            .lore(Component.text("• Click the editors below to edit each respectively.", NamedTextColor.GRAY))
            .slot(slot(1, 4))
            .build();

        UUID playerUUID = player.getUniqueId();
        MenuItem sse = MenuItem.builder(Material.GOAT_HORN)
            .name(Component.text("Shop SSE", NamedTextColor.GREEN))
            .lore(Component.text("• Players authorised here will be able to connect to the server's /sse endpoint", NamedTextColor.GREEN))
            .lore(Component.text("• and receive events fired by your QuickShops", NamedTextColor.GREEN))
            .lore(Component.text("• For example, when your shop sells an item or is out of stock", NamedTextColor.GREEN))
            .lore(Component.text("Click to add or remove players", NamedTextColor.WHITE))
            .action(ClickAction.openSilent(() -> editAuthorisedMenu(player, settings, AuthSettings.Type.SHOP_SSE)))
            .slot(slot(3, 2))
            .withGlint()
            .mutateItem(item -> item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.INSTRUMENT).build()))
            .build();

        MenuItem query = MenuItem.builder(Material.BARREL)
            .name(Component.text("Shop Query", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
            .lore(Component.text("• Players authorised here will be able to query all your shops in the /shop endpoint", NamedTextColor.DARK_GREEN))
            .lore(Component.text("• This bypasses your shop data not being public in your opt out settings", NamedTextColor.DARK_GREEN))
            .lore(Component.text("Click to add or remove players", NamedTextColor.WHITE))
            .action(ClickAction.openSilent(() -> editAuthorisedMenu(player, settings, AuthSettings.Type.SHOP_QUERY)))
            .slot(slot(3, 6))
            .withGlint()
            .build();

        menu.addItem(main).addItem(sse).addItem(query).addItem(createMainMenuButton(player));
        return menu.build();
    }

    public MenuInventory createHelpMenu(Player player) {
        MenuInventory.Builder menu = MenuInventory.builder()
            .title(Component.text("API Guide", NamedTextColor.GRAY, TextDecoration.BOLD))
            .rows(3);

        MenuItem temp = MenuItem.builder(Material.BEDROCK)
            .name(Component.text("Not ready", NamedTextColor.GRAY, TextDecoration.BOLD))
            .lore(Component.text("This feature is still in development.", NamedTextColor.GRAY))
            .slot(slot(1, 4))
            .withGlint()
            .build();

        menu.addItem(temp).addItem(createMainMenuButton(player));
        return menu.build();
    }

    private MenuItem createMainMenuButton(Player player) {
        return MenuItem.builder(Material.BARRIER)
            .name(Component.text("Main Menu", NamedTextColor.GREEN, TextDecoration.BOLD))
            .lore(Component.text("Click to open", NamedTextColor.GRAY))
            .action(ClickAction.openSilent(() -> createRoot(player)))
            .slot(SlotAnchor.bottomRight())
            .build();
    }

    private MenuItem createBackButton(Supplier<MenuInventory> supplier) {
        return MenuItem.builder(Material.BARRIER)
            .name(Component.text("Back", NamedTextColor.RED, TextDecoration.BOLD))
            .lore(Component.text("Click to go back", NamedTextColor.GRAY))
            .action(ClickAction.openSilent(supplier))
            .slot(SlotAnchor.bottomRight())
            .build();
    }

    private MenuInventory editAuthorisedMenu(Player player, AuthSettings settings, AuthSettings.Type type) {
        MenuInventory.Builder menu = MenuInventory.builder()
            .title(Component.text("What would you like to do?", NamedTextColor.AQUA, TextDecoration.BOLD))
            .rows(3);

        UUID playerUUID = player.getUniqueId();
        MenuItem add = MenuItem.builder(Material.GREEN_WOOL)
            .name(Component.text("Add a player", NamedTextColor.GREEN))
            .lore(Component.text("Currently authorised: " + settings.size(type), NamedTextColor.GRAY))
            .action(ClickAction.userInput(Component.text("The name or UUID of the player", NamedTextColor.GREEN), input -> {
                String text = input.getText();
                InputResponse response = InputResponse.reOpen(() -> {
                    signInputs.remove(playerUUID);
                    return editAuthorisedMenu(player, settings, type);
                });
                UUID uuid;
                String name;
                try {
                    Pair<UUID, String> pair = parsePlayer(text);
                    uuid = pair.getFirst();
                    name = pair.getSecond();
                } catch (IllegalStateException e) {
                    player.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
                    return response;
                }
                if (playerUUID.equals(uuid)) {
                    player.sendMessage(Component.text("You cannot authorise yourself! You already have full access to your own information.", NamedTextColor.RED));
                    return response;
                }
                if (settings.authorize(AuthSettings.Type.SHOP_SSE, uuid)) {
                    player.sendMessage(Component.text(name + " is already authorised for " + type.name(), NamedTextColor.RED));
                    return response;
                }
                auth.authMap.put(playerUUID, settings.add(type, uuid));
                authPending.add(playerUUID);
                player.sendMessage(Component.text("Successfully authorised player " + name + " for " + type.name(), NamedTextColor.GREEN)
                    .appendNewline()
                    .append(Component.text("(UUID: " + uuid + ")", NamedTextColor.GRAY))
                );
                return response;
            }))
            .slot(slot(1, 2))
            .withGlint()
            .build();

        MenuItem remove = MenuItem.builder(Material.RED_WOOL)
            .name(Component.text("Remove a player", NamedTextColor.RED))
            .lore(Component.text("Currently authorised: " + settings.size(type), NamedTextColor.GRAY))
            .action(ClickAction.openSilent(() -> removeAuthorised(player, settings, type)))
            .slot(slot(1, 4))
            .withGlint()
            .build();

        MenuItem reset = MenuItem.builder(Material.TNT)
            .name(Component.text("Reset", NamedTextColor.RED, TextDecoration.BOLD))
            .lore(Component.text("Un-authorise everyone from " + type.name(), NamedTextColor.RED))
            .action(ClickAction.openSilent(() -> {
                auth.authMap.put(playerUUID, settings.clear(type));
                authPending.add(playerUUID);
                player.sendMessage(Component.text("Successfully cleared your auth list for " + type.name(), NamedTextColor.GREEN));
                return createAuthMenu(player);
            }))
            .slot(slot(1, 6))
            .withGlint()
            .build();

        menu.addItem(add).addItem(remove).addItem(reset).addItem(createBackButton(() -> createAuthMenu(player)));
        return menu.build();
    }

    private MenuInventory removeAuthorised(Player player, AuthSettings settings, AuthSettings.Type type) {
        MenuInventory.Builder menu = MenuInventory.builder()
            .title(Component.text("Remove authorised players", NamedTextColor.RED, TextDecoration.BOLD));

        menu.addItem(createBackButton(() -> editAuthorisedMenu(player, settings, type)));
        int index = 0;
        Set<UUID> authorised = settings.authorised().get(type);
        if (authorised == null || authorised.isEmpty()) {
            MenuItem empty = MenuItem.builder(Material.WRITTEN_BOOK)
                .name(Component.text("No authorised players", NamedTextColor.RED))
                .lore(Component.text("There is noone to remove.", NamedTextColor.GRAY))
                .slot(slot(0, 4))
                .build();

            return menu.size(1).addItem(empty).build();
        }
        menu.size(authorised.size() + 1);
        UUID playerUUID = player.getUniqueId();
        for (UUID uuid : authorised) {
            String name = Optional.ofNullable(getNameFromUUID(uuid)).orElse(uuid.toString());
            MenuItem head = MenuItem.builder(Material.PLAYER_HEAD)
                .skullOwner(uuid)
                .name(Component.text(name, NamedTextColor.GREEN))
                .lore(Component.text("Click to remove " + name + "'s authorisation for " + type.name(), NamedTextColor.RED))
                .action(ClickAction.openSilent(() -> {
                    auth.authMap.put(playerUUID, settings.remove(type, uuid));
                    authPending.add(playerUUID);
                    player.sendMessage(Component.text("Successfully un-authorised player " + name + " for " + type.name(), NamedTextColor.GREEN)
                        .appendNewline()
                        .append(Component.text("(UUID: " + uuid + ")", NamedTextColor.GRAY))
                    );

                    return removeAuthorised(player, settings, type); // Refresh the inventory
                }))
                .slot(index)
                .build();

            menu.addItem(head);
            if (++index > 50) break;
        }

        return menu.build();
    }

    private Pair<UUID, String> parsePlayer(String text) throws IllegalStateException {
        UUID uuid;
        try {
            uuid = UUID.fromString(text);
        } catch (IllegalArgumentException ignored) {
            uuid = getUUIDFromName(text);
        }
        if (uuid == null) {
            throw new IllegalStateException("Could not resolve that username or UUID!");
        }

        String name = getNameFromUUID(uuid);
        if (name == null) {
            throw new IllegalStateException("No username found");
        }
        return new Pair<>(uuid, name);
    }

    private String getNameFromUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        Resident res = TownyAPI.getInstance().getResident(uuid);
        return res != null ? res.getName() : null;
    }

    private UUID getUUIDFromName(String name) {
        if (name == null) {
            return null;
        }
        Resident res = TownyAPI.getInstance().getResident(name);
        return res != null ? res.getUUID() : null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInputStart(StartAwaitingInputEvent event) {
        signInputs.add(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        signInputs.remove(uuid);
        authPending.remove(uuid);
        optOutPending.remove(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof MenuInventory)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        player.getScheduler().runDelayed(plugin, t -> {
            UUID uuid = player.getUniqueId();
            if (signInputs.contains(uuid) || player.getOpenInventory().getTopInventory().getHolder(false) instanceof MenuInventory) {
                return; // Player is still editing
            }
            if (optOutPending.contains(uuid)) {
                optOutPending.remove(uuid);
                optOut.saveOptOut(uuid);
                player.sendMessage(Component.text("Successfully saved your new API opt out settings", NamedTextColor.GREEN));
            }
            if (authPending.contains(uuid)) {
                authPending.remove(uuid);
                auth.saveAuthSettings(uuid);
                player.sendMessage(Component.text("Successfully saved your new API authorisation settings", NamedTextColor.GREEN));
            }
        }, () -> {
            optOutPending.remove(player.getUniqueId());
            authPending.remove(player.getUniqueId());
        }, 50);
    }

    private Slot slot(int fromTop, int fromLeft) {
        return new SlotAnchor(VerticalAnchor.fromTop(fromTop), HorizontalAnchor.fromLeft(fromLeft));
    }
}
