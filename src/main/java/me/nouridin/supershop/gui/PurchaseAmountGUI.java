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

import java.util.UUID;

public class PurchaseAmountGUI extends BaseGUI {

    private final Shop shop;
    private final ShopItem shopItem;
    private int selectedAmount;

    public PurchaseAmountGUI(supershop plugin, Player player, Shop shop, ShopItem shopItem) {
        super(plugin, player, plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.purchaseamount.title"), 27);
        this.shop = shop;
        this.shopItem = shopItem;
        this.selectedAmount = 1;
    }

    @Override
    protected void setupInventory() {
        fillBorder(Material.ORANGE_STAINED_GLASS_PANE);

        setupItemDisplay();
        setupAmountControls();
        setupActionButtons();
    }

    private void setupItemDisplay() {
        UUID playerUUID = player.getUniqueId();
        ItemStack displayItem = shopItem.getItemStack().clone();
        inventory.setItem(4, displayItem);

        ItemStack itemInfo = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.item-name", shopItem.getFormattedItemName()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.available", String.valueOf(shopItem.getQuantity())),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.price-per-item", shopItem.getFormattedPrice()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.total-cost", calculateTotalCost()),
            "",
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.seller", shop.getOwnerName()));
        inventory.setItem(13, itemInfo);
    }

    private void setupAmountControls() {
        UUID playerUUID = player.getUniqueId();
        // Decrease buttons
        ItemStack decrease10 = createButton(Material.RED_CONCRETE,
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.decrease-10.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.decrease-10.lore"));
        inventory.setItem(9, decrease10);

        ItemStack decrease1 = createButton(Material.RED_CONCRETE,
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.decrease-1.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.decrease-1.lore"));
        inventory.setItem(10, decrease1);

        // Amount display
        ItemStack amountDisplay = createInfoItem(plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.amount-display", String.valueOf(selectedAmount)),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.max-available", String.valueOf(shopItem.getQuantity())));
        inventory.setItem(11, amountDisplay);

        // Increase buttons
        ItemStack increase1 = createButton(Material.GREEN_CONCRETE,
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.increase-1.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.increase-1.lore"));
        inventory.setItem(12, increase1);

        ItemStack increase10 = createButton(Material.GREEN_CONCRETE,
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.increase-10.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.increase-10.lore"));
        inventory.setItem(14, increase10);

        // Max button
        ItemStack maxButton = createButton(Material.EMERALD,
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.max-button.name", String.valueOf(shopItem.getQuantity())),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.max-button.lore"));
        inventory.setItem(15, maxButton);
    }

    private void setupActionButtons() {
        UUID playerUUID = player.getUniqueId();
        // Check if player can afford
        boolean canAfford = canPlayerAfford();

        // Purchase button
        ItemStack purchase = createButton(
            canAfford ? Material.DIAMOND : Material.BARRIER,
            canAfford ? plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.purchase-button.name") : plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.cannot-afford-button.name"),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.purchase-button.lore1", String.valueOf(selectedAmount), shopItem.getFormattedItemName()),
            plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.purchase-button.lore2", calculateTotalCost()),
            "",
            canAfford ? plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.purchase-button.confirm-lore") : plugin.getLocaleManager().getMessage(playerUUID, "gui.purchaseamount.purchase-button.not-enough-lore"));
        inventory.setItem(21, purchase);

        // Back button
        ItemStack back = createBackButton();
        inventory.setItem(18, back);

        // Close button
        ItemStack close = createCloseButton();
        inventory.setItem(26, close);
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        switch (slot) {
            case 9: // -10
                selectedAmount = Math.max(1, selectedAmount - 10);
                refresh();
                break;
            case 10: // -1
                selectedAmount = Math.max(1, selectedAmount - 1);
                refresh();
                break;
            case 12: // +1
                selectedAmount = Math.min(shopItem.getQuantity(), selectedAmount + 1);
                refresh();
                break;
            case 14: // +10
                selectedAmount = Math.min(shopItem.getQuantity(), selectedAmount + 10);
                refresh();
                break;
            case 15: // Max
                selectedAmount = shopItem.getQuantity();
                refresh();
                break;
            case 21: // Purchase
                handlePurchase();
                break;
            case 18: // Back
                close();
                new ShopBrowseGUI(plugin, player, shop).open();
                break;
            case 26: // Close
                close();
                break;
        }
    }

    private void handlePurchase() {
        if (!canPlayerAfford()) {
            plugin.getMessageUtils().sendMessage(player, "gui.purchaseamount.error.cannot-afford");
            plugin.getMessageUtils().sendMessage(player, "gui.purchaseamount.error.needs", calculateTotalCost());
            return;
        }

        if (selectedAmount > shopItem.getQuantity()) {
            plugin.getMessageUtils().sendMessage(player, "gui.purchaseamount.error.not-enough-stock");
            return;
        }

        if (plugin.getShopManager().processPurchase(player, shop, shopItem, selectedAmount)) {
            close();
            plugin.getMessageUtils().sendMessage(player, "gui.purchaseamount.purchase-success.title");
            plugin.getMessageUtils().sendMessage(player, "gui.purchaseamount.purchase-success.summary", String.valueOf(selectedAmount), shopItem.getFormattedItemName());
        }
    }

    private String calculateTotalCost() {
        if (shopItem.getPriceItems().isEmpty()) {
            return plugin.getLocaleManager().getMessage(player.getUniqueId(), "gui.free");
        }

        StringBuilder cost = new StringBuilder();
        for (int i = 0; i < shopItem.getPriceItems().size(); i++) {
            ItemStack priceItem = shopItem.getPriceItems().get(i);
            int totalAmount = priceItem.getAmount() * selectedAmount;

            cost.append(totalAmount).append("x ");
            if (priceItem.hasItemMeta() && priceItem.getItemMeta().hasDisplayName()) {
                cost.append(plugin.getMessageUtils().stripColor(priceItem.getItemMeta().getDisplayName()));
            } else {
                cost.append(priceItem.getType().name().toLowerCase().replace("_", " "));
            }

            if (i < shopItem.getPriceItems().size() - 1) {
                cost.append(" + ");
            }
        }

        return cost.toString();
    }

    private boolean canPlayerAfford() {
        for (ItemStack priceItem : shopItem.getPriceItems()) {
            int neededAmount = priceItem.getAmount() * selectedAmount;
            if (!hasItem(player, priceItem, neededAmount)) {
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
}
