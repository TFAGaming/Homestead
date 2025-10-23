package tfagaming.projects.minecraft.homestead.commands.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.integrations.WorldGuardAPI;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.particles.ChunkParticlesSpawner;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLocation;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class ClaimCommand extends CommandBuilder {
    public ClaimCommand() {
        super("claim");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        Chunk chunk = player.getLocation().getChunk();

        if (ChunksManager.isChunkInDisabledWorld(chunk)) {
            PlayerUtils.sendMessage(player, 20);
            return true;
        }

        boolean isWorldGuardProtectingRegionsEnabled = Homestead.config.get("worldguard.protect-existing-regions");

        if (isWorldGuardProtectingRegionsEnabled) {
            if (WorldGuardAPI.isChunkInWorldGuardRegion(chunk)) {
                PlayerUtils.sendMessage(player, 133);
                    return true;
            }
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            if (RegionsManager.getRegionsOwnedByPlayer(player).size() > 0) {
                TargetRegionSession.randomizeRegion(player);

                region = TargetRegionSession.getRegion(player);
            } else {
                if (!player.hasPermission("homestead.region.create")) {
                    PlayerUtils.sendMessage(player, 8);
                    return true;
                }

                if (PlayerLimits.hasReachedLimit(player, PlayerLimits.LimitType.REGIONS)) {
                    PlayerUtils.sendMessage(player, 116);
                    return true;
                }

                region = RegionsManager.createRegion(player.getName(),
                        player, true);

                new TargetRegionSession(player, region);
            }
        }

        if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                RegionControlFlags.CLAIM_CHUNKS)) {
            return true;
        }

        Region regionOwnsThisChunk = ChunksManager.getRegionOwnsTheChunk(chunk);

        if (regionOwnsThisChunk != null) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{region}", regionOwnsThisChunk.getName());

            PlayerUtils.sendMessage(player, 21, replacements);
            return true;
        }

        if (PlayerLimits.hasReachedLimit(region.getOwner(), PlayerLimits.LimitType.CHUNKS_PER_REGION)) {
            PlayerUtils.sendMessage(player, 116);
            return true;
        }

        boolean isClaimedSuccessfully = ChunksManager.claimChunk(region.getUniqueId(), chunk, player);

        if (isClaimedSuccessfully) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{region}", region.getName());

            PlayerUtils.sendMessage(player, 22, replacements);

            if (region.getLocation() == null) {
                region.setLocation(new SerializableLocation(player.getLocation()));
            }

            new ChunkParticlesSpawner(player);
        }

        return true;
    }

    @Override
    public List<String> onAutoComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
