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

package me.nouridin.Search;

import me.nouridin.supershop.MessageUtils;
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
            MessageUtils.sendMessage(sender, "&cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getConfigManager().isSearchBookEnabled()) {
            MessageUtils.sendMessage(player, "&cSearch Book functionality is currently disabled!");
            return true;
        }
        
        if (plugin.getConfigManager().isSearchBookRequirePermission() && 
            !player.hasPermission("supershop.searchbook")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to use the Search Book!");
            return true;
        }
        
        if (isOnCooldown(player)) {
            long remainingTime = getRemainingCooldown(player);
            MessageUtils.sendMessage(player, "&cYou must wait " + remainingTime + " seconds before searching again!");
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
        MessageUtils.sendMessage(player, "&aSearching for: &e" + searchQuery);
        
        setCooldown(player);
        
        return true;
    }
    
    private void handleDirectSearch(Player player, String searchQuery) {
        MessageUtils.sendMessage(player, "&7Searching for: &e" + searchQuery);
        
        List<SearchResult> results = plugin.getSearchManager().searchByItemName(searchQuery, player);
        
        if (results.isEmpty()) {
            MessageUtils.sendMessage(player, "&cNo items found matching your search!");
            MessageUtils.sendMessage(player, "&7Try searching for:");
            MessageUtils.sendMessage(player, "&7- Material names (e.g., 'diamond', 'iron sword')");
            MessageUtils.sendMessage(player, "&7- Item display names");
            MessageUtils.sendMessage(player, "&7- Partial names (e.g., 'sword' for all swords)");
            return;
        }
        
        int maxResults = plugin.getConfigManager().getMaxSearchResults();
        if (results.size() > maxResults) {
            results = results.subList(0, maxResults);
            MessageUtils.sendMessage(player, "&7Showing first " + maxResults + " results...");
        }
        
        displaySearchResults(player, results, searchQuery);
    }
    
    private void displaySearchResults(Player player, List<SearchResult> results, String searchQuery) {
        MessageUtils.sendMessage(player, MessageUtils.createHeader("Search Results: " + searchQuery));
        
        for (int i = 0; i < Math.min(results.size(), 10); i++) {
            SearchResult result = results.get(i);
            
            String itemInfo = String.format("&e%dx %s &7- &a%s", 
                result.getQuantity(),
                result.getItemName(),
                result.getFormattedPrice());
            
            String locationInfo = String.format("&7%s at %s (%s)", 
                result.getOwnerName(),
                result.getCoordinatesString(),
                result.getFormattedDistance());
            
            MessageUtils.sendMessage(player, "&7" + (i + 1) + ". " + itemInfo);
            MessageUtils.sendMessage(player, "   " + locationInfo);
            
            if (result.hasDescription()) {
                MessageUtils.sendMessage(player, "   &7\"" + result.getDescription() + "\"");
            }
        }
        
        if (results.size() > 10) {
            MessageUtils.sendMessage(player, "&7... and " + (results.size() - 10) + " more results");
            MessageUtils.sendMessage(player, "&7Visit the shops to see all available items!");
        }
        
        MessageUtils.sendMessage(player, MessageUtils.createFooter());
        MessageUtils.sendMessage(player, "&7Tip: Right-click shop chests to browse and purchase items!");
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