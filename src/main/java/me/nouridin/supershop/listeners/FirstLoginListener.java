package me.nouridin.supershop.listeners;

import me.nouridin.supershop.supershop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FirstLoginListener implements Listener {

    private final supershop plugin;

    public FirstLoginListener(supershop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if the player has received the first login message before
        if (!plugin.getDatabaseManager().hasReceivedFirstLoginMessage(player.getUniqueId())) {
            // Send the welcome message
            plugin.getMessageUtils().sendMessage(player, "first-login-welcome");

            // Record that the player has received the message
            plugin.getDatabaseManager().recordFirstLoginMessage(player.getUniqueId());
        }
    }
}