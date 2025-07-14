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
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RegionInvitedPlayersMenu {
    List<OfflinePlayer> invitedPlayers;

    public List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < invitedPlayers.size(); i++) {
            OfflinePlayer invitedPlayer = invitedPlayers.get(i);

            HashMap<String, String> replacements = new HashMap<>();

            replacements.put("{playername}", invitedPlayer.getName());

            items.add(MenuUtils.getButton(30, replacements));
        }

        return items;
    }

    public RegionInvitedPlayersMenu(Player player, Region region) {
        invitedPlayers = region.getInvitedPlayers();

        PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(10), 9 * 4,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(), getItems(player, region), (_player, event) -> {
                    new ManagePlayersMenu(player, region);
                }, (_player, context) -> {
                    if (context.getIndex() >= invitedPlayers.size()) {
                        return;
                    }

                    OfflinePlayer invitedPlayer = invitedPlayers.get(context.getIndex());

                    if (context.getEvent().isLeftClick()) {
                        if (region.isPlayerInvited(invitedPlayer)) {
                            region.removePlayerInvite(invitedPlayer);

                            Map<String, String> replacements = new HashMap<String, String>();
                            replacements.put("{playername}", invitedPlayer.getName());

                            PlayerUtils.sendMessage(player, 37, replacements);

                            PaginationMenu instance = context.getInstance();

                            invitedPlayers = region.getInvitedPlayers();

                            instance.setItems(getItems(player, region));
                        }
                    }
                });

        gui.addActionButton(0, MenuUtils.getButton(29), (_player, event) -> {
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

                PlayerUtils.sendMessage(player, 36, replacements);

                RegionsManager.addNewLog(region.getUniqueId(), 2, replacements);

                Homestead.getInstance().runSyncTask(() -> {
                    new RegionInvitedPlayersMenu(player, region);
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
                    new RegionInvitedPlayersMenu(player, region);
                });
            }, 75);
        });

        gui.addActionButton(2, MenuUtils.getButton(31), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            if (region.getInvitedPlayers().size() == 0) {
                PlayerUtils.sendMessage(player, 76);
                return;
            }

            region.setInvitedPlayers(new ArrayList<>());

            PlayerUtils.sendMessage(player, 95);

            Homestead.getInstance().runSyncTask(() -> {
                new RegionInvitedPlayersMenu(player, region);
            });
        });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
