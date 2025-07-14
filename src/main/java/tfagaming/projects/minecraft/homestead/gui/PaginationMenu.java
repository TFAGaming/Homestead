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
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class PaginationMenu implements Listener {
    private final Homestead plugin;
    private final String title;
    private final int size;
    private final int contentSize;
    private final ItemStack nextPageItem;
    private final ItemStack prevPageItem;
    private final List<ItemStack> items;
    private final Map<Integer, BiConsumer<Player, InventoryClickEvent>> bottomRowActions = new HashMap<>();
    private final Map<Integer, ItemStack> bottomRowActionItems = new HashMap<>();
    private final BiConsumer<Player, InventoryClickEvent> goBackCallback;
    private final BiConsumer<Player, ClickContext> clickCallback;
    private boolean pageChanged = false;
    private Player player;
    private ItemStack fillerItemstack = null;

    private int currentPage;

    public PaginationMenu(String title, int size, ItemStack nextPageItem, ItemStack prevPageItem, List<ItemStack> items,
            BiConsumer<Player, InventoryClickEvent> goBackCallback, BiConsumer<Player, ClickContext> clickCallback) {
        if (size % 9 != 0 || size < 36) {
            throw new IllegalArgumentException("Inventory size must be a multiple of 9 and at least 36.");
        }

        this.plugin = Homestead.getInstance();
        this.title = title;
        this.size = size;
        this.contentSize = size - 18;
        this.nextPageItem = nextPageItem;
        this.prevPageItem = prevPageItem;
        this.items = items;
        this.goBackCallback = goBackCallback;
        this.clickCallback = clickCallback;
        this.currentPage = 0;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void addActionButton(int index, ItemStack item, BiConsumer<Player, InventoryClickEvent> callback) {
        int[] validSlots = { size - 6, size - 5, size - 4 };

        if (index < 0 || index >= validSlots.length) {
            throw new IllegalArgumentException("Invalid index. Only 0, 1, or 2 are allowed.");
        }

        int slot = validSlots[index];

        bottomRowActions.put(slot, callback);
        bottomRowActionItems.put(slot, item);
    }

    public void open(Player player) {
        this.player = player;

        player.openInventory(createPage(currentPage));

        InventoryManager.register(player, this);
    }

    public void open(Player player, ItemStack filler) {
        this.player = player;

        Inventory inventory = createPage(currentPage);

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, filler);
        }

        for (int i = size - 9; i < size; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        fillerItemstack = filler;

        player.openInventory(inventory);

        InventoryManager.register(player, this);
    }

    public void replaceSlot(int index, ItemStack newItem) {
        if (index < 0 || index >= items.size()) {
            return;
        }

        items.set(index, newItem);

        int start = currentPage * contentSize;
        int end = Math.min(start + contentSize, items.size());

        if (index >= start && index < end) {
            int slot = (index - start) + 9;

            if (player != null) {
                Inventory inventory = player.getOpenInventory().getTopInventory();

                inventory.setItem(slot, newItem);
            }
        }
    }

    public void setItems(List<ItemStack> newItems) {
        boolean wasOpen = player != null && player.getOpenInventory() != null;
        // String oldTitle = wasOpen ? player.getOpenInventory().getTitle() : null;

        this.items.clear();
        this.items.addAll(newItems);

        int totalPages = getTotalPages();

        if (currentPage >= totalPages && totalPages > 0) {
            currentPage = totalPages - 1;
        } else if (totalPages == 0) {
            currentPage = 0;
        }

        if (wasOpen) {
            InventoryManager.unregister(player);

            Inventory newInventory = createPage(currentPage);

            pageChanged = true;

            player.openInventory(newInventory);

            InventoryManager.register(player, this);

            new BukkitRunnable() {
                @Override
                public void run() {
                    pageChanged = false;
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) items.size() / contentSize);
    }

    private Inventory createPage(int page) {
        Inventory inventory = Bukkit.createInventory(null, size,
                Formatters.formatPaginationMenuTitle(title, page + 1, getTotalPages()));

        int start = page * contentSize;
        int end = Math.min(start + contentSize, items.size());

        for (int i = start, slot = 9; i < end; i++, slot++) {
            inventory.setItem(slot, items.get(i));
        }

        if (page > 0) {
            inventory.setItem(size - 9, prevPageItem);
        } else {
            inventory.setItem(size - 9, MenuUtils.getBackButton());
        }

        if (end < items.size()) {
            inventory.setItem(size - 1, nextPageItem);
        }

        int[] validSlots = { size - 6, size - 5, size - 4 };

        for (int i = 0; i < validSlots.length; i++) {
            if (bottomRowActionItems.containsKey(validSlots[i])) {
                inventory.setItem(validSlots[i], bottomRowActionItems.get(validSlots[i]));
            }
        }

        if (fillerItemstack != null) {
            for (int i = 0; i < 9; i++) {
                inventory.setItem(i, fillerItemstack);
            }

            for (int i = size - 9; i < size; i++) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, fillerItemstack);
                }
            }
        }

        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (InventoryManager.getMenu(player) != this) {
            return;
        }

        if (!event.getView().getTitle().startsWith(Formatters.formatPaginationMenuTitle(title, currentPage + 1, getTotalPages()))) {
            return;
        }

        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (bottomRowActions.containsKey(slot)) {
            bottomRowActions.get(slot).accept(player, event);
            return;
        }

        if (slot < 9) {
            return;
        }

        if (slot == size - 9 && currentPage > 0) {
            currentPage--;
            pageChanged = true;
            player.openInventory(createPage(currentPage));
            return;
        }

        if (slot == size - 1 && (currentPage + 1) * contentSize < items.size()) {
            currentPage++;
            pageChanged = true;
            player.openInventory(createPage(currentPage));
            return;
        }

        if (slot == size - 9) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    goBackCallback.accept(player, event);

                    destroy();
                }
            }.runTask(plugin);
        }

        if (slot >= 9 && slot < size - 9) {
            int itemIndex = currentPage * contentSize + (slot - 9);

            if (itemIndex < items.size()) {
                clickCallback.accept(player, new ClickContext(event, itemIndex, items, this));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (pageChanged) {
            pageChanged = false;
            return;
        }

        Player player = (Player) event.getPlayer();

        if (InventoryManager.getMenu(player) == this) {
            InventoryManager.unregister(player);

            if (!InventoryManager.hasMenu(player)) {
                destroy();
            }
        }
    }

    public static class ClickContext {
        private final InventoryClickEvent event;
        private final int index;
        private final List<ItemStack> items;
        private final PaginationMenu instance;

        public ClickContext(InventoryClickEvent event, int index, List<ItemStack> items, PaginationMenu instance) {
            this.event = event;
            this.index = index;
            this.items = items;
            this.instance = instance;
        }

        public InventoryClickEvent getEvent() {
            return event;
        }

        public int getIndex() {
            return index;
        }

        public List<ItemStack> getItems() {
            return items;
        }

        public PaginationMenu getInstance() {
            return instance;
        }
    }
}