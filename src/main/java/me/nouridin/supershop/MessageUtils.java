package me.nouridin.supershop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static void sendMessage(Player player, String message) {
        if (player != null && player.isOnline()) {
            player.sendMessage(colorize(message));
        }
    }
    
    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(colorize(message));
        }
    }
    
    public static void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(colorize(message));
    }
    
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }
    
    public static String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " day" + (days != 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours != 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        }
    }
    
    public static String createSeparator(char character, int length) {
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < length; i++) {
            separator.append(character);
        }
        return separator.toString();
    }
    
    public static String createHeader(String title) {
        String separator = createSeparator('=', 50);
        return "&6" + separator + "\n" + 
               "&e" + title + "\n" + 
               "&6" + separator;
    }
    
    public static String createFooter() {
        return "&6" + createSeparator('=', 50);
    }
    
    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }
}