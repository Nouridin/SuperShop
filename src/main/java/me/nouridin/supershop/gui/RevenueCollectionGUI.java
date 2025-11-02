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

package me.nouridin.supershop.gui;

import me.nouridin.supershop.util.MessageUtils;
import me.nouridin.supershop.models.Shop;
import me.nouridin.supershop.supershop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RevenueCollectionGUI extends BaseGUI {
    
    private final Shop shop;
    private int currentPage;
    private final int itemsPerPage = 21; // 3 rows of 7 items
    
    public RevenueCollectionGUI(supershop plugin, Player player, Shop shop) {
        super(plugin, player, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-title"), 54);
        this.shop = shop;
        this.currentPage = 0;
    }
    
    @Override
    protected void setupInventory() {
        fillBorder(Material.YELLOW_STAINED_GLASS_PANE);
        
        setupRevenueInfo();
        displayRevenueItems();
        setupControls();
        setupNavigation();
    }
    
    private void setupRevenueInfo() {
        ItemStack revenueInfo = createInfoItem(
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-info-title"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-info-shop", shop.getOwnerName()),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-info-location", shop.getCoordinatesString()),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-info-revenue-stacks", String.valueOf(shop.getRevenueItemCount())),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-info-total-items", String.valueOf(shop.getTotalRevenueItems())),
            "",
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-info-collect-all-tip"));
        inventory.setItem(4, revenueInfo);
    }
    
    private void displayRevenueItems() {
        List<ItemStack> revenueItems = shop.getRevenue();
        
        if (revenueItems.isEmpty()) {
            ItemStack noRevenue = createInfoItem(
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-no-revenue-title"),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-no-revenue-line1"),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-no-revenue-line2"),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-no-revenue-line3"));
            inventory.setItem(22, noRevenue);
            return;
        }
        
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, revenueItems.size());
        
        // Display items in slots 10-16, 19-25, 28-34
        int[] itemSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int slotIndex = 0;
        
        for (int i = startIndex; i < endIndex && slotIndex < itemSlots.length; i++) {
            ItemStack revenueItem = revenueItems.get(i);
            ItemStack displayItem = createRevenueItemDisplay(revenueItem);
            inventory.setItem(itemSlots[slotIndex], displayItem);
            slotIndex++;
        }
    }
    
    private ItemStack createRevenueItemDisplay(ItemStack revenueItem) {
        ItemStack displayItem = revenueItem.clone();
        ItemMeta meta = displayItem.getItemMeta();
        
        if (meta != null) {
            String itemName = getItemDisplayName(revenueItem);
            meta.setDisplayName(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-item-display-name", itemName)));
            
            List<String> lore = new ArrayList<>();
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-item-amount", String.valueOf(revenueItem.getAmount()))));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-item-type", revenueItem.getType().name().toLowerCase().replace("_", " "))));
            lore.add("");
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-item-source")));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-item-collect-tip")));
            
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }
        
        return displayItem;
    }
    
    private String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return plugin.getMessageUtils().stripColor(item.getItemMeta().getDisplayName());
        }
        return item.getType().name().toLowerCase().replace("_", " ");
    }
    
    private void setupControls() {
        if (shop.hasRevenue()) {
            // Collect all button
            ItemStack collectAll = createButton(Material.EMERALD,
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-collect-all-name"),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-collect-all-lore1", String.valueOf(shop.getRevenueItemCount())),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-collect-all-lore2", String.valueOf(shop.getTotalRevenueItems())),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-collect-all-lore3"));
            inventory.setItem(49, collectAll);
        }
    }
    
    private void setupNavigation() {
        List<ItemStack> revenueItems = shop.getRevenue();
        int totalPages = (int) Math.ceil((double) revenueItems.size() / itemsPerPage);
        
        // Previous page
        if (currentPage > 0) {
            ItemStack prevPage = createButton(Material.ARROW, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-previous-page"));
            inventory.setItem(45, prevPage);
        }
        
        // Page info
        if (totalPages > 1) {
            ItemStack pageInfo = createInfoItem(plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-page-info", String.valueOf(currentPage + 1), String.valueOf(totalPages)));
            inventory.setItem(48, pageInfo);
        }
        
        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = createButton(Material.ARROW, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-next-page"));
            inventory.setItem(53, nextPage);
        }
        
        // Back button
        ItemStack back = createBackButton();
        inventory.setItem(46, back);
        
        // Close button
        ItemStack close = createCloseButton();
        inventory.setItem(52, close);
    }
    
    @Override
    protected void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        
        switch (slot) {
            case 49: // Collect all
                handleCollectAll();
                break;
            case 45: // Previous page
                if (currentPage > 0) {
                    currentPage--;
                    refresh();
                }
                break;
            case 53: // Next page
                List<ItemStack> revenueItems = shop.getRevenue();
                int totalPages = (int) Math.ceil((double) revenueItems.size() / itemsPerPage);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    refresh();
                }
                break;
            case 46: // Back
                close();
                new ShopManagementGUI(plugin, player, shop).open();
                break;
            case 52: // Close
                close();
                break;
        }
    }
    
    private void handleCollectAll() {
        if (!shop.hasRevenue()) {
            plugin.getMessageUtils().sendMessage(player, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui-revenuecollection-error-no-revenue"));
            return;
        }
        
        if (plugin.getShopManager().collectRevenue(shop.getShopId(), player)) {
            close();
            new ShopManagementGUI(plugin, player, shop).open();
        }
    }
}