package me.nouridin.supershop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopManager {
    
    private final supershop plugin;
    private final Map<Location, Shop> shopsByLocation;
    private final Map<UUID, Shop> shopsById;
    private final Map<UUID, Set<UUID>> shopsByOwner;
    
    public ShopManager(supershop plugin) {
        this.plugin = plugin;
        this.shopsByLocation = new ConcurrentHashMap<>();
        this.shopsById = new ConcurrentHashMap<>();
        this.shopsByOwner = new ConcurrentHashMap<>();
    }
    
    public Shop createShop(Player owner, Location location) {
        if (isShopAtLocation(location)) {
            return null;
        }
        
        UUID shopId = UUID.randomUUID();
        Shop shop = new Shop(shopId, owner.getUniqueId(), owner.getName(), location);
        
        shopsByLocation.put(location, shop);
        shopsById.put(shopId, shop);
        shopsByOwner.computeIfAbsent(owner.getUniqueId(), k -> new HashSet<>()).add(shopId);
        
        plugin.getDatabaseManager().saveShop(shop);
        
        MessageUtils.sendMessage(owner, "&aShop created successfully at " + shop.getCoordinatesString());
        return shop;
    }
    
    public boolean removeShop(UUID shopId, Player player, org.bukkit.block.Block chestBlock) {
        Shop shop = shopsById.get(shopId);
        if (shop == null) {
            return false;
        }
        
        if (!shop.getOwnerId().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "&cYou can only remove your own shops! Use '/shop force remove' if you're an operator.");
            return false;
        }
        
        // Store items and revenue in the chest
        storeShopContentsInInventory(shop, player);
        
        // Remove shop from memory and database
        removeShopFromSystem(shop);
        
        MessageUtils.sendMessage(player, "&aShop removed successfully!");
        MessageUtils.sendMessage(player, "&7All items and revenue have been stored in your inventory.");
        return true;
    }
    
    public boolean forceRemoveShop(UUID shopId, Player player) {
        Shop shop = shopsById.get(shopId);
        if (shop == null) {
            return false;
        }
        
        // Allow shop owners to force remove their own shops (for GUI removal)
        // Or allow OPs to force remove any shop
        if (!shop.getOwnerId().equals(player.getUniqueId()) && !player.isOp()) {
            MessageUtils.sendMessage(player, "&cYou can only force remove your own shops or be an operator!");
            return false;
        }
        
        // Remove shop from memory and database
        removeShopFromSystem(shop);
        
        // Only show the "items not returned" message for OP force removals of other players' shops
        if (!shop.getOwnerId().equals(player.getUniqueId()) && player.isOp()) {
            MessageUtils.sendMessage(player, "&cShop force removed! Items were not returned to owner.");
        }
        
        return true;
    }

    private void storeShopContentsInInventory(Shop shop, Player owner) {
        List<ItemStack> allItems = new ArrayList<>();

        // Collect all shop items
        for (ShopItem shopItem : shop.getItems()) {
            ItemStack item = shopItem.getItemStack().clone();
            item.setAmount(shopItem.getQuantity());
            allItems.add(item);
        }

        // Collect all revenue items
        allItems.addAll(shop.getRevenue());

        if (allItems.isEmpty()) {
            MessageUtils.sendMessage(owner, "&7No items or revenue to return.");
            return;
        }

        int itemsToInventory = 0;
        int itemsDropped = 0;

        for (ItemStack item : allItems) {
            if (item == null || item.getType() == Material.AIR) continue;

            // Attempt to add to player inventory
            Map<Integer, ItemStack> leftover = owner.getInventory().addItem(item);
            if (leftover.isEmpty()) {
                itemsToInventory += item.getAmount();
            } else {
                // Inventory full, drop the leftovers
                for (ItemStack drop : leftover.values()) {
                    owner.getWorld().dropItemNaturally(owner.getLocation(), drop);
                    itemsDropped += drop.getAmount();
                }
            }
        }

        // Notify player
        if (itemsToInventory > 0) {
            MessageUtils.sendMessage(owner, "&aAdded " + itemsToInventory + " items to your inventory.");
        }
        if (itemsDropped > 0) {
            MessageUtils.sendMessage(owner, "&cDropped " + itemsDropped + " items on the ground (inventory was full).");
        }
    }


    private void removeShopFromSystem(Shop shop) {
        shopsByLocation.remove(shop.getLocation());
        shopsById.remove(shop.getShopId());
        Set<UUID> ownerShops = shopsByOwner.get(shop.getOwnerId());
        if (ownerShops != null) {
            ownerShops.remove(shop.getShopId());
            if (ownerShops.isEmpty()) {
                shopsByOwner.remove(shop.getOwnerId());
            }
        }
        
        plugin.getDatabaseManager().deleteShop(shop.getShopId());
    }
    
    public Shop getShopAtLocation(Location location) {
        return shopsByLocation.get(location);
    }
    
    public boolean isShopAtLocation(Location location) {
        return shopsByLocation.containsKey(location);
    }
    
    public Shop getShopById(UUID shopId) {
        return shopsById.get(shopId);
    }
    
    public List<Shop> getShopsByOwner(UUID ownerId) {
        Set<UUID> shopIds = shopsByOwner.get(ownerId);
        if (shopIds == null) {
            return new ArrayList<>();
        }
        
        List<Shop> shops = new ArrayList<>();
        for (UUID shopId : shopIds) {
            Shop shop = shopsById.get(shopId);
            if (shop != null) {
                shops.add(shop);
            }
        }
        return shops;
    }
    
    public Collection<Shop> getAllShops() {
        return shopsById.values();
    }
    
    public boolean addItemToShop(UUID shopId, ShopItem item, Player player) {
        Shop shop = shopsById.get(shopId);
        if (shop == null) {
            MessageUtils.sendMessage(player, "&cShop not found!");
            return false;
        }
        
        if (!shop.getOwnerId().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "&cYou don't own this shop!");
            return false;
        }
        
        shop.addItem(item);
        plugin.getDatabaseManager().saveShopItem(shopId, item);
        
        MessageUtils.sendMessage(player, "&aItem added to shop successfully!");
        return true;
    }
    
    public boolean removeItemFromShop(UUID shopId, UUID itemId, Player player) {
        Shop shop = shopsById.get(shopId);
        if (shop == null) {
            MessageUtils.sendMessage(player, "&cShop not found!");
            return false;
        }
        
        if (!shop.getOwnerId().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "&cYou don't own this shop!");
            return false;
        }
        
        ShopItem itemToRemove = shop.getItems().stream()
            .filter(item -> item.getItemId().equals(itemId))
            .findFirst()
            .orElse(null);
        
        if (itemToRemove == null) {
            MessageUtils.sendMessage(player, "&cItem not found in shop!");
            return false;
        }
        
        shop.removeItem(itemToRemove);
        plugin.getDatabaseManager().deleteShopItem(shopId, itemId);
        
        MessageUtils.sendMessage(player, "&aItem removed from shop successfully!");
        return true;
    }
    
    public boolean processPurchase(Player buyer, Shop shop, ShopItem item, int quantity) {
        if (!item.isAvailable() || item.getQuantity() < quantity) {
            MessageUtils.sendMessage(buyer, "&cItem is not available or insufficient quantity!");
            return false;
        }
        
        if (!hasRequiredItems(buyer, item.getPriceItems(), quantity)) {
            MessageUtils.sendMessage(buyer, "&cYou don't have the required items to make this purchase!");
            return false;
        }
        
        // Calculate total payment
        List<ItemStack> totalPayment = new ArrayList<>();
        for (ItemStack priceItem : item.getPriceItems()) {
            ItemStack payment = priceItem.clone();
            payment.setAmount(priceItem.getAmount() * quantity);
            totalPayment.add(payment);
        }
        
        // Remove payment items from buyer
        removeRequiredItems(buyer, item.getPriceItems(), quantity);
        
        // Add payment to shop revenue
        shop.addRevenue(totalPayment);
        
        // Give purchased item to buyer
        ItemStack purchasedItem = item.getItemStack();
        purchasedItem.setAmount(quantity);
        buyer.getInventory().addItem(purchasedItem);
        
        // Reduce shop item quantity
        item.reduceQuantity(quantity);
        
        // If item quantity reaches 0, remove it completely from the shop
        if (item.getQuantity() <= 0) {
            shop.removeItem(item);
            plugin.getDatabaseManager().deleteShopItem(shop.getShopId(), item.getItemId());
            
            // Notify owner that item sold out
            Player owner = plugin.getServer().getPlayer(shop.getOwnerId());
            if (owner != null && owner.isOnline()) {
                MessageUtils.sendMessage(owner, "&e" + item.getFormattedItemName() + " &7sold out in your shop!");
            }
        } else {
            // Save updated quantity
            plugin.getDatabaseManager().saveShopItem(shop.getShopId(), item);
        }
        
        // Save shop (for revenue)
        plugin.getDatabaseManager().saveShop(shop);
        
        MessageUtils.sendMessage(buyer, "&aPurchase successful! You bought " + quantity + "x " + item.getFormattedItemName());
        
        // Notify shop owner
        Player owner = plugin.getServer().getPlayer(shop.getOwnerId());
        if (owner != null && owner.isOnline()) {
            MessageUtils.sendMessage(owner, "&a" + buyer.getName() + " bought " + quantity + "x " + 
                item.getFormattedItemName() + " from your shop!");
            MessageUtils.sendMessage(owner, "&7Revenue is waiting for collection at your shop.");
        }
        
        return true;
    }
    
    public boolean collectRevenue(UUID shopId, Player player) {
        Shop shop = shopsById.get(shopId);
        if (shop == null) {
            MessageUtils.sendMessage(player, "&cShop not found!");
            return false;
        }
        
        if (!shop.getOwnerId().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "&cYou don't own this shop!");
            return false;
        }
        
        if (!shop.hasRevenue()) {
            MessageUtils.sendMessage(player, "&cNo revenue to collect!");
            return false;
        }
        
        // Add all revenue items to player's inventory
        int itemsCollected = 0;
        for (ItemStack revenueItem : shop.getRevenue()) {
            player.getInventory().addItem(revenueItem);
            itemsCollected += revenueItem.getAmount();
        }
        
        // Clear shop revenue
        shop.clearRevenue();
        plugin.getDatabaseManager().saveShop(shop);
        
        MessageUtils.sendMessage(player, "&aCollected " + itemsCollected + " items from shop revenue!");
        return true;
    }
    
    private boolean hasRequiredItems(Player player, List<ItemStack> requiredItems, int multiplier) {
        for (ItemStack requiredItem : requiredItems) {
            int neededAmount = requiredItem.getAmount() * multiplier;
            if (!hasItem(player, requiredItem, neededAmount)) {
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
    
    private void removeRequiredItems(Player player, List<ItemStack> requiredItems, int multiplier) {
        for (ItemStack requiredItem : requiredItems) {
            int neededAmount = requiredItem.getAmount() * multiplier;
            removeItem(player, requiredItem, neededAmount);
        }
    }
    
    private void removeItem(Player player, ItemStack item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
            ItemStack invItem = player.getInventory().getItem(i);
            if (invItem != null && invItem.isSimilar(item)) {
                int removeAmount = Math.min(remaining, invItem.getAmount());
                invItem.setAmount(invItem.getAmount() - removeAmount);
                remaining -= removeAmount;
                
                if (invItem.getAmount() <= 0) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }
    
    public void loadAllShops() {
        List<Shop> shops = plugin.getDatabaseManager().loadAllShops();
        for (Shop shop : shops) {
            shopsByLocation.put(shop.getLocation(), shop);
            shopsById.put(shop.getShopId(), shop);
            shopsByOwner.computeIfAbsent(shop.getOwnerId(), k -> new HashSet<>()).add(shop.getShopId());
        }
        
        MessageUtils.sendConsoleMessage("&aLoaded " + shops.size() + " shops from database.");
    }
    
    public void saveAllShops() {
        for (Shop shop : shopsById.values()) {
            plugin.getDatabaseManager().saveShop(shop);
        }
        
        MessageUtils.sendConsoleMessage("&aSaved " + shopsById.size() + " shops to database.");
    }
    
    public Map<String, Integer> getShopStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_shops", shopsById.size());
        stats.put("active_shops", (int) shopsById.values().stream().filter(Shop::isActive).count());
        stats.put("total_items", shopsById.values().stream().mapToInt(shop -> shop.getItems().size()).sum());
        stats.put("total_revenue_items", shopsById.values().stream().mapToInt(Shop::getTotalRevenueItems).sum());
        return stats;
    }
}