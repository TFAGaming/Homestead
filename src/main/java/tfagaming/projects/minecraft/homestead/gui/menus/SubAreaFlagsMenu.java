package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableSubArea;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class SubAreaFlagsMenu {
    private final HashSet<UUID> cooldowns = new HashSet<>();

    public SubAreaFlagsMenu(Player player, Region region, SerializableSubArea subArea) {
        List<ItemStack> items = new ArrayList<>();

        for (String flagString : PlayerFlags.getFlags()) {
            boolean value = FlagsCalculator.isFlagSet(subArea.getFlags(), PlayerFlags.valueOf(flagString));

            items.add(MenuUtils.getFlagButton(flagString, value));
        }

        PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(16), 9 * 5,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(), items, (_player, event) -> {
                    new SubAreaSettingsMenu(player, region, subArea);
                }, (_player, context) -> {
                    if (cooldowns.contains(player.getUniqueId())) {
                        return;
                    }

                    if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                            RegionControlFlags.MANAGE_SUBAREAS)) {
                        return;
                    }

                    String flagString = PlayerFlags.getFlags().get(context.getIndex());

                    List<String> disabledFlags = Homestead.config.get("disabled-flags");

                    if (disabledFlags.contains(flagString)) {
                        PlayerUtils.sendMessage(player, 42);
                        return;
                    }

                    long flag = PlayerFlags.valueOf(flagString);

                    if (context.getEvent().isLeftClick()) {
                        PaginationMenu instance = context.getInstance();

                        long flags = subArea.getFlags();

                        boolean isSet = FlagsCalculator.isFlagSet(flags, flag);
                        long newFlags;

                        if (isSet) {
                            newFlags = FlagsCalculator.removeFlag(flags, flag);
                        } else {
                            newFlags = FlagsCalculator.addFlag(flags, flag);
                        }

                        region.setSubAreaFlags(subArea.getId(), newFlags);

                        cooldowns.add(player.getUniqueId());

                        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 500.0f, 1.0f);

                        Map<String, String> replacements = new HashMap<String, String>();
                        replacements.put("{flag}", flagString);
                        replacements.put("{state}", Formatters.getFlag(!isSet));
                        replacements.put("{region}", region.getName());
                        replacements.put("{subarea}", subArea.getName());

                        PlayerUtils.sendMessage(player, 63, replacements);

                        instance.replaceSlot(context.getIndex(),
                                MenuUtils.getFlagButton(flagString, !isSet));

                        Homestead.getInstance().runAsyncTaskLater(() -> {
                            cooldowns.remove(player.getUniqueId());
                        }, 1);
                    }
                });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}