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
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

public class RegionsWithWelcomeSignsMenu {
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

            items.add(MenuUtils.getButton(47, replacements));

        }

        return items;
    }

    public RegionsWithWelcomeSignsMenu(Player player) {
        regions = new ArrayList<>();
        regions.addAll(RegionsManager.getRegionsWithWelcomeSigns());

        regions = ListUtils.removeDuplications(regions);

        PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(0), 9 * 5,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(), getItems(player), (_player, event) -> {
                    _player.closeInventory();
                }, (_player, context) -> {
                    if (context.getIndex() >= regions.size()) {
                        return;
                    }

                    Region region = regions.get(context.getIndex());

                    if (context.getEvent().isLeftClick()) {
                        player.closeInventory();

                        new DelayedTeleport(player, region.getWelcomeSign().getBukkitLocation());
                    }
                });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
