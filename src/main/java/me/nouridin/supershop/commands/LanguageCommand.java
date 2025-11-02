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

package me.nouridin.supershop.commands;

import me.nouridin.supershop.supershop;
import me.nouridin.supershop.managers.LocaleManager;
import me.nouridin.supershop.util.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LanguageCommand implements CommandExecutor, TabCompleter {

    private final supershop plugin;
    private final LocaleManager localeManager;
    private final MessageUtils messageUtils;

    public LanguageCommand(supershop plugin) {
        this.plugin = plugin;
        this.localeManager = plugin.getLocaleManager();
        this.messageUtils = plugin.getMessageUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtils.sendMessage(sender, localeManager.getMessage("command-player-only"));
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (args.length == 0) {
            // Display current language and available languages
            String currentLocale = localeManager.getPlayerLocale(playerUUID);
            Set<String> availableLocales = localeManager.getAvailableLocales();

            messageUtils.sendMessage(player, localeManager.getMessage(playerUUID, "language.current", currentLocale));
            messageUtils.sendMessage(player, localeManager.getMessage(playerUUID, "language.available", String.join(", ", availableLocales)));
            messageUtils.sendMessage(player, localeManager.getMessage(playerUUID, "language.usage"));
            return true;
        }

        String newLocale = args[0].toLowerCase();

        if (!localeManager.isLocaleAvailable(newLocale)) {
            messageUtils.sendMessage(player, localeManager.getMessage(playerUUID, "language.not-available", newLocale));
            return true;
        }

        localeManager.setPlayerLocale(playerUUID, newLocale);
        messageUtils.sendMessage(player, localeManager.getMessage(playerUUID, "language.set-success", newLocale));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return localeManager.getAvailableLocales().stream()
                    .filter(locale -> locale.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
