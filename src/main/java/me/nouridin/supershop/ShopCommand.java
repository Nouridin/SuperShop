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
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ShopCommand implements CommandExecutor, TabCompleter {
    
    private final supershop plugin;
    
    public ShopCommand(supershop plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "&cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                handleCreateShop(player);
                break;
            case "remove":
            case "delete":
                handleRemoveShop(player);
                break;
            case "force":
                if (args.length >= 2 && args[1].equalsIgnoreCase("remove")) {
                    handleForceRemoveShop(player);
                } else {
                    sendHelpMessage(player);
                }
                break;
            case "list":
                handleListShops(player);
                break;
            case "manage":
                handleManageShop(player, args);
                break;
            case "info":
                handleShopInfo(player);
                break;
            case "give":
                handleGiveSearchBook(player, args);
                break;
            case "stats":
                handleStats(player);
                break;
            case "reload":
                handleReload(player);
                break;
            case "help":
            default:
                sendHelpMessage(player);
                break;
        }
        
        return true;
    }
    
    private void handleCreateShop(Player player) {
        if (plugin.getConfigManager().isRequirePermissionToCreate() && 
            !player.hasPermission("supershop.create")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to create shops!");
            return;
        }
        
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            MessageUtils.sendMessage(player, "&cYou must be looking at a chest to create a shop!");
            return;
        }
        
        if (plugin.getShopManager().isShopAtLocation(targetBlock.getLocation())) {
            MessageUtils.sendMessage(player, "&cA shop already exists at this location!");
            return;
        }
        
        List<Shop> playerShops = plugin.getShopManager().getShopsByOwner(player.getUniqueId());
        int maxShops = plugin.getConfigManager().getMaxShopsPerPlayer();
        if (playerShops.size() >= maxShops && !player.hasPermission("supershop.unlimited")) {
            MessageUtils.sendMessage(player, "&cYou have reached the maximum number of shops (" + maxShops + ")!");
            return;
        }
        
        Shop shop = plugin.getShopManager().createShop(player, targetBlock.getLocation());
        if (shop != null) {
            MessageUtils.sendMessage(player, "&aShop created successfully!");
            MessageUtils.sendMessage(player, "&7Right-click the chest to manage your shop.");
            MessageUtils.sendMessage(player, "&7Use /shop info while looking at the chest for details.");
        } else {
            MessageUtils.sendMessage(player, "&cFailed to create shop. Please try again.");
        }
    }
    
    private void handleRemoveShop(Player player) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            MessageUtils.sendMessage(player, "&cYou must be looking at a chest to remove a shop!");
            return;
        }
        
        Shop shop = plugin.getShopManager().getShopAtLocation(targetBlock.getLocation());
        if (shop == null) {
            MessageUtils.sendMessage(player, "&cNo shop found at this location!");
            return;
        }
        
        if (plugin.getShopManager().removeShop(shop.getShopId(), player, targetBlock)) {
            MessageUtils.sendMessage(player, "&aShop removed successfully!");
        }
    }
    
    private void handleForceRemoveShop(Player player) {
        if (!player.isOp()) {
            MessageUtils.sendMessage(player, "&cOnly operators can force remove shops!");
            return;
        }
        
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            MessageUtils.sendMessage(player, "&cYou must be looking at a chest to force remove a shop!");
            return;
        }
        
        Shop shop = plugin.getShopManager().getShopAtLocation(targetBlock.getLocation());
        if (shop == null) {
            MessageUtils.sendMessage(player, "&cNo shop found at this location!");
            return;
        }
        
        if (plugin.getShopManager().forceRemoveShop(shop.getShopId(), player)) {
            MessageUtils.sendMessage(player, "&aShop force removed successfully!");
            MessageUtils.sendMessage(player, "&7Owner: &e" + shop.getOwnerName());
        }
    }
    
    private void handleListShops(Player player) {
        List<Shop> shops = plugin.getShopManager().getShopsByOwner(player.getUniqueId());
        
        if (shops.isEmpty()) {
            MessageUtils.sendMessage(player, "&cYou don't have any shops!");
            return;
        }
        
        MessageUtils.sendMessage(player, MessageUtils.createHeader("Your Shops"));
        for (int i = 0; i < shops.size(); i++) {
            Shop shop = shops.get(i);
            String status = shop.isActive() ? "&aActive" : "&cInactive";
            MessageUtils.sendMessage(player, String.format("&7%d. %s &7- %s &7- %d items &7- &e/shop manage %d", 
                i + 1, shop.getCoordinatesString(), status, shop.getItems().size(), i + 1));
        }
        MessageUtils.sendMessage(player, MessageUtils.createFooter());
        MessageUtils.sendMessage(player, "&7Use &e/shop manage <number> &7to manage a specific shop remotely");
    }
    
    private void handleManageShop(Player player, String[] args) {
        List<Shop> shops = plugin.getShopManager().getShopsByOwner(player.getUniqueId());
        
        if (shops.isEmpty()) {
            MessageUtils.sendMessage(player, "&cYou don't have any shops!");
            MessageUtils.sendMessage(player, "&7Create a shop first with &e/shop create");
            return;
        }
        
        if (args.length < 2) {
            // Show shop selection GUI
            openShopSelectionGUI(player, shops);
            return;
        }
        
        try {
            int shopNumber = Integer.parseInt(args[1]);
            if (shopNumber < 1 || shopNumber > shops.size()) {
                MessageUtils.sendMessage(player, "&cInvalid shop number! Use a number between 1 and " + shops.size());
                MessageUtils.sendMessage(player, "&7Use &e/shop list &7to see your shops");
                return;
            }
            
            Shop selectedShop = shops.get(shopNumber - 1);
            MessageUtils.sendMessage(player, "&aOpening remote management for shop at " + selectedShop.getCoordinatesString());
            new ShopManagementGUI(plugin, player, selectedShop).open();
            
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "&cInvalid shop number! Please enter a valid number.");
            MessageUtils.sendMessage(player, "&7Use &e/shop list &7to see your shops");
        }
    }
    
    private void openShopSelectionGUI(Player player, List<Shop> shops) {
        new ShopSelectionGUI(plugin, player, shops).open();
    }
    
    private void handleShopInfo(Player player) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            MessageUtils.sendMessage(player, "&cYou must be looking at a chest to get shop info!");
            return;
        }
        
        Shop shop = plugin.getShopManager().getShopAtLocation(targetBlock.getLocation());
        if (shop == null) {
            MessageUtils.sendMessage(player, "&cNo shop found at this location!");
            return;
        }
        
        MessageUtils.sendMessage(player, MessageUtils.createHeader("Shop Information"));
        MessageUtils.sendMessage(player, "&7Owner: &e" + shop.getOwnerName());
        MessageUtils.sendMessage(player, "&7Location: &e" + shop.getCoordinatesString());
        MessageUtils.sendMessage(player, "&7World: &e" + shop.getWorldName());
        MessageUtils.sendMessage(player, "&7Items for sale: &e" + shop.getItems().size());
        MessageUtils.sendMessage(player, "&7Status: " + (shop.isActive() ? "&aActive" : "&cInactive"));
        MessageUtils.sendMessage(player, "&7Created: &e" + MessageUtils.formatDuration(System.currentTimeMillis() - shop.getCreatedAt()) + " ago");
        MessageUtils.sendMessage(player, MessageUtils.createFooter());
    }
    
    private void handleGiveSearchBook(Player player, String[] args) {
        if (!player.hasPermission("supershop.give")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to give search books!");
            return;
        }
        
        Player target = player;
        if (args.length > 1) {
            target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                MessageUtils.sendMessage(player, "&cPlayer not found!");
                return;
            }
        }
        
        ItemStack searchBook = createSearchBook();
        target.getInventory().addItem(searchBook);
        
        if (target.equals(player)) {
            MessageUtils.sendMessage(player, "&aYou received a Search Book!");
        } else {
            MessageUtils.sendMessage(player, "&aGave Search Book to " + target.getName());
            MessageUtils.sendMessage(target, "&aYou received a Search Book from " + player.getName() + "!");
        }
    }
    
    private void handleStats(Player player) {
        if (!player.hasPermission("supershop.stats")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to view stats!");
            return;
        }
        
        Map<String, Integer> stats = plugin.getShopManager().getShopStatistics();
        
        MessageUtils.sendMessage(player, MessageUtils.createHeader("Shop Statistics"));
        MessageUtils.sendMessage(player, "&7Total Shops: &e" + MessageUtils.formatNumber(stats.get("total_shops")));
        MessageUtils.sendMessage(player, "&7Active Shops: &e" + MessageUtils.formatNumber(stats.get("active_shops")));
        MessageUtils.sendMessage(player, "&7Total Items: &e" + MessageUtils.formatNumber(stats.get("total_items")));
        MessageUtils.sendMessage(player, MessageUtils.createFooter());
    }
    
    private void handleReload(Player player) {
        if (!player.hasPermission("supershop.reload")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to reload the plugin!");
            return;
        }
        
        plugin.getConfigManager().reloadConfigs();
        MessageUtils.sendMessage(player, "&aPlugin configuration reloaded!");
    }
    
    private ItemStack createSearchBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize("&6&lSearch Book"));
            meta.setLore(Arrays.asList(
                MessageUtils.colorize("&7Right-click to search for items"),
                MessageUtils.colorize("&7being sold in player shops"),
                "",
                MessageUtils.colorize("&7Use /searchbook <item> to search"),
                MessageUtils.colorize("&eWL Super Shop")
            ));
            book.setItemMeta(meta);
        }
        return book;
    }
    
    private void sendHelpMessage(Player player) {
        MessageUtils.sendMessage(player, MessageUtils.createHeader("WL Super Shop Commands"));
        MessageUtils.sendMessage(player, "&e/shop create &7- Create a shop at the chest you're looking at");
        MessageUtils.sendMessage(player, "&e/shop remove &7- Remove the shop you're looking at");
        MessageUtils.sendMessage(player, "&e/shop list &7- List all your shops");
        MessageUtils.sendMessage(player, "&e/shop manage [number] &7- Manage your shops remotely");
        MessageUtils.sendMessage(player, "&e/shop info &7- Get information about a shop");
        
        if (player.isOp()) {
            MessageUtils.sendMessage(player, "&e/shop force remove &7- Force remove any shop (OP only)");
        }
        
        if (player.hasPermission("supershop.give")) {
            MessageUtils.sendMessage(player, "&e/shop give [player] &7- Give a search book");
        }
        
        if (player.hasPermission("supershop.stats")) {
            MessageUtils.sendMessage(player, "&e/shop stats &7- View shop statistics");
        }
        
        if (player.hasPermission("supershop.reload")) {
            MessageUtils.sendMessage(player, "&e/shop reload &7- Reload plugin configuration");
        }
        
        MessageUtils.sendMessage(player, MessageUtils.createFooter());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList("create", "remove", "list", "manage", "info", "help"));
            
            if (sender.isOp()) {
                subCommands.add("force");
            }
            
            if (sender.hasPermission("supershop.give")) {
                subCommands.add("give");
            }
            
            if (sender.hasPermission("supershop.stats")) {
                subCommands.add("stats");
            }
            
            if (sender.hasPermission("supershop.reload")) {
                subCommands.add("reload");
            }
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("force")) {
            if (sender.isOp()) {
                completions.add("remove");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("manage")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                List<Shop> shops = plugin.getShopManager().getShopsByOwner(player.getUniqueId());
                for (int i = 1; i <= shops.size(); i++) {
                    completions.add(String.valueOf(i));
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}