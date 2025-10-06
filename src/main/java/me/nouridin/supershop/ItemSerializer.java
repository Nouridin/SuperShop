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

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ItemSerializer {
    
    public static String serialize(ItemStack item) {
        if (item == null) {
            return "";
        }
        
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            dataOutput.writeObject(item);
            dataOutput.close();
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            
        } catch (IOException e) {
            MessageUtils.sendConsoleMessage("&cFailed to serialize ItemStack: " + e.getMessage());
            return "";
        }
    }
    
    public static ItemStack deserialize(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            
            return item;
            
        } catch (IOException | ClassNotFoundException e) {
            MessageUtils.sendConsoleMessage("&cFailed to deserialize ItemStack: " + e.getMessage());
            return null;
        }
    }
    
    public static String serializeItemList(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            dataOutput.writeInt(items.size());
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            
        } catch (IOException e) {
            MessageUtils.sendConsoleMessage("&cFailed to serialize ItemStack list: " + e.getMessage());
            return "";
        }
    }
    
    public static List<ItemStack> deserializeItemList(String data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            
            int size = dataInput.readInt();
            List<ItemStack> items = new ArrayList<>();
            
            for (int i = 0; i < size; i++) {
                ItemStack item = (ItemStack) dataInput.readObject();
                items.add(item);
            }
            
            dataInput.close();
            return items;
            
        } catch (IOException | ClassNotFoundException e) {
            MessageUtils.sendConsoleMessage("&cFailed to deserialize ItemStack list: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}