package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.playerinput.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RegionMembersMenu {
    List<SerializableMember> members;

    public List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < members.size(); i++) {
            SerializableMember member = members.get(i);

            HashMap<String, String> replacements = new HashMap<>();

            replacements.put("{region}", region.getName());
            replacements.put("{playername}", member.getBukkitOfflinePlayer().getName());
            replacements.put("{member-joinedat}", Formatters.formatDate(member.getJoinedAt()));
            replacements.put("{taxes-dueon}", Formatters.formatRemainingTime(member.getTaxesAt()));
            replacements.put("{tax-amount}", Formatters.formatBalance(region.getTaxesAmount()));

            items.add(MenuUtils.getButton(24, replacements, member.getBukkitOfflinePlayer()));
        }

        return items;
    }

    public RegionMembersMenu(Player player, Region region) {
        members = region.getMembers();

        PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(5), 9 * 4,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(), getItems(player, region), (_player, event) -> {
                    new ManagePlayersMenu(player, region);
                }, (_player, context) -> {
                    if (context.getIndex() >= members.size()) {
                        return;
                    }

                    SerializableMember member = members.get(context.getIndex());

                    if (context.getEvent().isShiftClick() && context.getEvent().isRightClick()) {
                        new PlayerInfoMenu(player, member.getBukkitOfflinePlayer(), () -> {
                            new RegionMembersMenu(player, region);
                        });
                    } else if (context.getEvent().isRightClick()) {
                        if (!player.hasPermission("homestead.region.flags.members")) {
                            PlayerUtils.sendMessage(player, 8);
                            return;
                        }

                        new MemberRgControlFlagsMenu(player, region, member);
                    } else if (context.getEvent().isShiftClick() && context.getEvent().isLeftClick()) {
                        if (region.isPlayerMember(member.getBukkitOfflinePlayer())) {
                            if (!player.hasPermission("homestead.region.players.untrust")) {
                                PlayerUtils.sendMessage(player, 8);
                                return;
                            }

                            if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                                    RegionControlFlags.UNTRUST_PLAYERS)) {
                                return;
                            }

                            region.removeMember(member.getBukkitOfflinePlayer());

                            Map<String, String> replacements = new HashMap<String, String>();
                            replacements.put("{player}", member.getBukkitOfflinePlayer().getName());
                            replacements.put("{region}", region.getName());

                            PlayerUtils.sendMessage(player, 38, replacements);

                            RegionsManager.addNewLog(region.getUniqueId(), 3, replacements);

                            PaginationMenu instance = context.getInstance();

                            members = region.getMembers();

                            instance.setItems(getItems(player, region));
                        }
                    } else if (context.getEvent().isLeftClick()) {
                        if (!player.hasPermission("homestead.region.flags.members")) {
                            PlayerUtils.sendMessage(player, 8);
                            return;
                        }

                        new MemberPlayerFlagsMenu(player, region, member);
                    }
                });

        gui.addActionButton(1, MenuUtils.getButton(29), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            if (!player.hasPermission("homestead.region.players.trust")) {
                PlayerUtils.sendMessage(player, 8);
                return;
            }

            player.closeInventory();

            new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
                OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

                region.addPlayerInvite(targetPlayer);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{playername}", targetPlayer.getName());
                replacements.put("{region}", region.getName());
                replacements.put("{ownername}", region.getOwner().getName());

                PlayerUtils.sendMessage(player, 36, replacements);
                PlayerUtils.sendMessage(targetPlayer.getPlayer(), 139, replacements);

                RegionsManager.addNewLog(region.getUniqueId(), 2, replacements);

                Homestead.getInstance().runSyncTask(() -> {
                    new RegionMembersMenu(player, region);
                });
            }, (message) -> {
                OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(message);

                if (target == null) {
                    Map<String, String> replacements = new HashMap<String, String>();
                    replacements.put("{playername}", message);

                    PlayerUtils.sendMessage(player, 29, replacements);
                    return false;
                }

                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.TRUST_PLAYERS)) {
                    return false;
                }

                if (region.isPlayerBanned(target)) {
                    PlayerUtils.sendMessage(player, 74);
                    return false;
                }

                if (region.isPlayerMember(target)) {
                    Map<String, String> replacements = new HashMap<String, String>();
                    replacements.put("{playername}", target.getName());

                    PlayerUtils.sendMessage(player, 48, replacements);
                    return false;
                }

                if (region.isPlayerInvited(target)) {
                    Map<String, String> replacements = new HashMap<String, String>();
                    replacements.put("{playername}", target.getName());

                    PlayerUtils.sendMessage(player, 35, replacements);
                    return false;
                }

                if (target.getUniqueId().equals(region.getOwnerId())) {
                    PlayerUtils.sendMessage(player, 30);
                    return false;
                }

                if (PlayerLimits.hasReachedLimit(region.getOwner(), PlayerLimits.LimitType.MEMBERS_PER_REGION)) {
                    PlayerUtils.sendMessage(player, 116);
                    return false;
                }

                return true;
            }, (__player) -> {
                Homestead.getInstance().runSyncTask(() -> {
                    new RegionMembersMenu(player, region);
                });
            }, 75);
        });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
