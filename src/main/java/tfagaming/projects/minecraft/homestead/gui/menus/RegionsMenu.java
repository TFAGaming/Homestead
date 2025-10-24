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
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;

/**
 * Paginated list of regions a player can interact with.
 * <p>
 * Operators can toggle a special "show all regions" mode using the first item.
 * The toggle status is displayed in the item's lore and the click action is
 * explicit. For non-operators, only owned and member regions are shown.
 * <p>
 * Right-click on a region attempts a teleport to the region's spawn if allowed.
 * Left-click opens the {@link RegionMenu}. Shift+Left sets the player's
 * targeted region. Shift+Right opens a read-only {@link RegionInfoMenu}.
 */
public class RegionsMenu {

    /**
     * Per-session toggle storage for the "show all regions" operator mode.
     * Contains the UUIDs of operators who have enabled the mode.
     */
    private static final Set<UUID> ADMIN_SHOW_ALL = ConcurrentHashMap.newKeySet();

    /**
     * The list of regions to display for the current viewer. Contents depend on
     * whether the operator toggle is enabled.
     */
    private List<Region> regions = new ArrayList<>();

    /**
     * Returns whether the given player has "show all regions" mode enabled.
     *
     * @param p player
     * @return true if player is op and has the toggle enabled
     */
    private static boolean isShowAllEnabled(Player p) {
        return PlayerUtils.isOperator(p) && ADMIN_SHOW_ALL.contains(p.getUniqueId());
    }

    /**
     * Toggles the "show all regions" mode for the given player.
     * No-op for non-operators.
     *
     * @param p player
     */
    private static void toggleShowAll(Player p) {
        if (!PlayerUtils.isOperator(p)) return;

        UUID id = p.getUniqueId();

        if (!ADMIN_SHOW_ALL.add(id)) ADMIN_SHOW_ALL.remove(id);
    }

    /**
     * Computes the region list to show for the provided player.
     * If the operator toggle is enabled, returns all regions; otherwise only
     * owned regions and regions where the player is a member.
     *
     * @param player viewer
     * @return list of regions to display
     */
    private List<Region> computeRegionList(Player player) {
        if (isShowAllEnabled(player)) {
            return new ArrayList<>(RegionsManager.getAll());
        }
        List<Region> list = new ArrayList<>();
        list.addAll(RegionsManager.getRegionsOwnedByPlayer(player));
        list.addAll(RegionsManager.getRegionsHasPlayerAsMember(player));
        return ListUtils.removeDuplications(list);
    }

    /**
     * Creates the operator-only toggle item that switches between showing all
     * regions and showing only personal regions. The item's material indicates
     * the current state (emerald when ON, redstone when OFF).
     *
     * @param player viewer
     * @return toggle item stack
     */
    private static ItemStack createAdminToggleItem(Player player) {
        if (isShowAllEnabled(player)) {
            return MenuUtils.getButton(62);
        } else {
            return MenuUtils.getButton(63);
        }
    }

    /**
     * Builds the list of item stacks for the current page.
     * If the player is an operator, the first item is the toggle item.
     *
     * @param player viewer
     * @return item list for the menu
     */
    private List<ItemStack> getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();

        if (PlayerUtils.isOperator(player)) {
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

    /**
     * Constructs and opens the regions pagination menu for the given player.
     * <ul>
     *     <li>Index 0 (operators only): toggle item to show all regions.</li>
     *     <li>Left-click on region: open {@link RegionMenu}.</li>
     *     <li>Right-click on region: teleport to region spawn if permitted
     *         (OP or owner or both {@link PlayerFlags#TELEPORT_SPAWN} and
     *         {@link PlayerFlags#PASSTHROUGH} are set for the player).</li>
     *     <li>Shift+Left on region: set as targeted region.</li>
     *     <li>Shift+Right on region: open {@link RegionInfoMenu}.</li>
     * </ul>
     *
     * @param player viewer
     */
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
                    boolean hasToggle = PlayerUtils.isOperator(_player);
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

                        boolean allowed = PlayerUtils.isOperator(_player)
                                || _player.getUniqueId().equals(region.getOwnerId())
                                || (PlayerUtils.hasPermissionFlag(region.getUniqueId(), _player, PlayerFlags.TELEPORT_SPAWN)
                                && PlayerUtils.hasPermissionFlag(region.getUniqueId(), _player, PlayerFlags.PASSTHROUGH));

                        if (!allowed) {
                            Map<String, String> replacements = new HashMap<>();
                            replacements.put("{region}", region.getName());
                            PlayerUtils.sendMessage(_player, 45, replacements);
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
