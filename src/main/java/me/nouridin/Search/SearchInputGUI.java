package me.nouridin.Search;

import me.nouridin.supershop.MessageUtils;
import me.nouridin.supershop.supershop;
import org.bukkit.entity.Player;

public class SearchInputGUI {
    
    private final supershop plugin;
    private final Player player;
    private final String previousSearch;

    public SearchInputGUI(supershop plugin, Player player, String currentSearch) {
        this.plugin = plugin;
        this.player = player;
        this.previousSearch = currentSearch;
    }

    public void open() {
        // Close any open GUI first
        player.closeInventory();

        // Prompt the player for input
        MessageUtils.sendMessage(player, "&eEnter your search query in chat.");
        MessageUtils.sendMessage(player, "&7Type 'cancel' to go back.");

        // Use the ChatInputManager to wait for the player's response
        plugin.getChatInputManager().requestInput(player, input -> {
            if (input.equalsIgnoreCase("cancel")) {
                MessageUtils.sendMessage(player, "&cSearch cancelled. Opening marketplace...");
                new SearchBookGUI(plugin, player, previousSearch).open();
                return;
            }

            String searchText = input.trim();
            if (!searchText.isEmpty()) {
                MessageUtils.sendMessage(player, "&aSearching for: &e" + searchText);
            } else {
                MessageUtils.sendMessage(player, "&aShowing all items in marketplace.");
            }

            // Open the search book with the new query
            new SearchBookGUI(plugin, player, searchText).open();
        });
    }
}
