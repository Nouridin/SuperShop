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