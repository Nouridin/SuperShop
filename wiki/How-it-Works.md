# How SuperShop Works

Ever wondered what's happening behind the scenes when you create a shop or search for an item? This guide explains the magic of SuperShop in simple terms.

## Your Shop is a Special Chest

When you create a shop with `/shop create`, the SuperShop plugin doesn't create a new, complex item. Instead, it simply marks your chest as a special "shop chest." All the information about your shop—its name, the items you're selling, and their prices—is stored in a database and linked to the location of that chest.

This means your shop is tied directly to the chest block in the world. If the chest is destroyed, the shop is gone! (Don't worry, the plugin is smart enough to try and give you your items back if this happens).

## A Decentralized Economy

SuperShop is called a "decentralized" trading system. That sounds complicated, but it just means there isn't one central shop or auction house that everyone uses. Instead, every player's shop is its own independent business.

*   **You set the prices**: You have complete control over what you charge for your items.
*   **You manage the stock**: It's up to you to keep your shop stocked with the items you want to sell.
*   **Revenue goes to you**: When another player buys from your shop, the payment goes directly to you as "revenue" that you can collect from your shop menu.

This creates a dynamic, player-driven economy where prices can change based on supply and demand.

## Searching the World for Bargains

When you use the `/searchbook <item>` command, the plugin quickly scans its database of all the shop chests on the server. It checks every single shop to see if they are selling the item you're looking for.

It then gathers all the results and displays them to you in a neat, organized menu, allowing you to see the prices and quantities available from every seller at once. This makes it easy to find the best deals without having to run around to every shop on the server.
