package me.nouridin.supershop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SearchBookGUI extends BaseGUI {
    
    private final Player searcher;
    private List<SearchResult> allResults;
    private List<SearchResult> filteredResults;
    private int currentPage;
    private final int itemsPerPage = 7; // 7 items in vertical list
    
    // Search and filter settings
    private String searchQuery = "";
    private SortType sortType = SortType.DISTANCE;
    private boolean showOnlyAffordable = false;
    
    public enum SortType {
        DISTANCE("Distance"),
        NAME_AZ("Name A-Z"),
        NAME_ZA("Name Z-A"),
        PRICE_LOW("Price Low"),
        PRICE_HIGH("Price High"),
        QUANTITY("Quantity");
        
        private final String displayName;
        
        SortType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public SearchBookGUI(supershop plugin, Player player) {
        this(plugin, player, "");
    }
    
    public SearchBookGUI(supershop plugin, Player player, String initialSearch) {
        super(plugin, player, "&4&lMarketplace Browser", 54);
        this.searcher = player;
        this.currentPage = 0;
        this.searchQuery = initialSearch;
        loadAllItems();
        applyFilters();
    }
    
    @Override
    protected void setupInventory() {
        fillBorder(Material.PURPLE_STAINED_GLASS_PANE);
        
        setupHeader();
        setupFilters();
        displayItemList();
        setupPagination();
    }
    
    private void setupHeader() {
        // Search info and controls
        ItemStack searchInfo = createInfoItem("&6Marketplace Browser",
            "&7Total items: &e" + allResults.size(),
            "&7Filtered results: &e" + filteredResults.size(),
            "&7Current search: &f" + (searchQuery.isEmpty() ? "All Items" : searchQuery),
            "&7Sort: &e" + sortType.getDisplayName());
        inventory.setItem(4, searchInfo);
        
        // Search button
        ItemStack searchButton = createButton(Material.COMPASS, "&eSearch Items",
            "&7Current: &f" + (searchQuery.isEmpty() ? "All Items" : searchQuery),
            "&7Click to search by item name");
        inventory.setItem(1, searchButton);
        
        // Clear search
        if (!searchQuery.isEmpty()) {
            ItemStack clearSearch = createButton(Material.BARRIER, "&cClear Search",
                "&7Click to show all items");
            inventory.setItem(2, clearSearch);
        }
        
        // Refresh button
        ItemStack refresh = createButton(Material.LIME_DYE, "&aRefresh",
            "&7Click to reload marketplace data");
        inventory.setItem(7, refresh);
    }
    
    private void setupFilters() {
        // Sort button
        ItemStack sortButton = createButton(Material.HOPPER, "&eSort: " + sortType.getDisplayName(),
            "&7Click to change sorting",
            "&7• Distance (nearest first)",
            "&7• Name A-Z / Z-A",
            "&7• Price Low / High",
            "&7• Quantity");
        inventory.setItem(3, sortButton);
        
        // Affordable filter
        ItemStack affordableFilter = createButton(
            showOnlyAffordable ? Material.EMERALD : Material.REDSTONE,
            showOnlyAffordable ? "&aAffordable Only" : "&cShow All Items",
            "&7Click to toggle affordable filter",
            showOnlyAffordable ? "&7Showing only items you can afford" : "&7Showing all items");
        inventory.setItem(5, affordableFilter);
        
        // Quick filters
        ItemStack popularItems = createButton(Material.DIAMOND, "&ePopular Items",
            "&7Show most commonly sold items");
        inventory.setItem(0, popularItems);
        
        ItemStack nearbyShops = createButton(Material.ENDER_PEARL, "&eNearby Shops",
            "&7Show shops within 1000 blocks");
        inventory.setItem(8, nearbyShops);
    }
    
    private void displayItemList() {
        if (filteredResults.isEmpty()) {
            ItemStack noResults = createInfoItem("&cNo Items Found",
                searchQuery.isEmpty() ? "&7No items are currently for sale" : "&7No items match: &f" + searchQuery,
                "&7Try:",
                "&7• Clearing your search",
                "&7• Changing filters",
                "&7• Refreshing the data");
            inventory.setItem(22, noResults);
            return;
        }
        
        // Display 7 items vertically in slots 10, 19, 28, 37, 11, 20, 29
        int[] itemSlots = {10, 19, 28, 37, 11, 20, 29};
        
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredResults.size());
        
        for (int i = 0; i < itemSlots.length; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < endIndex) {
                SearchResult result = filteredResults.get(itemIndex);
                ItemStack displayItem = createSearchResultDisplay(result, i + 1);
                inventory.setItem(itemSlots[i], displayItem);
            }
        }
    }
    
    private ItemStack createSearchResultDisplay(SearchResult result, int position) {
        ItemStack displayItem = result.getShopItem().getItemStack().clone();
        displayItem.setAmount(result.getQuantity());
        ItemMeta meta = displayItem.getItemMeta();
        
        if (meta != null) {
            String itemName = result.getItemName();
            meta.setDisplayName(MessageUtils.colorize("&e" + position + ". " + itemName));
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.colorize("&7Quantity: &f" + result.getQuantity()));
            lore.add(MessageUtils.colorize("&7Price: &a" + result.getFormattedPrice()));
            lore.add(MessageUtils.colorize("&7Seller: &b" + result.getOwnerName()));
            lore.add(MessageUtils.colorize("&7Location: &f" + result.getCoordinatesString()));
            lore.add(MessageUtils.colorize("&7World: &f" + result.getWorldName()));
            lore.add(MessageUtils.colorize("&7Distance: &e" + result.getFormattedDistance()));
            
            if (result.hasDescription()) {
                lore.add("");
                lore.add(MessageUtils.colorize("&7Description:"));
                lore.add(MessageUtils.colorize("&f" + result.getDescription()));
            }
            
            lore.add("");
            
            // Affordability check
            // Check if player owns this shop
            if (result.getShop().getOwnerId().equals(player.getUniqueId())) {
                lore.add(MessageUtils.colorize("&6⚠ This is your own shop"));
                lore.add(MessageUtils.colorize("&7Click to manage your shop"));
            } else if (canPlayerAfford(result.getShopItem())) {
                lore.add(MessageUtils.colorize("&a✓ You can afford this!"));
                lore.add(MessageUtils.colorize("&eClick to visit shop"));
            } else {
                lore.add(MessageUtils.colorize("&c✗ Cannot afford"));
                lore.add(MessageUtils.colorize("&7Need: &f" + result.getFormattedPrice()));
                lore.add(MessageUtils.colorize("&eClick to visit anyway"));
            }
            
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }
        
        return displayItem;
    }
    
    private void setupPagination() {
        int totalPages = (int) Math.ceil((double) filteredResults.size() / itemsPerPage);
        
        // Previous page
        if (currentPage > 0) {
            ItemStack prevPage = createButton(Material.ARROW, "&7← Previous Page",
                "&7Page " + currentPage + " of " + totalPages);
            inventory.setItem(48, prevPage);
        }
        
        // Page info
        ItemStack pageInfo = createInfoItem("&7Page " + (currentPage + 1) + " of " + Math.max(1, totalPages),
            "&7Showing " + Math.min(itemsPerPage, filteredResults.size() - (currentPage * itemsPerPage)) + " of " + filteredResults.size() + " items");
        inventory.setItem(49, pageInfo);
        
        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = createButton(Material.ARROW, "&7Next Page →",
                "&7Page " + (currentPage + 2) + " of " + totalPages);
            inventory.setItem(50, nextPage);
        }
        
        // Navigation info
        if (totalPages > 1) {
            ItemStack navInfo = createInfoItem("&7Navigation",
                "&7Use arrows to browse pages",
                "&7" + filteredResults.size() + " total results");
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
            
            searchQuery = "Popular Items";
            currentPage = 0;
            applySorting();
            refresh();
            
            MessageUtils.sendMessage(player, "&aShowing popular items! Found " + filteredResults.size() + " results.");
        }
    }
    
    private void handleSearch() {
        close();
        new SearchInputGUI(plugin, player, searchQuery).open();
    }
    
    private void handleClearSearch() {
        searchQuery = "";
        currentPage = 0;
        applyFilters();
        refresh();
        MessageUtils.sendMessage(player, "&aSearch cleared! Showing all " + filteredResults.size() + " items.");
    }
    
    private void handleSortType() {
        SortType[] types = SortType.values();
        int currentIndex = sortType.ordinal();
        sortType = types[(currentIndex + 1) % types.length];
        
        applySorting();
        refresh();
        MessageUtils.sendMessage(player, "&aSorting by: &e" + sortType.getDisplayName());
    }
    
    private void handleAffordableFilter() {
        showOnlyAffordable = !showOnlyAffordable;
        currentPage = 0;
        applyFilters();
        refresh();
        
        String status = showOnlyAffordable ? "affordable items only" : "all items";
        MessageUtils.sendMessage(player, "&aShowing " + status + "! Found " + filteredResults.size() + " results.");
    }
    
    private void handleNearbyShops() {
        filteredResults = allResults.stream()
            .filter(result -> result.getDistance() <= 1000)
            .collect(Collectors.toList());
        
        searchQuery = "Nearby Shops";
        currentPage = 0;
        applySorting();
        refresh();
        
        MessageUtils.sendMessage(player, "&aShowing nearby shops! Found " + filteredResults.size() + " items within 1000 blocks.");
    }
    
    private void handleRefresh() {
        loadAllItems();
        applyFilters();
        refresh();
        MessageUtils.sendMessage(player, "&aMarketplace refreshed! Found " + allResults.size() + " total items.");
    }
    
    private void handleItemSelection(SearchResult result, InventoryClickEvent event) {
        close();
        
        // Check if player owns this shop
        if (result.getShop().getOwnerId().equals(player.getUniqueId())) {
            // Open shop management GUI for own shop
            new ShopManagementGUI(plugin, player, result.getShop()).open();
            MessageUtils.sendMessage(player, "&aOpening your shop management!");
        } else {
            // Open shop browse GUI for other players' shops
            new ShopBrowseGUI(plugin, player, result.getShop()).open();
        }
    }
    
    private SearchResult getSearchResultFromSlot(int slot) {
        int[] itemSlots = {10, 19, 28, 37, 11, 20, 29};
        
        for (int i = 0; i < itemSlots.length; i++) {
            if (itemSlots[i] == slot) {
                int itemIndex = currentPage * itemsPerPage + i;
                if (itemIndex < filteredResults.size()) {
                    return filteredResults.get(itemIndex);
                }
                break;
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
        if (!searchQuery.isEmpty() && !searchQuery.equals("Popular Items") && !searchQuery.equals("Nearby Shops")) {
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
}