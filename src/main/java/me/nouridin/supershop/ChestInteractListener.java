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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

public class ChestInteractListener implements Listener {
    
    private final supershop plugin;
    
    public ChestInteractListener(supershop plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.CHEST) {
            return;
        }

        Player player = event.getPlayer();
        Shop shop = plugin.getShopManager().getShopAtLocation(clickedBlock.getLocation());

        if (shop == null) {
            return;
        }

        event.setCancelled(true);

        if (shop.getOwnerId().equals(player.getUniqueId())) {
            // Owner can always open, even if inactive
            if (player.isSneaking()) {
                showQuickShopInfo(player, shop);
            } else {
                showShopManagement(player, shop);
            }
        } else {
            // Non-owners cannot open inactive shops
            if (!shop.isActive()) {
                MessageUtils.sendMessage(player, "&cThis shop is currently inactive!");
                return;
            }

            // Always show the shop GUI, even if empty
            showShopBrowsing(player, shop);
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.CHEST) {
            return;
        }
        
        Shop shop = plugin.getShopManager().getShopAtLocation(block.getLocation());
        if (shop == null) {
            return;
        }
        
        Player player = event.getPlayer();
        
        if (!shop.getOwnerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            MessageUtils.sendMessage(player, "&cYou cannot break a shop chest that doesn't belong to you!");
            MessageUtils.sendMessage(player, "&7Shop owner: &e" + shop.getOwnerName());
            MessageUtils.sendMessage(player, "&7Use '/shop force remove' if you're an operator.");
            return;
        }
        
        if (shop.getOwnerId().equals(player.getUniqueId())) {
            if (!player.isSneaking()) {
                event.setCancelled(true);
                MessageUtils.sendMessage(player, "&cHold SHIFT while breaking to confirm shop removal!");
                MessageUtils.sendMessage(player, "&7This will permanently delete your shop and all its items.");
                return;
            }
            
            plugin.getShopManager().removeShop(shop.getShopId(), player, block);
        }
    }
    
    private void showQuickShopInfo(Player player, Shop shop) {
        MessageUtils.sendMessage(player, "&7&m" + MessageUtils.createSeparator('-', 30));
        MessageUtils.sendMessage(player, "&6&lQuick Shop Info");
        MessageUtils.sendMessage(player, "&7Items for sale: &e" + shop.getItems().size());
        MessageUtils.sendMessage(player, "&7Total stock: &e" + getTotalStock(shop));
        MessageUtils.sendMessage(player, "&7Status: " + (shop.isActive() ? "&aActive" : "&cInactive"));
        MessageUtils.sendMessage(player, "&7Last updated: &e" + 
            MessageUtils.formatDuration(System.currentTimeMillis() - shop.getLastUpdated()) + " ago");
        MessageUtils.sendMessage(player, "&7&m" + MessageUtils.createSeparator('-', 30));
        MessageUtils.sendMessage(player, "&7Right-click normally to manage shop");
    }
    
    private void showShopManagement(Player player, Shop shop) {
        new ShopManagementGUI(plugin, player, shop).open();
    }
    
    private void showShopBrowsing(Player player, Shop shop) {
        new ShopBrowseGUI(plugin, player, shop).open();
    }
    
    private int getTotalStock(Shop shop) {
        return shop.getItems().stream()
            .filter(item -> item.isAvailable())
            .mapToInt(item -> item.getQuantity())
            .sum();
    }
}