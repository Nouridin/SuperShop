# Super Shop - Complete Trading System

A comprehensive, decentralized player-driven trading system for Minecraft servers. This plugin allows players to create their own shops anywhere in the world using chests, and provides a powerful search system to find items across all player shops.

## 🔧 Core Features

### 🧱 Player Shops (Trading Chests)
- **Decentralized Trading**: Players can set up shops anywhere in the world using chests
- **Custom Pricing**: Set any item(s) as payment for your goods (e.g., 64x Stone for 3x Gold Ingots)
- **Flexible Inventory**: Sell any items from your inventory with custom descriptions
- **Shop Management**: Easy commands for managing your shops and items

### 📖 Search Book (Marketplace Finder)
- **Global Search**: Find specific items being sold across all player shops
- **Advanced Filtering**: Filter by distance, seller name, world, and more
- **Location Information**: Get exact coordinates and distance to shops
- **Popular Items**: View the most commonly sold items

### 🎯 Key Benefits
- **Player-Driven Economy**: No admin shops required - players control the market
- **Fair Trading**: Transparent pricing and availability
- **Easy Discovery**: Find what you need quickly with the search system
- **Scalable**: Supports unlimited shops and items

## 📁 Project Structure (All in Same Directory)

All files are located in `src/main/java/me/yourname/supershop/`:

```
SuperShop.java              # Main plugin class
Shop.java                     # Shop data model
ShopItem.java                 # Shop item data model
SearchResult.java             # Search result data model
MessageUtils.java             # Message formatting utilities
ItemSerializer.java          # Item serialization utilities
ConfigManager.java            # Configuration management
DatabaseManager.java          # Database operations (SQLite/MySQL)
ShopManager.java              # Shop business logic
SearchManager.java            # Search functionality
ShopCommand.java              # Shop commands (/shop)
SearchBookCommand.java        # Search commands (/searchbook)
ChestInteractListener.java    # Chest interaction handling
SearchBookListener.java       # Search book usage
```

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Paper/Spigot 1.21 or higher
- Maven (for building)

### Building
```bash
mvn clean package
```

### Installation
1. Download the compiled JAR from the `target/` directory
2. Place it in your server's `plugins/` folder
3. Restart your server
4. Configure the plugin in `plugins/WLSuperShop/config.yml`

## 📋 Commands

### Player Commands
- `/shop create` - Create a shop at the chest you're looking at
- `/shop remove` - Remove the shop you're looking at
- `/shop list` - List all your shops
- `/shop info` - Get information about a shop
- `/searchbook <item>` - Search for items in player shops

### Admin Commands
- `/shop give [player]` - Give a search book to a player
- `/shop stats` - View server shop statistics
- `/shop reload` - Reload plugin configuration

## 🔑 Permissions

### Player Permissions
- `supershop.create` - Create shops (default: true)
- `supershop.searchbook` - Use search book (default: true)

### Admin Permissions
- `supershop.admin` - Manage all shops (default: op)
- `supershop.give` - Give search books (default: op)
- `supershop.stats` - View statistics (default: op)
- `supershop.reload` - Reload config (default: op)
- `supershop.unlimited` - Bypass shop limits (default: op)
- `supershop.nocooldown` - Bypass search cooldowns (default: op)

## ⚙️ Configuration

The plugin supports extensive configuration through `config.yml`:

### Database Settings
- SQLite (default) or MySQL support
- Configurable connection settings

### Shop Settings
- Maximum shops per player
- Maximum items per shop
- Cross-world trading
- Permission requirements

### Search Settings
- Search result limits
- Search radius
- Cooldown settings
- Permission requirements

## 🗄️ Database

The plugin automatically creates the necessary database tables:
- `shops` - Store shop information
- `shop_items` - Store items for sale

Supports both SQLite (default) and MySQL databases.

## 🎮 How to Use

### Creating a Shop
1. Place a chest where you want your shop
2. Look at the chest and run `/shop create`
3. Right-click the chest to see shop management options

### Shopping
1. Right-click any shop chest to browse items
2. See available items, prices, and descriptions
3. Use `/searchbook <item>` to find specific items

### Finding Items
1. Get a Search Book with `/shop give` (or from an admin)
2. Right-click the Search Book to see search instructions
3. Use `/searchbook <item name>` to search for items
4. Visit the shops to purchase items

## 🔧 Current Implementation Status

### ✅ Fully Working Features:
- **Shop Creation & Management**: Create, remove, list, and get info about shops
- **Database Storage**: Full SQLite/MySQL support with persistent data
- **Search System**: Complete search functionality with distance sorting
- **Permission System**: Comprehensive permission controls
- **Configuration**: Extensive customization options
- **Commands**: All core commands with tab completion

### 🚧 Coming Soon (GUI Features):
- **Shop Management GUI**: Visual interface for adding/removing items
- **Shop Browsing GUI**: Visual interface for customers to browse and buy
- **Search GUI**: Visual search interface with filters
- **Item Management**: Add items, set prices, and descriptions through GUI

### 📊 Current Capabilities:
- Players can create shops at any chest
- Shop data is stored persistently in database
- Search system finds items across all shops
- Distance-based search results
- Shop ownership and permission controls
- Admin tools for management and statistics

## 🔧 Technical Details

### Architecture
- **Modular Design**: Separated concerns with managers and models
- **Database Abstraction**: Supports multiple database types
- **Event-Driven**: Uses Bukkit event system for interactions
- **Permission Integration**: Full Bukkit permission support

### Performance
- **Efficient Storage**: Optimized database queries
- **Memory Management**: Concurrent data structures
- **Search Optimization**: Distance-based sorting and filtering


---

**WL Super Shop** - Empowering players to create their own economy!
