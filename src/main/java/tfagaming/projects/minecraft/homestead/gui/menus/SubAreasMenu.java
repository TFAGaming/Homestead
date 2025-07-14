package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableSubArea;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

public class SubAreasMenu {
    List<SerializableSubArea> subAreas;

    public List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < subAreas.size(); i++) {
            SerializableSubArea subArea = subAreas.get(i);

            HashMap<String, String> replacements = new HashMap<>();

            replacements.put("{region}", region.getName());
            replacements.put("{subarea}", subArea.getName());
            replacements.put("{subarea-volume}", String.valueOf(subArea.getVolume()));
            replacements.put("{subarea-createdat}", Formatters.formatDate(subArea.getCreatedAt()));

            items.add(MenuUtils.getButton(42, replacements));
        }

        return items;
    }

    public SubAreasMenu(Player player, Region region) {
        subAreas = region.getSubAreas();

        PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(14), 9 * 4,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(), getItems(player, region), (_player, event) -> {
                    new RegionMenu(player, region);
                }, (_player, context) -> {
                    if (context.getIndex() >= subAreas.size()) {
                        return;
                    }

                    SerializableSubArea subArea = subAreas.get(context.getIndex());

                    if (context.getEvent().isLeftClick()) {
                        new SubAreaSettingsMenu(player, region, subArea);
                    }
                });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
