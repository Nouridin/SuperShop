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

package me.nouridin.supershop.managers;

import me.nouridin.supershop.gui.BaseGUI;
import me.nouridin.supershop.supershop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

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