package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.SubAreasMenu;
import tfagaming.projects.minecraft.homestead.listeners.SelectionToolListener;
import tfagaming.projects.minecraft.homestead.listeners.SelectionToolListener.Selection;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.subareas.SubAreaUtils;

public class SubAreasSubCmd extends SubCommandBuilder {
    public SubAreasSubCmd() {
        super("subareas");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            PlayerUtils.sendMessage(player, 4);
            return true;
        }

        if (args.length == 1) {
            new SubAreasMenu(player, region);
            return true;
        }

        if (args.length < 3) {
            PlayerUtils.sendMessage(player, 0);
            return true;
        }

        if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                RegionControlFlags.MANAGE_SUBAREAS)) {
            return true;
        }

        switch (args[1]) {
            case "create": {
                if (!player.hasPermission("homestead.region.subareas.create")) {
                    PlayerUtils.sendMessage(player, 8);
                    return true;
                }

                Selection session = SelectionToolListener.getPlayerSession(player);

                if (session == null) {
                    PlayerUtils.sendMessage(player, 54);
                    return true;
                }

                Block firstCorner = session.getFirstPosition();
                Block secondCorner = session.getSecondPosition();

                for (Chunk chunk : ChunkUtils.getChunksInArea(firstCorner, secondCorner)) {
                    Region chunkRegion = ChunksManager.getRegionOwnsTheChunk(chunk);

                    if (chunkRegion == null || !chunkRegion.getUniqueId().equals(region.getUniqueId())) {
                        PlayerUtils.sendMessage(player, 55);
                        return true;
                    }
                }

                if (SubAreaUtils.isIntersectingOtherSubArea(region.getUniqueId(), new SerializableBlock(firstCorner),
                        new SerializableBlock(secondCorner))) {
                    PlayerUtils.sendMessage(player, 56);
                    return true;
                }

                String name = args[2];

                if (!StringUtils.isValidSubAreaName(name)) {
                    PlayerUtils.sendMessage(player, 57);
                    return true;
                }

                if (region.isSubAreaNameUsed(name)) {
                    PlayerUtils.sendMessage(player, 58);
                    return true;
                }

                if (PlayerLimits.hasReachedLimit(region.getOwner(), PlayerLimits.LimitType.SUBAREAS_PER_REGION)) {
                    PlayerUtils.sendMessage(player, 116);
                    return true;
                }

                SerializableSubArea subArea = new SerializableSubArea(region.getUniqueId(), name,
                        firstCorner.getWorld(), firstCorner, secondCorner, region.getPlayerFlags());

                int volume = subArea.getVolume();
                int maxVolume = PlayerLimits.getLimitValue(player, PlayerLimits.LimitType.MAX_SUBAREA_VOLUME);

                if (volume >= maxVolume) {
                    Map<String, String> replacements = new HashMap<String, String>();
                    replacements.put("{max}", String.valueOf(maxVolume));
                    replacements.put("{volume}", String.valueOf(volume));

                    PlayerUtils.sendMessage(player, 117);
                    return true;
                }

                region.addSubArea(subArea);

                SelectionToolListener.cancelPlayerSession(player);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{subarea}", name);
                replacements.put("{subarea-volume}", String.valueOf(volume));

                PlayerUtils.sendMessage(player, 59, replacements);

                break;
            }
            case "rename": {
                if (!player.hasPermission("homestead.region.subareas.rename")) {
                    PlayerUtils.sendMessage(player, 8);
                    return true;
                }

                if (args.length < 4) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                String name = args[2];
                String newName = args[3];

                SerializableSubArea subArea = region.getSubArea(name);

                if (subArea == null) {
                    PlayerUtils.sendMessage(player, 60);
                    return true;
                }

                if (!StringUtils.isValidSubAreaName(newName)) {
                    PlayerUtils.sendMessage(player, 57);
                    return true;
                }

                if (subArea.getName().equalsIgnoreCase(newName)) {
                    PlayerUtils.sendMessage(player, 11);
                    return true;
                }

                if (region.isSubAreaNameUsed(newName)) {
                    PlayerUtils.sendMessage(player, 58);
                    return true;
                }

                final String oldName = subArea.getName();

                region.setSubAreaName(subArea.getId(), newName);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{oldname}", oldName);
                replacements.put("{newname}", newName);

                PlayerUtils.sendMessage(player, 61, replacements);

                break;
            }
            case "delete": {
                if (!player.hasPermission("homestead.region.subareas.delete")) {
                    PlayerUtils.sendMessage(player, 8);
                    return true;
                }

                String name = args[2];

                SerializableSubArea subArea = region.getSubArea(name);

                if (subArea == null) {
                    PlayerUtils.sendMessage(player, 60);
                    return true;
                }

                region.removeSubArea(subArea.getId());

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{subarea}", subArea.getName());

                PlayerUtils.sendMessage(player, 62, replacements);

                break;
            }
            case "flags": {
                if (!player.hasPermission("homestead.region.subareas.flags")) {
                    PlayerUtils.sendMessage(player, 8);
                    return true;
                }

                if (args.length < 4) {
                    PlayerUtils.sendMessage(player, 0);
                    return true;
                }

                String name = args[2];

                SerializableSubArea subArea = region.getSubArea(name);

                if (subArea == null) {
                    PlayerUtils.sendMessage(player, 60);
                    return true;
                }

                String flagInput = args[3];

                if (!PlayerFlags.getFlags().contains(flagInput)) {
                    PlayerUtils.sendMessage(player, 41);
                    return true;
                }

                long flags = subArea.getFlags();
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

                region.setSubAreaFlags(subArea.getId(), newFlags);

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{flag}", flagInput);
                replacements.put("{state}", currentState ? "Deny" : "Allow");
                replacements.put("{region}", region.getName());
                replacements.put("{subarea}", subArea.getName());

                PlayerUtils.sendMessage(player, 63, replacements);

                break;
            }
            default:
                PlayerUtils.sendMessage(player, 0);
                break;
        }

        return true;
    }
}
