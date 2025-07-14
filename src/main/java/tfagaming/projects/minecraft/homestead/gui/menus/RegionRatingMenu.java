package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RegionRatingMenu {
    public RegionRatingMenu(Player player, Region region, Runnable backButton) {
        Menu gui = new Menu(MenuUtils.getTitle(17).replace("{region}", region.getName()), 9 * 3);

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("{region}", region.getName());
        replacements.put("{player-rate}",
                region.isPlayerRated(player) ? Formatters.formatRating(region.getPlayerRate(player).getRate())
                        : Formatters.getNone());

        ItemStack rateNumber1 = MenuUtils.getButton(48, replacements);

        gui.addItem(11, rateNumber1, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            region.addPlayerRate(player, 1);

            PlayerUtils.sendMessage(player, 134);

            new RegionRatingMenu(player, region, backButton);
        });

        ItemStack rateNumber2 = MenuUtils.getButton(49, replacements);

        gui.addItem(12, rateNumber2, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            region.addPlayerRate(player, 2);

            PlayerUtils.sendMessage(player, 134);

            new RegionRatingMenu(player, region, backButton);
        });

        ItemStack rateNumber3 = MenuUtils.getButton(50, replacements);

        gui.addItem(13, rateNumber3, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            region.addPlayerRate(player, 3);

            PlayerUtils.sendMessage(player, 134);

            new RegionRatingMenu(player, region, backButton);
        });

        ItemStack rateNumber4 = MenuUtils.getButton(51, replacements);

        gui.addItem(14, rateNumber4, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            region.addPlayerRate(player, 4);

            PlayerUtils.sendMessage(player, 134);

            new RegionRatingMenu(player, region, backButton);
        });

        ItemStack rateNumber5 = MenuUtils.getButton(52, replacements);

        gui.addItem(15, rateNumber5, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            region.addPlayerRate(player, 5);

            PlayerUtils.sendMessage(player, 134);

            new RegionRatingMenu(player, region, backButton);
        });

        ItemStack ratedBefore = region.isPlayerRated(player)
                ? MenuUtils.getButton(54, replacements)
                : MenuUtils.getButton(53);

        gui.addItem(22, ratedBefore, (_player, event) -> {
            // Do nothing
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
