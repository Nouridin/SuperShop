package me.nouridin.supershop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SearchInputGUI extends BaseGUI {

    private StringBuilder searchInput;
    private final String currentSearch;

    // Fixed keyboard layout (full QWERTY)
    private static final char[][] KEYBOARD = {
            {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'},
            {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'},
            {'z', 'x', 'c', 'v', 'b', 'n', 'm'}
    };

    public SearchInputGUI(supershop plugin, Player player, String currentSearch) {
        super(plugin, player, "&4&lSearch Input", 54);
        this.currentSearch = currentSearch;
        this.searchInput = new StringBuilder(currentSearch);
    }

    @Override
    protected void setupInventory() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);

        setupDisplay();
        setupKeyboard();
        setupControls();
    }

    private void setupDisplay() {
        // Current input display
        String displayText = searchInput.length() > 0 ? searchInput.toString() : "Type your search...";
        ItemStack inputDisplay = createInfoItem("&eSearch Input",
                "&7Current: &f" + displayText,
                "&7Length: &f" + searchInput.length() + "/32",
                "",
                "&7Use the keyboard below to type",
                "&7Click letters to add them",
                "&7Click controls to modify text");
        inventory.setItem(4, inputDisplay);
    }

    private void setupKeyboard() {
        // First row: Q W E R T Y U I O P (slots 10-19)
        int slot = 10;
        for (char c : KEYBOARD[0]) {
            ItemStack key = createButton(Material.WHITE_CONCRETE, "&f" + Character.toUpperCase(c),
                    "&7Click to add '" + c + "' to search");
            inventory.setItem(slot++, key);
        }

        // Second row: A S D F G H J K L (slots 20-28)
        slot = 20;
        for (char c : KEYBOARD[1]) {
            ItemStack key = createButton(Material.WHITE_CONCRETE, "&f" + Character.toUpperCase(c),
                    "&7Click to add '" + c + "' to search");
            inventory.setItem(slot++, key);
        }

        // Third row: Z X C V B N M (slots 29-35)
        slot = 29;
        for (char c : KEYBOARD[2]) {
            ItemStack key = createButton(Material.WHITE_CONCRETE, "&f" + Character.toUpperCase(c),
                    "&7Click to add '" + c + "' to search");
            inventory.setItem(slot++, key);
        }

        // Space bar (single, not double)
        ItemStack spaceBar = createButton(Material.LIGHT_GRAY_CONCRETE, "&fSPACE",
                "&7Click to add a space");
        inventory.setItem(39, spaceBar);
    }

    private void setupControls() {
        // Backspace (moved to 40 so it doesn’t overlap with letters)
        ItemStack backspace = createButton(Material.RED_CONCRETE, "&cBackspace",
                "&7Click to remove last character");
        inventory.setItem(40, backspace);

        // Clear all
        ItemStack clear = createButton(Material.BARRIER, "&cClear All",
                "&7Click to clear entire search");
        inventory.setItem(41, clear);

        // Numbers 0-9 (slots 42-51)
        for (int i = 0; i <= 9; i++) {
            ItemStack number = createButton(Material.YELLOW_CONCRETE, "&e" + i,
                    "&7Click to add '" + i + "' to search");
            inventory.setItem(42 + i, number);
        }

        // Search button
        ItemStack search = createButton(Material.EMERALD, "&aSearch",
                "&7Click to search for: &f" + (searchInput.length() > 0 ? searchInput.toString() : "all items"),
                "&7This will close the keyboard and show results");
        inventory.setItem(49, search);

        // Cancel button
        ItemStack cancel = createButton(Material.BARRIER, "&cCancel",
                "&7Click to cancel and return to search book");
        inventory.setItem(45, cancel);

        // Suggestions
        if (searchInput.length() > 0) {
            List<String> suggestions = plugin.getSearchManager().getSearchSuggestions(searchInput.toString());
            if (!suggestions.isEmpty()) {
                ItemStack suggestionButton = createButton(Material.BOOK, "&eSuggestions",
                        "&7Based on your input:",
                        suggestions.size() > 0 ? "&f• " + suggestions.get(0) : "",
                        suggestions.size() > 1 ? "&f• " + suggestions.get(1) : "",
                        suggestions.size() > 2 ? "&f• " + suggestions.get(2) : "",
                        "&7Click to use first suggestion");
                inventory.setItem(53, suggestionButton);
            }
        }
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        // Handle keyboard input
        if (slot >= 10 && slot <= 19) {
            char c = KEYBOARD[0][slot - 10];
            addCharacter(c);
        } else if (slot >= 20 && slot <= 28) {
            char c = KEYBOARD[1][slot - 20];
            addCharacter(c);
        } else if (slot >= 29 && slot <= 35) {
            char c = KEYBOARD[2][slot - 29];
            addCharacter(c);
        } else if (slot == 39) {
            addCharacter(' ');
        } else if (slot >= 42 && slot <= 51) {
            char c = (char) ('0' + (slot - 42));
            addCharacter(c);
        }

        // Handle controls
        switch (slot) {
            case 40: // Backspace
                if (searchInput.length() > 0) {
                    searchInput.deleteCharAt(searchInput.length() - 1);
                    refresh();
                }
                break;
            case 41: // Clear all
                searchInput.setLength(0);
                refresh();
                break;
            case 49: // Search
                performSearch();
                break;
            case 45: // Cancel
                close();
                new SearchBookGUI(plugin, player).open();
                break;
            case 53: // Use suggestion
                if (searchInput.length() > 0) {
                    List<String> suggestions = plugin.getSearchManager().getSearchSuggestions(searchInput.toString());
                    if (!suggestions.isEmpty()) {
                        searchInput.setLength(0);
                        searchInput.append(suggestions.get(0));
                        refresh();
                    }
                }
                break;
        }
    }

    private void addCharacter(char c) {
        if (searchInput.length() < 32) {
            searchInput.append(c);
            refresh();
        } else {
            MessageUtils.sendMessage(player, "&cSearch is too long! Maximum 32 characters.");
        }
    }

    private void performSearch() {
        String finalSearch = searchInput.toString().trim();
        close();

        // Create new search GUI with the search query
        SearchBookGUI searchGUI = new SearchBookGUI(plugin, player, finalSearch);
        searchGUI.open();

        if (!finalSearch.isEmpty()) {
            MessageUtils.sendMessage(player, "&aSearching for: &e" + finalSearch);
        } else {
            MessageUtils.sendMessage(player, "&aShowing all items in marketplace.");
        }
    }
}
