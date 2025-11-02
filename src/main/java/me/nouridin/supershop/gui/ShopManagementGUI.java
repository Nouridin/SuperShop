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

import me.nouridin.supershop.models.Shop;
import me.nouridin.supershop.models.ShopItem;
import me.nouridin.supershop.supershop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopManagementGUI extends BaseGUI {

    private final Shop shop;
    private int currentPage;
    private final int itemsPerPage = 21; // 3 rows of 7 items

    public ShopManagementGUI(supershop plugin, Player player, Shop shop) {
        super(plugin, player, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.shopmanagement.title"), 54);
        this.shop = shop;
        this.currentPage = 0;
    }

    @Override
    protected void setupInventory() {
        fillBorder(Material.BLUE_STAINED_GLASS_PANE);

        setupShopInfo();
        setupControlButtons();
        displayItems();
        setupNavigation();
    }

    private void setupShopInfo() {
        UUID playerUUID = player.getUniqueId();
        ItemStack shopInfo = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.shop-info.title"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.shop-info.location", shop.getCoordinatesString()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.shop-info.world", shop.getWorldName()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.shop-info.items", String.valueOf(shop.getItems().size())),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.shop-info.status", (shop.isActive() ? plugin.getLocaleManager().getMessage(playerUUID, "gui.active") : plugin.getLocaleManager().getMessage(playerUUID, "gui.inactive"))),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.shop-info.revenue", (shop.hasRevenue() ? plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.shop-info.revenue-waiting", String.valueOf(shop.getRevenueItemCount())) : plugin.getLocaleManager().getMessage(playerUUID, "none"))));
        inventory.setItem(4, shopInfo);
    }

    private void setupControlButtons() {
        UUID playerUUID = player.getUniqueId();
        // Add item button
        ItemStack addItem = createButton(Material.EMERALD, plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.add-item-button.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.add-item-button.lore1"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.add-item-button.lore2"));
        inventory.setItem(0, addItem);

        // Toggle shop status
        ItemStack toggleStatus = createButton(
            shop.isActive() ? Material.REDSTONE : Material.EMERALD,
            shop.isActive() ? plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.deactivate-shop-button.name") : plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.activate-shop-button.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.toggle-status-button.lore", (shop.isActive() ? plugin.getLocaleManager().getMessage(playerUUID, "gui.deactivate") : plugin.getLocaleManager().getMessage(playerUUID, "gui.activate"))));
        inventory.setItem(1, toggleStatus);

        // Collect revenue button
        if (shop.hasRevenue()) {
            ItemStack collectRevenue = createButton(Material.GOLD_INGOT, plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.collect-revenue-button.name"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.collect-revenue-button.lore1", String.valueOf(shop.getRevenueItemCount())),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.collect-revenue-button.lore2", String.valueOf(shop.getTotalRevenueItems())),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.collect-revenue-button.lore3"));
            inventory.setItem(2, collectRevenue);
        } else {
            ItemStack noRevenue = createButton(Material.GRAY_DYE, plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.no-revenue-button.name"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.no-revenue-button.lore1"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.no-revenue-button.lore2"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.no-revenue-button.lore3"));
            inventory.setItem(2, noRevenue);
        }

        // Remove shop button - Enhanced for direct removal
        ItemStack removeShop = createButton(Material.BARRIER, plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.remove-shop-button.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.remove-shop-button.lore1"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.remove-shop-button.lore2"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.remove-shop-button.lore3"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.remove-shop-button.lore4"));
        inventory.setItem(8, removeShop);
    }

    private void displayItems() {
        List<ShopItem> items = shop.getItems();
        UUID playerUUID = player.getUniqueId();

        if (items.isEmpty()) {
            ItemStack noItems = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.no-items.title"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.no-items.lore"));
            inventory.setItem(22, noItems);
            return;
        }

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        // Display items in slots 10-16, 19-25, 28-34
        int[] itemSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int slotIndex = 0;

        for (int i = startIndex; i < endIndex && slotIndex < itemSlots.length; i++) {
            ShopItem shopItem = items.get(i);
            ItemStack displayItem = createShopItemDisplay(shopItem);
            inventory.setItem(itemSlots[slotIndex], displayItem);
            slotIndex++;
        }
    }

    private ItemStack createShopItemDisplay(ShopItem shopItem) {
        ItemStack displayItem = shopItem.getItemStack().clone();
        displayItem.setAmount(Math.min(64, Math.max(1, shopItem.getQuantity())));
        ItemMeta meta = displayItem.getItemMeta();
        UUID playerUUID = player.getUniqueId();

        if (meta != null) {
            String itemName = shopItem.getFormattedItemName();
            meta.setDisplayName(plugin.getMessageUtils().colorize("&e" + itemName));

            List<String> lore = new ArrayList<>();
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.item.quantity", String.valueOf(shopItem.getQuantity()))));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.item.price", shopItem.getFormattedPrice())));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.item.status", (shopItem.isAvailable() ? plugin.getLocaleManager().getMessage(playerUUID, "gui.available") : plugin.getLocaleManager().getMessage(playerUUID, "gui.unavailable")))));

            if (shopItem.hasDescription()) {
                lore.add("");
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.item.description")));
                lore.add(plugin.getMessageUtils().colorize("&f" + shopItem.getDescription()));
            }

            lore.add("");
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.item.left-click")));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.item.right-click")));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopmanagement.item.shift-click")));

            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }

        return displayItem;
    }

    private void setupNavigation() {
        List<ShopItem> items = shop.getItems();
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        UUID playerUUID = player.getUniqueId();

        // Previous page
        if (currentPage > 0) {
            ItemStack prevPage = createButton(Material.ARROW, plugin.getLocaleManager().getMessage(playerUUID, "gui.previous-page"));
            inventory.setItem(45, prevPage);
        }

        // Page info
        if (totalPages > 1) {
            ItemStack pageInfo = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.page-info", String.valueOf(currentPage + 1), String.valueOf(totalPages)));
            inventory.setItem(49, pageInfo);
        }

        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = createButton(Material.ARROW, plugin.getLocaleManager().getMessage(playerUUID, "gui.next-page"));
            inventory.setItem(53, nextPage);
        }

        // Close button
        ItemStack close = createCloseButton();
        inventory.setItem(46, close);
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        switch (slot) {
            case 0: // Add item
                handleAddItem();
                break;
            case 1: // Toggle status
                handleToggleStatus();
                break;
            case 2: // Collect revenue
                handleCollectRevenue();
                break;
            case 8: // Remove shop - Direct removal
                handleRemoveShop();
                break;
            case 45: // Previous page
                if (currentPage > 0) {
                    currentPage--;
                    refresh();
                }
                break;
            case 53: // Next page
                List<ShopItem> items = shop.getItems();
                int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    refresh();
                }
                break;
            case 46: // Close
                close();
                break;
            default:
                // Handle item management
                ShopItem shopItem = getShopItemFromSlot(slot);
                if (shopItem != null) {
                    handleItemManagement(shopItem, event);
                }
                break;
        }
    }

    private void handleAddItem() {
        close();
        new AddItemGUI(plugin, player, shop).open();
    }

    private void handleToggleStatus() {
        shop.setActive(!shop.isActive());
        plugin.getDatabaseManager().saveShop(shop);

        String status = shop.isActive() ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.activated") : plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.deactivated");
        plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-status-changed", status);

        refresh();
    }

    private void handleCollectRevenue() {
        if (!shop.hasRevenue()) {
            plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.error.no-revenue");
            return;
        }

        if (plugin.getShopManager().collectRevenue(shop.getShopId(), player)) {
            refresh(); // Refresh to update the revenue display
        }
    }

    private void handleRemoveShop() {
        close();

        // Directly remove the shop and return all items to the player
        removeShopAndReturnItems();
    }

    private void removeShopAndReturnItems() {
        List<ItemStack> allItems = new ArrayList<>();

        // Collect all shop items
        for (ShopItem shopItem : shop.getItems()) {
            if (shopItem.getQuantity() > 0) {
                ItemStack item = shopItem.getItemStack().clone();
                item.setAmount(shopItem.getQuantity());
                allItems.add(item);
            }
        }

        // Collect all revenue items
        if (shop.hasRevenue()) {
            allItems.addAll(shop.getRevenue());
        }

        // Try to get the chest block for storage
        org.bukkit.block.Block chestBlock = shop.getLocation().getBlock();
        boolean chestExists = chestBlock.getType() == Material.CHEST;

        if (chestExists) {
            // Use the existing shop removal method that stores items in chest
            if (plugin.getShopManager().removeShop(shop.getShopId(), player, chestBlock)) {
                plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.success");
                plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.items-in-chest");
            } else {
                plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.error.failed-to-remove");
                forceRemoveAndReturnItems(allItems);
            }
        } else {
            // Chest doesn't exist, remove shop and return items directly
            plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.error.chest-not-found");
            forceRemoveAndReturnItems(allItems);
        }
    }

    private void forceRemoveAndReturnItems(List<ItemStack> allItems) {
        // Force remove the shop from the system
        if (!plugin.getShopManager().forceRemoveShop(shop.getShopId(), player)) {
            plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.error.failed-to-remove-from-system");
            return;
        }

        if (allItems.isEmpty()) {
            plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.success-no-items");
            return;
        }

        int itemsToInventory = 0;
        int itemsDropped = 0;

        // Try to add items to player inventory first
        for (ItemStack item : allItems) {
            if (item == null || item.getType() == Material.AIR) continue;

            var leftover = player.getInventory().addItem(item);
            if (leftover.isEmpty()) {
                itemsToInventory += item.getAmount();
            } else {
                // Player inventory is full, drop items on ground
                for (ItemStack dropItem : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), dropItem);
                    itemsDropped += dropItem.getAmount();
                }
            }
        }

        // Notify player about item distribution
        plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.success");
        if (itemsToInventory > 0) {
            plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.items-returned", String.valueOf(itemsToInventory));
        }
        if (itemsDropped > 0) {
            plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.items-dropped", String.valueOf(itemsDropped));
            plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.pickup-items");
        }
        if (itemsToInventory == 0 && itemsDropped == 0) {
            plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.shop-removed.no-items-to-return");
        }
    }

    private void handleItemManagement(ShopItem shopItem, InventoryClickEvent event) {
        if (event.isShiftClick()) {
            // Toggle availability
            shopItem.setAvailable(!shopItem.isAvailable());
            plugin.getDatabaseManager().saveShopItem(shop.getShopId(), shopItem);

            String status = shopItem.isAvailable() ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.available") : plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.unavailable");
            plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.item-marked-as", status);

            refresh();
        } else if (event.isRightClick()) {
            // Remove item
            if (plugin.getShopManager().removeItemFromShop(shop.getShopId(), shopItem.getItemId(), player)) {

                // Give the item back to the player
                ItemStack item = shopItem.toItemStack(); // assuming you have this method
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);

                // Drop on ground if inventory is full
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }

                plugin.getMessageUtils().sendMessage(player, "gui.shopmanagement.item-returned-to-inventory");

                refresh();
            }
        } else {
            // Edit item
            close();
            new EditItemGUI(plugin, player, shop, shopItem).open();
        }
    }

    private ShopItem getShopItemFromSlot(int slot) {
        int[] itemSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < itemSlots.length; i++) {
            if (itemSlots[i] == slot) {
                List<ShopItem> items = shop.getItems();
                int itemIndex = currentPage * itemsPerPage + i;
                if (itemIndex < items.size()) {
                    return items.get(itemIndex);
                }
                break;
            }
        }

        return null;
    }
}
