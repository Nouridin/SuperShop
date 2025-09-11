package me.nouridin.supershop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatInputManager implements Listener {

    private final supershop plugin;
    private final Map<UUID, Consumer<String>> inputListeners = new ConcurrentHashMap<>();

    public ChatInputManager(supershop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void requestInput(Player player, Consumer<String> onInput) {
        inputListeners.put(player.getUniqueId(), onInput);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (inputListeners.containsKey(playerId)) {
            event.setCancelled(true);
            String input = event.getMessage();

            // Remove the listener before processing to prevent re-entry
            Consumer<String> listener = inputListeners.remove(playerId);

            // Run the logic on the main server thread
            plugin.getServer().getScheduler().runTask(plugin, () -> listener.accept(input));
        }
    }
}