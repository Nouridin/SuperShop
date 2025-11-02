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

import me.nouridin.supershop.gui.ShopBrowseGUI;
import me.nouridin.supershop.gui.ShopManagementGUI;
import me.nouridin.supershop.models.Shop;
import me.nouridin.supershop.supershop;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
                plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.shop-inactive");
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
            plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.cannot-break-not-owner");
            plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.shop-owner", shop.getOwnerName());
            plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.force-remove-tip");
            return;
        }

        if (shop.getOwnerId().equals(player.getUniqueId())) {
            if (!player.isSneaking()) {
                event.setCancelled(true);
                plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.shift-to-confirm-removal");
                plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.removal-warning");
                return;
            }

            plugin.getShopManager().removeShop(shop.getShopId(), player, block);
        }
    }

    private void showQuickShopInfo(Player player, Shop shop) {
        plugin.getMessageUtils().sendMessage(player, plugin.getMessageUtils().createSeparator('-', 30));
        plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.quick-info.title");
        plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.quick-info.items-for-sale", String.valueOf(shop.getItems().size()));
        plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.quick-info.total-stock", String.valueOf(getTotalStock(shop)));
        plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.quick-info.status", (shop.isActive() ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.active") : plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.inactive")));
        plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.quick-info.last-updated",
            plugin.getMessageUtils().formatDuration(System.currentTimeMillis() - shop.getLastUpdated(), player.getUniqueId()));
        plugin.getMessageUtils().sendMessage(player, plugin.getMessageUtils().createSeparator('-', 30));
        plugin.getMessageUtils().sendMessage(player, "listener.chest-interact.quick-info.manage-tip");
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
