package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionsWithWelcomeSignsMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

public class VisitRegionSubCmd extends SubCommandBuilder {
    public VisitRegionSubCmd() {
        super("visit");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (Homestead.config.isWelcomeSignEnabled()) {
            if (args.length < 2) {
                new RegionsWithWelcomeSignsMenu(player);

                return true;
            }

            String playerName = args[1];

            OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

            if (target == null) {
                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{playername}", playerName);

                PlayerUtils.sendMessage(player, 29, replacements);
                return true;
            }

            String indexInput = args.length >= 3 ? args[2] : "0";

            if (!NumberUtils.isValidInteger(indexInput)) {
                PlayerUtils.sendMessage(player, 137);
                return true;
            }

            int index = Integer.parseInt(indexInput);

            List<Region> regions = RegionsManager.getRegionsOwnedByPlayer(target);
            List<Region> filteredRegions = new ArrayList<>();

            for (Region region : regions) {
                if (region.getWelcomeSign() != null) {
                    filteredRegions.add(region);
                }
            }

            if (filteredRegions.size() == 0) {
                PlayerUtils.sendMessage(player, 137);
                return true;
            }

            if (filteredRegions.size() > 0 && (index < 0 || index > filteredRegions.size() - 1)) {
                PlayerUtils.sendMessage(player, 137);
                return true;
            }

            new DelayedTeleport(player, filteredRegions.get(index).getWelcomeSign().getBukkitLocation());
        } else {
            if (args.length < 2) {
                PlayerUtils.sendMessage(player, 0);
                return true;
            }

            String regionName = args[1];

            Region region = RegionsManager.findRegion(regionName);

            if (region == null) {
                PlayerUtils.sendMessage(player, 9);
                return false;
            }

            if (region.getLocation() == null) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 71, replacements);
                return true;
            }

            if (!PlayerUtils.isOperator(player)
                    && !player.getUniqueId().equals(region.getOwnerId())
                    && !(PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TELEPORT_SPAWN)
                            && PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH))) {
                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 131, replacements);
                return true;
            }

            new DelayedTeleport(player, region.getLocation().getBukkitLocation());
        }

        return true;
    }
}
