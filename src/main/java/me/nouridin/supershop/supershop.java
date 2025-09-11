package me.nouridin.supershop;

import me.nouridin.Search.SearchBookCommand;
import me.nouridin.Search.SearchBookListener;
import me.nouridin.Search.SearchManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class supershop extends JavaPlugin {

    private static supershop instance;
    private ShopManager shopManager;
    private SearchManager searchManager;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private GUIManager guiManager;
    private ChatInputManager chatInputManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        shopManager = new ShopManager(this);
        searchManager = new SearchManager(this);
        guiManager = new GUIManager(this);
        chatInputManager = new ChatInputManager(this);
        
        // Register commands
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("searchbook").setExecutor(new SearchBookCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new ChestInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new SearchBookListener(this), this);
        
        // Load shops from database
        shopManager.loadAllShops();
        
        getLogger().info("Super Shop has been enabled!");

        // Check for updates
        UpdateChecker updateChecker = new UpdateChecker(this);
        updateChecker.checkForUpdates();

    }

    @Override
    public void onDisable() {
        if (shopManager != null) {
            shopManager.saveAllShops();
        }
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().info("Super Shop has been disabled!");
    }
    
    public static supershop getInstance() {
        return instance;
    }
    
    public ShopManager getShopManager() {
        return shopManager;
    }
    
    public SearchManager getSearchManager() {
        return searchManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public GUIManager getGuiManager() {
        return guiManager;
    }

    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }
}