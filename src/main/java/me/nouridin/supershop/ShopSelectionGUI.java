package me.nouridin.supershop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopSelectionGUI extends BaseGUI {
    
    private final List<Shop> shops;
    private int currentPage;
    private final int shopsPerPage = 21; // 3 rows of 7 shops
    
    public ShopSelectionGUI(supershop plugin, Player player, List<Shop> shops) {
        super(plugin, player, "&6Select Shop to Manage", 54);
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
        ItemStack header = createInfoItem("&6Remote Shop Management",
            "&7Total shops: &e" + shops.size(),
            "&7Click on a shop to manage it remotely",
            "&7You can manage shops from anywhere!");
        inventory.setItem(4, header);
    }
    
    private void displayShops() {
        if (shops.isEmpty()) {
            ItemStack noShops = createInfoItem("&cNo Shops Found",
                "&7You don't have any shops yet!",
                "&7Create a shop first with &e/shop create");
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
        String status = shop.isActive() ? "&aActive" : "&cInactive";
        
        ItemStack shopItem = new ItemStack(displayMaterial);
        ItemMeta meta = shopItem.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize("&e" + shopNumber + ". Shop at " + shop.getCoordinatesString()));
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.colorize("&7Status: " + status));
            lore.add(MessageUtils.colorize("&7World: &f" + shop.getWorldName()));
            lore.add(MessageUtils.colorize("&7Items for sale: &e" + shop.getItems().size()));
            lore.add(MessageUtils.colorize("&7Total stock: &e" + getTotalStock(shop)));
            
            if (shop.hasRevenue()) {
                lore.add(MessageUtils.colorize("&7Revenue: &a" + shop.getTotalRevenueItems() + " items waiting"));
            } else {
                lore.add(MessageUtils.colorize("&7Revenue: &7No revenue to collect"));
            }
            
            lore.add(MessageUtils.colorize("&7Created: &e" + 
                MessageUtils.formatDuration(System.currentTimeMillis() - shop.getCreatedAt()) + " ago"));
            lore.add("");
            lore.add(MessageUtils.colorize("&eClick to manage this shop remotely"));
            lore.add(MessageUtils.colorize("&7You can add items, set prices, collect revenue"));
            
            meta.setLore(lore);
            shopItem.setItemMeta(meta);
        }
        
        return shopItem;
    }
    
    private void setupNavigation() {
        int totalPages = (int) Math.ceil((double) shops.size() / shopsPerPage);
        
        // Previous page
        if (currentPage > 0) {
            ItemStack prevPage = createButton(Material.ARROW, "&7← Previous Page",
                "&7Page " + currentPage + " of " + totalPages);
            inventory.setItem(45, prevPage);
        }
        
        // Page info
        if (totalPages > 1) {
            ItemStack pageInfo = createInfoItem("&7Page " + (currentPage + 1) + " of " + totalPages,
                "&7Showing " + Math.min(shopsPerPage, shops.size() - (currentPage * shopsPerPage)) + " of " + shops.size() + " shops");
            inventory.setItem(49, pageInfo);
        } else {
            ItemStack shopInfo = createInfoItem("&7Your Shops",
                "&7Total: " + shops.size() + " shops",
                "&7Click any shop to manage it");
            inventory.setItem(49, shopInfo);
        }
        
        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = createButton(Material.ARROW, "&7Next Page →",
                "&7Page " + (currentPage + 2) + " of " + totalPages);
            inventory.setItem(53, nextPage);
        }
        
        // Close button
        ItemStack close = createCloseButton();
        inventory.setItem(46, close);
        
        // Help button
        ItemStack help = createButton(Material.BOOK, "&eHelp",
            "&7Remote shop management allows you to:",
            "&7• Add and remove items",
            "&7• Set prices and descriptions", 
            "&7• Collect revenue",
            "&7• Activate/deactivate shops",
            "&7• All from any distance!");
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
        MessageUtils.sendMessage(player, "&aOpening remote management for shop at " + shop.getCoordinatesString());
        new ShopManagementGUI(plugin, player, shop).open();
    }
    
    private int getTotalStock(Shop shop) {
        return shop.getItems().stream()
            .filter(item -> item.isAvailable())
            .mapToInt(item -> item.getQuantity())
            .sum();
    }
}