package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

public class TopRatingRegionsMenu {
    List<Region> regions;

    public List<ItemStack> getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);

            HashMap<String, String> replacements = new HashMap<>();

            replacements.put("{rank}", String.valueOf(i + 1));
            replacements.put("{region}", region.getName());
            replacements.put("{region-displayname}", region.getDisplayName());
            replacements.put("{region-owner}", region.getOwner().getName());
            replacements.put("{region-bank}", Formatters.formatBalance(region.getBank()));
            replacements.put("{region-createdat}", Formatters.formatDate(region.getCreatedAt()));
            replacements.put("{region-rating}", Formatters.formatRating(RegionsManager.getAverageRating(region)));
            replacements.put("{region-members}", String.valueOf(region.getMembers().size()));
            replacements.put("{region-chunks}", String.valueOf(region.getChunks().size()));

            items.add(MenuUtils.getButton(55, replacements));
        }

        return items;
    }

    public TopRatingRegionsMenu(Player player) {
        regions = new ArrayList<>();
        regions.addAll(RegionsManager.sortRegions(RegionsManager.RegionSorting.RATING));

        regions = ListUtils.removeDuplications(regions);

        PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(22), 9 * 5,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(), getItems(player), (_player, event) -> {
                    _player.closeInventory();
                }, (_player, context) -> {
                    if (context.getIndex() >= regions.size()) {
                        return;
                    }

                    Region region = regions.get(context.getIndex());

                    if (context.getEvent().isLeftClick()) {
                        new RegionInfoMenu(player, region, () -> {
                            new TopRatingRegionsMenu(player);
                        });
                    }
                });

        gui.addActionButton(1, MenuUtils.getButton(60), (_player, event) -> {
            if (event.isLeftClick()) {
                new TopBankRegionsMenu(player);
            } else if (event.isRightClick()) {
                new TopOldestRegionMenu(player);
            }
        });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
