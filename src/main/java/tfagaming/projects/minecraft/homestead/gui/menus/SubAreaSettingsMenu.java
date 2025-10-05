package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.sessions.playerinput.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableSubArea;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class SubAreaSettingsMenu {
    public SubAreaSettingsMenu(Player player, Region region, SerializableSubArea subArea) {
        Menu gui = new Menu(MenuUtils.getTitle(15).replace("{subarea}", subArea.getName()), 9 * 3);

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("{subarea}", subArea.getName());

        ItemStack renameSubAreaButton = MenuUtils.getButton(43, replacements);

        gui.addItem(11, renameSubAreaButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            if (!player.hasPermission("homestead.region.subareas.rename")) {
                PlayerUtils.sendMessage(player, 8);
                return;
            }

            player.closeInventory();

            new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
                final String oldName = subArea.getName();

                region.setSubAreaName(subArea.getId(), input);

                replacements.put("{oldname}", oldName);
                replacements.put("{newname}", input);

                PlayerUtils.sendMessage(player, 61, replacements);

                Homestead.getInstance().runSyncTask(() -> {
                    new SubAreaSettingsMenu(player, region, subArea);
                });
            }, (message) -> {
                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.MANAGE_SUBAREAS)) {
                    return false;
                }

                if (!StringUtils.isValidSubAreaName(message)) {
                    PlayerUtils.sendMessage(player, 57);
                    return false;
                }

                if (subArea.getName().equalsIgnoreCase(message)) {
                    PlayerUtils.sendMessage(player, 11);
                    return false;
                }

                if (region.isSubAreaNameUsed(message)) {
                    PlayerUtils.sendMessage(player, 58);
                    return false;
                }

                return true;
            }, (__player) -> {
                Homestead.getInstance().runSyncTask(() -> {
                    new SubAreaSettingsMenu(player, region, subArea);
                });
            }, 88);
        });

        ItemStack flagsSubAreabutton = MenuUtils.getButton(44, replacements);

        gui.addItem(13, flagsSubAreabutton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            if (!player.hasPermission("homestead.region.subareas.flags")) {
                PlayerUtils.sendMessage(player, 8);
                return;
            }

            new SubAreaFlagsMenu(player, region, subArea);
        });

        ItemStack deleteSubAreaButton = MenuUtils.getButton(45, replacements);

        gui.addItem(15, deleteSubAreaButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            if (!player.hasPermission("homestead.region.subareas.delete")) {
                PlayerUtils.sendMessage(player, 8);
                return;
            }

            if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                    RegionControlFlags.MANAGE_SUBAREAS)) {
                return;
            }

            region.removeSubArea(subArea.getId());

            PlayerUtils.sendMessage(player, 62, replacements);

            new SubAreasMenu(player, region);
        });

        gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new SubAreasMenu(player, region);
        });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
