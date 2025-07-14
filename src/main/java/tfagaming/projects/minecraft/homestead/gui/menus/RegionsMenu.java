package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

public class RegionsMenu {
    List<Region> regions;

    public List<ItemStack> getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);

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
        regions = new ArrayList<>();
        regions.addAll(RegionsManager.getRegionsOwnedByPlayer(player));
        regions.addAll(RegionsManager.getRegionsHasPlayerAsMember(player));

        regions = ListUtils.removeDuplications(regions);

        PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(0), 9 * 4,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(), getItems(player), (_player, event) -> {
                    _player.closeInventory();
                }, (_player, context) -> {
                    if (context.getIndex() >= regions.size()) {
                        return;
                    }

                    Region region = regions.get(context.getIndex());

                    if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
                        new RegionInfoMenu(player, region, () -> {
                            new RegionsMenu(player);
                        });
                    } else if (context.getEvent().isRightClick()) {
                        if (region.getLocation() == null) {
                            Map<String, String> replacements = new HashMap<>();
                            replacements.put("{region}", region.getName());

                            PlayerUtils.sendMessage(player, 71, replacements);
                            return;
                        }

                        new DelayedTeleport(player, region.getLocation().getBukkitLocation());
                    } else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
                        if (TargetRegionSession.getRegion(player) != null && TargetRegionSession.getRegion(player).getUniqueId().equals(region.getUniqueId())) {
                            return;
                        }

                        new TargetRegionSession(player, region);

                        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 500.0f, 1.0f);

                        Map<String, String> replacements = new HashMap<String, String>();
                        replacements.put("{region}", region.getName());

                        PlayerUtils.sendMessage(player, 12, replacements);

                        PaginationMenu instance = context.getInstance();

                        regions = RegionsManager.getRegionsOwnedByPlayer(player);

                        instance.setItems(getItems(player));
                    } else if (context.getEvent().isLeftClick()) {
                        new RegionMenu(player, region);
                    }
                });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
