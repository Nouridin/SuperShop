package me.nouridin.supershop;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final supershop plugin;
    private FileConfiguration config;
    
    public ConfigManager(supershop plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        setDefaults();
    }
    
    private void setDefaults() {
        config.addDefault("database.type", "sqlite");
        config.addDefault("database.host", "localhost");
        config.addDefault("database.port", 3306);
        config.addDefault("database.database", "supershop");
        config.addDefault("database.username", "root");
        config.addDefault("database.password", "");
        
        config.addDefault("shop.max-shops-per-player", 10);
        config.addDefault("shop.max-items-per-shop", 54);
        config.addDefault("shop.allow-cross-world-trading", true);
        config.addDefault("shop.require-permission-to-create", false);
        
        config.addDefault("search-book.enabled", true);
        config.addDefault("search-book.max-results", 50);
        config.addDefault("search-book.search-radius", 10000);
        config.addDefault("search-book.require-permission", false);
        config.addDefault("search-book.cooldown-seconds", 5);
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    // Database getters
    public String getDatabaseType() { return config.getString("database.type", "sqlite"); }
    public String getDatabaseHost() { return config.getString("database.host", "localhost"); }
    public int getDatabasePort() { return config.getInt("database.port", 3306); }
    public String getDatabaseName() { return config.getString("database.database", "supershop"); }
    public String getDatabaseUsername() { return config.getString("database.username", "root"); }
    public String getDatabasePassword() { return config.getString("database.password", ""); }
    
    // Shop getters
    public int getMaxShopsPerPlayer() { return config.getInt("shop.max-shops-per-player", 10); }
    public int getMaxItemsPerShop() { return config.getInt("shop.max-items-per-shop", 54); }
    public boolean isAllowCrossWorldTrading() { return config.getBoolean("shop.allow-cross-world-trading", true); }
    public boolean isRequirePermissionToCreate() { return config.getBoolean("shop.require-permission-to-create", false); }
    
    // Search Book getters
    public boolean isSearchBookEnabled() { return config.getBoolean("search-book.enabled", true); }
    public int getMaxSearchResults() { return config.getInt("search-book.max-results", 50); }
    public double getSearchRadius() { return config.getDouble("search-book.search-radius", 10000); }
    public boolean isSearchBookRequirePermission() { return config.getBoolean("search-book.require-permission", false); }
    public int getSearchCooldownSeconds() { return config.getInt("search-book.cooldown-seconds", 5); }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
}