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

package me.nouridin.supershop.util;

import me.nouridin.supershop.models.Shop;
import me.nouridin.supershop.models.ShopItem;
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