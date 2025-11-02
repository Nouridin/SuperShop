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

import me.nouridin.supershop.supershop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LocaleManager {

    private final supershop plugin;
    private final Map<String, FileConfiguration> loadedLocales;
    private final Map<UUID, String> playerLocales; // Stores player UUID to locale string
    private final String defaultLocale = "en"; // Default locale if not specified or found
    private final File localeFolder;

    public LocaleManager(supershop plugin) {
        this.plugin = plugin;
        this.loadedLocales = new HashMap<>();
        this.playerLocales = new HashMap<>();
        this.localeFolder = new File(plugin.getDataFolder(), "loc");
        if (!localeFolder.exists()) {
            localeFolder.mkdirs();
        }
        
        // Ensure default locale messages are saved and loaded
        saveDefaultLocale(defaultLocale);
        loadLocaleConfig(defaultLocale);
        
        // Player locales are loaded in the main plugin class after this manager is initialized.
    }

    public String getPlayerLocale(UUID playerUUID) {
        return playerLocales.getOrDefault(playerUUID, defaultLocale);
    }

    public void setPlayerLocale(UUID playerUUID, String locale) {
        // Validate if the locale exists
        if (!isLocaleAvailable(locale)) {
            plugin.getLogger().warning("Attempted to set unknown locale '" + locale + "' for player " + playerUUID + ". Falling back to default.");
            playerLocales.put(playerUUID, defaultLocale);
            // Save the default choice to the database
            plugin.getDatabaseManager().savePlayerLocale(playerUUID, defaultLocale);
            return;
        }
        
        // Load the locale config if it's not already loaded
        if (!loadedLocales.containsKey(locale)) {
            loadLocaleConfig(locale);
        }

        playerLocales.put(playerUUID, locale);
        // Save the player's preference to the database
        plugin.getDatabaseManager().savePlayerLocale(playerUUID, locale);
    }

    public String getMessage(UUID playerUUID, String key) {
        String locale = getPlayerLocale(playerUUID);
        FileConfiguration config = loadedLocales.get(locale);
        if (config == null || !config.contains(key)) {
            // Fallback to default if player's locale config isn't loaded or key is missing
            config = loadedLocales.get(defaultLocale);
            if (config == null) {
                plugin.getLogger().severe("Default locale config not loaded! Cannot retrieve message for key: " + key);
                return "Error: Default locale config missing.";
            }
        }
        return config.getString(key, "Translation for key '" + key + "' not found for locale '" + locale + "'.");
    }

    public String getMessage(UUID playerUUID, String key, String... args) {
        String message = getMessage(playerUUID, key);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i]);
        }
        return message;
    }
    
    // Overload for console/non-player messages, uses default locale
    public String getMessage(String key) {
        // Using a null UUID will make getPlayerLocale return the default locale.
        return getMessage((UUID) null, key);
    }

    // Overload for console/non-player messages with default value
    public String getMessage(String key, String defaultValue) {
        FileConfiguration config = loadedLocales.get(defaultLocale);
        if (config == null) {
            plugin.getLogger().severe("Default locale config not loaded! Cannot retrieve message for key: " + key);
            return defaultValue;
        }
        return config.getString(key, defaultValue);
    }

    // Overload for console/non-player messages with args
    public String getMessage(String key, String... args) {
        String message = getMessage(key);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i]);
        }
        return message;
    }


    public boolean loadLocaleConfig(String locale) {
        if (loadedLocales.containsKey(locale)) {
            return true; // Already loaded
        }

        File localeFile = new File(localeFolder, locale + ".yml");
        FileConfiguration config;

        if (!localeFile.exists()) {
            // Try to save it from the resources folder
            saveDefaultLocale(locale);
            // Check again if it exists now
            if (!localeFile.exists()) {
                 plugin.getLogger().warning("Locale file " + locale + ".yml not found in resources or data folder's loc directory.");
                return false;
            }
        }
        
        config = YamlConfiguration.loadConfiguration(localeFile);
        
        // Also load the default from resources to fill in any missing keys
        InputStream defaultStream = plugin.getResource("loc/" + locale + ".yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }

        loadedLocales.put(locale, config);
        return true;
    }

    private void saveDefaultLocale(String locale) {
        File localeFile = new File(localeFolder, locale + ".yml");
        if (!localeFile.exists()) {
            // The resource path inside the jar
            String resourcePath = "loc/" + locale + ".yml";
            // saveResource will not overwrite if the file exists.
            plugin.saveResource(resourcePath, false);
        }
    }

    public void reloadLocales() {
        loadedLocales.clear();
        
        // Reload all available locales from the loc folder
        Set<String> availableLocales = getAvailableLocales();
        availableLocales.add(defaultLocale); // Ensure default is always there

        for (String locale : availableLocales) {
            saveDefaultLocale(locale); // This will copy from jar if not present
            loadLocaleConfig(locale);
        }

        // Re-apply locales for online players to ensure they get updated messages
        for (UUID playerUUID : playerLocales.keySet()) {
            String locale = playerLocales.get(playerUUID);
            if (!availableLocales.contains(locale)) {
                playerLocales.put(playerUUID, defaultLocale);
            }
        }
    }
    
    public Map<UUID, String> getPlayerLocaleMap() {
        return Collections.unmodifiableMap(playerLocales);
    }

    public void setPlayerLocaleMap(Map<UUID, String> playerLocales) {
        this.playerLocales.clear();
        this.playerLocales.putAll(playerLocales);
        // Ensure all loaded player locales are actually loaded into loadedLocales map
        for (String locale : new HashSet<>(playerLocales.values())) {
            loadLocaleConfig(locale);
        }
    }

    public Set<String> getAvailableLocales() {
        Set<String> available = new HashSet<>();
        // Locales on disk
        if (localeFolder.exists() && localeFolder.isDirectory()) {
            File[] files = localeFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    String locale = fileName.substring(0, fileName.length() - ".yml".length());
                    available.add(locale);
                }
            }
        }
        // It's tricky to get locales from inside the jar resources dynamically.
        // For now, we rely on what's been saved to the loc folder.
        // The default 'en' should be added manually if needed.
        available.add(defaultLocale);
        return available;
    }
    
    public boolean isLocaleAvailable(String locale) {
        return getAvailableLocales().contains(locale) || plugin.getResource("loc/" + locale + ".yml") != null;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }
}
