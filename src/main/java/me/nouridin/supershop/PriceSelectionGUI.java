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
import java.util.Arrays;
import java.util.List;

public class PriceSelectionGUI extends BaseGUI {

    private final AddItemGUI parentGUI;
    private final List<PriceItem> priceItems;
    private int currentPage = 0;
    private final int itemsPerPage = 28; // 4 rows of 7 items

    // Common materials that can be used as currency
    private static final Material[] CURRENCY_MATERIALS = {
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT,
         Material.NETHERITE_INGOT, Material.COPPER_INGOT, Material.COAL, Material.REDSTONE,
         Material.LAPIS_LAZULI, Material.QUARTZ, Material.EXPERIENCE_BOTTLE, Material.ENDER_PEARL,
         Material.BLAZE_ROD, Material.GHAST_TEAR, Material.NETHER_STAR, Material.DRAGON_BREATH,
          Material.TOTEM_OF_UNDYING, Material.ELYTRA, Material.ENCHANTED_GOLDEN_APPLE,
          Material.GOLDEN_APPLE, Material.BREAD, Material.COOKED_BEEF, Material.WHEAT,
         Material.CARROT, Material.POTATO, Material.BEETROOT, Material.SWEET_BERRIES,
         Material.COBBLESTONE, Material.STONE, Material.OAK_LOG, Material.SPRUCE_LOG,
          Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
          Material.CRIMSON_STEM, Material.WARPED_STEM, Material.SAND, Material.GRAVEL,
          Material.DIRT, Material.GRASS_BLOCK, Material.OBSIDIAN, Material.END_STONE,
            Material.NETHERRACK, Material.SOUL_SAND, Material.GLOWSTONE, Material.SEA_LANTERN
    };

    public PriceSelectionGUI(supershop plugin, Player player, AddItemGUI parentGUI) {
        super(plugin, player, "&4&lSelect Price Items", 54);
        this.parentGUI = parentGUI;
        this.priceItems = new ArrayList<>();
    }

    @Override
    protected void setupInventory() {
        fillBorder(Material.BLUE_STAINED_GLASS_PANE);

        setupInstructions();
        setupMaterialGrid();
        setupCurrentPriceItems();
        setupControls();
    }

    private void setupInstructions() {
        ItemStack instructions = createInfoItem("&6Price Selection",
                "&7Click on materials to add them as price items",
                "&7Use +/- buttons to adjust quantities",
                "&7Click 'Done' when finished");
        inventory.setItem(4, instructions);
    }

    private void setupMaterialGrid() {
        int startSlot = 10;
        int currentSlot = startSlot;
        int itemsShown = 0;
        int startIndex = currentPage * itemsPerPage;

        for (int i = startIndex; i < CURRENCY_MATERIALS.length && itemsShown < itemsPerPage; i++) {
            Material material = CURRENCY_MATERIALS[i];

            // Skip to next row if we've filled 7 items in current row
            if ((currentSlot - startSlot) % 9 >= 7) {
                currentSlot = startSlot + ((currentSlot - startSlot) / 9 + 1) * 9;
            }

            // Stop if we've gone past the available area
            if (currentSlot >= 46) break;

            ItemStack item = new ItemStack(material, 1);
            ItemStack button = createButton(material, "&e" + getDisplayName(material),
                    "&7Click to add as price item",
                    "&7Current in price: &f" + getCurrentQuantity(material));

            inventory.setItem(currentSlot, button);
            currentSlot++;
            itemsShown++;
        }
    }

    private void setupCurrentPriceItems() {
        // Show current price items in the bottom area
        ItemStack priceLabel = createInfoItem("&6Current Price Items",
                "&7Items that will be required as payment:");
        inventory.setItem(46, priceLabel);

        for (int i = 0; i < Math.min(priceItems.size(), 6); i++) {
            PriceItem priceItem = priceItems.get(i);
            ItemStack display = new ItemStack(priceItem.getMaterial(), priceItem.getQuantity());
            ItemStack button = createButton(priceItem.getMaterial(),
                    "&e" + priceItem.getQuantity() + "x " + getDisplayName(priceItem.getMaterial()),
                    "&7Left click: +1",
                    "&7Right click: -1",
                    "&7Shift+Right click: Remove");
            inventory.setItem(47 + i, button);
        }
    }

    private void setupControls() {
        // Page navigation
        if (currentPage > 0) {
            ItemStack prevPage = createButton(Material.ARROW, "&7← Previous Page");
            inventory.setItem(45, prevPage);
        }

        if ((currentPage + 1) * itemsPerPage < CURRENCY_MATERIALS.length) {
            ItemStack nextPage = createButton(Material.ARROW, "&7Next Page →");
            inventory.setItem(53, nextPage);
        }

        // Done button
        ItemStack done = createButton(Material.EMERALD, "&aDone",
                "&7Click to confirm price selection");
        inventory.setItem(49, done);

        // Back button
        ItemStack back = createBackButton();
        inventory.setItem(48, back);
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) return;

        // Handle material selection (slots 10-45, excluding borders and specific areas)
        if (slot >= 10 && slot <= 45 && !isInBorderOrSpecialArea(slot)) {
            Material material = clickedItem.getType();
            if (isValidCurrencyMaterial(material)) {
                addOrUpdatePriceItem(material, 1);
                refresh();
                return;
            }
        }

        // Handle current price item modification (slots 47-52)
        if (slot >= 47 && slot <= 52) {
            int priceIndex = slot - 47;
            if (priceIndex < priceItems.size()) {
                PriceItem priceItem = priceItems.get(priceIndex);

                if (event.isLeftClick()) {
                    // Increase quantity
                    priceItem.setQuantity(Math.min(priceItem.getQuantity() + 1, 64));
                } else if (event.isRightClick()) {
                    if (event.isShiftClick()) {
                        // Remove item
                        priceItems.remove(priceIndex);
                    } else {
                        // Decrease quantity
                        int newQuantity = priceItem.getQuantity() - 1;
                        if (newQuantity <= 0) {
                            priceItems.remove(priceIndex);
                        } else {
                            priceItem.setQuantity(newQuantity);
                        }
                    }
                }
                refresh();
                return;
            }
        }

        // Handle control buttons
        switch (slot) {
            case 45: // Previous page
                if (currentPage > 0) {
                    currentPage--;
                    refresh();
                }
                break;
            case 53: // Next page
                if ((currentPage + 1) * itemsPerPage < CURRENCY_MATERIALS.length) {
                    currentPage++;
                    refresh();
                }
                break;
            case 49: // Done
                handleDone();
                break;
            case 48: // Back
                close();
                parentGUI.open();
                break;
        }
    }

    private void handleDone() {
        // Convert PriceItem list to ItemStack list
        List<ItemStack> itemStacks = new ArrayList<>();
        for (PriceItem priceItem : priceItems) {
            itemStacks.add(new ItemStack(priceItem.getMaterial(), priceItem.getQuantity()));
        }

        // Set the price items in the parent GUI
        parentGUI.setPriceItems(itemStacks);

        close();
        parentGUI.open();
    }

    private void addOrUpdatePriceItem(Material material, int quantity) {
        // Check if this material is already in the price list
        for (PriceItem priceItem : priceItems) {
            if (priceItem.getMaterial() == material) {
                priceItem.setQuantity(Math.min(priceItem.getQuantity() + quantity, 64));
                return;
            }
        }

        // Add new price item if not found and we have space
        if (priceItems.size() < 6) {
            priceItems.add(new PriceItem(material, quantity));
        }
    }

    private int getCurrentQuantity(Material material) {
        for (PriceItem priceItem : priceItems) {
            if (priceItem.getMaterial() == material) {
                return priceItem.getQuantity();
            }
        }
        return 0;
    }

    private boolean isValidCurrencyMaterial(Material material) {
        return Arrays.asList(CURRENCY_MATERIALS).contains(material);
    }

    private boolean isInBorderOrSpecialArea(int slot) {
        // Check if slot is in border or special areas we want to avoid
        return slot == 46 || (slot >= 47 && slot <= 52);
    }

    private String getDisplayName(Material material) {
        return material.name().toLowerCase().replace("_", " ");
    }

    // Inner class to represent a price item
    private static class PriceItem {
        private Material material;
        private int quantity;

        public PriceItem(Material material, int quantity) {
            this.material = material;
            this.quantity = quantity;
        }

        public Material getMaterial() { return material; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}