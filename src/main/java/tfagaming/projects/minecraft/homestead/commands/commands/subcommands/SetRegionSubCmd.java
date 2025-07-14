package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.integrations.maps.RegionIconTools;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLocation;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.MapColor;

public class SetRegionSubCmd extends SubCommandBuilder {
    public SetRegionSubCmd() {
        super("set");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            PlayerUtils.sendMessage(player, 0);
            return true;
        }

        String setType = args[1].toLowerCase();

        switch (setType) {
            case "displayname": {
                if (args.length < 3) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                List<String> regionDisplayNameList = Arrays.asList(args).subList(2, args.length);
                String regionDisplayName = String.join(" ", regionDisplayNameList);

                Region region = TargetRegionSession.getRegion(player);

                if (region == null) {
                    PlayerUtils.sendMessage(player, 4);
                    return false;
                }

                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.RENAME_REGION)) {
                    return true;
                }

                if (!StringUtils.isValidRegionDisplayName(regionDisplayName)) {
                    PlayerUtils.sendMessage(player, 14);
                    return true;
                }

                if (region.getDisplayName().equals(regionDisplayName)) {
                    PlayerUtils.sendMessage(player, 11);
                    return true;
                }

                final String oldDisplayName = region.getDisplayName();

                region.setDisplayName(regionDisplayName);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{olddisplayname}", oldDisplayName);
                replacements.put("{newdisplayname}", region.getDisplayName());

                PlayerUtils.sendMessage(player, 15, replacements);

                break;
            }
            case "description": {
                if (args.length < 3) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                List<String> descriptionList = Arrays.asList(args).subList(2, args.length);
                String description = String.join(" ", descriptionList);

                Region region = TargetRegionSession.getRegion(player);

                if (region == null) {
                    PlayerUtils.sendMessage(player, 4);
                    return false;
                }

                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.SET_DESCRIPTION)) {
                    return true;
                }

                if (!StringUtils.isValidRegionDescription(description)) {
                    PlayerUtils.sendMessage(player, 16);
                    return true;
                }

                if (region.getDescription().equals(description)) {
                    PlayerUtils.sendMessage(player, 11);
                    return true;
                }

                final String oldDescription = region.getDescription();

                region.setDescription(description);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{olddescription}", oldDescription);
                replacements.put("{newdescription}", region.getDescription());

                PlayerUtils.sendMessage(player, 17, replacements);

                break;
            }
            case "mapcolor": {
                if (!player.hasPermission("homestead.region.dynamicmaps.color")) {
                    PlayerUtils.sendMessage(player, 8);
                    return true;
                }

                if (args.length < 3) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                String colorInput = args[2].toLowerCase();

                Region region = TargetRegionSession.getRegion(player);

                if (region == null) {
                    PlayerUtils.sendMessage(player, 4);
                    return false;
                }

                if (!MapColor.getAll().contains(colorInput)) {
                    PlayerUtils.sendMessage(player, 18);
                    return true;
                }

                int color = MapColor.parseFromString(colorInput);

                if (region.getMapColor() == color) {
                    PlayerUtils.sendMessage(player, 11);
                    return true;
                }

                final int oldColor = region.getMapColor();

                region.setMapColor(color);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{oldcolor}", MapColor.convertToColoredStringWithColorName(oldColor));
                replacements.put("{newcolor}", MapColor.convertToColoredStringWithColorName(region.getMapColor()));

                PlayerUtils.sendMessage(player, 19, replacements);

                break;
            }
            case "spawn": {
                Region region = TargetRegionSession.getRegion(player);

                if (region == null) {
                    PlayerUtils.sendMessage(player, 4);
                    return false;
                }

                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.SET_SPAWN)) {
                    return true;
                }

                Location location = player.getLocation();

                region.setLocation(new SerializableLocation(location));

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{region}", region.getName());
                replacements.put("{location}", Formatters.formatLocation(location));

                PlayerUtils.sendMessage(player, 72, replacements);

                RegionsManager.addNewLog(region.getUniqueId(), 1, replacements);

                break;
            }
            case "icon": {
                if (!player.hasPermission("homestead.region.dynamicmaps.icon")) {
                    PlayerUtils.sendMessage(player, 8);
                    return true;
                }

                if (args.length < 3) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                boolean isEnabled = Homestead.config.get("dynamic-maps.icons.enabled");

                if (!isEnabled) {
                    PlayerUtils.sendMessage(player, 105);

                    return true;
                }

                String iconInput = args[2];

                Region region = TargetRegionSession.getRegion(player);

                if (region == null) {
                    PlayerUtils.sendMessage(player, 4);
                    return false;
                }

                if (iconInput.equalsIgnoreCase("none") || iconInput.equalsIgnoreCase("default")) {
                    region.setIcon(iconInput);

                    Map<String, String> replacements = new HashMap<String, String>();
                    replacements.put("{region}", region.getName());

                    PlayerUtils.sendMessage(player, 100, replacements);

                    return true;
                }

                if (!RegionIconTools.isValidIcon(iconInput)) {
                    PlayerUtils.sendMessage(player, 99);

                    return true;
                }

                region.setIcon(iconInput);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 100, replacements);

                break;
            }
            case "tax": {
                if (args.length < 3) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                if (!Homestead.vault.isEconomyReady()) {
                    PlayerUtils.sendMessage(player, 69);
                    Logger.warning("The player \"" + player.getName() + "\" (UUID: " + player.getUniqueId()
                            + ") executed a command that requires economy implementation, but it's disabled.");
                    Logger.warning(
                            "The execution has been ignored, you may resolve this issue by installing a plugin that implements economy on the server.");

                    return true;
                }

                boolean isEnabled = Homestead.config.get("taxes.enabled");

                if (!isEnabled) {
                    PlayerUtils.sendMessage(player, 105);

                    return true;
                }

                String taxInput = args[2];

                Region region = TargetRegionSession.getRegion(player);

                if (region == null) {
                    PlayerUtils.sendMessage(player, 4);
                    return false;
                }

                if (!NumberUtils.isValidDouble(taxInput)) {
                    PlayerUtils.sendMessage(player, 102);

                    return true;
                }

                double taxAmount = Double.parseDouble(taxInput);

                double minTax = Homestead.config.get("taxes.min-tax");
                double maxTax = Homestead.config.get("taxes.max-tax");

                if (taxAmount <= minTax || taxAmount > maxTax) {
                    Map<String, String> replacements = new HashMap<String, String>();
                    replacements.put("{min}", Formatters.formatBalance(minTax));
                    replacements.put("{max}", Formatters.formatBalance(maxTax));

                    PlayerUtils.sendMessage(player, 104, replacements);

                    return true;
                }

                region.setTaxesAmount(taxAmount);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{region}", region.getName());
                replacements.put("{tax-amount}", Formatters.formatBalance(taxAmount));

                PlayerUtils.sendMessage(player, 103, replacements);

                break;
            }
            case "target": {
                if (args.length < 3) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                String regionName = args[2];

                Region region = RegionsManager.findRegion(regionName);

                if (region == null) {
                    PlayerUtils.sendMessage(player, 9);
                    return true;
                }

                if (!PlayerUtils.isOperator(player)
                        && !(region.getOwnerId().equals(player.getUniqueId()) || region.isPlayerMember(player))) {
                    PlayerUtils.sendMessage(player, 10);
                    return true;
                }

                if (TargetRegionSession.hasSession(player)
                        && TargetRegionSession.getRegion(player).getUniqueId().equals(region.getUniqueId())) {
                    PlayerUtils.sendMessage(player, 11);
                    return true;
                }

                new TargetRegionSession(player, region);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 12, replacements);

                break;
            }
            default: {
                PlayerUtils.sendMessage(player, 0);
                break;
            }
        }

        return true;
    }
}
