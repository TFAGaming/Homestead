package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class FlagsOverrideSubCmd extends SubCommandBuilder {
    public FlagsOverrideSubCmd() {
        super("flagsoverride");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (args.length < 2) {
            PlayerUtils.sendMessage(sender, 0);
            return true;
        }

        for (Region region : RegionsManager.getAll()) {
            String setType = args[1].toLowerCase();

            switch (setType) {
                case "member": {
                    if (args.length < 4) {
                        PlayerUtils.sendMessage(sender, 0);
                        return true;
                    }

                    String targetName = args[2];

                    OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

                    if (target == null) {
                        Map<String, String> replacements = new HashMap<String, String>();
                        replacements.put("{playername}", targetName);

                        PlayerUtils.sendMessage(sender, 29, replacements);
                        return true;
                    }

                    if (!region.isPlayerMember(target)) {
                        Map<String, String> replacements = new HashMap<String, String>();
                        replacements.put("{playername}", target.getName());
                        replacements.put("{region}", region.getName());

                        PlayerUtils.sendMessage(sender, 40, replacements);
                        return true;
                    }

                    String flagInput = args[3];

                    if (!PlayerFlags.getFlags().contains(flagInput)) {
                        PlayerUtils.sendMessage(sender, 41);
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
                    replacements.put("{sender}", target.getName());
                    replacements.put("{region}", region.getName());

                    PlayerUtils.sendMessage(sender, 43, replacements);

                    break;
                }
                case "global": {
                    if (args.length < 3) {
                        PlayerUtils.sendMessage(sender, 0);
                        return true;
                    }

                    String flagInput = args[2];

                    if (!PlayerFlags.getFlags().contains(flagInput)) {
                        PlayerUtils.sendMessage(sender, 41);
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

                    PlayerUtils.sendMessage(sender, 44, replacements);

                    break;
                }
                case "world": {
                    if (args.length < 3) {
                        PlayerUtils.sendMessage(sender, 0);
                        return true;
                    }

                    String flagInput = args[2];

                    if (!WorldFlags.getFlags().contains(flagInput)) {
                        PlayerUtils.sendMessage(sender, 41);
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

                    PlayerUtils.sendMessage(sender, 49, replacements);

                    break;
                }
                default: {
                    PlayerUtils.sendMessage(sender, 0);
                    break;
                }
            }
        }

        return true;
    }
}
