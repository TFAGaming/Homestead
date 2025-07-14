package tfagaming.projects.minecraft.homestead.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class InventoryManager {
    private static final Map<Player, Object> playerMenus = new HashMap<>();

    public static void register(Player player, Menu menu) {
        playerMenus.put(player, menu);
    }

    public static void register(Player player, PaginationMenu menu) {
        playerMenus.put(player, menu);
    }

    public static void unregister(Player player) {
        playerMenus.remove(player);
    }

    public static Object getMenu(Player player) {
        return playerMenus.get(player);
    }

    public static boolean hasMenu(Player player) {
        return playerMenus.containsKey(player);
    }
}