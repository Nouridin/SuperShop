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
import me.nouridin.supershop.supershop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopSelectionGUI extends BaseGUI {

    private final List<Shop> shops;
    private int currentPage;
    private final int shopsPerPage = 21; // 3 rows of 7 shops

    public ShopSelectionGUI(supershop plugin, Player player, List<Shop> shops) {
        super(plugin, player, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.shopselection.title"), 54);
        this.shops = shops;
        this.currentPage = 0;
    }

    @Override
    protected void setupInventory() {
        fillBorder(Material.BLUE_STAINED_GLASS_PANE);

        setupHeader();
        displayShops();
        setupNavigation();
    }

    private void setupHeader() {
        UUID playerUUID = player.getUniqueId();
        ItemStack header = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.header.title"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.header.total-shops", String.valueOf(shops.size())),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.header.lore1"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.header.lore2"));
        inventory.setItem(4, header);
    }

    private void displayShops() {
        UUID playerUUID = player.getUniqueId();
        if (shops.isEmpty()) {
            ItemStack noShops = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.no-shops.title"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.no-shops.lore1"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.no-shops.lore2"));
            inventory.setItem(22, noShops);
            return;
        }

        int startIndex = currentPage * shopsPerPage;
        int endIndex = Math.min(startIndex + shopsPerPage, shops.size());

        // Display shops in slots 10-16, 19-25, 28-34
        int[] shopSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int slotIndex = 0;

        for (int i = startIndex; i < endIndex && slotIndex < shopSlots.length; i++) {
            Shop shop = shops.get(i);
            ItemStack shopDisplay = createShopDisplay(shop, i + 1);
            inventory.setItem(shopSlots[slotIndex], shopDisplay);
            slotIndex++;
        }
    }

    private ItemStack createShopDisplay(Shop shop, int shopNumber) {
        Material displayMaterial = shop.isActive() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
        UUID playerUUID = player.getUniqueId();
        String status = shop.isActive() ? plugin.getLocaleManager().getMessage(playerUUID, "gui.active") : plugin.getLocaleManager().getMessage(playerUUID, "gui.inactive");

        ItemStack shopItem = new ItemStack(displayMaterial);
        ItemMeta meta = shopItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.item.name", String.valueOf(shopNumber), shop.getCoordinatesString())));

            List<String> lore = new ArrayList<>();
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.item.status", status)));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.item.world", shop.getWorldName())));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.item.items-for-sale", String.valueOf(shop.getItems().size()))));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.item.total-stock", String.valueOf(getTotalStock(shop)))));

            if (shop.hasRevenue()) {
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.item.revenue-waiting", String.valueOf(shop.getTotalRevenueItems()))));
            } else {
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.item.no-revenue")));
            }

            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.item.created",
                plugin.getMessageUtils().formatDuration(System.currentTimeMillis() - shop.getCreatedAt(), playerUUID))));
            lore.add("");
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.item.click-to-manage")));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.item.manage-lore")));

            meta.setLore(lore);
            shopItem.setItemMeta(meta);
        }

        return shopItem;
    }

    private void setupNavigation() {
        int totalPages = (int) Math.ceil((double) shops.size() / shopsPerPage);
        UUID playerUUID = player.getUniqueId();

        // Previous page
        if (currentPage > 0) {
            ItemStack prevPage = createButton(Material.ARROW, plugin.getLocaleManager().getMessage(playerUUID, "gui.previous-page"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.page-info-short", String.valueOf(currentPage), String.valueOf(totalPages)));
            inventory.setItem(45, prevPage);
        }

        // Page info
        if (totalPages > 1) {
            ItemStack pageInfo = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.page-info", String.valueOf(currentPage + 1), String.valueOf(totalPages)),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.showing-shops", String.valueOf(Math.min(shopsPerPage, shops.size() - (currentPage * shopsPerPage))), String.valueOf(shops.size())));
            inventory.setItem(49, pageInfo);
        } else {
            ItemStack shopInfo = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.your-shops.title"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.your-shops.total", String.valueOf(shops.size())),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.your-shops.lore"));
            inventory.setItem(49, shopInfo);
        }

        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = createButton(Material.ARROW, plugin.getLocaleManager().getMessage(playerUUID, "gui.next-page"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.page-info-short", String.valueOf(currentPage + 2), String.valueOf(totalPages)));
            inventory.setItem(53, nextPage);
        }

        // Close button
        ItemStack close = createCloseButton();
        inventory.setItem(46, close);

        // Help button
        ItemStack help = createButton(Material.BOOK, plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.help-button.title"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.help-button.lore1"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.help-button.lore2"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.help-button.lore3"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.help-button.lore4"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.help-button.lore5"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.shopselection.help-button.lore6"));
        inventory.setItem(52, help);
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        switch (slot) {
            case 45: // Previous page
                if (currentPage > 0) {
                    currentPage--;
                    refresh();
                }
                break;
            case 53: // Next page
                int totalPages = (int) Math.ceil((double) shops.size() / shopsPerPage);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    refresh();
                }
                break;
            case 46: // Close
                close();
                break;
            case 52: // Help
                // Help button clicked, just show the tooltip
                break;
            default:
                // Handle shop selection
                Shop selectedShop = getShopFromSlot(slot);
                if (selectedShop != null) {
                    handleShopSelection(selectedShop);
                }
                break;
        }
    }

    private Shop getShopFromSlot(int slot) {
        int[] shopSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < shopSlots.length; i++) {
            if (shopSlots[i] == slot) {
                int shopIndex = currentPage * shopsPerPage + i;
                if (shopIndex < shops.size()) {
                    return shops.get(shopIndex);
                }
                break;
            }
        }

        return null;
    }

    private void handleShopSelection(Shop shop) {
        close();
        plugin.getMessageUtils().sendMessage(player, "gui.shopselection.opening-management", shop.getCoordinatesString());
        new ShopManagementGUI(plugin, player, shop).open();
    }

    private int getTotalStock(Shop shop) {
        return shop.getItems().stream()
            .filter(item -> item.isAvailable())
            .mapToInt(item -> item.getQuantity())
            .sum();
    }
}
