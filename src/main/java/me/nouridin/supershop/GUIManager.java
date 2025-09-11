package me.nouridin.supershop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIManager implements Listener {

    private final Map<UUID, BaseGUI> openGuis = new ConcurrentHashMap<>();

    public GUIManager(supershop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void registerGUI(Player player, BaseGUI gui) {
        openGuis.put(player.getUniqueId(), gui);
    }

    public void unregisterGUI(Player player) {
        openGuis.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        BaseGUI gui = openGuis.get(event.getWhoClicked().getUniqueId());
        if (gui != null) {
            gui.onInventoryClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        BaseGUI gui = openGuis.get(event.getPlayer().getUniqueId());
        if (gui != null) {
            gui.onInventoryClose(event);
        }
    }
}