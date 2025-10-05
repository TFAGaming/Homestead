package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLog;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RegionLogsMenu {
    List<SerializableLog> logs;

    public List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < logs.size(); i++) {
            SerializableLog log = logs.get(i);

            HashMap<String, String> replacements = new HashMap<>();

            replacements.put("{region}", region.getName());
            replacements.put("{index}", String.valueOf(i + 1));
            replacements.put("{log-sentat}", Formatters.formatDate(log.getSentAt()));
            replacements.put("{log-author}", log.getAuthor());
            replacements.put("{log-message}", log.getMessage());

            items.add(MenuUtils.getButton(log.isRead() ? 40 : 39, replacements));
        }

        return items;
    }

    public RegionLogsMenu(Player player, Region region) {
        logs = region.getLogs();

        PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(13), 9 * 5,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(), getItems(player, region), (_player, event) -> {
                    new RegionMenu(player, region);
                }, (_player, context) -> {
                    if (context.getIndex() >= logs.size()) {
                        return;
                    }

                    if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                            RegionControlFlags.MANAGE_LOGS)) {
                        return;
                    }

                    SerializableLog log = logs.get(context.getIndex());

                    if (context.getEvent().isLeftClick()) {
                        region.setLogAsRead(log.getId());

                        PaginationMenu instance = context.getInstance();

                        logs = region.getLogs();

                        instance.setItems(getItems(player, region));
                    } else if (context.getEvent().isRightClick()) {
                        region.removeLog(log.getId());

                        PaginationMenu instance = context.getInstance();

                        logs = region.getLogs();

                        instance.setItems(getItems(player, region));
                    }
                });

        gui.addActionButton(0, MenuUtils.getButton(46), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            if (region.getLogs().size() == 0) {
                PlayerUtils.sendMessage(player, 91);
                return;
            }

            for (SerializableLog log : region.getLogs()) {
                region.setLogAsRead(log.getId());
            }

            PlayerUtils.sendMessage(player, 92);

            Homestead.getInstance().runSyncTask(() -> {
                new RegionLogsMenu(player, region);
            });
        });

        gui.addActionButton(2, MenuUtils.getButton(41), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            if (region.getLogs().size() == 0) {
                PlayerUtils.sendMessage(player, 83);
                return;
            }

            region.setLogs(new ArrayList<>());

            PlayerUtils.sendMessage(player, 93);

            Homestead.getInstance().runSyncTask(() -> {
                new RegionLogsMenu(player, region);
            });
        });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
