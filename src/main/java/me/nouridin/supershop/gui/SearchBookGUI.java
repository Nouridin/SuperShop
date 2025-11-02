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
import me.nouridin.supershop.util.SearchResult;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SearchBookGUI extends BaseGUI {

    private final Player searcher;
    private List<SearchResult> allResults;
    private List<SearchResult> filteredResults;
    private int currentPage;
    private final int itemsPerPage = 28; // 4 rows of 7 items

    // Search and filter settings
    private String searchQuery = "";
    private SortType sortType = SortType.DISTANCE;
    private boolean showOnlyAffordable = false;

    public enum SortType {
        DISTANCE("gui.search.sort.distance"),
        NAME_AZ("gui.search.sort.name-az"),
        NAME_ZA("gui.search.sort.name-za"),
        PRICE_LOW("gui.search.sort.price-low"),
        PRICE_HIGH("gui.search.sort.price-high"),
        QUANTITY("gui.search.sort.quantity");

        private final String localeKey;

        SortType(String localeKey) {
            this.localeKey = localeKey;
        }

        public String getLocaleKey() {
            return localeKey;
        }
    }

    public SearchBookGUI(supershop plugin, Player player) {
        this(plugin, player, "");
    }

    public SearchBookGUI(supershop plugin, Player player, String initialSearch) {
        super(plugin, player, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.search.title"), 54);
        this.searcher = player;
        this.currentPage = 0;
        this.searchQuery = initialSearch;
        loadAllItems();
        applyFilters();
    }

    @Override
    protected void setupInventory() {
        fillBorderWith(Material.PURPLE_STAINED_GLASS_PANE);

        setupHeader();
        setupFilters();
        displayItemList();
        setupPagination();
    }

    private void setupHeader() {
        UUID playerUUID = player.getUniqueId();
        // Search info and controls
        ItemStack searchInfo = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.header.title"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.header.total-items", String.valueOf(allResults.size())),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.header.filtered-results", String.valueOf(filteredResults.size())),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.header.current-search", (searchQuery.isEmpty() ? plugin.getLocaleManager().getMessage(playerUUID, "gui.search.all-items") : searchQuery)),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.header.sort", plugin.getLocaleManager().getMessage(playerUUID, sortType.getLocaleKey())));
        inventory.setItem(4, searchInfo);

        // Search button
        ItemStack searchButton = createButton(Material.COMPASS, plugin.getLocaleManager().getMessage(playerUUID, "gui.search.search-button.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.search-button.lore1", (searchQuery.isEmpty() ? plugin.getLocaleManager().getMessage(playerUUID, "gui.search.all-items") : searchQuery)),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.search-button.lore2"));
        inventory.setItem(1, searchButton);

        // Clear search
        if (!searchQuery.isEmpty()) {
            ItemStack clearSearch = createButton(Material.BARRIER, plugin.getLocaleManager().getMessage(playerUUID, "gui.search.clear-search-button.name"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.search.clear-search-button.lore"));
            inventory.setItem(2, clearSearch);
        }

        // Refresh button
        ItemStack refresh = createButton(Material.LIME_DYE, plugin.getLocaleManager().getMessage(playerUUID, "gui.search.refresh-button.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.refresh-button.lore"));
        inventory.setItem(7, refresh);
    }

    private void setupFilters() {
        UUID playerUUID = player.getUniqueId();
        // Sort button
        ItemStack sortButton = createButton(Material.HOPPER, plugin.getLocaleManager().getMessage(playerUUID, "gui.search.sort-button.name", plugin.getLocaleManager().getMessage(playerUUID, sortType.getLocaleKey())),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.sort-button.lore1"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.sort-button.lore2"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.sort-button.lore3"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.sort-button.lore4"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.sort-button.lore5"));
        inventory.setItem(3, sortButton);

        // Affordable filter
        ItemStack affordableFilter = createButton(
            showOnlyAffordable ? Material.EMERALD : Material.REDSTONE,
            showOnlyAffordable ? plugin.getLocaleManager().getMessage(playerUUID, "gui.search.affordable-only-button.name-on") : plugin.getLocaleManager().getMessage(playerUUID, "gui.search.affordable-only-button.name-off"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.affordable-only-button.lore1"),
            showOnlyAffordable ? plugin.getLocaleManager().getMessage(playerUUID, "gui.search.affordable-only-button.lore2-on") : plugin.getLocaleManager().getMessage(playerUUID, "gui.search.affordable-only-button.lore2-off"));
        inventory.setItem(5, affordableFilter);

        // Quick filters
        ItemStack popularItems = createButton(Material.DIAMOND, plugin.getLocaleManager().getMessage(playerUUID, "gui.search.popular-items-button.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.popular-items-button.lore"));
        inventory.setItem(0, popularItems);

        ItemStack nearbyShops = createButton(Material.ENDER_PEARL, plugin.getLocaleManager().getMessage(playerUUID, "gui.search.nearby-shops-button.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.nearby-shops-button.lore"));
        inventory.setItem(8, nearbyShops);
    }

    private void displayItemList() {
        UUID playerUUID = player.getUniqueId();
        if (filteredResults.isEmpty()) {
            ItemStack noResults = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.no-results.title"),
                searchQuery.isEmpty() ? plugin.getLocaleManager().getMessage(playerUUID, "gui.search.no-results.no-items-for-sale") : plugin.getLocaleManager().getMessage(playerUUID, "gui.search.no-results.no-items-match", searchQuery),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.search.no-results.try"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.search.no-results.try-clear-search"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.search.no-results.try-change-filters"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.search.no-results.try-refresh"));
            inventory.setItem(22, noResults);
            return;
        }

        // Display 28 items in the 4 middle rows, excluding the first and last column
        List<Integer> itemSlotsList = new ArrayList<>();
        for (int row = 1; row < 5; row++) { // Rows 2, 3, 4, 5 (0-indexed)
            for (int col = 1; col < 8; col++) { // Columns 2-8 (0-indexed)
                itemSlotsList.add(row * 9 + col);
            }
        }
        int[] itemSlots = itemSlotsList.stream().mapToInt(i -> i).toArray();

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredResults.size());

        for (int i = 0; i < itemSlots.length; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < endIndex) {
                SearchResult result = filteredResults.get(itemIndex);
                // The position number should be relative to the current page's view
                int displayPosition = (currentPage * itemsPerPage) + i + 1;
                ItemStack displayItem = createSearchResultDisplay(result, displayPosition);
                inventory.setItem(itemSlots[i], displayItem);
            }
        }
    }

    private ItemStack createSearchResultDisplay(SearchResult result, int position) {
        ItemStack displayItem = result.getShopItem().getItemStack().clone();
        displayItem.setAmount(result.getQuantity());
        ItemMeta meta = displayItem.getItemMeta();
        UUID playerUUID = player.getUniqueId();

        if (meta != null) {
            String itemName = result.getItemName();
            meta.displayName(Component.text(plugin.getMessageUtils().colorize("&e" + position + ". " + itemName)));

            List<String> lore = new ArrayList<>();
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.quantity", String.valueOf(result.getQuantity()))));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.price", result.getFormattedPrice())));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.seller", result.getOwnerName())));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.location", result.getCoordinatesString())));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.world", result.getWorldName())));
            lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.distance", result.getFormattedDistance())));

            if (result.hasDescription()) {
                lore.add("");
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.description")));
                lore.add(plugin.getMessageUtils().colorize("&f" + result.getDescription()));
            }

            lore.add("");

            // Affordability check
            // Check if player owns this shop
            if (result.getShop().getOwnerId().equals(player.getUniqueId())) {
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.own-shop-warning")));
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.own-shop-manage")));
            } else if (canPlayerAfford(result.getShopItem())) {
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.can-afford")));
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.click-to-visit")));
            } else {
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.cannot-afford")));
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.needs", result.getFormattedPrice())));
                lore.add(plugin.getMessageUtils().colorize(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.item.click-to-visit-anyway")));
            }

            // Convert the list of strings to a list of components for the new API
            List<Component> componentLore = new ArrayList<>();
            for (String line : lore) {
                componentLore.add(Component.text(plugin.getMessageUtils().colorize(line)));
            }
            meta.lore(componentLore);
            displayItem.setItemMeta(meta);
        }

        return displayItem;
    }

    private void setupPagination() {
        int totalPages = (int) Math.ceil((double) filteredResults.size() / itemsPerPage);
        UUID playerUUID = player.getUniqueId();

        // Previous page
        if (currentPage > 0) {
            ItemStack prevPage = createButton(Material.ARROW, plugin.getLocaleManager().getMessage(playerUUID, "gui.previous-page"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.page-info-short", String.valueOf(currentPage), String.valueOf(totalPages)));
            inventory.setItem(48, prevPage);
        }

        // Page info
        ItemStack pageInfo = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.page-info", String.valueOf(currentPage + 1), String.valueOf(Math.max(1, totalPages))),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.search.pagination.showing-items", String.valueOf(Math.min(itemsPerPage, filteredResults.size() - (currentPage * itemsPerPage))), String.valueOf(filteredResults.size())));
        inventory.setItem(49, pageInfo);

        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = createButton(Material.ARROW, plugin.getLocaleManager().getMessage(playerUUID, "gui.next-page"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.page-info-short", String.valueOf(currentPage + 2), String.valueOf(totalPages)));
            inventory.setItem(50, nextPage);
        }

        // Navigation info
        if (totalPages > 1) {
            ItemStack navInfo = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.search.pagination.navigation-info.title"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.search.pagination.navigation-info.lore1"),
                plugin.getLocaleManager().getMessage(playerUUID, "gui.search.pagination.navigation-info.lore2", String.valueOf(filteredResults.size())));
            inventory.setItem(46, navInfo);
        }

        // Close button
        ItemStack close = createCloseButton();
        inventory.setItem(53, close);
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        switch (slot) {
            case 0: // Popular items
                handlePopularItems();
                break;
            case 1: // Search
                handleSearch();
                break;
            case 2: // Clear search
                handleClearSearch();
                break;
            case 3: // Sort
                handleSortType();
                break;
            case 5: // Affordable filter
                handleAffordableFilter();
                break;
            case 7: // Refresh
                handleRefresh();
                break;
            case 8: // Nearby shops
                handleNearbyShops();
                break;
            case 48: // Previous page
                if (currentPage > 0) {
                    currentPage--;
                    refresh();
                }
                break;
            case 50: // Next page
                int totalPages = (int) Math.ceil((double) filteredResults.size() / itemsPerPage);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    refresh();
                }
                break;
            case 53: // Close
                close();
                break;
            default:
                // Handle item selection
                SearchResult result = getSearchResultFromSlot(slot);
                if (result != null) {
                    handleItemSelection(result, event);
                }
                break;
        }
    }

    private void handlePopularItems() {
        List<Material> popularMaterials = plugin.getSearchManager().getPopularItems();
        if (!popularMaterials.isEmpty()) {
            filteredResults = allResults.stream()
                .filter(result -> popularMaterials.contains(result.getShopItem().getItemStack().getType()))
                .collect(Collectors.toList());

            searchQuery = plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.search.popular-items-search-query");
            currentPage = 0;
            applySorting();
            refresh();

            plugin.getMessageUtils().sendMessage(player, "gui.search.showing-popular-items", String.valueOf(filteredResults.size()));
        }
    }

    private void handleSearch() {
        close();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            new SearchInputGUI(plugin, player, searchQuery).open();
        });
    }

    private void handleClearSearch() {
        searchQuery = "";
        currentPage = 0;
        applyFilters();
        refresh();
        plugin.getMessageUtils().sendMessage(player, "gui.search.search-cleared", String.valueOf(filteredResults.size()));
    }

    private void handleSortType() {
        SortType[] types = SortType.values();
        int currentIndex = sortType.ordinal();
        sortType = types[(currentIndex + 1) % types.length];

        applySorting();
        refresh();
        plugin.getMessageUtils().sendMessage(player, "gui.search.sorting-by", plugin.getLocaleManager().getMessage(player.getUniqueId(), sortType.getLocaleKey()));
    }

    private void handleAffordableFilter() {
        showOnlyAffordable = !showOnlyAffordable;
        currentPage = 0;
        applyFilters();
        refresh();

        String status = showOnlyAffordable ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.search.affordable-only-status") : plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.search.all-items-status");
        plugin.getMessageUtils().sendMessage(player, "gui.search.showing-filter-status", status, String.valueOf(filteredResults.size()));
    }

    private void handleNearbyShops() {
        filteredResults = allResults.stream()
            .filter(result -> result.getDistance() <= 1000)
            .collect(Collectors.toList());

        searchQuery = plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.search.nearby-shops-search-query");
        currentPage = 0;
        applySorting();
        refresh();

        plugin.getMessageUtils().sendMessage(player, "gui.search.showing-nearby-shops", String.valueOf(filteredResults.size()));
    }

    private void handleRefresh() {
        loadAllItems();
        applyFilters();
        refresh();
        plugin.getMessageUtils().sendMessage(player, "gui.search.marketplace-refreshed", String.valueOf(allResults.size()));
    }

    private void handleItemSelection(SearchResult result, InventoryClickEvent event) {
        close();

        // Check if player owns this shop
        if (result.getShop().getOwnerId().equals(player.getUniqueId())) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // Open shop management GUI for own shop
                new ShopManagementGUI(plugin, player, result.getShop()).open();
                plugin.getMessageUtils().sendMessage(player, "gui.search.opening-own-shop");
            });
        } else {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // Open shop browse GUI for other players' shops
                new ShopBrowseGUI(plugin, player, result.getShop()).open();
            });
        }
    }

    private SearchResult getSearchResultFromSlot(int slot) {
        // Check if the slot is within the item display area
        int row = slot / 9;
        int col = slot % 9;

        if (row >= 1 && row <= 4 && col >= 1 && col <= 7) {
            // Calculate the index based on its position in the 4x7 grid
            int rowIndexInGrid = row - 1; // 0-3
            int colIndexInGrid = col - 1; // 0-6
            int itemArrayIndex = (rowIndexInGrid * 7) + colIndexInGrid;

            int itemIndex = (currentPage * itemsPerPage) + itemArrayIndex;

            if (itemIndex >= 0 && itemIndex < filteredResults.size()) {
                return filteredResults.get(itemIndex);
            }
        }

        return null;
    }

    private void loadAllItems() {
        allResults = new ArrayList<>();

        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!shop.isActive()) continue;

            double distance = shop.getDistanceFrom(searcher.getLocation());

            for (ShopItem item : shop.getItems()) {
                if (item.isAvailable()) {
                    allResults.add(new SearchResult(shop, item, distance));
                }
            }
        }
    }

    private void applyFilters() {
        filteredResults = new ArrayList<>(allResults);

        // Apply search filter
        if (!searchQuery.isEmpty() && !searchQuery.equals(plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.search.popular-items-search-query")) && !searchQuery.equals(plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.search.nearby-shops-search-query"))) {
            String query = searchQuery.toLowerCase();
            filteredResults = filteredResults.stream()
                .filter(result -> {
                    String itemName = result.getItemName().toLowerCase();
                    String description = result.getDescription().toLowerCase();
                    String materialName = result.getShopItem().getItemStack().getType().name().toLowerCase().replace("_", " ");

                    return itemName.contains(query) || description.contains(query) || materialName.contains(query);
                })
                .collect(Collectors.toList());
        }

        // Apply affordable filter
        if (showOnlyAffordable) {
            filteredResults = filteredResults.stream()
                .filter(result -> canPlayerAfford(result.getShopItem()))
                .collect(Collectors.toList());
        }

        applySorting();
    }

    private void applySorting() {
        switch (sortType) {
            case DISTANCE:
                filteredResults.sort(Comparator.comparingDouble(SearchResult::getDistance));
                break;
            case NAME_AZ:
                filteredResults.sort(Comparator.comparing(SearchResult::getItemName));
                break;
            case NAME_ZA:
                filteredResults.sort(Comparator.comparing(SearchResult::getItemName).reversed());
                break;
            case QUANTITY:
                filteredResults.sort(Comparator.comparingInt(SearchResult::getQuantity).reversed());
                break;
            case PRICE_LOW:
                filteredResults.sort(Comparator.comparing(result -> result.getShopItem().getPriceItems().size()));
                break;
            case PRICE_HIGH:
                filteredResults.sort(Comparator.comparing((SearchResult result) -> result.getShopItem().getPriceItems().size()).reversed());
                break;
        }
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

    private void fillBorderWith(Material material) {
        ItemStack borderItem = new ItemStack(material);
        ItemMeta meta = borderItem.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            borderItem.setItemMeta(meta);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderItem);
            }
        }
    }
}
