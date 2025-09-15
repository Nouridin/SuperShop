# SuperShop Configuration Guide

Welcome, server admin! This guide will walk you through the `config.yml` file for SuperShop. Here, you can tweak the plugin's settings to best fit your server's economy and playstyle.

## Database Settings (`database`)

This section controls where the plugin stores all its data, like shop locations and items.

*   `type`: Choose your database type.
    *   `sqlite`: The default and easiest option. It stores data in a file within the plugin's folder. Perfect for most servers.
    *   `mysql`: A more advanced option for larger servers or server networks. If you use this, you must fill out the settings below.
*   `host`, `port`, `database`, `username`, `password`: These are your MySQL database credentials. Only needed if `type` is set to `mysql`.

## Shop Rules (`shop`)

These settings control the rules for creating and managing shops on your server.

*   `max-shops-per-player`: Sets the maximum number of shops a single player can own. Set to `-1` for unlimited. Players with the `supershop.unlimited` permission can bypass this limit.
*   `max-items-per-shop`: The maximum number of unique item stacks a single shop can hold. This is limited by the chest size, so the max is `54`.
*   `allow-cross-world-trading`: If `true`, players can buy from shops even if they are in a different world.
*   `require-permission-to-create`: 
    *   If `false` (default), any player can create a shop using `/shop create`.
    *   If `true`, only players with the `supershop.create` permission will be able to create shops.

## Item Search Settings (`search-book`)

These settings control the `/searchbook` feature, which allows players to find items for sale.

*   `enabled`: Set to `false` to completely disable the item search feature.
*   `max-results`: The maximum number of search results that will be shown in the menu.
*   `search-radius`: The maximum distance (in blocks) that the search will look for shops. A high number like `10000` makes it a global search.
*   `require-permission`: 
    *   If `false` (default), any player can use the `/searchbook` command.
    *   If `true`, only players with the `supershop.searchbook` permission can use it.
*   `cooldown-seconds`: The number of seconds a player must wait between using the search command. This helps prevent server lag. Players with the `supershop.nocooldown` permission can bypass this.
