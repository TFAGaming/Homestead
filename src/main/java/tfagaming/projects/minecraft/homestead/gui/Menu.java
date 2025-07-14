package tfagaming.projects.minecraft.homestead.gui;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Menu implements Listener {
    private final Homestead plugin;
    private final Inventory inventory;
    private final Map<Integer, BiConsumer<Player, InventoryClickEvent>> callbacks;

    public Menu(String title, int size) {
        this.plugin = Homestead.getInstance();
        this.inventory = Bukkit.createInventory(null, size, ChatColorTranslator.translate(title));
        this.callbacks = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public Menu(String title, int size, boolean beautiful) {
        this.plugin = Homestead.getInstance();
        this.inventory = Bukkit.createInventory(null, size, ChatColorTranslator.translate(title));
        this.callbacks = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void addItem(int slot, ItemStack itemStack, BiConsumer<Player, InventoryClickEvent> callback) {
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }

        inventory.setItem(slot, itemStack);
        callbacks.put(slot, callback);
    }

    public void open(Player player) {
        player.openInventory(inventory);

        InventoryManager.register(player, this);
    }

    public void open(Player player, ItemStack filler) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        player.openInventory(inventory);

        InventoryManager.register(player, this);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (InventoryManager.getMenu(player) == this && event.getInventory().equals(this.inventory)) {
            event.setCancelled(true);

            int slot = event.getRawSlot();

            if (callbacks.containsKey(slot)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callbacks.get(slot).accept(player, event);
                    }
                }.runTask(plugin);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (InventoryManager.getMenu(player) == this) {
            InventoryManager.unregister(player);

            if (!InventoryManager.hasMenu(player)) {
                unregister();
            }
        }
    }
}