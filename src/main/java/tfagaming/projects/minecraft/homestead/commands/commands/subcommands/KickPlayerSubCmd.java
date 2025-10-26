package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KickPlayerSubCmd extends SubCommandBuilder {
    public KickPlayerSubCmd() {
        super("kick");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("homestead.region.players.kick")) {
            PlayerUtils.sendMessage(player, 8);
            return true;
        }

        if (args.length < 2) {
            PlayerUtils.sendMessage(player, 0);
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            PlayerUtils.sendMessage(player, 4);
            return true;
        }

        if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                RegionControlFlags.KICK_PLAYERS)) {
            return true;
        }

        String targetName = args[1];

        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{playername}", targetName);

            PlayerUtils.sendMessage(player, 29, replacements);
            return true;
        }

        if (RegionsManager.isPlayerInsideRegion(target, region)) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{playername}", target.getName());

            PlayerUtils.sendMessage(player, 32, replacements);
            return true;
        }

        if (target.getUniqueId().equals(region.getOwnerId())) {
            PlayerUtils.sendMessage(player, 30);
            return true;
        }

        if (!RegionsManager.isPlayerInsideRegion(target, region)) {
            PlayerUtils.sendMessage(player, 143);
            return true;
        }

        Chunk chunk = ChunkUtils.findNearbyUnclaimedChunk(target);

        if (chunk != null) {
            PlayerUtils.teleportPlayerToChunk(target, chunk);
        }

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("{playername}", target.getName());
        replacements.put("{region}", region.getName());

        PlayerUtils.sendMessage(player, 144, replacements);

        return true;
    }
}
