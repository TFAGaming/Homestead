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
import tfagaming.projects.minecraft.homestead.sessions.playerinput.PlayerInputSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBannedPlayer;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RegionBannedPlayersMenu {
    List<SerializableBannedPlayer> bannedPlayers;

    public List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < bannedPlayers.size(); i++) {
            SerializableBannedPlayer bannedPlayer = bannedPlayers.get(i);

            HashMap<String, String> replacements = new HashMap<>();

            replacements.put("{region}", region.getName());
            replacements.put("{playername}", bannedPlayer.getBukkitOfflinePlayer().getName());
            replacements.put("{player-bannedat}", Formatters.formatDate(bannedPlayer.getBannedAt()));
            replacements.put("{player-banreason}", bannedPlayer.getReason());

            items.add(MenuUtils.getButton(27, replacements, bannedPlayer.getBukkitOfflinePlayer()));
        }

        return items;
    }

    public RegionBannedPlayersMenu(Player player, Region region) {
        bannedPlayers = region.getBannedPlayers();

        PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(9), 9 * 4,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(), getItems(player, region), (_player, event) -> {
                    new ManagePlayersMenu(player, region);
                }, (_player, context) -> {
                    if (context.getIndex() >= bannedPlayers.size()) {
                        return;
                    }

                    SerializableBannedPlayer bannedPlayer = bannedPlayers.get(context.getIndex());

                    if (context.getEvent().isLeftClick()) {
                        if (region.isPlayerBanned(bannedPlayer.getBukkitOfflinePlayer())) {
                            if (!player.hasPermission("homestead.region.players.unban")) {
                                PlayerUtils.sendMessage(player, 8);
                                return;
                            }

                            if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                                    RegionControlFlags.UNBAN_PLAYERS)) {
                                return;
                            }

                            region.unbanPlayer(bannedPlayer.getBukkitOfflinePlayer());

                            Map<String, String> replacements = new HashMap<String, String>();
                            replacements.put("{playername}", bannedPlayer.getBukkitOfflinePlayer().getName());
                            replacements.put("{region}", region.getName());

                            PlayerUtils.sendMessage(player, 34, replacements);

                            PaginationMenu instance = context.getInstance();

                            bannedPlayers = region.getBannedPlayers();

                            instance.setItems(getItems(player, region));
                        }
                    }
                });

        gui.addActionButton(0, MenuUtils.getButton(28), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            if (!player.hasPermission("homestead.region.players.ban")) {
                PlayerUtils.sendMessage(player, 8);
                return;
            }

            player.closeInventory();

            new PlayerInputSession(Homestead.getInstance(), player, (p, input) -> {
                OfflinePlayer targetPlayer = Homestead.getInstance().getOfflinePlayerSync(input);

                region.banPlayer(targetPlayer);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{playername}", targetPlayer.getName());
                replacements.put("{region}", region.getName());
                replacements.put("{reason}", Homestead.language.get("default.reason"));

                PlayerUtils.sendMessage(player, 31, replacements);

                if (region.isPlayerMember(targetPlayer)) {
                    region.removeMember(targetPlayer);
                }

                if (region.isPlayerInvited(targetPlayer)) {
                    region.removePlayerInvite(targetPlayer);
                }

                Homestead.getInstance().runSyncTask(() -> {
                    new RegionBannedPlayersMenu(player, region);
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
                        RegionControlFlags.BAN_PLAYERS)) {
                    return false;
                }

                if (region.isPlayerBanned(target)) {
                    Map<String, String> replacements = new HashMap<String, String>();
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
                    new RegionBannedPlayersMenu(player, region);
                });
            }, 73);
        });

        gui.addActionButton(2, MenuUtils.getButton(32), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            if (region.getBannedPlayers().size() == 0) {
                PlayerUtils.sendMessage(player, 77);
                return;
            }

            region.setBannedPlayers(new ArrayList<>());

            PlayerUtils.sendMessage(player, 94);

            Homestead.getInstance().runSyncTask(() -> {
                new RegionBannedPlayersMenu(player, region);
            });
        });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
