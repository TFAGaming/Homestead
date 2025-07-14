package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.GlobalPlayerFlagsMenu;
import tfagaming.projects.minecraft.homestead.gui.menus.WorldFlagsMenu;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class FlagsSubCmd extends SubCommandBuilder {
    public FlagsSubCmd() {
        super("flags");
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

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            PlayerUtils.sendMessage(player, 4);
            return true;
        }

        String setType = args[1].toLowerCase();

        switch (setType) {
            case "member": {
                if (!player.hasPermission("homestead.region.flags.members")) {
                    PlayerUtils.sendMessage(player, 8);
                    return true;
                }

                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.SET_MEMBER_FLAGS)) {
                    return true;
                }

                if (args.length < 4) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                String targetName = args[2];

                OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

                if (target == null) {
                    Map<String, String> replacements = new HashMap<String, String>();
                    replacements.put("{playername}", targetName);

                    PlayerUtils.sendMessage(player, 29, replacements);
                    return true;
                }

                if (!region.isPlayerMember(target)) {
                    Map<String, String> replacements = new HashMap<String, String>();
                    replacements.put("{playername}", target.getName());
                    replacements.put("{region}", region.getName());

                    PlayerUtils.sendMessage(player, 40, replacements);
                    return true;
                }

                String flagInput = args[3];

                if (!PlayerFlags.getFlags().contains(flagInput)) {
                    PlayerUtils.sendMessage(player, 41);
                    return true;
                }

                long flags = region.getMember(target).getFlags();
                long flag = PlayerFlags.valueOf(flagInput);

                boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

                if (args.length > 4) {
                    String flagStateInput = args[4];

                    switch (flagStateInput.toLowerCase()) {
                        case "1":
                        case "t":
                        case "true":
                        case "allow":
                            currentState = false;
                            break;
                        case "0":
                        case "f":
                        case "false":
                        case "deny":
                            currentState = true;
                            break;
                        default:
                            break;
                    }
                }

                long newFlags;

                if (currentState) {
                    newFlags = FlagsCalculator.removeFlag(flags, flag);
                } else {
                    newFlags = FlagsCalculator.addFlag(flags, flag);
                }

                region.setMemberFlags(region.getMember(target), newFlags);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{flag}", flagInput);
                replacements.put("{state}", currentState ? "Deny" : "Allow");
                replacements.put("{player}", target.getName());
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 43, replacements);

                break;
            }
            case "global": {
                if (!player.hasPermission("homestead.region.flags.global")) {
                    PlayerUtils.sendMessage(player, 8);
                    return true;
                }

                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.SET_GLOBAL_FLAGS)) {
                    return true;
                }

                if (args.length == 2) {
                    new GlobalPlayerFlagsMenu(player, region);
                    return true;
                }

                if (args.length < 3) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                String flagInput = args[2];

                if (!PlayerFlags.getFlags().contains(flagInput)) {
                    PlayerUtils.sendMessage(player, 41);
                    return true;
                }

                long flags = region.getPlayerFlags();
                long flag = PlayerFlags.valueOf(flagInput);

                boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

                if (args.length > 3) {
                    String flagStateInput = args[3];

                    switch (flagStateInput.toLowerCase()) {
                        case "1":
                        case "t":
                        case "true":
                        case "allow":
                            currentState = false;
                            break;
                        case "0":
                        case "f":
                        case "false":
                        case "deny":
                            currentState = true;
                            break;
                        default:
                            break;
                    }
                }

                long newFlags;

                if (currentState) {
                    newFlags = FlagsCalculator.removeFlag(flags, flag);
                } else {
                    newFlags = FlagsCalculator.addFlag(flags, flag);
                }

                region.setPlayerFlags(newFlags);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{flag}", flagInput);
                replacements.put("{state}", currentState ? "Deny" : "Allow");
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 44, replacements);

                break;
            }
            case "world": {
                if (!player.hasPermission("homestead.region.flags.world")) {
                    PlayerUtils.sendMessage(player, 8);
                    return true;
                }

                if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                        RegionControlFlags.SET_WORLD_FLAGS)) {
                    return true;
                }

                if (args.length == 2) {
                    new WorldFlagsMenu(player, region);
                    return true;
                }

                if (args.length < 3) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                String flagInput = args[2];

                if (!WorldFlags.getFlags().contains(flagInput)) {
                    PlayerUtils.sendMessage(player, 41);
                    return true;
                }

                long flags = region.getWorldFlags();
                long flag = WorldFlags.valueOf(flagInput);

                boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

                if (args.length > 3) {
                    String flagStateInput = args[3];

                    switch (flagStateInput.toLowerCase()) {
                        case "1":
                        case "t":
                        case "true":
                        case "allow":
                            currentState = false;
                            break;
                        case "0":
                        case "f":
                        case "false":
                        case "deny":
                            currentState = true;
                            break;
                        default:
                            break;
                    }
                }

                long newFlags;

                if (currentState) {
                    newFlags = FlagsCalculator.removeFlag(flags, flag);
                } else {
                    newFlags = FlagsCalculator.addFlag(flags, flag);
                }

                region.setWorldFlags(newFlags);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{flag}", flagInput);
                replacements.put("{state}", currentState ? "Deny" : "Allow");
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 49, replacements);

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
