package me.nouridin.supershop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PurchaseAmountGUI extends BaseGUI {
    
    private final Shop shop;
    private final ShopItem shopItem;
    private int selectedAmount;
    
    public PurchaseAmountGUI(supershop plugin, Player player, Shop shop, ShopItem shopItem) {
        super(plugin, player, "&6Select Purchase Amount", 27);
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
        ItemStack displayItem = shopItem.getItemStack().clone();
        inventory.setItem(4, displayItem);
        
        ItemStack itemInfo = createInfoItem("&e" + shopItem.getFormattedItemName(),
            "&7Available: &f" + shopItem.getQuantity(),
            "&7Price per item: &a" + shopItem.getFormattedPrice(),
            "&7Total cost: &a" + calculateTotalCost(),
            "",
            "&7Seller: &f" + shop.getOwnerName());
        inventory.setItem(13, itemInfo);
    }
    
    private void setupAmountControls() {
        // Decrease buttons
        ItemStack decrease10 = createButton(Material.RED_CONCRETE, "&c-10",
            "&7Click to decrease by 10");
        inventory.setItem(9, decrease10);
        
        ItemStack decrease1 = createButton(Material.RED_CONCRETE, "&c-1",
            "&7Click to decrease by 1");
        inventory.setItem(10, decrease1);
        
        // Amount display
        ItemStack amountDisplay = createInfoItem("&eAmount: &f" + selectedAmount,
            "&7Max available: &f" + shopItem.getQuantity());
        inventory.setItem(11, amountDisplay);
        
        // Increase buttons
        ItemStack increase1 = createButton(Material.GREEN_CONCRETE, "&a+1",
            "&7Click to increase by 1");
        inventory.setItem(12, increase1);
        
        ItemStack increase10 = createButton(Material.GREEN_CONCRETE, "&a+10",
            "&7Click to increase by 10");
        inventory.setItem(14, increase10);
        
        // Max button
        ItemStack maxButton = createButton(Material.EMERALD, "&aMax (" + shopItem.getQuantity() + ")",
            "&7Click to select maximum amount");
        inventory.setItem(15, maxButton);
    }
    
    private void setupActionButtons() {
        // Check if player can afford
        boolean canAfford = canPlayerAfford();
        
        // Purchase button
        ItemStack purchase = createButton(
            canAfford ? Material.DIAMOND : Material.BARRIER, 
            canAfford ? "&aPurchase" : "&cCannot Afford",
            "&7Buy " + selectedAmount + "x " + shopItem.getFormattedItemName(),
            "&7Total cost: &a" + calculateTotalCost(),
            "",
            canAfford ? "&eClick to confirm purchase!" : "&cYou don't have enough items!");
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
            MessageUtils.sendMessage(player, "&cYou cannot afford this purchase!");
            MessageUtils.sendMessage(player, "&7You need: &f" + calculateTotalCost());
            return;
        }
        
        if (selectedAmount > shopItem.getQuantity()) {
            MessageUtils.sendMessage(player, "&cNot enough items in stock!");
            return;
        }
        
        if (plugin.getShopManager().processPurchase(player, shop, shopItem, selectedAmount)) {
            close();
            MessageUtils.sendMessage(player, "&aPurchase completed successfully!");
            MessageUtils.sendMessage(player, "&7You bought &e" + selectedAmount + "x " + shopItem.getFormattedItemName());
        }
    }
    
    private String calculateTotalCost() {
        if (shopItem.getPriceItems().isEmpty()) {
            return "Free";
        }
        
        StringBuilder cost = new StringBuilder();
        for (int i = 0; i < shopItem.getPriceItems().size(); i++) {
            ItemStack priceItem = shopItem.getPriceItems().get(i);
            int totalAmount = priceItem.getAmount() * selectedAmount;
            
            cost.append(totalAmount).append("x ");
            if (priceItem.hasItemMeta() && priceItem.getItemMeta().hasDisplayName()) {
                cost.append(priceItem.getItemMeta().getDisplayName());
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