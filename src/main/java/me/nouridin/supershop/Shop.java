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

package me.nouridin.supershop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Shop {
    
    private UUID shopId;
    private UUID ownerId;
    private String ownerName;
    private Location location;
    private List<ShopItem> items;
    private List<ItemStack> revenue; // Store payment items here
    private boolean isActive;
    private long createdAt;
    private long lastUpdated;
    
    public Shop(UUID shopId, UUID ownerId, String ownerName, Location location) {
        this.shopId = shopId;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.location = location;
        this.items = new ArrayList<>();
        this.revenue = new ArrayList<>(); // Initialize revenue collection
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public UUID getShopId() { return shopId; }
    public void setShopId(UUID shopId) { this.shopId = shopId; }
    
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    
    public List<ShopItem> getItems() { return items; }
    public void setItems(List<ShopItem> items) { this.items = items; }
    
    public List<ItemStack> getRevenue() { return revenue; }
    public void setRevenue(List<ItemStack> revenue) { this.revenue = revenue; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { 
        isActive = active; 
        updateLastModified();
    }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public void addItem(ShopItem item) {
        this.items.add(item);
        updateLastModified();
    }
    
    public void removeItem(ShopItem item) {
        this.items.remove(item);
        updateLastModified();
    }
    
    // Revenue management methods
    public void addRevenue(ItemStack item) {
        // Try to stack with existing items
        for (ItemStack existingItem : revenue) {
            if (existingItem.isSimilar(item)) {
                int maxStack = existingItem.getMaxStackSize();
                int canAdd = maxStack - existingItem.getAmount();
                if (canAdd > 0) {
                    int toAdd = Math.min(canAdd, item.getAmount());
                    existingItem.setAmount(existingItem.getAmount() + toAdd);
                    item.setAmount(item.getAmount() - toAdd);
                    if (item.getAmount() <= 0) {
                        return; // All items stacked
                    }
                }
            }
        }
        
        // If we still have items left, add as new stack
        if (item.getAmount() > 0) {
            revenue.add(item.clone());
        }
        updateLastModified();
    }
    
    public void addRevenue(List<ItemStack> items) {
        for (ItemStack item : items) {
            if (item != null && item.getAmount() > 0) {
                addRevenue(item.clone());
            }
        }
    }
    
    public void clearRevenue() {
        revenue.clear();
        updateLastModified();
    }
    
    public boolean hasRevenue() {
        return !revenue.isEmpty();
    }
    
    public int getRevenueItemCount() {
        return revenue.size();
    }
    
    public int getTotalRevenueItems() {
        return revenue.stream().mapToInt(ItemStack::getAmount).sum();
    }
    
    private void updateLastModified() {
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public String getWorldName() {
        return location.getWorld() != null ? location.getWorld().getName() : "unknown";
    }
    
    public String getCoordinatesString() {
        return String.format("(%d, %d, %d)", 
            location.getBlockX(), 
            location.getBlockY(), 
            location.getBlockZ());
    }
    
    public double getDistanceFrom(Location otherLocation) {
        if (!location.getWorld().equals(otherLocation.getWorld())) {
            return Double.MAX_VALUE;
        }
        return location.distance(otherLocation);
    }
}