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

package me.nouridin.Search;

import me.nouridin.supershop.MessageUtils;
import me.nouridin.supershop.supershop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public class SearchBookListener implements Listener {
    
    private final supershop plugin;
    
    public SearchBookListener(supershop plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!isSearchBook(item)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!plugin.getConfigManager().isSearchBookEnabled()) {
            MessageUtils.sendMessage(player, "&cSearch Book functionality is currently disabled!");
            return;
        }
        
        if (plugin.getConfigManager().isSearchBookRequirePermission() && 
            !player.hasPermission("supershop.searchbook")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to use the Search Book!");
            return;
        }
        
        showSearchBookInterface(player);
    }
    
    private boolean isSearchBook(ItemStack item) {
        if (item == null || item.getType() != Material.WRITTEN_BOOK) {
            return false;
        }
        
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        
        String displayName = MessageUtils.stripColor(item.getItemMeta().getDisplayName());
        return displayName.equals("Search Book");
    }
    
    private void showSearchBookInterface(Player player) {
        new SearchBookGUI(plugin, player).open();
    }
}