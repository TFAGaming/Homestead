package tfagaming.projects.minecraft.homestead.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.integrations.WorldGuardAPI;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.particles.ChunkParticlesSpawner;
import tfagaming.projects.minecraft.homestead.sessions.autoclaim.AutoClaimSession;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLocation;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class PlayerAutoClaimListener implements Listener {
    private final Map<Player, Chunk> lastChunks = new WeakHashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Chunk currentChunk = player.getLocation().getChunk();

        if (!AutoClaimSession.hasSession(player)) {
            return;
        }

        if (lastChunks.containsKey(player)) {
            Chunk lastChunk = lastChunks.get(player);

            if (!currentChunk.equals(lastChunk)) {
                tryToClaim(player, currentChunk);
            }
        } else {
            tryToClaim(player, currentChunk);
        }

        lastChunks.put(player, currentChunk);
    }

    private void tryToClaim(Player player, Chunk chunk) {
        if (ChunksManager.isChunkInDisabledWorld(chunk)) {
            PlayerUtils.sendMessage(player, 20);
            return;
        }

        boolean isWorldGuardProtectingRegionsEnabled = Homestead.config.get("worldguard.protect-existing-regions");

        if (isWorldGuardProtectingRegionsEnabled) {
            if (WorldGuardAPI.isChunkInWorldGuardRegion(chunk)) {
                PlayerUtils.sendMessage(player, 133);
                    return;
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
                    return;
                }

                if (PlayerLimits.hasReachedLimit(player, PlayerLimits.LimitType.REGIONS)) {
                    PlayerUtils.sendMessage(player, 116);
                    return;
                }

                region = RegionsManager.createRegion(player.getName(),
                        player, true);

                new TargetRegionSession(player, region);
            }
        }

        if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                RegionControlFlags.CLAIM_CHUNKS)) {
            return;
        }

        Region regionOwnsThisChunk = ChunksManager.getRegionOwnsTheChunk(chunk);

        if (regionOwnsThisChunk != null) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{region}", regionOwnsThisChunk.getName());

            PlayerUtils.sendMessage(player, 21, replacements);
            return;
        }

        if (PlayerLimits.hasReachedLimit(region.getOwner(), PlayerLimits.LimitType.CHUNKS_PER_REGION)) {
            PlayerUtils.sendMessage(player, 116);
            return;
        }

        ChunksManager.claimChunk(region.getUniqueId(), chunk, player);

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("{region}", region.getName());

        PlayerUtils.sendMessage(player, 22, replacements);

        if (region.getLocation() == null) {
            region.setLocation(new SerializableLocation(player.getLocation()));
        }

        new ChunkParticlesSpawner(player);
    }
}