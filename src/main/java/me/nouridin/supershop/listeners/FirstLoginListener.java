/*
 * Copyright 2025 Nouridin Elhofy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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