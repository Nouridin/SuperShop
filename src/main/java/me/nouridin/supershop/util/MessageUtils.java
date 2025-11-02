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

package me.nouridin.supershop.util;

import me.nouridin.supershop.supershop;
import me.nouridin.supershop.managers.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MessageUtils {

    private final supershop plugin;
    private LocaleManager localeManager; // Made non-final

    public MessageUtils(supershop plugin) {
        this.plugin = plugin;
        // Removed localeManager initialization from here
    }

    // New method to set LocaleManager after it's initialized
    public void setLocaleManager(LocaleManager localeManager) {
        this.localeManager = localeManager;
    }

    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void sendMessage(Player player, String key, String... args) {
        if (player != null && player.isOnline()) {
            // Check if localeManager is initialized before using it
            String message = (localeManager != null) ? localeManager.getMessage(player.getUniqueId(), key, args) : "&cError: LocaleManager not initialized for key: " + key;
            player.sendMessage(colorize(message));
        }
    }

    public void sendMessage(CommandSender sender, String key, String... args) {
        if (sender != null) {
            UUID uuid = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;
            // Check if localeManager is initialized before using it
            String message = (localeManager != null) ? localeManager.getMessage(uuid, key, args) : "&cError: LocaleManager not initialized for key: " + key;
            sender.sendMessage(colorize(message));
        }
    }

    public void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(colorize(message));
    }

    public String formatNumber(long number) {
        return String.format("%,d", number);
    }

    public String formatDuration(long milliseconds, UUID playerUUID) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (localeManager == null) return "Error: LocaleManager not initialized";

        if (days > 0) {
            return days + " " + (days != 1 ? localeManager.getMessage(playerUUID, "time-unit-days") : localeManager.getMessage(playerUUID, "time-unit-day"));
        } else if (hours > 0) {
            return hours + " " + (hours != 1 ? localeManager.getMessage(playerUUID, "time-unit-hours") : localeManager.getMessage(playerUUID, "time-unit-hour"));
        } else if (minutes > 0) {
            return minutes + " " + (minutes != 1 ? localeManager.getMessage(playerUUID, "time-unit-minutes") : localeManager.getMessage(playerUUID, "time-unit-minute"));
        } else {
            return seconds + " " + (seconds != 1 ? localeManager.getMessage(playerUUID, "time-unit-seconds") : localeManager.getMessage(playerUUID, "time-unit-second"));
        }
    }

    public String createSeparator(char character, int length) {
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < length; i++) {
            separator.append(character);
        }
        return separator.toString();
    }

    public String createHeader(String title) {
        if (localeManager == null) return colorize("&cError: LocaleManager not initialized");
        String separatorChar = localeManager.getMessage("header-separator", "=");
        String separator = createSeparator(separatorChar.charAt(0), 50);
        String titleColor = localeManager.getMessage("header-title-color", "&e");
        String prefix = localeManager.getMessage("plugin-prefix", "&6[&eSuperShop&6] ");

        return colorize(prefix + "&6" + separator + "\n" +
                titleColor + title + "\n" +
                "&6" + separator);
    }

    public String createFooter() {
        if (localeManager == null) return colorize("&cError: LocaleManager not initialized");
        String separatorChar = localeManager.getMessage("footer-separator", "=");
        return colorize("&6" + createSeparator(separatorChar.charAt(0), 50));
    }

    public String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }
}
