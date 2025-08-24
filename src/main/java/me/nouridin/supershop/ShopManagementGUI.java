package me.nouridin.supershop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopManagementGUI extends BaseGUI {

    private final Shop shop;
    private int currentPage;
    private final int itemsPerPage = 21; // 3 rows of 7 item
    
    public ShopManagementGUI(supershop plugin, Player player, Shop shop) {
        super(plugin, player, "&4&lManage Shop", 54);
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
        ItemStack shopInfo = createInfoItem("&6Shop Information",
            "&7Location: &f" + shop.getCoordinatesString(),
            "&7World: &f" + shop.getWorldName(),
            "&7Items: &f" + shop.getItems().size(),
            "&7Status: " + (shop.isActive() ? "&aActive" : "&cInactive"),
            "&7Revenue: " + (shop.hasRevenue() ? "&e" + shop.getRevenueItemCount() + " stacks waiting" : "&7None"));
        inventory.setItem(4, shopInfo);
    }
    
    private void setupControlButtons() {
        // Add item button
        ItemStack addItem = createButton(Material.EMERALD, "&aAdd Item",
            "&7Click to add an item from your inventory",
            "&7to sell in this shop");
        inventory.setItem(0, addItem);
        
        // Toggle shop status
        ItemStack toggleStatus = createButton(
            shop.isActive() ? Material.REDSTONE : Material.EMERALD,
            shop.isActive() ? "&cDeactivate Shop" : "&aActivate Shop",
            "&7Click to " + (shop.isActive() ? "deactivate" : "activate") + " this shop");
        inventory.setItem(1, toggleStatus);
        
        // Collect revenue button
        if (shop.hasRevenue()) {
            ItemStack collectRevenue = createButton(Material.GOLD_INGOT, "&eCollect Revenue",
                "&7You have &e" + shop.getRevenueItemCount() + " stacks &7waiting",
                "&7Total items: &e" + shop.getTotalRevenueItems(),
                "&7Click to collect all revenue!");
            inventory.setItem(2, collectRevenue);
        } else {
            ItemStack noRevenue = createButton(Material.GRAY_DYE, "&7No Revenue",
                "&7No revenue to collect yet",
                "&7Revenue appears here when customers",
                "&7buy items from your shop");
            inventory.setItem(2, noRevenue);
        }
        
        // Remove shop button - Enhanced for direct removal
        ItemStack removeShop = createButton(Material.BARRIER, "&cRemove Shop",
            "&7Click to permanently remove this shop",
            "&7All items and revenue will be returned to you",
            "&c&lWARNING: This action is immediate!",
            "&eClick once to remove the shop completely");
        inventory.setItem(8, removeShop);
    }
    
    private void displayItems() {
        List<ShopItem> items = shop.getItems();
        
        if (items.isEmpty()) {
            ItemStack noItems = createInfoItem("&7No items in shop",
                "&7Click 'Add Item' to start selling items!");
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

        if (meta != null) {
            String itemName = shopItem.getFormattedItemName();
            meta.setDisplayName(MessageUtils.colorize("&e" + itemName));

            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.colorize("&7Quantity: &f" + shopItem.getQuantity()));
            lore.add(MessageUtils.colorize("&7Price: &a" + shopItem.getFormattedPrice()));
            lore.add(MessageUtils.colorize("&7Status: " + (shopItem.isAvailable() ? "&aAvailable" : "&cUnavailable")));

            if (shopItem.hasDescription()) {
                lore.add("");
                lore.add(MessageUtils.colorize("&7Description:"));
                lore.add(MessageUtils.colorize("&f" + shopItem.getDescription()));
            }

            lore.add("");
            lore.add(MessageUtils.colorize("&eLeft-click to edit"));
            lore.add(MessageUtils.colorize("&eRight-click to remove"));
            lore.add(MessageUtils.colorize("&eShift-click to toggle availability"));

            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }

        return displayItem;
    }

    private void setupNavigation() {
        List<ShopItem> items = shop.getItems();
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        
        // Previous page
        if (currentPage > 0) {
            ItemStack prevPage = createButton(Material.ARROW, "&7← Previous Page");
            inventory.setItem(45, prevPage);
        }
        
        // Page info
        if (totalPages > 1) {
            ItemStack pageInfo = createInfoItem("&7Page " + (currentPage + 1) + " of " + totalPages);
            inventory.setItem(49, pageInfo);
        }
        
        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = createButton(Material.ARROW, "&7Next Page →");
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
        
        String status = shop.isActive() ? "activated" : "deactivated";
        MessageUtils.sendMessage(player, "&aShop " + status + " successfully!");
        
        refresh();
    }
    
    private void handleCollectRevenue() {
        if (!shop.hasRevenue()) {
            MessageUtils.sendMessage(player, "&cNo revenue to collect!");
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
                MessageUtils.sendMessage(player, "&aShop removed successfully!");
                MessageUtils.sendMessage(player, "&7Items and revenue have been stored in the chest and your inventory.");
            } else {
                MessageUtils.sendMessage(player, "&cFailed to remove shop. Trying alternative method...");
                forceRemoveAndReturnItems(allItems);
            }
        } else {
            // Chest doesn't exist, remove shop and return items directly
            MessageUtils.sendMessage(player, "&eChest not found at shop location. Returning items directly to you.");
            forceRemoveAndReturnItems(allItems);
        }
    }
    
    private void forceRemoveAndReturnItems(List<ItemStack> allItems) {
        // Force remove the shop from the system
        if (!plugin.getShopManager().forceRemoveShop(shop.getShopId(), player)) {
            MessageUtils.sendMessage(player, "&cFailed to remove shop from system!");
            return;
        }
        
        if (allItems.isEmpty()) {
            MessageUtils.sendMessage(player, "&aShop removed successfully! No items to return.");
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
        MessageUtils.sendMessage(player, "&aShop removed successfully!");
        if (itemsToInventory > 0) {
            MessageUtils.sendMessage(player, "&aReturned " + itemsToInventory + " items to your inventory.");
        }
        if (itemsDropped > 0) {
            MessageUtils.sendMessage(player, "&eDropped " + itemsDropped + " items on the ground (inventory was full).");
            MessageUtils.sendMessage(player, "&7Pick them up quickly!");
        }
        if (itemsToInventory == 0 && itemsDropped == 0) {
            MessageUtils.sendMessage(player, "&7No items to return.");
        }
    }
    
    private void handleItemManagement(ShopItem shopItem, InventoryClickEvent event) {
        if (event.isShiftClick()) {
            // Toggle availability
            shopItem.setAvailable(!shopItem.isAvailable());
            plugin.getDatabaseManager().saveShopItem(shop.getShopId(), shopItem);
            
            String status = shopItem.isAvailable() ? "available" : "unavailable";
            MessageUtils.sendMessage(player, "&aItem marked as " + status + "!");
            
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

                MessageUtils.sendMessage(player, "&aItem returned to your inventory!");

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