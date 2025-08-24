package me.nouridin.supershop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopBrowseGUI extends BaseGUI {
    
    private final Shop shop;
    private int currentPage;
    private final int itemsPerPage = 21; // 3 rows of 7 items
    
    public ShopBrowseGUI(supershop plugin, Player player, Shop shop) {
        super(plugin, player, "&4&l" + shop.getOwnerName() + "'s Shop", 54);
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
        ItemStack shopInfo = createInfoItem("&6Shop Owner: &e" + shop.getOwnerName(),
            "&7Location: &f" + shop.getCoordinatesString(),
            "&7World: &f" + shop.getWorldName(),
            "&7Items for sale: &f" + getAvailableItems().size(),
            "&7Status: &aOpen for business!");
        inventory.setItem(4, shopInfo);
    }
    
    private void displayItems() {
        List<ShopItem> availableItems = getAvailableItems();
        
        if (availableItems.isEmpty()) {
            ItemStack noItems = createInfoItem("&cNo items for sale",
                "&7This shop currently has no items available.",
                "&7Check back later!");
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
        
        if (meta != null) {
            String itemName = shopItem.getFormattedItemName();
            meta.setDisplayName(MessageUtils.colorize("&e" + itemName));
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.colorize("&7Quantity: &f" + shopItem.getQuantity()));
            lore.add(MessageUtils.colorize("&7Price: &a" + shopItem.getFormattedPrice()));
            
            if (shopItem.hasDescription()) {
                lore.add("");
                lore.add(MessageUtils.colorize("&7Description:"));
                lore.add(MessageUtils.colorize("&f" + shopItem.getDescription()));
            }
            
            lore.add("");
            
            // Check if player can afford
            if (canPlayerAfford(shopItem)) {
                lore.add(MessageUtils.colorize("&aLeft-click to buy 1"));
                if (shopItem.getQuantity() > 1) {
                    lore.add(MessageUtils.colorize("&aShift-click to buy all (" + shopItem.getQuantity() + ")"));
                }
                lore.add(MessageUtils.colorize("&aRight-click to buy custom amount"));
            } else {
                lore.add(MessageUtils.colorize("&cYou cannot afford this item!"));
                lore.add(MessageUtils.colorize("&7You need: &f" + shopItem.getFormattedPrice()));
            }
            
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }
        
        return displayItem;
    }
    
    private void setupNavigation() {
        List<ShopItem> availableItems = getAvailableItems();
        int totalPages = (int) Math.ceil((double) availableItems.size() / itemsPerPage);
        
        // Previous page
        if (currentPage > 0) {
            ItemStack prevPage = createButton(Material.ARROW, "&7← Previous Page");
            inventory.setItem(45, prevPage);
        }
        
        // Page info
        if (totalPages > 1) {
            ItemStack pageInfo = createInfoItem("&7Page " + (currentPage + 1) + " of " + totalPages);
            inventory.setItem(49, pageInfo);
        } else if (!availableItems.isEmpty()) {
            ItemStack resultInfo = createInfoItem("&7Shopping at " + shop.getOwnerName() + "'s Shop",
                "&7Total items: &f" + availableItems.size());
            inventory.setItem(49, resultInfo);
        }
        
        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = createButton(Material.ARROW, "&7Next Page →");
            inventory.setItem(53, nextPage);
        }
        
        // Close button
        ItemStack close = createCloseButton();
        inventory.setItem(46, close);
        
        // Shop info button
        ItemStack shopDetails = createButton(Material.COMPASS, "&eShop Details",
            "&7Owner: &f" + shop.getOwnerName(),
            "&7Location: &f" + shop.getCoordinatesString(),
            "&7World: &f" + shop.getWorldName(),
            "&7Created: &f" + MessageUtils.formatDuration(System.currentTimeMillis() - shop.getCreatedAt()) + " ago");
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
            MessageUtils.sendMessage(player, "&cYou cannot afford this item!");
            MessageUtils.sendMessage(player, "&7You need: &f" + shopItem.getFormattedPrice());
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