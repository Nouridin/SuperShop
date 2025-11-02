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

package me.nouridin.supershop.gui;

import me.nouridin.supershop.models.Shop;
import me.nouridin.supershop.models.ShopItem;
import me.nouridin.supershop.supershop;
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
        super(plugin, player, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.title"), 54);
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
        ItemStack instructions = createInfoItem(
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.instructions.title"),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.instructions.line1"),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.instructions.line2"),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.instructions.line3"));
        inventory.setItem(4, instructions);
    }


    private void setupItemSelection() {
        // Item slot
        if (selectedItem != null) {
            inventory.setItem(ITEM_SLOT, selectedItem);
        } else {
            ItemStack placeholder = createButton(Material.GRAY_STAINED_GLASS_PANE,
                    plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.item-placeholder.name"),
                    plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.item-placeholder.lore"));
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
                    plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.price-placeholder.name", String.valueOf(i + 1)),
                    plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.price-placeholder.lore"));
                inventory.setItem(PRICE_SLOTS[i], placeholder);
            }
        }

        // Set price button (replaces drag-and-drop functionality)
        ItemStack setPriceButton = createButton(Material.GOLD_INGOT,
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.set-price-button.name"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.set-price-button.lore1"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.set-price-button.lore2"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.set-price-button.lore3", String.valueOf(priceItems.isEmpty() ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "none") : priceItems.size())));
        inventory.setItem(36, setPriceButton);

        // Clear price button
        if (!priceItems.isEmpty()) {
            ItemStack clearPrice = createButton(Material.BARRIER,
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.clear-price-button.name"),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.clear-price-button.lore"));
            inventory.setItem(37, clearPrice);
        }

        // Description button
        ItemStack descButton = createButton(Material.WRITABLE_BOOK,
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.description-button.name"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.description-button.lore", (description.isEmpty() ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "none") : description)),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.description-button.lore2"));
        inventory.setItem(38, descButton);
    }

    private void setupControls() {
        // Add item button
        if (selectedItem != null && !priceItems.isEmpty()) {
            ItemStack addItem = createButton(Material.EMERALD,
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.add-button.name"),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.additem.add-button.lore"));
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
        plugin.getMessageUtils().sendMessage(player, "gui.additem.description.prompt");
        plugin.getMessageUtils().sendMessage(player, "gui.additem.description.current", (description.isEmpty() ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "none") : description));

        // In a full implementation, you would use AsyncPlayerChatEvent to capture the description
        // For now, we'll just show a message
        plugin.getMessageUtils().sendMessage(player, "gui.additem.description.coming-soon");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new ShopManagementGUI(plugin, player, shop).open();
        }, 60L); // 3 seconds delay
    }

    private void handleAddItem() {
        if (selectedItem == null) {
            plugin.getMessageUtils().sendMessage(player, "gui.additem.error.no-item-selected");
            return;
        }

        ItemStack itemToSell = selectedItem.clone();
        itemToSell.setAmount(quantity);


        // Clean up price items (remove nulls)
        priceItems.removeIf(item -> item == null);

        if (priceItems.isEmpty()) {
            plugin.getMessageUtils().sendMessage(player, "gui.additem.error.no-price-set");
            return;
        }

        int maxStackSize = selectedItem.getMaxStackSize();

        if (maxStackSize == 1 && quantity > 1) {
            plugin.getMessageUtils().sendMessage(player, "gui.additem.error.non-stackable");
            return;
        }

        if (quantity > 64) {
            plugin.getMessageUtils().sendMessage(player, "gui.additem.error.quantity-too-high");
            return;
        }

        if (!hasEnoughItems(selectedItem, quantity)) {
            plugin.getMessageUtils().sendMessage(player, "gui.additem.error.not-enough-items");
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
