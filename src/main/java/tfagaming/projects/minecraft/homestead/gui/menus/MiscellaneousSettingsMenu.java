package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.playerinput.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLocation;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class MiscellaneousSettingsMenu {
    public MiscellaneousSettingsMenu(Player player, Region region) {
        Menu gui = new Menu(MenuUtils.getTitle(12), 9 * 3);

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("{region}", region.getName());
        replacements.put("{region-displayname}", region.getDisplayName());
        replacements.put("{region-description}", region.getDescription());

        ItemStack renameRegionButton = MenuUtils.getButton(34, replacements);

        gui.addItem(11, renameRegionButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            player.closeInventory();

            new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
                final String oldName = region.getName();

                region.setName(input);

                replacements.put("{oldname}", oldName);
                replacements.put("{newname}", input);

                PlayerUtils.sendMessage(player, 13, replacements);

                RegionsManager.addNewLog(region.getUniqueId(), 0, replacements);

                Homestead.getInstance().runSyncTask(() -> {
                    new MiscellaneousSettingsMenu(player, region);
                });
            }, (message) -> {
                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.RENAME_REGION)) {
                    return false;
                }

                if (!StringUtils.isValidRegionName(message)) {
                    PlayerUtils.sendMessage(player, 1);
                    return false;
                }

                if (message.equalsIgnoreCase(region.getName())) {
                    PlayerUtils.sendMessage(player, 11);
                    return false;
                }

                if (RegionsManager.isNameUsed(message)) {
                    PlayerUtils.sendMessage(player, 2);
                    return false;
                }

                return true;
            }, (__player) -> {
                Homestead.getInstance().runSyncTask(() -> {
                    new MiscellaneousSettingsMenu(player, region);
                });
            }, 78);
        });

        ItemStack setDisplaynameRegionButton = MenuUtils.getButton(35, replacements);

        gui.addItem(12, setDisplaynameRegionButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            player.closeInventory();

            new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
                final String oldDisplayName = region.getDisplayName();

                region.setDisplayName(input);

                replacements.put("{olddisplayname}", oldDisplayName);
                replacements.put("{newdisplayname}", region.getDisplayName());

                PlayerUtils.sendMessage(player, 15, replacements);

                Homestead.getInstance().runSyncTask(() -> {
                    new MiscellaneousSettingsMenu(player, region);
                });
            }, (message) -> {
                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.RENAME_REGION)) {
                    return false;
                }

                if (!StringUtils.isValidRegionDisplayName(message)) {
                    PlayerUtils.sendMessage(player, 14);
                    return false;
                }

                if (region.getDisplayName().equals(message)) {
                    PlayerUtils.sendMessage(player, 11);
                    return false;
                }

                return true;
            }, (__player) -> {
                Homestead.getInstance().runSyncTask(() -> {
                    new MiscellaneousSettingsMenu(player, region);
                });
            }, 79);
        });

        ItemStack setDescriptionRegionButton = MenuUtils.getButton(36, replacements);

        gui.addItem(13, setDescriptionRegionButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            player.closeInventory();

            new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
                final String oldDescription = region.getDescription();

                region.setDescription(input);

                replacements.put("{olddescription}", oldDescription);
                replacements.put("{newdescription}", region.getDescription());

                PlayerUtils.sendMessage(player, 17, replacements);

                Homestead.getInstance().runSyncTask(() -> {
                    new MiscellaneousSettingsMenu(player, region);
                });
            }, (message) -> {
                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.SET_DESCRIPTION)) {
                    return false;
                }

                if (!StringUtils.isValidRegionDescription(message)) {
                    PlayerUtils.sendMessage(player, 16);
                    return false;
                }

                if (region.getDescription().equals(message)) {
                    PlayerUtils.sendMessage(player, 11);
                    return false;
                }

                return true;
            }, (__player) -> {
                Homestead.getInstance().runSyncTask(() -> {
                    new MiscellaneousSettingsMenu(player, region);
                });
            }, 80);
        });

        ItemStack setLocationRegionButton = MenuUtils.getButton(37, replacements);

        gui.addItem(14, setLocationRegionButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                    RegionControlFlags.SET_SPAWN)) {
                return;
            }

            Location location = player.getLocation();

            region.setLocation(new SerializableLocation(location));

            replacements.put("{region}", region.getName());
            replacements.put("{location}", Formatters.formatLocation(location));

            PlayerUtils.sendMessage(player, 72, replacements);

            RegionsManager.addNewLog(region.getUniqueId(), 1, replacements);
        });

        ItemStack transferOwnershipRegionButton = MenuUtils.getButton(38, replacements);

        gui.addItem(15, transferOwnershipRegionButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            player.closeInventory();

            new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
                OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

                region.setOwner(targetPlayer);

                replacements.put("{playername}", targetPlayer.getName());
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 82, replacements);

                if (region.isPlayerMember(targetPlayer)) {
                    region.removeMember(targetPlayer);
                }

                if (region.isPlayerInvited(targetPlayer)) {
                    region.removePlayerInvite(targetPlayer);
                }

                Homestead.getInstance().runSyncTask(() -> {
                    new RegionsMenu(player);
                });
            }, (message) -> {
                OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

                if (target == null) {
                    replacements.put("{playername}", message);

                    PlayerUtils.sendMessage(player, 29, replacements);
                    return false;
                }

                if (!PlayerUtils.isOperator(player) && !region.getOwnerId().equals(player.getUniqueId())) {
                    PlayerUtils.sendMessage(player, 30);
                    return false;
                }

                if (region.isPlayerBanned(target)) {
                    replacements.put("{playername}", target.getName());

                    PlayerUtils.sendMessage(player, 32, replacements);
                    return false;
                }

                if (target.getUniqueId().equals(region.getOwnerId())) {
                    PlayerUtils.sendMessage(player, 30);
                    return false;
                }

                return true;
            }, (__player) -> {
                Homestead.getInstance().runSyncTask(() -> {
                    new MiscellaneousSettingsMenu(player, region);
                });
            }, 81);
        });

        gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new RegionMenu(player, region);
        });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
