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

package me.nouridin.supershop.commands;

import me.nouridin.supershop.*;
import me.nouridin.supershop.gui.ShopManagementGUI;
import me.nouridin.supershop.gui.ShopSelectionGUI;
import me.nouridin.supershop.managers.LocaleManager;
import me.nouridin.supershop.models.Shop;
import me.nouridin.supershop.util.MessageUtils;
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
import java.util.UUID;

public class ShopCommand implements CommandExecutor, TabCompleter {

    private final supershop plugin;
    private final MessageUtils messageUtils;
    private final LocaleManager localeManager;

    public ShopCommand(supershop plugin) {
        this.plugin = plugin;
        this.messageUtils = plugin.getMessageUtils();
        this.localeManager = plugin.getLocaleManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtils.sendMessage(sender, "command-player-only");
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
            messageUtils.sendMessage(player, "shop.create.no-permission");
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            messageUtils.sendMessage(player, "shop.create.not-looking-at-chest");
            return;
        }

        if (plugin.getShopManager().isShopAtLocation(targetBlock.getLocation())) {
            messageUtils.sendMessage(player, "shop.create.already-exists");
            return;
        }

        List<Shop> playerShops = plugin.getShopManager().getShopsByOwner(player.getUniqueId());
        int maxShops = plugin.getConfigManager().getMaxShopsPerPlayer();
        if (playerShops.size() >= maxShops && !player.hasPermission("supershop.unlimited")) {
            messageUtils.sendMessage(player, "shop.create.max-shops-reached", String.valueOf(maxShops));
            return;
        }

        Shop shop = plugin.getShopManager().createShop(player, targetBlock.getLocation());
        if (shop != null) {
            messageUtils.sendMessage(player, "shop.created.success");
            messageUtils.sendMessage(player, "shop.created.manage-tip");
            messageUtils.sendMessage(player, "shop.created.info-tip");
        } else {
            messageUtils.sendMessage(player, "shop.create.failed");
        }
    }

    private void handleRemoveShop(Player player) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            messageUtils.sendMessage(player, "shop.remove.not-looking-at-chest");
            return;
        }

        Shop shop = plugin.getShopManager().getShopAtLocation(targetBlock.getLocation());
        if (shop == null) {
            messageUtils.sendMessage(player, "shop.not-found-at-location");
            return;
        }

        if (plugin.getShopManager().removeShop(shop.getShopId(), player, targetBlock)) {
            messageUtils.sendMessage(player, "shop.removed.success");
        }
    }

    private void handleForceRemoveShop(Player player) {
        if (!player.isOp()) {
            messageUtils.sendMessage(player, "shop.force-remove.no-permission");
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            messageUtils.sendMessage(player, "shop.force-remove.not-looking-at-chest");
            return;
        }

        Shop shop = plugin.getShopManager().getShopAtLocation(targetBlock.getLocation());
        if (shop == null) {
            messageUtils.sendMessage(player, "shop.not-found-at-location");
            return;
        }

        if (plugin.getShopManager().forceRemoveShop(shop.getShopId(), player)) {
            messageUtils.sendMessage(player, "shop.force-removed.success");
            messageUtils.sendMessage(player, "shop.force-removed.owner", shop.getOwnerName());
        }
    }

    private void handleListShops(Player player) {
        List<Shop> shops = plugin.getShopManager().getShopsByOwner(player.getUniqueId());
        UUID playerUUID = player.getUniqueId();

        if (shops.isEmpty()) {
            messageUtils.sendMessage(player, "shop.list.no-shops");
            return;
        }

        messageUtils.sendMessage(player, messageUtils.createHeader(localeManager.getMessage(playerUUID, "shop.list.header")));
        for (int i = 0; i < shops.size(); i++) {
            Shop shop = shops.get(i);
            String status = shop.isActive() ? localeManager.getMessage(playerUUID, "shop.status.active") : localeManager.getMessage(playerUUID, "shop.status.inactive");
            messageUtils.sendMessage(player, "shop.list.item",
                String.valueOf(i + 1),
                shop.getCoordinatesString(),
                status,
                String.valueOf(shop.getItems().size()),
                String.valueOf(i + 1));
        }
        messageUtils.sendMessage(player, messageUtils.createFooter());
        messageUtils.sendMessage(player, "shop.list.manage-tip");
    }

    private void handleManageShop(Player player, String[] args) {
        List<Shop> shops = plugin.getShopManager().getShopsByOwner(player.getUniqueId());

        if (shops.isEmpty()) {
            messageUtils.sendMessage(player, "shop.list.no-shops");
            messageUtils.sendMessage(player, "shop.list.create-tip");
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
                messageUtils.sendMessage(player, "shop.manage.invalid-number", String.valueOf(shops.size()));
                messageUtils.sendMessage(player, "shop.manage.list-tip");
                return;
            }

            Shop selectedShop = shops.get(shopNumber - 1);
            messageUtils.sendMessage(player, "shop.manage.opening-remote", selectedShop.getCoordinatesString());
            new ShopManagementGUI(plugin, player, selectedShop).open();

        } catch (NumberFormatException e) {
            messageUtils.sendMessage(player, "shop.manage.invalid-number-format");
            messageUtils.sendMessage(player, "shop.manage.list-tip");
        }
    }

    private void openShopSelectionGUI(Player player, List<Shop> shops) {
        new ShopSelectionGUI(plugin, player, shops).open();
    }

    private void handleShopInfo(Player player) {
        Block targetBlock = player.getTargetBlockExact(5);
        UUID playerUUID = player.getUniqueId();
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            messageUtils.sendMessage(player, "shop.info.not-looking-at-chest");
            return;
        }

        Shop shop = plugin.getShopManager().getShopAtLocation(targetBlock.getLocation());
        if (shop == null) {
            messageUtils.sendMessage(player, "shop.not-found-at-location");
            return;
        }

        messageUtils.sendMessage(player, messageUtils.createHeader(localeManager.getMessage(playerUUID, "shop.info.header")));
        messageUtils.sendMessage(player, "shop.info.owner", shop.getOwnerName());
        messageUtils.sendMessage(player, "shop.info.location", shop.getCoordinatesString());
        messageUtils.sendMessage(player, "shop.info.world", shop.getWorldName());
        messageUtils.sendMessage(player, "shop.info.items-for-sale", String.valueOf(shop.getItems().size()));
        messageUtils.sendMessage(player, "shop.info.status", (shop.isActive() ? localeManager.getMessage(playerUUID, "shop.status.active") : localeManager.getMessage(playerUUID, "shop.status.inactive")));
        messageUtils.sendMessage(player, "shop.info.created", messageUtils.formatDuration(System.currentTimeMillis() - shop.getCreatedAt(), playerUUID));
        messageUtils.sendMessage(player, messageUtils.createFooter());
    }

    private void handleGiveSearchBook(Player player, String[] args) {
        if (!player.hasPermission("supershop.give")) {
            messageUtils.sendMessage(player, "shop.give.no-permission");
            return;
        }

        Player target = player;
        if (args.length > 1) {
            target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                messageUtils.sendMessage(player, "player-not-found");
                return;
            }
        }

        ItemStack searchBook = createSearchBook(player);
        target.getInventory().addItem(searchBook);

        if (target.equals(player)) {
            messageUtils.sendMessage(player, "shop.give.received-self");
        } else {
            messageUtils.sendMessage(player, "shop.give.given-other", target.getName());
            messageUtils.sendMessage(target, "shop.give.received-other", player.getName());
        }
    }

    private void handleStats(Player player) {
        if (!player.hasPermission("supershop.stats")) {
            messageUtils.sendMessage(player, "shop.stats.no-permission");
            return;
        }

        Map<String, Integer> stats = plugin.getShopManager().getShopStatistics();
        UUID playerUUID = player.getUniqueId();

        messageUtils.sendMessage(player, messageUtils.createHeader(localeManager.getMessage(playerUUID, "shop.stats.header")));
        messageUtils.sendMessage(player, "shop.stats.total-shops", messageUtils.formatNumber(stats.get("total_shops")));
        messageUtils.sendMessage(player, "shop.stats.active-shops", messageUtils.formatNumber(stats.get("active_shops")));
        messageUtils.sendMessage(player, "shop.stats.total-items", messageUtils.formatNumber(stats.get("total_items")));
        messageUtils.sendMessage(player, messageUtils.createFooter());
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("supershop.reload")) {
            messageUtils.sendMessage(player, "shop.reload.no-permission");
            return;
        }

        plugin.getConfigManager().reloadConfigs();
        plugin.getLocaleManager().reloadLocales();
        messageUtils.sendMessage(player, "shop.reload.success");
    }

    private ItemStack createSearchBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = book.getItemMeta();
        UUID playerUUID = player.getUniqueId();
        if (meta != null) {
            meta.setDisplayName(messageUtils.colorize(localeManager.getMessage(playerUUID, "searchbook.item.name")));
            meta.setLore(Arrays.asList(
                messageUtils.colorize(localeManager.getMessage(playerUUID, "searchbook.item.lore1")),
                messageUtils.colorize(localeManager.getMessage(playerUUID, "searchbook.item.lore2")),
                "",
                messageUtils.colorize(localeManager.getMessage(playerUUID, "searchbook.item.lore3")),
                messageUtils.colorize(localeManager.getMessage(playerUUID, "searchbook.item.lore4"))
            ));
            book.setItemMeta(meta);
        }
        return book;
    }

    private void sendHelpMessage(Player player) {
        UUID playerUUID = player.getUniqueId();
        messageUtils.sendMessage(player, messageUtils.createHeader(localeManager.getMessage(playerUUID, "shop.help.header")));
        messageUtils.sendMessage(player, "shop.help.create");
        messageUtils.sendMessage(player, "shop.help.remove");
        messageUtils.sendMessage(player, "shop.help.list");
        messageUtils.sendMessage(player, "shop.help.manage");
        messageUtils.sendMessage(player, "shop.help.info");

        if (player.isOp()) {
            messageUtils.sendMessage(player, "shop.help.force-remove");
        }

        if (player.hasPermission("supershop.give")) {
            messageUtils.sendMessage(player, "shop.help.give");
        }

        if (player.hasPermission("supershop.stats")) {
            messageUtils.sendMessage(player, "shop.help.stats");
        }

        if (player.hasPermission("supershop.reload")) {
            messageUtils.sendMessage(player, "shop.help.reload");
        }

        messageUtils.sendMessage(player, messageUtils.createFooter());
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
