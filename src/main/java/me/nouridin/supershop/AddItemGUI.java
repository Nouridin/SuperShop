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

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddItemGUI extends BaseGUI {
    
    private final Shop shop;
    private ItemStack selectedItem;
    private List<ItemStack> priceItems;
    private String description;
    private int quantity = 1;


    // Slot definitions
    private static final int ITEM_SLOT = 20;
    private static final int[] PRICE_SLOTS = {28, 29, 30, 31, 32, 33, 34};

    public AddItemGUI(supershop plugin, Player player, Shop shop) {
        super(plugin, player, "&4&lAdd Item to Shop", 54);
        this.shop = shop;
        this.priceItems = new ArrayList<>();
        this.description = "";

        // Make item and price slots interactable
        addInteractableSlot(ITEM_SLOT);
        addInteractableSlots(PRICE_SLOTS);
    }

    @Override
    protected void setupInventory() {
        fillBorder(Material.GREEN_STAINED_GLASS_PANE);

        setupInstructions();
        setupItemSelection();
        setupPriceArea();
        setupControls();
    }

    private void setupInstructions() {
        ItemStack instructions = createInfoItem("&6How to Add Items",
                "&71. Drag the item you want to sell into the slot below",
                "&72. Click 'Set Price Items' to choose currency items",
                "&73. Click 'Add Item' to confirm");
        inventory.setItem(4, instructions);
    }


    private void setupItemSelection() {
        // Item slot
        if (selectedItem != null) {
            inventory.setItem(ITEM_SLOT, selectedItem);
        } else {
            ItemStack placeholder = createButton(Material.GRAY_STAINED_GLASS_PANE,
                    "&7Drag item here", "&eDrag an item from your inventory to sell it");
            inventory.setItem(ITEM_SLOT, placeholder);
        }
    }

    private void setupPriceArea() {
        // Price items display
        for (int i = 0; i < PRICE_SLOTS.length; i++) {
            if (i < priceItems.size()) {
                inventory.setItem(PRICE_SLOTS[i], priceItems.get(i));
            } else {
                ItemStack placeholder = createButton(Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                    "&7Price Item " + (i + 1), "&7Set price using the button below");
                inventory.setItem(PRICE_SLOTS[i], placeholder);
            }
        }

        // Set price button (replaces drag-and-drop functionality)
        ItemStack setPriceButton = createButton(Material.GOLD_INGOT, "&eSet Price Items",
            "&7Click to open price selection menu",
            "&7Choose items and quantities without needing them in inventory",
            "&7Current price items: &f" + (priceItems.isEmpty() ? "None" : priceItems.size()));
        inventory.setItem(36, setPriceButton);

        // Clear price button
        if (!priceItems.isEmpty()) {
            ItemStack clearPrice = createButton(Material.BARRIER, "&cClear Price Items",
                "&7Remove all price items");
            inventory.setItem(37, clearPrice);
        }

        // Description button
        ItemStack descButton = createButton(Material.WRITABLE_BOOK, "&eSet Description",
            "&7Current: &f" + (description.isEmpty() ? "None" : description),
            "&7Click to set item description");
        inventory.setItem(38, descButton);
    }

    private void setupControls() {
        // Add item button
        if (selectedItem != null && !priceItems.isEmpty()) {
            ItemStack addItem = createButton(Material.EMERALD, "&aAdd Item to Shop",
                "&7Click to add this item to your shop");
            inventory.setItem(49, addItem);
        }

        // Back button
        ItemStack back = createBackButton();
        inventory.setItem(45, back);

        // Close button
        ItemStack close = createCloseButton();
        inventory.setItem(53, close);
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Handle item placement in item slot
        if (slot == ITEM_SLOT) {
            if (cursorItem != null && cursorItem.getType() != Material.AIR && !isPlaceholder(cursorItem)) {
                selectedItem = cursorItem.clone(); // Save full stack
                quantity = selectedItem.getAmount(); // Store full quantity
                ItemStack display = selectedItem.clone();
                display.setAmount(1); // Only show 1 in GUI
                inventory.setItem(ITEM_SLOT, display);
                event.setCursor(null);
                refresh();
                return;
            } else if (clickedItem != null && !isPlaceholder(clickedItem)) {
                selectedItem = null;
                quantity = 1;
                refresh();
                return;
            }
        }



        // Handle price item placement
        for (int i = 0; i < PRICE_SLOTS.length; i++) {
            if (slot == PRICE_SLOTS[i]) {
                if (cursorItem != null && cursorItem.getType() != Material.AIR && !isPlaceholder(cursorItem)) {
                    // Player is placing a price item
                    ItemStack priceItem = cursorItem.clone();
                    if (i < priceItems.size()) {
                        priceItems.set(i, priceItem);
                    } else {
                        // Fill any gaps in the list
                        while (priceItems.size() < i) {
                            priceItems.add(null);
                        }
                        priceItems.add(priceItem);
                    }
                    inventory.setItem(PRICE_SLOTS[i], priceItem);
                    event.setCursor(null);
                    refresh();
                    return;
                } else if (clickedItem != null && !isPlaceholder(clickedItem)) {
                    // Player is taking a price item back
                    if (i < priceItems.size()) {
                        priceItems.remove(i);
                        refresh();
                        return;
                    }
                }
            }
        }

        // Handle control buttons (these should be cancelled)
        event.setCancelled(true);

        switch (slot) {
            case 36: // Set price items
                close(); // This will trigger the unregister via GUIManager
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    new PriceSelectionGUI(plugin, player, this).open();
                });
                break;
            case 37: // Clear price items
                priceItems.clear();
                refresh();
                break;
            case 38: // Set description
                handleSetDescription();
                break;
            case 49: // Add item
                handleAddItem();
                break;
            case 45: // Back
                close();
                new ShopManagementGUI(plugin, player, shop).open();
                break;
            case 53: // Close
                close();
                break;
        }
    }

    private void handleSetDescription() {
        close();
        MessageUtils.sendMessage(player, "&eEnter item description in chat (or type 'cancel' to cancel):");
        MessageUtils.sendMessage(player, "&7Current description: &f" + (description.isEmpty() ? "None" : description));

        // In a full implementation, you would use AsyncPlayerChatEvent to capture the description
        // For now, we'll just show a message
        MessageUtils.sendMessage(player, "&7Description feature coming soon! Reopening shop management...");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new ShopManagementGUI(plugin, player, shop).open();
        }, 60L); // 3 seconds delay
    }

    private void handleAddItem() {
        if (selectedItem == null) {
            MessageUtils.sendMessage(player, "&cPlease select an item to sell!");
            return;
        }

        ItemStack itemToSell = selectedItem.clone();
        itemToSell.setAmount(quantity);


        // Clean up price items (remove nulls)
        priceItems.removeIf(item -> item == null);

        if (priceItems.isEmpty()) {
            MessageUtils.sendMessage(player, "&cPlease set at least one price item!");
            return;
        }

        int maxStackSize = selectedItem.getMaxStackSize();

        if (maxStackSize == 1 && quantity > 1) {
            MessageUtils.sendMessage(player, "&cThis item is non-stackable. You can only sell 1 at a time.");
            return;
        }

        if (quantity > 64) {
            MessageUtils.sendMessage(player, "&cYou cannot sell more than 64 of an item at once.");
            return;
        }

        if (!hasEnoughItems(selectedItem, quantity)) {
            MessageUtils.sendMessage(player, "&cYou don't have enough of this item!");
            return;
        }

        // Remove from inventory
        removeItemsFromInventory(selectedItem, quantity);

        // Clone item and set the correct amount
        itemToSell.setAmount(quantity);

        UUID itemId = UUID.randomUUID();
        ShopItem shopItem = new ShopItem(itemId, itemToSell, quantity, description, priceItems);

        if (plugin.getShopManager().addItemToShop(shop.getShopId(), shopItem, player)) {
            close();
            new ShopManagementGUI(plugin, player, shop).open();
        }
    }



    private boolean hasEnoughItems(ItemStack item, int needed) {
        int count = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.isSimilar(item)) {
                count += invItem.getAmount();
                if (count >= needed) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeItemsFromInventory(ItemStack item, int amount) {
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

    // Method called by PriceSelectionGUI to set the price items
    public void setPriceItems(List<ItemStack> priceItems) {
        this.priceItems = new ArrayList<>(priceItems);
    }
}