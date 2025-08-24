package me.nouridin.supershop;

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
        super(plugin, player, "&4&lEdit Item", 54);
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

        ItemStack itemInfo = createInfoItem("&eEditing: &f" + shopItem.getFormattedItemName(),
                "&7Original quantity: &f" + shopItem.getQuantity(),
                "&7Current quantity: &f" + quantity,
                "&7Status: " + (shopItem.isAvailable() ? "&aAvailable" : "&cUnavailable"));
        inventory.setItem(13, itemInfo);
    }

    private void setupQuantityControls() {
        // Decrease quantity
        ItemStack decreaseQty = createButton(Material.RED_CONCRETE, "&c-1", 
            "&7Click to decrease quantity");
        inventory.setItem(19, decreaseQty);
        
        ItemStack decrease10 = createButton(Material.RED_CONCRETE, "&c-10", 
            "&7Click to decrease quantity by 10");
        inventory.setItem(10, decrease10);
        
        // Quantity display
        ItemStack quantityDisplay = createInfoItem("&eQuantity: &f" + quantity);
        inventory.setItem(22, quantityDisplay);
        
        // Increase quantity
        ItemStack increaseQty = createButton(Material.GREEN_CONCRETE, "&a+1", 
            "&7Click to increase quantity");
        inventory.setItem(25, increaseQty);
        
        ItemStack increase10 = createButton(Material.GREEN_CONCRETE, "&a+10", 
            "&7Click to increase quantity by 10");
        inventory.setItem(16, increase10);
    }
    
    private void setupPriceArea() {
        // Price items display
        for (int i = 0; i < PRICE_SLOTS.length; i++) {
            if (i < priceItems.size()) {
                inventory.setItem(PRICE_SLOTS[i], priceItems.get(i));
            } else {
                ItemStack placeholder = createButton(Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                    "&7Price Item " + (i + 1), "&eDrag an item here to set as price");
                inventory.setItem(PRICE_SLOTS[i], placeholder);
            }
        }
        
        // Clear price button
        if (!priceItems.isEmpty()) {
            ItemStack clearPrice = createButton(Material.BARRIER, "&cClear Price Items",
                "&7Remove all price items");
            inventory.setItem(37, clearPrice);
        }
        
        // Description button
        ItemStack descButton = createButton(Material.WRITABLE_BOOK, "&eEdit Description",
            "&7Current: &f" + (description.isEmpty() ? "None" : description),
            "&7Click to edit item description");
        inventory.setItem(38, descButton);
        
        // Toggle availability
        ItemStack toggleAvail = createButton(
            shopItem.isAvailable() ? Material.REDSTONE : Material.EMERALD,
            shopItem.isAvailable() ? "&cMake Unavailable" : "&aMake Available",
            "&7Click to toggle item availability");
        inventory.setItem(39, toggleAvail);
    }
    
    private void setupControls() {
        // Save changes button
        ItemStack saveChanges = createButton(Material.EMERALD, "&aSave Changes",
            "&7Click to save all changes to this item");
        inventory.setItem(49, saveChanges);
        
        // Back button
        ItemStack back = createBackButton();
        inventory.setItem(45, back);
        
        // Remove item button
        ItemStack removeItem = createButton(Material.BARRIER, "&cRemove Item",
            "&7Click to remove this item from the shop",
            "&c&lWARNING: This cannot be undone!");
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
                        MessageUtils.sendMessage(player, "&cNo space in your inventory to return the item!");
                        return;
                    }
                    refresh();
                } else {
                    MessageUtils.sendMessage(player, "&cCannot reduce below quantity of 1.");
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
                        MessageUtils.sendMessage(player, "&cNo space in your inventory to return the item!");
                        return;
                    }
                    refresh();
                } else {
                    MessageUtils.sendMessage(player, "&cMinimum quantity is 1.");
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
                            MessageUtils.sendMessage(player, "&cThis item cannot be stacked. Quantity must remain 1.");
                        }
                    } else {
                        MessageUtils.sendMessage(player, "&cFailed to remove item from your inventory.");
                    }
                } else {
                    MessageUtils.sendMessage(player, "&cYou don’t have enough of the item to increase the quantity.");
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
                        MessageUtils.sendMessage(player, "&cThis item cannot be stacked. Quantity must remain 1.");
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
                            MessageUtils.sendMessage(player, "&cFailed to remove the required items.");
                        }
                    }
                } else {
                    MessageUtils.sendMessage(player, "&cYou don't have enough of the item to add more.");
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
                MessageUtils.sendMessage(player, "&aItem availability toggled!");
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
        MessageUtils.sendMessage(player, "&eEnter new description in chat (or type 'cancel' to cancel):");
        MessageUtils.sendMessage(player, "&7Current description: &f" + (description.isEmpty() ? "None" : description));
        
        // In a full implementation, you would use AsyncPlayerChatEvent to capture the description
        // For now, we'll just show a message
        MessageUtils.sendMessage(player, "&7Description editing feature coming soon! Reopening edit menu...");
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new EditItemGUI(plugin, player, shop, shopItem).open();
        }, 60L); // 3 seconds delay
    }
    
    private void handleSaveChanges() {
        // Clean up price items (remove nulls)
        priceItems.removeIf(item -> item == null);
        
        if (priceItems.isEmpty()) {
            MessageUtils.sendMessage(player, "&cPlease set at least one price item!");
            return;
        }
        
        // Update shop item
        shopItem.setQuantity(quantity);
        shopItem.setPriceItems(priceItems);
        shopItem.setDescription(description);
        
        // Save to database
        plugin.getDatabaseManager().saveShopItem(shop.getShopId(), shopItem);
        
        MessageUtils.sendMessage(player, "&aItem updated successfully!");
        
        close();
        new ShopManagementGUI(plugin, player, shop).open();

        int available = countItemInInventory(player, shopItem.getItemStack());
        if (quantity > available) {
            MessageUtils.sendMessage(player, "&cYou don't have enough of the item to set that quantity (" + quantity + " > " + available + ").");
            return;
        }

        if (quantity > 64) {
            MessageUtils.sendMessage(player, "&cYou cannot save an item with quantity greater than 64.");
            return;
        }

        if (!isStackable(shopItem.getItemStack()) && quantity > 1) {
            MessageUtils.sendMessage(player, "&cThis item cannot be stacked. Quantity must be 1.");
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

            MessageUtils.sendMessage(player, "&aItem returned to your inventory!");

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