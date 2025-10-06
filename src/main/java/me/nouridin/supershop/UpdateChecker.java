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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker implements Listener {
    private final JavaPlugin plugin;
    private final String currentVersion;
    private boolean updateAvailable = false;
    private String latestVersion = "";

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();

        // Register join listener so ops/admins are notified even if they log in later
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/super-shop/version");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", plugin.getName() + " UpdateChecker");
                conn.connect();

                if (conn.getResponseCode() != 200) return;

                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();
                reader.close();

                if (versions.size() > 0) {
                    JsonObject latest = versions.get(0).getAsJsonObject();
                    latestVersion = latest.get("version_number").getAsString();

                    if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                        updateAvailable = true;
                        notifyUpdateNow();
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[SuperShop] Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private void notifyUpdateNow() {
        // Run sync for player + console messages
        Bukkit.getScheduler().runTask(plugin, () -> {
            String separator = "§6§m" + "----------------------------------------";

            // Console notification (colors preserved in most terminals)
            Bukkit.getConsoleSender().sendMessage(separator);
            Bukkit.getConsoleSender().sendMessage("§e[§aSuperShop§e] §cA new update is available!");
            Bukkit.getConsoleSender().sendMessage("§7Current version: §f" + currentVersion);
            Bukkit.getConsoleSender().sendMessage("§7Latest version: §a" + latestVersion);
            Bukkit.getConsoleSender().sendMessage("§eDownload: §bhttps://modrinth.com/plugin/super-shop");
            Bukkit.getConsoleSender().sendMessage(separator);

            // Notify all currently online ops/admins
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp() || player.hasPermission("supershop.update.notify")) {
                    sendPlayerUpdateMessage(player);
                }
            }
        });
    }

    // Also notify admins/ops when they join later
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!updateAvailable) return;
        Player player = event.getPlayer();
        if (player.isOp() || player.hasPermission("supershop.update.notify")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> sendPlayerUpdateMessage(player), 40L); // ~2 seconds after join
        }
    }

    private void sendPlayerUpdateMessage(Player player) {
        String separator = "§6§m" + "----------------------------------------";
        player.sendMessage(separator);
        player.sendMessage("§e§l⚡ SuperShop Update Available! ⚡");
        player.sendMessage("§7Current: §f" + currentVersion + " §8➜ §a" + latestVersion);
        player.sendMessage("§eDownload: §bhttps://modrinth.com/plugin/super-shop");
        player.sendMessage(separator);
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
