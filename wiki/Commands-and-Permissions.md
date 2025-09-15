# Commands and Permissions

This guide covers all the commands you can use with SuperShop and the permissions that control them. 

## Player Commands

These are the commands that every player can use to interact with shops.

*   ` /shop ` - The main command for SuperShop. You can also use `/shops` or `/tradingchest`.
    *   ` /shop create <name> ` - Creates a new shop from the chest you are looking at.
        *   **Permission**: `supershop.create` (usually given to all players by default)
    *   ` /shop remove ` - Removes your shop.
    *   ` /shop list ` - Opens a menu to manage all of your shops from anywhere.
    *   ` /shop info ` - Shows information about your shop.
*   ` /searchbook <item name> ` - Searches for an item across all player shops. You can also use `/search` or `/finditem`.
    *   **Permission**: `supershop.searchbook` (usually given to all players by default)

## Admin Commands

These commands are typically only available to server administrators (ops).

*   ` /shop give <player> ` - Gives a search book to a specific player.
    *   **Permission**: `supershop.give`
*   ` /shop stats ` - Shows statistics about all the shops on the server.
    *   **Permission**: `supershop.stats`
*   ` /shop reload ` - Reloads the plugin's configuration file.
    *   **Permission**: `supershop.reload`

## Special Permissions

These permissions grant special abilities and are usually only given out by server admins.

*   `supershop.admin` - A powerful permission that lets you manage and edit any shop on the server.
*   `supershop.unlimited` - Allows you to create more shops than the server limit.
*   `supershop.nocooldown` - Lets you use the `/searchbook` command without any waiting time.
*   `supershop.teleport` - Allows you to teleport to shops directly from the search results.
*   `supershop.update.notify` - You will receive a message in chat when a new version of the plugin is available.
