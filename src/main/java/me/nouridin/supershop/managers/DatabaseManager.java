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

package me.nouridin.supershop.managers;

import me.nouridin.supershop.*;
import me.nouridin.supershop.models.Shop;
import me.nouridin.supershop.models.ShopItem;
import me.nouridin.supershop.util.ItemSerializer;
import me.nouridin.supershop.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    
    private final supershop plugin;
    private Connection connection;
    private final String databaseType;
    
    public DatabaseManager(supershop plugin) {
        this.plugin = plugin;
        this.databaseType = plugin.getConfigManager().getDatabaseType();
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            if (databaseType.equalsIgnoreCase("sqlite")) {
                initializeSQLite();
            } else if (databaseType.equalsIgnoreCase("mysql")) {
                initializeMySQL();
            } else {
                plugin.getMessageUtils().sendConsoleMessage("&cUnsupported database type: " + databaseType);
                return;
            }
            
            createTables();
            plugin.getMessageUtils().sendConsoleMessage("&aDatabase initialized successfully!");
            
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/shops.db";
        connection = DriverManager.getConnection(url);
    }
    
    private void initializeMySQL() throws SQLException {
        String host = plugin.getConfigManager().getDatabaseHost();
        int port = plugin.getConfigManager().getDatabasePort();
        String database = plugin.getConfigManager().getDatabaseName();
        String username = plugin.getConfigManager().getDatabaseUsername();
        String password = plugin.getConfigManager().getDatabasePassword();
        
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true", 
            host, port, database);
        
        connection = DriverManager.getConnection(url, username, password);
    }
    
    private void createTables() throws SQLException {
        String createShopsTable = "CREATE TABLE IF NOT EXISTS shops (" +
            "shop_id VARCHAR(36) PRIMARY KEY," +
            "owner_id VARCHAR(36) NOT NULL," +
            "owner_name VARCHAR(16) NOT NULL," +
            "world_name VARCHAR(50) NOT NULL," +
            "x INTEGER NOT NULL," +
            "y INTEGER NOT NULL," +
            "z INTEGER NOT NULL," +
            "is_active BOOLEAN DEFAULT TRUE," +
            "created_at BIGINT NOT NULL," +
            "last_updated BIGINT NOT NULL," +
            "revenue_data TEXT DEFAULT ''" +
            ")";
        
        String createShopItemsTable = "CREATE TABLE IF NOT EXISTS shop_items (" +
            "item_id VARCHAR(36) PRIMARY KEY," +
            "shop_id VARCHAR(36) NOT NULL," +
            "item_data TEXT NOT NULL," +
            "quantity INTEGER NOT NULL," +
            "description TEXT," +
            "price_data TEXT NOT NULL," +
            "is_available BOOLEAN DEFAULT TRUE" +
            ")";

        String createPlayerLocalesTable = "CREATE TABLE IF NOT EXISTS player_locales (" +
            "player_id VARCHAR(36) PRIMARY KEY," +
            "locale VARCHAR(10) NOT NULL" +
            ")";

        String createFirstLoginTable = "CREATE TABLE IF NOT EXISTS first_login_tracking (" +
            "player_id VARCHAR(36) PRIMARY KEY" +
            ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createShopsTable);
            stmt.execute(createShopItemsTable);
            stmt.execute(createPlayerLocalesTable);
            stmt.execute(createFirstLoginTable);
            
            // Add revenue_data column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE shops ADD COLUMN revenue_data TEXT DEFAULT ''");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
        }
    }
    
    public void saveShop(Shop shop) {
        String sql = databaseType.equalsIgnoreCase("sqlite") ?
            "INSERT OR REPLACE INTO shops (shop_id, owner_id, owner_name, world_name, x, y, z, is_active, created_at, last_updated, revenue_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" :
            "INSERT INTO shops (shop_id, owner_id, owner_name, world_name, x, y, z, is_active, created_at, last_updated, revenue_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE owner_name = VALUES(owner_name), world_name = VALUES(world_name), x = VALUES(x), y = VALUES(y), z = VALUES(z), is_active = VALUES(is_active), last_updated = VALUES(last_updated), revenue_data = VALUES(revenue_data)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, shop.getShopId().toString());
            stmt.setString(2, shop.getOwnerId().toString());
            stmt.setString(3, shop.getOwnerName());
            stmt.setString(4, shop.getWorldName());
            stmt.setInt(5, shop.getLocation().getBlockX());
            stmt.setInt(6, shop.getLocation().getBlockY());
            stmt.setInt(7, shop.getLocation().getBlockZ());
            stmt.setBoolean(8, shop.isActive());
            stmt.setLong(9, shop.getCreatedAt());
            stmt.setLong(10, shop.getLastUpdated());
            
            String serializedRevenue = "";
            try {
                serializedRevenue = ItemSerializer.serializeItemList(shop.getRevenue());
            } catch (IOException e) {
                plugin.getMessageUtils().sendConsoleMessage("&cFailed to serialize revenue for shop " + shop.getShopId() + ": " + e.getMessage());
            }
            stmt.setString(11, serializedRevenue);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to save shop: " + e.getMessage());
        }
    }
    
    public void saveShopItem(UUID shopId, ShopItem item) {
        String sql = databaseType.equalsIgnoreCase("sqlite") ?
            "INSERT OR REPLACE INTO shop_items (item_id, shop_id, item_data, quantity, description, price_data, is_available) VALUES (?, ?, ?, ?, ?, ?, ?)" :
            "INSERT INTO shop_items (item_id, shop_id, item_data, quantity, description, price_data, is_available) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE item_data = VALUES(item_data), quantity = VALUES(quantity), description = VALUES(description), price_data = VALUES(price_data), is_available = VALUES(is_available)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, item.getItemId().toString());
            stmt.setString(2, shopId.toString());
            
            String serializedItemData = "";
            try {
                serializedItemData = ItemSerializer.serialize(item.getItemStack());
            } catch (IOException e) {
                plugin.getMessageUtils().sendConsoleMessage("&cFailed to serialize item stack for shop item " + item.getItemId() + ": " + e.getMessage());
            }
            stmt.setString(3, serializedItemData);
            
            stmt.setInt(4, item.getQuantity());
            stmt.setString(5, item.getDescription());
            
            String serializedPriceData = "";
            try {
                serializedPriceData = ItemSerializer.serializeItemList(item.getPriceItems());
            } catch (IOException e) {
                plugin.getMessageUtils().sendConsoleMessage("&cFailed to serialize price items for shop item " + item.getItemId() + ": " + e.getMessage());
            }
            stmt.setString(6, serializedPriceData);
            
            stmt.setBoolean(7, item.isAvailable());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to save shop item: " + e.getMessage());
        }
    }
    
    public List<Shop> loadAllShops() {
        List<Shop> shops = new ArrayList<>();
        String sql = "SELECT * FROM shops WHERE is_active = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Shop shop = createShopFromResultSet(rs);
                if (shop != null) {
                    loadShopItems(shop);
                    shops.add(shop);
                }
            }
            
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to load shops: " + e.getMessage());
        }
        
        return shops;
    }
    
    private void loadShopItems(Shop shop) {
        String sql = "SELECT * FROM shop_items WHERE shop_id = ? AND is_available = TRUE AND quantity > 0";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, shop.getShopId().toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ShopItem item = createShopItemFromResultSet(rs);
                    if (item != null && item.getQuantity() > 0) {
                        shop.addItem(item);
                    }
                }
            }
            
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to load shop items: " + e.getMessage());
        }
    }
    
    private Shop createShopFromResultSet(ResultSet rs) throws SQLException {
        UUID shopId = UUID.fromString(rs.getString("shop_id"));
        UUID ownerId = UUID.fromString(rs.getString("owner_id"));
        String ownerName = rs.getString("owner_name");
        String worldName = rs.getString("world_name");
        int x = rs.getInt("x");
        int y = rs.getInt("y");
        int z = rs.getInt("z");
        boolean isActive = rs.getBoolean("is_active");
        long createdAt = rs.getLong("created_at");
        long lastUpdated = rs.getLong("last_updated");
        String revenueData = rs.getString("revenue_data");
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getMessageUtils().sendConsoleMessage("&cWorld '" + worldName + "' not found for shop " + shopId);
            return null;
        }
        
        Location location = new Location(world, x, y, z);
        Shop shop = new Shop(shopId, ownerId, ownerName, location);
        shop.setActive(isActive);
        shop.setCreatedAt(createdAt);
        shop.setLastUpdated(lastUpdated);
        
        // Load revenue data
        if (revenueData != null && !revenueData.isEmpty()) {
            try {
                List<ItemStack> revenue = ItemSerializer.deserializeItemList(revenueData);
                shop.setRevenue(revenue);
            } catch (IOException | ClassNotFoundException e) {
                plugin.getMessageUtils().sendConsoleMessage("&cFailed to deserialize revenue for shop " + shopId + ": " + e.getMessage());
            }
        }
        
        return shop;
    }
    
    private ShopItem createShopItemFromResultSet(ResultSet rs) throws SQLException {
        UUID itemId = UUID.fromString(rs.getString("item_id"));
        String itemData = rs.getString("item_data");
        int quantity = rs.getInt("quantity");
        String description = rs.getString("description");
        String priceData = rs.getString("price_data");
        boolean isAvailable = rs.getBoolean("is_available");
        
        ItemStack itemStack = null;
        try {
            itemStack = ItemSerializer.deserialize(itemData);
        } catch (IOException | ClassNotFoundException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to deserialize item stack for shop item " + itemId + ": " + e.getMessage());
        }

        List<ItemStack> priceItems = null;
        try {
            priceItems = ItemSerializer.deserializeItemList(priceData);
        } catch (IOException | ClassNotFoundException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to deserialize price items for shop item " + itemId + ": " + e.getMessage());
        }
        
        if (itemStack == null || priceItems == null) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to deserialize item data for item " + itemId);
            return null;
        }
        
        ShopItem item = new ShopItem(itemId, itemStack, quantity, description, priceItems);
        item.setAvailable(isAvailable);
        
        return item;
    }
    
    public void deleteShop(UUID shopId) {
        String sql = "UPDATE shops SET is_active = FALSE WHERE shop_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, shopId.toString());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to delete shop: " + e.getMessage());
        }
    }
    
    public void deleteShopItem(UUID shopId, UUID itemId) {
        String sql = "UPDATE shop_items SET is_available = FALSE WHERE shop_id = ? AND item_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, shopId.toString());
            stmt.setString(2, itemId.toString());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to delete shop item: " + e.getMessage());
        }
    }

    public void savePlayerLocale(UUID playerUUID, String locale) {
        String sql = databaseType.equalsIgnoreCase("sqlite") ?
            "INSERT OR REPLACE INTO player_locales (player_id, locale) VALUES (?, ?)" :
            "INSERT INTO player_locales (player_id, locale) VALUES (?, ?) ON DUPLICATE KEY UPDATE locale = VALUES(locale)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, locale);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to save player locale for " + playerUUID + ": " + e.getMessage());
        }
    }

    public Map<UUID, String> loadAllPlayerLocales() {
        Map<UUID, String> playerLocales = new HashMap<>();
        String sql = "SELECT player_id, locale FROM player_locales";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UUID playerUUID = UUID.fromString(rs.getString("player_id"));
                String locale = rs.getString("locale");
                playerLocales.put(playerUUID, locale);
            }
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to load player locales: " + e.getMessage());
        }
        return playerLocales;
    }

    public boolean hasReceivedFirstLoginMessage(UUID playerUUID) {
        String sql = "SELECT 1 FROM first_login_tracking WHERE player_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to check first login message status for " + playerUUID + ": " + e.getMessage());
            return false;
        }
    }

    public void recordFirstLoginMessage(UUID playerUUID) {
        String sql = databaseType.equalsIgnoreCase("sqlite") ?
            "INSERT OR IGNORE INTO first_login_tracking (player_id) VALUES (?)" :
            "INSERT IGNORE INTO first_login_tracking (player_id) VALUES (?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to record first login message for " + playerUUID + ": " + e.getMessage());
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getMessageUtils().sendConsoleMessage("&aDatabase connection closed.");
            }
        } catch (SQLException e) {
            plugin.getMessageUtils().sendConsoleMessage("&cFailed to close database connection: " + e.getMessage());
        }
    }
}