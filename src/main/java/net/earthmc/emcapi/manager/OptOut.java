package net.earthmc.emcapi.manager;

import dev.warriorrr.inventories.gui.MenuInventory;
import dev.warriorrr.inventories.gui.MenuItem;
import dev.warriorrr.inventories.gui.action.ClickAction;
import net.earthmc.emcapi.EMCAPI;
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
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OptOut implements Listener {
    private final Map<UUID, OptOutSettings> opted = new ConcurrentHashMap<>();
    private final Set<UUID> pending = ConcurrentHashMap.newKeySet();
    private final EMCAPI plugin;

    public OptOut(final EMCAPI plugin) {
        this.plugin = plugin;
    }

    public boolean playerOptedOut(UUID uuid, OptOutType type) {
        return opted.containsKey(uuid) && opted.get(uuid).optedOut(type);
    }

    public @Nullable OptOutSettings getPlayerSettings(UUID uuid) {
        return opted.get(uuid);
    }

    public void saveOptOut(UUID uuid) {
        OptOutSettings settings = opted.get(uuid);
        if (settings == null) {
            return;
        }

        if (!plugin.getDatabase().ready()) {
            plugin.getSLF4JLogger().warn("The database has not been properly configured yet, opt out changes will not persist across restarts.");
            return;
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            boolean delete = settings.isRedundant();
            try (Connection connection = plugin.getDatabase().getConnection();
                 PreparedStatement ps = connection.prepareStatement(delete ? "DELETE FROM opt_out WHERE uuid = ?"
                : "INSERT INTO opt_out (uuid, override_all, towny_resident, online_status, quickshops, mcmmo_stats) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "override_all = VALUES(override_all), " +
                     "towny_resident = VALUES(towny_resident), " +
                     "online_status = VALUES(online_status), " +
                     "quickshops = VALUES(quickshops), " +
                     "mcmmo_stats = VALUES(mcmmo_stats)"
            )) {
                ps.setString(1, uuid.toString());
                if (!delete) {
                    ps.setBoolean(2, settings.override());
                    ps.setBoolean(3, settings.townyResident());
                    ps.setBoolean(4, settings.onlineStatus());
                    ps.setBoolean(5, settings.quickShops());
                    ps.setBoolean(6, settings.mcmmo());
                }

                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getSLF4JLogger().warn("Failed to update opt out status for {}", uuid, e);
            }
        });
    }

    public void loadOptOut() {
        try (final Connection connection = plugin.getDatabase().getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM opt_out"); final ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    OptOutSettings settings = new OptOutSettings(rs.getBoolean("override_all"), rs.getBoolean("towny_resident"), rs.getBoolean("online_status"), rs.getBoolean("quickshops"), rs.getBoolean("mcmmo_stats"));
                    opted.put(uuid, settings);
                } catch (IllegalArgumentException e) {
                    plugin.getSLF4JLogger().warn("Invalid uuid format '{}' for value in row of table opt_out", rs.getString("uuid"));
                } catch (SQLException e) {
                    plugin.getSLF4JLogger().warn("SQLException while loading opt out", e);
                }
            }
        } catch (SQLException e) {
            plugin.getSLF4JLogger().warn("Failed to load opted out players", e);
        }
    }

    public void openEditor(Player player) {
        OptOutSettings settings = opted.getOrDefault(player.getUniqueId(), OptOutSettings.DEFAULT);
        MenuInventory.Builder menu = MenuInventory.builder()
            .title(Component.text("Manage your API privacy", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
            .rows(3);

        boolean override = settings.override();
        Component name = override ? Component.text("Override: All data disabled.", NamedTextColor.RED, TextDecoration.BOLD)
            : Component.text("Override is disabled. Data is shared per-feature.", NamedTextColor.GREEN);
        Component lore = override ? Component.text("Click to disable your override settings", NamedTextColor.DARK_GREEN)
            : Component.text("Enabling this will override your other preferences", NamedTextColor.DARK_RED);

        menu.addItem(MenuItem.builder(Material.BARRIER)
            .slot(4)
            .name(name)
            .lore(lore)
            .withGlint(override)
            .action(ClickAction.run(() -> {
                opted.put(player.getUniqueId(), settings.override(!override));
                pending.add(player.getUniqueId());
                openEditor(player);
            }))
            .build());

        boolean resident = settings.townyResident();
        name = Component.text("Resident Status", resident ? NamedTextColor.RED : NamedTextColor.GREEN);
        lore = resident ? Component.text("Click to show your resident data", NamedTextColor.DARK_GREEN)
            : Component.text("Click to hide your resident data", NamedTextColor.DARK_RED);
        menu.addItem(MenuItem.builder(Material.PLAYER_HEAD)
            .slot(10)
            .skullOwner(player.getUniqueId())
            .name(name)
            .lore(lore)
            .action(ClickAction.run(() -> {
                opted.put(player.getUniqueId(), settings.update(OptOutType.TOWNY_RESIDENT, !resident));
                pending.add(player.getUniqueId());
                openEditor(player);
            }))
            .build());

        boolean online = settings.onlineStatus();
        name = Component.text("Online Status", online ? NamedTextColor.RED : NamedTextColor.GREEN);
        lore = online ? Component.text("Click to re-enable your online status", NamedTextColor.DARK_GREEN)
            : Component.text("Click to disable your online status", NamedTextColor.DARK_RED);
        menu.addItem(MenuItem.builder(Material.EMERALD)
            .slot(12)
            .name(name)
            .lore(lore)
            .withGlint(online)
            .action(ClickAction.run(() -> {
                opted.put(player.getUniqueId(), settings.update(OptOutType.ONLINE_STATUS, !online));
                pending.add(player.getUniqueId());
                openEditor(player);
            }))
            .build());

        boolean shops = settings.quickShops();
        name = Component.text("QuickShops", shops ? NamedTextColor.RED : NamedTextColor.GREEN);
        lore = shops ? Component.text("Click to make your shop data public", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
            : Component.text("Click to make your shop data private, only accessible with your API key", NamedTextColor.DARK_RED, TextDecoration.BOLD);
        menu.addItem(MenuItem.builder(Material.CHEST)
            .slot(14)
            .name(name)
            .lore(lore)
            .withGlint(shops)
            .action(ClickAction.run(() -> {
                opted.put(player.getUniqueId(), settings.update(OptOutType.QUICKSHOPS, !shops));
                pending.add(player.getUniqueId());
                openEditor(player);
            }))
            .build());

        boolean mcmmo = settings.mcmmo();
        name = Component.text("McMMO Stats", mcmmo ? NamedTextColor.RED : NamedTextColor.GREEN);
        lore = mcmmo ? Component.text("Click to make your mcMMO stats public", NamedTextColor.DARK_GREEN)
            : Component.text("Click to make your mcMMO stats private, only accessible with your API key", NamedTextColor.DARK_RED);
        menu.addItem(MenuItem.builder(Material.DIAMOND_AXE)
            .slot(16)
            .name(name)
            .lore(lore)
            .withGlint(mcmmo)
            .action(ClickAction.run(() -> {
                opted.put(player.getUniqueId(), settings.update(OptOutType.MCMMO, !mcmmo));
                pending.add(player.getUniqueId());
                openEditor(player);
            }))
            .build());

        menu.build().open(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof MenuInventory)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        player.getScheduler().runDelayed(plugin, t -> {
            if (pending.contains(player.getUniqueId()) && !(player.getOpenInventory().getTopInventory().getHolder(false) instanceof MenuInventory)) {
                pending.remove(player.getUniqueId());
                saveOptOut(player.getUniqueId());
                player.sendMessage(Component.text("Successfully saved your new API opt out settings", NamedTextColor.GREEN));
            }
        }, null, 50);
    }
}
