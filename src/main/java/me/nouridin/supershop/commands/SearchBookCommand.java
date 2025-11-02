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

import me.nouridin.supershop.gui.SearchBookGUI;
import me.nouridin.supershop.util.SearchResult;
import me.nouridin.supershop.supershop;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class SearchBookCommand implements CommandExecutor, TabCompleter {
    
    private final supershop plugin;
    private final Map<UUID, Long> cooldowns;
    
    public SearchBookCommand(supershop plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtils().sendMessage(sender, "command-player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getConfigManager().isSearchBookEnabled()) {
            plugin.getMessageUtils().sendMessage(player, "searchbook-disabled");
            return true;
        }
        
        if (plugin.getConfigManager().isSearchBookRequirePermission() && 
            !player.hasPermission("supershop.searchbook")) {
            plugin.getMessageUtils().sendMessage(player, "searchbook-no-permission");
            return true;
        }
        
        if (isOnCooldown(player)) {
            long remainingTime = getRemainingCooldown(player);
            plugin.getMessageUtils().sendMessage(player, "searchbook-cooldown", String.valueOf(remainingTime));
            return true;
        }
        
        if (args.length == 0) {
            // Open the search GUI directly
            new SearchBookGUI(plugin, player).open();
            return true;
        }
        
        String searchQuery = String.join(" ", args);
        // Open the search GUI with the search query
        new SearchBookGUI(plugin, player, searchQuery).open();
        plugin.getMessageUtils().sendMessage(player, "searchbook-searching-for", searchQuery);
        
        setCooldown(player);
        
        return true;
    }
    
    private void handleDirectSearch(Player player, String searchQuery) {
        plugin.getMessageUtils().sendMessage(player, "searchbook-searching-for", searchQuery);
        
        List<SearchResult> results = plugin.getSearchManager().searchByItemName(searchQuery, player);
        
        if (results.isEmpty()) {
            plugin.getMessageUtils().sendMessage(player, "searchbook-no-results");
            plugin.getMessageUtils().sendMessage(player, "searchbook-try-searching-for");
            plugin.getMessageUtils().sendMessage(player, "searchbook-tip-material-names");
            plugin.getMessageUtils().sendMessage(player, "searchbook-tip-display-names");
            plugin.getMessageUtils().sendMessage(player, "searchbook-tip-partial-names");
            return;
        }
        
        int maxResults = plugin.getConfigManager().getMaxSearchResults();
        if (results.size() > maxResults) {
            results = results.subList(0, maxResults);
            plugin.getMessageUtils().sendMessage(player, "searchbook-showing-first-results", String.valueOf(maxResults));
        }
        
        displaySearchResults(player, results, searchQuery);
    }
    
    private void displaySearchResults(Player player, List<SearchResult> results, String searchQuery) {
        plugin.getMessageUtils().sendMessage(player, plugin.getMessageUtils().createHeader(plugin.getLocaleManager().getMessage(player.getUniqueId(), "searchbook-results-header", searchQuery)));
        
        for (int i = 0; i < Math.min(results.size(), 10); i++) {
            SearchResult result = results.get(i);
            
            String itemInfo = plugin.getLocaleManager().getMessage(player.getUniqueId(), "searchbook-result-item-info", 
                String.valueOf(result.getQuantity()),
                result.getItemName(),
                result.getFormattedPrice());
            
            String locationInfo = plugin.getLocaleManager().getMessage(player.getUniqueId(), "searchbook-result-location-info", 
                result.getOwnerName(),
                result.getCoordinatesString(),
                result.getFormattedDistance());
            
            plugin.getMessageUtils().sendMessage(player, "searchbook-result-line-item", String.valueOf(i + 1), itemInfo);
            plugin.getMessageUtils().sendMessage(player, "searchbook-result-line-location", locationInfo);
            
            if (result.hasDescription()) {
                plugin.getMessageUtils().sendMessage(player, "searchbook-result-line-description", result.getDescription());
            }
        }
        
        if (results.size() > 10) {
            plugin.getMessageUtils().sendMessage(player, "searchbook-more-results", String.valueOf(results.size() - 10));
            plugin.getMessageUtils().sendMessage(player, "searchbook-visit-shops-tip");
        }
        
        plugin.getMessageUtils().sendMessage(player, plugin.getMessageUtils().createFooter());
        plugin.getMessageUtils().sendMessage(player, "searchbook-browse-tip");
    }
    
    private boolean isOnCooldown(Player player) {
        if (player.hasPermission("supershop.nocooldown")) {
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long lastUse = cooldowns.get(playerId);
        long cooldownTime = plugin.getConfigManager().getSearchCooldownSeconds() * 1000L;
        
        return System.currentTimeMillis() - lastUse < cooldownTime;
    }
    
    private long getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        long lastUse = cooldowns.get(playerId);
        long cooldownTime = plugin.getConfigManager().getSearchCooldownSeconds() * 1000L;
        long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
        
        return Math.max(0, remaining / 1000);
    }
    
    private void setCooldown(Player player) {
        if (!player.hasPermission("supershop.nocooldown")) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> suggestions = plugin.getSearchManager().getSearchSuggestions(args[0]);
            completions.addAll(suggestions);
            
            List<Material> popularItems = plugin.getSearchManager().getPopularItems();
            for (Material material : popularItems) {
                String materialName = material.name().toLowerCase().replace("_", " ");
                if (materialName.startsWith(args[0].toLowerCase())) {
                    completions.add(materialName);
                }
            }
        }
        
        return completions;
    }
}
