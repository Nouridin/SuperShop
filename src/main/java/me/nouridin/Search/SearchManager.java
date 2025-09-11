package me.nouridin.Search;

import me.nouridin.supershop.Shop;
import me.nouridin.supershop.ShopItem;
import me.nouridin.supershop.supershop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class SearchManager {
    
    private final supershop plugin;
    
    public SearchManager(supershop plugin) {
        this.plugin = plugin;
    }
    
    public List<SearchResult> searchByItemName(String itemName, Player searcher) {
        return searchByItemName(itemName, searcher.getLocation(), null, null);
    }
    
    public List<SearchResult> searchByItemName(String itemName, Location searchLocation, 
                                             String sellerFilter, Double maxDistance) {
        List<SearchResult> results = new ArrayList<>();
        
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!shop.isActive()) {
                continue;
            }
            
            if (sellerFilter != null && !shop.getOwnerName().toLowerCase().contains(sellerFilter.toLowerCase())) {
                continue;
            }
            
            double distance = shop.getDistanceFrom(searchLocation);
            
            if (maxDistance != null && distance > maxDistance) {
                continue;
            }
            
            for (ShopItem item : shop.getItems()) {
                if (!item.isAvailable()) {
                    continue;
                }
                
                if (matchesItemName(item, itemName)) {
                    results.add(new SearchResult(shop, item, distance));
                }
            }
        }
        
        results.sort(Comparator.comparingDouble(SearchResult::getDistance));
        
        return results;
    }
    
    public List<SearchResult> searchByMaterial(Material material, Location searchLocation) {
        List<SearchResult> results = new ArrayList<>();
        
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!shop.isActive()) {
                continue;
            }
            
            double distance = shop.getDistanceFrom(searchLocation);
            
            for (ShopItem item : shop.getItems()) {
                if (!item.isAvailable()) {
                    continue;
                }
                
                if (item.getItemStack().getType() == material) {
                    results.add(new SearchResult(shop, item, distance));
                }
            }
        }
        
        results.sort(Comparator.comparingDouble(SearchResult::getDistance));
        return results;
    }
    
    public List<SearchResult> searchByOwner(String ownerName, Location searchLocation) {
        List<SearchResult> results = new ArrayList<>();
        
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!shop.isActive()) {
                continue;
            }
            
            if (!shop.getOwnerName().toLowerCase().contains(ownerName.toLowerCase())) {
                continue;
            }
            
            double distance = shop.getDistanceFrom(searchLocation);
            
            for (ShopItem item : shop.getItems()) {
                if (item.isAvailable()) {
                    results.add(new SearchResult(shop, item, distance));
                }
            }
        }
        
        results.sort(Comparator.comparingDouble(SearchResult::getDistance));
        return results;
    }
    
    public List<SearchResult> getNearbyShops(Location location, double radius) {
        List<SearchResult> results = new ArrayList<>();
        
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!shop.isActive()) {
                continue;
            }
            
            double distance = shop.getDistanceFrom(location);
            if (distance <= radius) {
                for (ShopItem item : shop.getItems()) {
                    if (item.isAvailable()) {
                        results.add(new SearchResult(shop, item, distance));
                    }
                }
            }
        }
        
        results.sort(Comparator.comparingDouble(SearchResult::getDistance));
        return results;
    }
    
    public List<SearchResult> getAllAvailableItems(Location searchLocation) {
        List<SearchResult> results = new ArrayList<>();
        
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!shop.isActive()) {
                continue;
            }
            
            double distance = shop.getDistanceFrom(searchLocation);
            
            for (ShopItem item : shop.getItems()) {
                if (item.isAvailable()) {
                    results.add(new SearchResult(shop, item, distance));
                }
            }
        }
        
        results.sort(Comparator.comparingDouble(SearchResult::getDistance));
        return results;
    }
    
    public List<Material> getPopularItems() {
        Map<Material, Integer> itemCounts = new HashMap<>();
        
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!shop.isActive()) {
                continue;
            }
            
            for (ShopItem item : shop.getItems()) {
                if (item.isAvailable()) {
                    Material material = item.getItemStack().getType();
                    itemCounts.put(material, itemCounts.getOrDefault(material, 0) + 1);
                }
            }
        }
        
        return itemCounts.entrySet().stream()
            .sorted(Map.Entry.<Material, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .limit(10)
            .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
    }
    
    public List<String> getSearchSuggestions(String partialInput) {
        Set<String> suggestions = new HashSet<>();
        String lowerInput = partialInput.toLowerCase();
        
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!shop.isActive()) {
                continue;
            }
            
            for (ShopItem item : shop.getItems()) {
                if (item.isAvailable()) {
                    String itemName = item.getFormattedItemName().toLowerCase();
                    if (itemName.contains(lowerInput)) {
                        suggestions.add(item.getFormattedItemName());
                    }
                    
                    String materialName = item.getItemStack().getType().name().toLowerCase().replace("_", " ");
                    if (materialName.contains(lowerInput)) {
                        suggestions.add(materialName);
                    }
                }
            }
        }
        
        List<String> result = new ArrayList<>(suggestions);
        result.sort(String::compareTo);
        return result.subList(0, Math.min(10, result.size()));
    }
    
    private boolean matchesItemName(ShopItem item, String searchQuery) {
        String query = searchQuery.toLowerCase();
        
        String displayName = item.getFormattedItemName().toLowerCase();
        if (displayName.contains(query)) {
            return true;
        }
        
        String materialName = item.getItemStack().getType().name().toLowerCase().replace("_", " ");
        if (materialName.contains(query)) {
            return true;
        }
        
        if (item.hasDescription() && item.getDescription().toLowerCase().contains(query)) {
            return true;
        }
        
        return false;
    }
}