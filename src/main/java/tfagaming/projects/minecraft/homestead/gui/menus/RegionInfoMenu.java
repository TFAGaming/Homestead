package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager.RegionSorting;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

public class RegionInfoMenu {
    public RegionInfoMenu(Player player, Region region, Runnable backButton) {
        Menu gui = new Menu(MenuUtils.getTitle(8).replace("{region}", region.getName()), 9 * 3);

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("{region}", region.getName());
        replacements.put("{region-createdat}", Formatters.formatDate(region.getCreatedAt()));
        replacements.put("{region-members}", Formatters.getMembersOfRegion(region));
        replacements.put("{region-bank}", Formatters.formatBalance(region.getBank()));
        replacements.put("{region-rating}", Formatters.formatRating(RegionsManager.getAverageRating(region)));
        replacements.put("{region-owner}", region.getOwner().getName());
        replacements.put("{region-global-rank}", String.valueOf(RegionsManager.getGlobalRank(region.getUniqueId())));
        replacements.put("{region-rank-bank}",
                String.valueOf(RegionsManager.getRank(RegionSorting.BANK, region.getUniqueId())));
        replacements.put("{region-rank-chunks}",
                String.valueOf(RegionsManager.getRank(RegionSorting.CHUNKS_COUNT, region.getUniqueId())));
        replacements.put("{region-rank-members}",
                String.valueOf(RegionsManager.getRank(RegionSorting.MEMBERS_COUNT, region.getUniqueId())));
        replacements.put("{region-rank-rating}",
                String.valueOf(RegionsManager.getRank(RegionSorting.RATING, region.getUniqueId())));

        ItemStack infoButton = MenuUtils.getButton(25, replacements);

        gui.addItem(11, infoButton, (_player, event) -> {
            // Do nothing
        });

        ItemStack membersButton = MenuUtils.getButton(26, replacements);

        gui.addItem(13, membersButton, (_player, event) -> {
            // Do nothing
        });

        ItemStack rateButton = MenuUtils.getButton(61, replacements);

        gui.addItem(15, rateButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new RegionRatingMenu(player, region, () -> {
                new RegionInfoMenu(player, region, backButton);
            });
        });

        gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            backButton.run();
        });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
