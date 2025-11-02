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
        plugin.getMessageUtils().sendMessage(player, "gui.searchinput.prompt");
        plugin.getMessageUtils().sendMessage(player, "gui.searchinput.cancel-instruction");

        // Use the ChatInputManager to wait for the player's response
        plugin.getChatInputManager().requestInput(player, input -> {
            if (input.equalsIgnoreCase("cancel")) {
                plugin.getMessageUtils().sendMessage(player, "gui.searchinput.search-cancelled");
                new SearchBookGUI(plugin, player, previousSearch).open();
                return;
            }

            String searchText = input.trim();
            if (!searchText.isEmpty()) {
                plugin.getMessageUtils().sendMessage(player, "gui.searchinput.searching-for", searchText);
            } else {
                plugin.getMessageUtils().sendMessage(player, "gui.searchinput.showing-all-items");
            }

            // Open the search book with the new query
            new SearchBookGUI(plugin, player, searchText).open();
        });
    }
}
