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

import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.UUID;

public class ShopItem {
    
    private UUID itemId;
    private ItemStack itemStack;
    private int quantity;
    private String description;
    private List<ItemStack> priceItems;
    private boolean isAvailable;
    
    public ShopItem(UUID itemId, ItemStack itemStack, int quantity, List<ItemStack> priceItems) {
        this.itemId = itemId;
        this.itemStack = itemStack.clone();
        this.quantity = quantity;
        this.priceItems = priceItems;
        this.description = "";
        this.isAvailable = true;
    }
    
    public ShopItem(UUID itemId, ItemStack itemStack, int quantity, String description, List<ItemStack> priceItems) {
        this(itemId, itemStack, quantity, priceItems);
        this.description = description;
    }
    
    // Getters and Setters
    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }
    
    public ItemStack getItemStack() { return itemStack.clone(); }
    public void setItemStack(ItemStack itemStack) { this.itemStack = itemStack.clone(); }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<ItemStack> getPriceItems() { return priceItems; }
    public void setPriceItems(List<ItemStack> priceItems) { this.priceItems = priceItems; }
    
    public boolean isAvailable() { return isAvailable && quantity > 0; }
    public void setAvailable(boolean available) { isAvailable = available; }
    
    public boolean reduceQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }
    
    public void addQuantity(int amount) {
        quantity += amount;
    }
    
    public String getFormattedPrice() {
        if (priceItems.isEmpty()) {
            return "Free";
        }
        
        StringBuilder priceString = new StringBuilder();
        for (int i = 0; i < priceItems.size(); i++) {
            ItemStack priceItem = priceItems.get(i);
            priceString.append(priceItem.getAmount())
                      .append("x ")
                      .append(getItemDisplayName(priceItem));
            
            if (i < priceItems.size() - 1) {
                priceString.append(" + ");
            }
        }
        
        return priceString.toString();
    }
    
    private String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().toLowerCase().replace("_", " ");
    }
    
    public String getFormattedItemName() {
        return getItemDisplayName(itemStack);
    }
    
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public ItemStack toItemStack() {
        ItemStack clone = itemStack.clone();
        clone.setAmount(quantity); // make sure it respects the stored quantity
        return clone;
    }

}