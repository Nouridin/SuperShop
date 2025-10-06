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