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
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopBrowseGUI extends BaseGUI {

    private final Shop shop;
    private int currentPage;
    private final int itemsPerPage = 21; // 3 rows of 7 items

    public ShopBrowseGUI(supershop plugin, Player player, Shop shop) {
        super(plugin, player, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.shopbrowse.title", shop.getOwnerName()), 54);
        this.shop = shop;
        this.currentPage = 0;
    }

    @Override
    protected void setupInventory() {
        fillBorder(Material.CYAN_STAINED_GLASS_PANE);

        setupShopInfo();
        displayItems();
        setupNavigation();
    }

    private void setupShopInfo() {
        UUID playerUUID = player.getUniqueId();
        ItemStack shopInfo = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shop-info.title", shop.getOwnerName()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shop-info.location", shop.getCoordinatesString()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shop-info.world", shop.getWorldName()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shop-info.items-for-sale", String.valueOf(getAvailableItems().size())),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shop-info.status"));
        inventory.setItem(4, shopInfo);
    }

    private void displayItems() {
        List<ShopItem> availableItems = getAvailableItems();
        UUID playerUUID = player.getUniqueId();

        if (availableItems.isEmpty()) {
            ItemStack noItems = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.no-items.title"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.no-items.lore1"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.no-items.lore2"));
            inventory.setItem(22, noItems);
            return;
        }

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, availableItems.size());

        // Display items in slots 10-16, 19-25, 28-34
        int[] itemSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int slotIndex = 0;

        for (int i = startIndex; i < endIndex && slotIndex < itemSlots.length; i++) {
            ShopItem shopItem = availableItems.get(i);
            ItemStack displayItem = createShopItemDisplay(shopItem);
            inventory.setItem(itemSlots[slotIndex], displayItem);
            slotIndex++;
        }
    }

    private ItemStack createShopItemDisplay(ShopItem shopItem) {
        ItemStack displayItem = shopItem.getItemStack().clone();
        ItemMeta meta = displayItem.getItemMeta();
        UUID playerUUID = player.getUniqueId();

        if (meta != null) {
            String itemName = shopItem.getFormattedItemName();
            meta.setDisplayName(plugin.getMessageUtils().colorize("&e" + itemName));

            List<String> lore = new ArrayList<>();
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.item.quantity", String.valueOf(shopItem.getQuantity()))));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.item.price", shopItem.getFormattedPrice())));

            if (shopItem.hasDescription()) {
                lore.add("");
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.item.description")));
                lore.add(plugin.getMessageUtils().colorize("&f" + shopItem.getDescription()));
            }

            lore.add("");

            // Check if player can afford
            if (canPlayerAfford(shopItem)) {
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.item.can-afford.buy-one")));
                if (shopItem.getQuantity() > 1) {
                    lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.item.can-afford.buy-all", String.valueOf(shopItem.getQuantity()))));
                }
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.item.can-afford.buy-custom")));
            } else {
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.item.cannot-afford.title")));
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.item.cannot-afford.needs", shopItem.getFormattedPrice())));
            }

            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }

        return displayItem;
    }

    private void setupNavigation() {
        List<ShopItem> availableItems = getAvailableItems();
        int totalPages = (int) Math.ceil((double) availableItems.size() / itemsPerPage);
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
        } else if (!availableItems.isEmpty()) {
            ItemStack resultInfo = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shopping-at", shop.getOwnerName()),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.total-items", String.valueOf(availableItems.size())));
            inventory.setItem(49, resultInfo);
        }

        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = createButton(Material.ARROW, plugin.getLocaleManager().getMessage(playerUUID, "gui.next-page"));
            inventory.setItem(53, nextPage);
        }

        // Close button
        ItemStack close = createCloseButton();
        inventory.setItem(46, close);

        // Shop info button
        ItemStack shopDetails = createButton(Material.COMPASS, plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shop-details.title"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shop-details.owner", shop.getOwnerName()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shop-details.location", shop.getCoordinatesString()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shop-details.world", shop.getWorldName()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopbrowse.shop-details.created", plugin.getMessageUtils().formatDuration(System.currentTimeMillis() - shop.getCreatedAt(), playerUUID)));
        inventory.setItem(47, shopDetails);
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        // Handle navigation
        switch (slot) {
            case 45: // Previous page
                if (currentPage > 0) {
                    currentPage--;
                    refresh();
                }
                break;
            case 53: // Next page
                List<ShopItem> availableItems = getAvailableItems();
                int totalPages = (int) Math.ceil((double) availableItems.size() / itemsPerPage);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    refresh();
                }
                break;
            case 46: // Close
                close();
                break;
            case 47: // Shop details (just close, info already shown)
                break;
            default:
                // Handle item purchase
                ShopItem shopItem = getShopItemFromSlot(slot);
                if (shopItem != null) {
                    handleItemPurchase(shopItem, event);
                }
                break;
        }
    }

    private void handleItemPurchase(ShopItem shopItem, InventoryClickEvent event) {
        if (!canPlayerAfford(shopItem)) {
            plugin.getMessageUtils().sendMessage(player, "gui.shopbrowse.error.cannot-afford");
            plugin.getMessageUtils().sendMessage(player, "gui.shopbrowse.error.needs", shopItem.getFormattedPrice());
            return;
        }

        int quantity = 1;

        if (event.isShiftClick()) {
            // Buy all available
            quantity = shopItem.getQuantity();
        } else if (event.isRightClick()) {
            // Custom amount - open input GUI
            close();
            new PurchaseAmountGUI(plugin, player, shop, shopItem).open();
            return;
        }

        // Process purchase
        if (plugin.getShopManager().processPurchase(player, shop, shopItem, quantity)) {
            refresh(); // Refresh to update quantities
        }
    }

    private ShopItem getShopItemFromSlot(int slot) {
        int[] itemSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < itemSlots.length; i++) {
            if (itemSlots[i] == slot) {
                List<ShopItem> availableItems = getAvailableItems();
                int itemIndex = currentPage * itemsPerPage + i;
                if (itemIndex < availableItems.size()) {
                    return availableItems.get(itemIndex);
                }
                break;
            }
        }

        return null;
    }

    private List<ShopItem> getAvailableItems() {
        return shop.getItems().stream()
            .filter(ShopItem::isAvailable)
            .collect(Collectors.toList());
    }

    private boolean canPlayerAfford(ShopItem shopItem) {
        for (ItemStack priceItem : shopItem.getPriceItems()) {
            if (!hasItem(player, priceItem, priceItem.getAmount())) {
                return false;
            }
        }
        return true;
    }

    private boolean hasItem(Player player, ItemStack item, int amount) {
        int count = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.isSimilar(item)) {
                count += invItem.getAmount();
                if (count >= amount) {
                    return true;
                }
            }
        }
        return false;
    }
}
