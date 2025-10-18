<div align="center">

# SuperShop

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/Nouridin/SuperShop/actions) [![Version](https://img.shields.io/badge/version-1.2.1-blue)](https://modrinth.com/plugin/super-shop) [![Discord](https://img.shields.io/discord/1403066205228503150?color=7289DA&label=Discord&logo=discord&logoColor=white)](https://discord.gg/s2pevNCbv4) [![Modrinth](https://img.shields.io/modrinth/dt/super-shop?color=00AF5C&label=Modrinth&logo=modrinth)](https://modrinth.com/plugin/super-shop) [![Java](https://img.shields.io/badge/Java-21+-red?logo=openjdk&logoColor=white)]() [![Spigot](https://img.shields.io/badge/API-Spigot%2FPaper-orange?logo=spigotmc&logoColor=white)]() [![License](https://img.shields.io/github/license/Nouridin/SuperShop)]()


A decentralized, player-driven chest shop plugin for Spigot/Paper servers.
</div>

---

## Installation

1. Download the latest release from [Modrinth](https://modrinth.com/plugin/super-shop).
2. Place the JAR file into your server’s `plugins/` folder.
3. Restart the server — configuration files will be generated automatically.


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

### Architecture Overview

| Component | Description |
|------------|-------------|
| `SuperShop.java` | Main plugin class; initializes managers, commands, and listeners. |
| `ConfigManager.java` | Loads and provides access to `config.yml` and `messages.yml`. |
| `DatabaseManager.java` | Handles database connections (SQLite/MySQL). |
| `ShopManager.java` | Core logic for creating and managing shops. |
| `SearchManager.java` | Powers global item search. |
| `GUIManager.java` | Handles GUI registration and events. |
| `BaseGUI.java` | Abstract base for all GUI menus. |


## Contributing

Contributions are welcome. Please follow this process:

1.  Fork the repository.
2.  Create a new branch for your feature or bug fix.
3.  Commit your changes and push them to your fork.
4.  Submit a pull request with a clear description of your changes.

## Links

*   **[Modrinth](https://modrinth.com/plugin/super-shop)**: Download the latest version of the plugin.
*   **[Discord](https://discord.gg/s2pevNCbv4)**: Join the community for support and discussion.
