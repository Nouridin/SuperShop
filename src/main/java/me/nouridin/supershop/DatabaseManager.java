package me.nouridin.supershop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.io.File;
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
                MessageUtils.sendConsoleMessage("&cUnsupported database type: " + databaseType);
                return;
            }
            
            createTables();
            MessageUtils.sendConsoleMessage("&aDatabase initialized successfully!");
            
        } catch (SQLException e) {
            MessageUtils.sendConsoleMessage("&cFailed to initialize database: " + e.getMessage());
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
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createShopsTable);
            stmt.execute(createShopItemsTable);
            
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
            stmt.setString(11, ItemSerializer.serializeItemList(shop.getRevenue()));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            MessageUtils.sendConsoleMessage("&cFailed to save shop: " + e.getMessage());
        }
    }
    
    public void saveShopItem(UUID shopId, ShopItem item) {
        String sql = databaseType.equalsIgnoreCase("sqlite") ?
            "INSERT OR REPLACE INTO shop_items (item_id, shop_id, item_data, quantity, description, price_data, is_available) VALUES (?, ?, ?, ?, ?, ?, ?)" :
            "INSERT INTO shop_items (item_id, shop_id, item_data, quantity, description, price_data, is_available) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE item_data = VALUES(item_data), quantity = VALUES(quantity), description = VALUES(description), price_data = VALUES(price_data), is_available = VALUES(is_available)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, item.getItemId().toString());
            stmt.setString(2, shopId.toString());
            stmt.setString(3, ItemSerializer.serialize(item.getItemStack()));
            stmt.setInt(4, item.getQuantity());
            stmt.setString(5, item.getDescription());
            stmt.setString(6, ItemSerializer.serializeItemList(item.getPriceItems()));
            stmt.setBoolean(7, item.isAvailable());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            MessageUtils.sendConsoleMessage("&cFailed to save shop item: " + e.getMessage());
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
            MessageUtils.sendConsoleMessage("&cFailed to load shops: " + e.getMessage());
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
            MessageUtils.sendConsoleMessage("&cFailed to load shop items: " + e.getMessage());
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
            MessageUtils.sendConsoleMessage("&cWorld '" + worldName + "' not found for shop " + shopId);
            return null;
        }
        
        Location location = new Location(world, x, y, z);
        Shop shop = new Shop(shopId, ownerId, ownerName, location);
        shop.setActive(isActive);
        shop.setCreatedAt(createdAt);
        shop.setLastUpdated(lastUpdated);
        
        // Load revenue data
        if (revenueData != null && !revenueData.isEmpty()) {
            List<ItemStack> revenue = ItemSerializer.deserializeItemList(revenueData);
            shop.setRevenue(revenue);
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
        
        var itemStack = ItemSerializer.deserialize(itemData);
        var priceItems = ItemSerializer.deserializeItemList(priceData);
        
        if (itemStack == null || priceItems == null) {
            MessageUtils.sendConsoleMessage("&cFailed to deserialize item data for item " + itemId);
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
            MessageUtils.sendConsoleMessage("&cFailed to delete shop: " + e.getMessage());
        }
    }
    
    public void deleteShopItem(UUID shopId, UUID itemId) {
        String sql = "UPDATE shop_items SET is_available = FALSE WHERE shop_id = ? AND item_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, shopId.toString());
            stmt.setString(2, itemId.toString());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            MessageUtils.sendConsoleMessage("&cFailed to delete shop item: " + e.getMessage());
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                MessageUtils.sendConsoleMessage("&aDatabase connection closed.");
            }
        } catch (SQLException e) {
            MessageUtils.sendConsoleMessage("&cFailed to close database connection: " + e.getMessage());
        }
    }
}