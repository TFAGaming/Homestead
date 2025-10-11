package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

/**
 * Regions list menu. Operators can toggle a special "Show all regions" mode
 * via the first list item. Status is shown in the item's lore, with a clear
 * click instruction.
 */
public class RegionsMenu {

    private static final Set<UUID> ADMIN_SHOW_ALL = ConcurrentHashMap.newKeySet();

    private List<Region> regions = new ArrayList<>();

    private static boolean isShowAllEnabled(Player p) {
        return p.isOp() && ADMIN_SHOW_ALL.contains(p.getUniqueId());
    }

    private static void toggleShowAll(Player p) {
        if (!p.isOp()) return;
        UUID id = p.getUniqueId();
        if (!ADMIN_SHOW_ALL.add(id)) ADMIN_SHOW_ALL.remove(id);
    }

    private List<Region> computeRegionList(Player player) {
        if (isShowAllEnabled(player)) {
            return new ArrayList<>(RegionsManager.getAll());
        }
        List<Region> list = new ArrayList<>();
        list.addAll(RegionsManager.getRegionsOwnedByPlayer(player));
        list.addAll(RegionsManager.getRegionsHasPlayerAsMember(player));
        return ListUtils.removeDuplications(list);
    }

    private static ItemStack createAdminToggleItem(Player player) {
        boolean on = isShowAllEnabled(player);

        ItemStack item = new ItemStack(on ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Show all regions");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Status: " + (on ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        lore.add(ChatColor.DARK_GRAY + (on ? "(Currently showing all regions.)" : "(Showing only your regions.)"));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Left-click: " + ChatColor.WHITE + "Toggle");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private List<ItemStack> getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();

        if (player.isOp()) {
            items.add(createAdminToggleItem(player));
        }

        for (Region region : regions) {
            HashMap<String, String> replacements = new HashMap<>();
            replacements.put("{region}", region.getName());
            replacements.put("{region-displayname}", region.getDisplayName());
            replacements.put("{region-owner}", region.getOwner().getName());
            replacements.put("{region-bank}", Formatters.formatBalance(region.getBank()));
            replacements.put("{region-createdat}", Formatters.formatDate(region.getCreatedAt()));

            Region targetRegion = TargetRegionSession.getRegion(player);
            if (targetRegion != null && targetRegion.getUniqueId().equals(region.getUniqueId())) {
                items.add(MenuUtils.getButton(5, replacements));
            } else {
                items.add(MenuUtils.getButton(4, replacements));
            }
        }

        return items;
    }

    public RegionsMenu(Player player) {
        this.regions = computeRegionList(player);

        PaginationMenu gui = new PaginationMenu(
                MenuUtils.getTitle(0),
                9 * 4,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(),
                getItems(player),
                (_player, event) -> _player.closeInventory(),
                (_player, context) -> {
                    boolean hasToggle = _player.isOp();
                    int index = context.getIndex();

                    if (hasToggle && index == 0) {
                        if (context.getEvent().isLeftClick()) {
                            toggleShowAll(_player);
                            new RegionsMenu(_player);
                        }
                        return;
                    }

                    if (hasToggle) index--;

                    if (index < 0 || index >= regions.size()) return;

                    Region region = regions.get(index);

                    if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
                        new RegionInfoMenu(_player, region, () -> new RegionsMenu(_player));
                        return;
                    }

                    if (context.getEvent().isRightClick()) {
                        if (region.getLocation() == null) {
                            Map<String, String> replacements = new HashMap<>();
                            replacements.put("{region}", region.getName());
                            PlayerUtils.sendMessage(_player, 71, replacements);
                            return;
                        }
                        new DelayedTeleport(_player, region.getLocation().getBukkitLocation());
                        return;
                    }

                    if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
                        if (TargetRegionSession.getRegion(_player) != null
                                && TargetRegionSession.getRegion(_player).getUniqueId().equals(region.getUniqueId())) {
                            return;
                        }

                        new TargetRegionSession(_player, region);
                        _player.playSound(_player.getLocation(), Sound.BLOCK_LEVER_CLICK, 500.0f, 1.0f);

                        Map<String, String> replacements = new HashMap<>();
                        replacements.put("{region}", region.getName());
                        PlayerUtils.sendMessage(_player, 12, replacements);

                        PaginationMenu instance = context.getInstance();
                        regions = computeRegionList(_player);
                        instance.setItems(getItems(_player));
                        return;
                    }

                    if (context.getEvent().isLeftClick()) {
                        new RegionMenu(_player, region);
                    }
                }
        );

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
