# SuperShop

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/Nouridin/SuperShop/actions) [![Version](https://img.shields.io/badge/version-1.2.0-blue)](https://modrinth.com/plugin/super-shop) [![Discord](https://img.shields.io/discord/862423973435244544?color=7289DA&label=Discord&logo=discord&logoColor=white)](https://discord.gg/s2pevNCbv4) [![Modrinth](https://img.shields.io/modrinth/dt/super-shop?color=00AF5C&label=Modrinth&logo=modrinth)](https://modrinth.com/plugin/super-shop)

A decentralized, player-driven chest shop plugin for Spigot/Paper servers.

## Features

*   **Chest-based Shops**: Players can create a shop from any chest.
*   **GUI Management**: All shop and item management is handled through a clean GUI.
*   **Player-driven Economy**: No admin shops. Players set their own prices for items.
*   **Global Search**: A searchable book allows players to find items across all shops on the server.
*   **Remote Management**: Players can manage their shops from anywhere using the `/shop list` command.
*   **Database Support**: Supports SQLite for simple setups and MySQL for larger servers.

## Getting Started (for Developers)

### Prerequisites

*   Java 21+
*   Maven
*   Spigot/Paper 1.21+

### Building

Clone the repository and run the following command:

```bash
mvn clean package
```

The compiled JAR will be located in the `target/` directory.

## Architecture Overview

The plugin follows a modular, manager-based architecture to separate concerns.

*   `supershop.java`: The main plugin class, responsible for initializing managers and registering commands/listeners.
*   `ConfigManager.java`: Loads and provides access to `config.yml` and `messages.yml`.
*   `DatabaseManager.java`: Handles all database connections and queries for both SQLite and MySQL.
*   `ShopManager.java`: Manages the core logic for shops, including creation, removal, and item management.
*   `SearchManager.java`: Powers the item search functionality.
*   `GUIManager.java`: Manages the lifecycle of all GUIs, directing events to the correct open interface.
*   `BaseGUI.java`: An abstract class that all GUI menus extend, providing a consistent structure for creating new interfaces.

## Contributing

Contributions are welcome. Please follow this process:

1.  Fork the repository.
2.  Create a new branch for your feature or bug fix.
3.  Commit your changes and push them to your fork.
4.  Submit a pull request with a clear description of your changes.

## Links

*   **[Modrinth](https://modrinth.com/plugin/super-shop)**: Download the latest version of the plugin.
*   **[Discord](https://discord.gg/s2pevNCbv4)**: Join the community for support and discussion.
