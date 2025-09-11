package me.nouridin.Search;

import me.nouridin.supershop.Shop;
import me.nouridin.supershop.ShopItem;
import org.bukkit.Location;

public class SearchResult {
    
    private Shop shop;
    private ShopItem shopItem;
    private double distance;
    
    public SearchResult(Shop shop, ShopItem shopItem, double distance) {
        this.shop = shop;
        this.shopItem = shopItem;
        this.distance = distance;
    }
    
    // Getters
    public Shop getShop() { return shop; }
    public ShopItem getShopItem() { return shopItem; }
    public double getDistance() { return distance; }
    
    public String getFormattedDistance() {
        if (distance == Double.MAX_VALUE) {
            return "Different World";
        }
        
        if (distance < 1000) {
            return String.format("%.1f blocks", distance);
        } else {
            return String.format("%.1f km", distance / 1000);
        }
    }
    
    public Location getLocation() { return shop.getLocation(); }
    public String getOwnerName() { return shop.getOwnerName(); }
    public String getCoordinatesString() { return shop.getCoordinatesString(); }
    public String getWorldName() { return shop.getWorldName(); }
    public String getItemName() { return shopItem.getFormattedItemName(); }
    public int getQuantity() { return shopItem.getQuantity(); }
    public String getFormattedPrice() { return shopItem.getFormattedPrice(); }
    public String getDescription() { return shopItem.getDescription(); }
    public boolean hasDescription() { return shopItem.hasDescription(); }
}