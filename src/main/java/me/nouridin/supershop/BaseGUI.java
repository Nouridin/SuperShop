package me.nouridin.supershop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseGUI implements Listener {

    protected final supershop plugin;
    protected final Player player;
    protected Inventory inventory;
    protected final String title;
    protected final int size;
    protected final Set<Integer> interactableSlots;

    public BaseGUI(supershop plugin, Player player, String title, int size) {
        this.plugin = plugin;
        this.player = player;
        this.title = MessageUtils.colorize(title);
        this.size = size;
        this.interactableSlots = new HashSet<>();

        // Register once
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        this.inventory = Bukkit.createInventory(null, size, title);
        setupInventory();
        player.openInventory(inventory);
    }

    public void close() {
        player.closeInventory();
    }

    protected abstract void setupInventory();
    protected abstract void handleClick(InventoryClickEvent event);

    protected void handleClose(InventoryCloseEvent event) {
        // Optional: cleanup if needed, but do NOT unregister events here
    }

    protected void addInteractableSlot(int slot) {
        interactableSlots.add(slot);
    }

    protected void addInteractableSlots(int... slots) {
        for (int slot : slots) {
            interactableSlots.add(slot);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player clickedPlayer)) return;
        if (!clickedPlayer.equals(player)) return;

        int slot = event.getSlot();

        // Allow interactions with player inventory
        if (event.getClickedInventory() != inventory) return;

        // Allow specific interactable slots
        if (interactableSlots.contains(slot)) {
            handleClick(event);
            return;
        }

        // Cancel all other GUI clicks
        event.setCancelled(true);
        handleClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getPlayer() instanceof Player closedPlayer)) return;
        if (!closedPlayer.equals(player)) return;

        handleClose(event);
    }

    protected ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize(name));
            if (lore.length > 0) {
                meta.setLore(Arrays.stream(lore)
                        .map(MessageUtils::colorize)
                        .collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createInfoItem(String name, String... info) {
        return createButton(Material.PAPER, name, info);
    }

    protected void fillBorder(Material material) {
        ItemStack border = createButton(material, " ");

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(inventory.getSize() - 9 + i, border);
        }

        for (int i = 9; i < inventory.getSize() - 9; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }

    protected ItemStack createBackButton() {
        return createButton(Material.ARROW, "&7← Back", "&eClick to go back");
    }

    protected ItemStack createCloseButton() {
        return createButton(Material.BARRIER, "&cClose", "&eClick to close this menu");
    }

    public void refresh() {
        inventory.clear();
        setupInventory();
    }

    protected boolean isPlaceholder(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        String displayName = item.getItemMeta().getDisplayName();
        return displayName.contains("Place item here") ||
                displayName.contains("Price Item") ||
                displayName.equals(" ");
    }
}
