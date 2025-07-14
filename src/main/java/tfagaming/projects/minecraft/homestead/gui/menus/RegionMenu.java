package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.HashMap;
import java.util.stream.Collectors;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager.RegionSorting;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.other.UpkeepUtils;
import tfagaming.projects.minecraft.homestead.weatherandtime.TimeType;
import tfagaming.projects.minecraft.homestead.weatherandtime.WeatherType;

public class RegionMenu {
    public RegionMenu(Player player, Region region) {
        Menu gui = new Menu(MenuUtils.getTitle(1).replace("{region}", region.getName()), 9 * 4);

        boolean isEconomyEnabled = Homestead.vault.isEconomyReady();
        boolean isUpkeepEnabled = isEconomyEnabled && (boolean) Homestead.config.get("upkeep.enabled");
        boolean isRentEnabled = isEconomyEnabled && (boolean) Homestead.config.get("renting.enabled");

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("{region}", region.getName());
        replacements.put("{region-owner}", region.getOwner().getName());
        replacements.put("{region-bank}", Formatters.formatBalance(region.getBank()));
        replacements.put("{region-createdat}", Formatters.formatDate(region.getCreatedAt()));
        replacements.put("{region-chunks}", String.valueOf(region.getChunks().size()));
        replacements.put("{region-members}", String.valueOf(region.getMembers().size()));
        replacements.put("{upkeep-enabled}", Formatters.getEnabled(isUpkeepEnabled));
        replacements.put("{upkeep-date}", Formatters.formatRemainingTime(region.getUpkeepAt()));
        replacements.put("{upkeep-amount}",
                Formatters.formatBalance(UpkeepUtils.getAmountToPay(region.getChunks().size())));
        replacements.put("{region-global-rank}", String.valueOf(RegionsManager.getGlobalRank(region.getUniqueId())));
        replacements.put("{region-rank-bank}",
                String.valueOf(RegionsManager.getRank(RegionSorting.BANK, region.getUniqueId())));
        replacements.put("{region-rank-chunks}",
                String.valueOf(RegionsManager.getRank(RegionSorting.CHUNKS_COUNT, region.getUniqueId())));
        replacements.put("{region-rank-members}",
                String.valueOf(RegionsManager.getRank(RegionSorting.MEMBERS_COUNT, region.getUniqueId())));
        replacements.put("{region-rank-rating}",
                String.valueOf(RegionsManager.getRank(RegionSorting.RATING, region.getUniqueId())));
        replacements.put("{region-logs}", String.valueOf(region.getLogs().size()));
        replacements.put("{region-logs-unread}", String
                .valueOf(region.getLogs().stream().filter((log) -> !log.isRead()).collect(Collectors.toList()).size()));
        replacements.put("{region-weather}", WeatherType.from(region.getWeather()));
        replacements.put("{region-time}", TimeType.from(region.getTime()));

        ItemStack membersButton = MenuUtils.getButton(6, replacements);

        gui.addItem(10, membersButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new ManagePlayersMenu(player, region);
        });

        ItemStack claimlistButton = MenuUtils.getButton(7, replacements);

        gui.addItem(11, claimlistButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new RegionClaimedChunksMenu(player, region);
        });

        ItemStack flagsButton = MenuUtils.getButton(8, replacements);

        gui.addItem(12, flagsButton, (_player, event) -> {
            if (event.isLeftClick()) {
                if (!player.hasPermission("homestead.region.flags.global")) {
                    PlayerUtils.sendMessage(player, 8);
                    return;
                }

                new GlobalPlayerFlagsMenu(player, region);
            } else if (event.isRightClick()) {
                if (!player.hasPermission("homestead.region.flags.world")) {
                    PlayerUtils.sendMessage(player, 8);
                    return;
                }

                new WorldFlagsMenu(player, region);
            }

            // new RegionFlagsMenu(player, region, isOperator);
        });

        ItemStack miscellaneousButton = MenuUtils.getButton(9, replacements);

        gui.addItem(13, miscellaneousButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new MiscellaneousSettingsMenu(player, region);
        });

        boolean isSubAreasEnabled = Homestead.config.get("sub-areas.enabled");

        replacements.put("{subareas-enabled}", Formatters.getEnabled(isSubAreasEnabled));
        replacements.put("{region-subareas}", String.valueOf(region.getSubAreas().size()));

        ItemStack subareasButton = MenuUtils.getButton(10, replacements);

        gui.addItem(14, subareasButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new SubAreasMenu(player, region);
        });

        ItemStack upkeepButton = MenuUtils.getButton(11, replacements);

        gui.addItem(21, upkeepButton, (_player, event) -> {
            // Do nothing
        });

        SerializableRent rent = region.getRent();

        if (rent != null) {
            replacements.put("{rent-enabled}", Formatters.getEnabled(isRentEnabled));
            replacements.put("{rent-renter}", rent.getPlayer().getName());
            replacements.put("{rent-price}", Formatters.formatBalance(rent.getPrice()));
            replacements.put("{rent-until}", Formatters.formatRemainingTime(rent.getUntilAt()));
        } else {
            replacements.put("{rent-enabled}", Formatters.getEnabled(isRentEnabled));
            replacements.put("{rent-renter}", Formatters.getNone());
            replacements.put("{rent-price}", Formatters.getNone());
            replacements.put("{rent-until}", Formatters.getNone());
        }

        ItemStack rentButton = MenuUtils.getButton(12, replacements);

        gui.addItem(22, rentButton, (_player, event) -> {
            if (event.isLeftClick()) {
                if (region.getRent() == null) {
                    PlayerUtils.sendMessage(player, 128);
                } else {
                    region.setRent(null);

                    PlayerUtils.sendMessage(player, 127);

                    new RegionMenu(player, region);
                }
            }
        });

        ItemStack informationButton = MenuUtils.getButton(15, replacements);

        gui.addItem(23, informationButton, (_player, event) -> {
            // Do nothing
        });

        ItemStack logsButton = MenuUtils.getButton(13, replacements);

        gui.addItem(15, logsButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new RegionLogsMenu(player, region);
        });

        ItemStack weatherAndTimeButton = MenuUtils.getButton(16, replacements);

        gui.addItem(16, weatherAndTimeButton, (_player, event) -> {
            if (event.isLeftClick()) {
                region.setWeather(WeatherType.next(region.getWeather()));
            } else if (event.isRightClick()) {
                region.setTime(TimeType.next(region.getTime()));
            }

            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 500.0f, 1.0f);

            new RegionMenu(player, region);

            return;
        });

        gui.addItem(27, MenuUtils.getBackButton(), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new RegionsMenu(player);
        });

        if (region.isPlayerMember(player)) {
            ItemStack leaveButton = MenuUtils.getButton(14, replacements);

            gui.addItem(35, leaveButton, (_player, event) -> {
                if (!event.isLeftClick()) {
                    return;
                }

                region.removeMember(player);

                replacements.put("{playername}", player.getName());

                RegionsManager.addNewLog(region.getUniqueId(), 4, replacements);

                new RegionsMenu(player);
            });
        }

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
