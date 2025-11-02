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
import java.util.Map;

public class EditItemGUI extends BaseGUI {

    private final Shop shop;
    private final ShopItem shopItem;
    private List<ItemStack> priceItems;
    private int quantity;
    private String description;

    // Slot definitions
    private static final int[] PRICE_SLOTS = {28, 29, 30, 31, 32, 33, 34};

    public EditItemGUI(supershop plugin, Player player, Shop shop, ShopItem shopItem) {
        super(plugin, player, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.title"), 54);
        this.shop = shop;
        this.shopItem = shopItem;
        this.priceItems = new ArrayList<>(shopItem.getPriceItems());
        this.quantity = shopItem.getQuantity();
        this.description = shopItem.getDescription();

        // Make price slots interactable
        addInteractableSlots(PRICE_SLOTS);
    }

    @Override
    protected void setupInventory() {
        fillBorder(Material.ORANGE_STAINED_GLASS_PANE);

        setupItemDisplay();
        setupQuantityControls();
        setupPriceArea();
        setupControls();
    }

    private void setupItemDisplay() {
        ItemStack displayItem = shopItem.getItemStack().clone();
        displayItem.setAmount(Math.min(quantity, 64)); // Visual stack in slot 4
        inventory.setItem(4, displayItem);

        ItemStack itemInfo = createInfoItem(
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.editing-title", shopItem.getFormattedItemName()),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.original-quantity", String.valueOf(shopItem.getQuantity())),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.current-quantity", String.valueOf(quantity)),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.status", (shopItem.isAvailable() ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.available") : plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.unavailable"))));
        inventory.setItem(13, itemInfo);
    }

    private void setupQuantityControls() {
        // Decrease quantity
        ItemStack decreaseQty = createButton(Material.RED_CONCRETE,
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.decrease-1.name"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.decrease-1.lore"));
        inventory.setItem(19, decreaseQty);

        ItemStack decrease10 = createButton(Material.RED_CONCRETE,
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.decrease-10.name"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.decrease-10.lore"));
        inventory.setItem(10, decrease10);

        // Quantity display
        ItemStack quantityDisplay = createInfoItem(plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.quantity-display", String.valueOf(quantity)));
        inventory.setItem(22, quantityDisplay);

        // Increase quantity
        ItemStack increaseQty = createButton(Material.GREEN_CONCRETE,
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.increase-1.name"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.increase-1.lore"));
        inventory.setItem(25, increaseQty);

        ItemStack increase10 = createButton(Material.GREEN_CONCRETE,
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.increase-10.name"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.increase-10.lore"));
        inventory.setItem(16, increase10);
    }

    private void setupPriceArea() {
        // Price items display
        for (int i = 0; i < PRICE_SLOTS.length; i++) {
            if (i < priceItems.size()) {
                inventory.setItem(PRICE_SLOTS[i], priceItems.get(i));
            } else {
                ItemStack placeholder = createButton(Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                    plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.price-placeholder.name", String.valueOf(i + 1)),
                    plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.price-placeholder.lore"));
                inventory.setItem(PRICE_SLOTS[i], placeholder);
            }
        }

        // Clear price button
        if (!priceItems.isEmpty()) {
            ItemStack clearPrice = createButton(Material.BARRIER,
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.clear-price-button.name"),
                plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.clear-price-button.lore"));
            inventory.setItem(37, clearPrice);
        }

        // Description button
        ItemStack descButton = createButton(Material.WRITABLE_BOOK,
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.description-button.name"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.description-button.lore", (description.isEmpty() ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "none") : description)),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.description-button.lore2"));
        inventory.setItem(38, descButton);

        // Toggle availability
        ItemStack toggleAvail = createButton(
            shopItem.isAvailable() ? Material.REDSTONE : Material.EMERALD,
            shopItem.isAvailable() ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.make-unavailable") : plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.make-available"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.toggle-availability.lore"));
        inventory.setItem(39, toggleAvail);
    }

    private void setupControls() {
        // Save changes button
        ItemStack saveChanges = createButton(Material.EMERALD,
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.save-button.name"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.save-button.lore"));
        inventory.setItem(49, saveChanges);

        // Back button
        ItemStack back = createBackButton();
        inventory.setItem(45, back);

        // Remove item button
        ItemStack removeItem = createButton(Material.BARRIER,
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.remove-button.name"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.remove-button.lore1"),
            plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.edititem.remove-button.lore2"));
        inventory.setItem(53, removeItem);
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        ItemStack cursorItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();

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
            case 10: {
                if (quantity > 1) {
                    int toRemove = Math.min(10, quantity - 1);
                    quantity -= toRemove;

                    ItemStack refundItem = shopItem.getItemStack().clone();
                    refundItem.setAmount(toRemove);
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(refundItem);
                    } else {
                        plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.no-inventory-space");
                        return;
                    }
                    refresh();
                } else {
                    plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.min-quantity-1");
                }
                break;
            }
            case 19: {
                if (quantity > 1) {
                    quantity--;
                    ItemStack refundItem = shopItem.getItemStack().clone();
                    refundItem.setAmount(1);
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(refundItem);
                    } else {
                        plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.no-inventory-space");
                        return;
                    }
                    refresh();
                } else {
                    plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.min-quantity-1");
                }
                break;
            }
            case 25: {
                ItemStack targetItem = shopItem.getItemStack().clone();
                targetItem.setAmount(1);

                int available = countItemInInventory(player, targetItem);
                if (available >= 1) {
                    if (player.getInventory().removeItem(targetItem).isEmpty()) {
                        if (isStackable(targetItem)) {
                            quantity++;
                            if (quantity > 64) quantity = 64;
                            refresh();
                        } else {
                            plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.non-stackable");
                        }
                    } else {
                        plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.remove-item-failed");
                    }
                } else {
                    plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.not-enough-items-to-increase");
                }
                break;
            }
            case 16: {
                ItemStack targetItem = shopItem.getItemStack().clone();
                targetItem.setAmount(1);

                int available = countItemInInventory(player, targetItem);
                int toAdd = Math.min(10, available);

                if (toAdd > 0) {
                    if (!isStackable(targetItem)) {
                        plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.non-stackable");
                        return;
                    }
                    int newTotal = quantity + toAdd;
                    if (newTotal > 64) toAdd = 64 - quantity;
                    if (toAdd > 0) {
                        targetItem.setAmount(toAdd);
                        if (player.getInventory().removeItem(targetItem).isEmpty()) {
                            quantity += toAdd;
                            refresh();
                        } else {
                            plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.remove-required-items-failed");
                        }
                    }
                } else {
                    plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.not-enough-items-to-add");
                }
                break;
            }
            case 37: // Clear price items
                priceItems.clear();
                refresh();
                break;
            case 38: // Edit description
                handleEditDescription();
                break;
            case 39: // Toggle availability
                shopItem.setAvailable(!shopItem.isAvailable());
                plugin.getDatabaseManager().saveShopItem(shop.getShopId(), shopItem);
                plugin.getMessageUtils().sendMessage(player, "gui.edititem.availability-toggled");
                refresh();
                break;
            case 49: // Save changes
                handleSaveChanges();
                break;
            case 45: // Back
                close();
                new ShopManagementGUI(plugin, player, shop).open();
                break;
            case 53: // Remove item
                handleRemoveItem();
                break;
        }
    }

    private void handleEditDescription() {
        close();
        plugin.getMessageUtils().sendMessage(player, "gui.edititem.description.prompt");
        plugin.getMessageUtils().sendMessage(player, "gui.edititem.description.current", (description.isEmpty() ? plugin.getLocaleManager().getMessage(player.getUniqueId(), "none") : description));

        // In a full implementation, you would use AsyncPlayerChatEvent to capture the description
        // For now, we'll just show a message
        plugin.getMessageUtils().sendMessage(player, "gui.edititem.description.coming-soon");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new EditItemGUI(plugin, player, shop, shopItem).open();
        }, 60L); // 3 seconds delay
    }

    private void handleSaveChanges() {
        // Clean up price items (remove nulls)
        priceItems.removeIf(item -> item == null);

        if (priceItems.isEmpty()) {
            plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.no-price-set");
            return;
        }

        // Update shop item
        shopItem.setQuantity(quantity);
        shopItem.setPriceItems(priceItems);
        shopItem.setDescription(description);

        // Save to database
        plugin.getDatabaseManager().saveShopItem(shop.getShopId(), shopItem);

        plugin.getMessageUtils().sendMessage(player, "gui.edititem.item-updated-success");

        close();
        new ShopManagementGUI(plugin, player, shop).open();

        int available = countItemInInventory(player, shopItem.getItemStack());
        if (quantity > available) {
            plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.not-enough-items-to-set-quantity", String.valueOf(quantity), String.valueOf(available));
            return;
        }

        if (quantity > 64) {
            plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.quantity-too-high");
            return;
        }

        if (!isStackable(shopItem.getItemStack()) && quantity > 1) {
            plugin.getMessageUtils().sendMessage(player, "gui.edititem.error.non-stackable");
            return;
        }

    }

    private void handleRemoveItem() {
        if (plugin.getShopManager().removeItemFromShop(shop.getShopId(), shopItem.getItemId(), player)) {
            // Return the item to the player
            ItemStack item = shopItem.getItemStack().clone();
            item.setAmount(shopItem.getQuantity());

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);

            // Drop leftovers on the ground if inventory is full
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }

            plugin.getMessageUtils().sendMessage(player, "gui.edititem.item-returned-to-inventory");

            close();
            new ShopManagementGUI(plugin, player, shop).open();
        }
    }


    private int countItemInInventory(Player player, ItemStack target) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(target)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private boolean isStackable(ItemStack item) {
        return item.getMaxStackSize() > 1;
    }

}
