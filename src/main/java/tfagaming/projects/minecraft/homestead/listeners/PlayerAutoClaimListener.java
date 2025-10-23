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

/**
 * Listener that manages automatic chunk claiming when a player moves between chunks.
 * <p>
 * This system automatically claims chunks during an active AutoClaim session,
 * ensures safe performance by applying cooldowns, and prevents duplicate particle tasks.
 * </p>
 */
public class PlayerAutoClaimListener implements Listener {

    /** Stores the last chunk location of each player to detect when they enter a new chunk. */
    private final Map<Player, Chunk> lastChunks = new WeakHashMap<>();

    /** Tracks the timestamp of the player's last claim attempt to prevent spam. */
    private final Map<Player, Long> lastClaimAttempt = new WeakHashMap<>();

    /** Minimum delay between automatic claim attempts in milliseconds. */
    private static final long CLAIM_COOLDOWN_MS = 500;

    /**
     * Triggered whenever a player moves.
     * <p>
     * If AutoClaim mode is enabled for the player and they move into a new chunk,
     * the system attempts to claim that chunk for the player's current or active region.
     * </p>
     *
     * @param event The player movement event.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Chunk currentChunk = player.getLocation().getChunk();

        if (!AutoClaimSession.hasSession(player)) {
            return;
        }

        Chunk lastChunk = lastChunks.get(player);
        if (lastChunk == null || !currentChunk.equals(lastChunk)) {
            tryToClaim(player, currentChunk);
            lastChunks.put(player, currentChunk);
        }
    }

    /**
     * Attempts to claim a chunk for the player's region.
     * <p>
     * The method enforces cooldowns, permission checks, and chunk adjacency rules.
     * It also ensures the player owns or has rights to modify the region.
     * If successful, a claim success message is sent and border particles are displayed.
     * </p>
     *
     * @param player The player attempting to claim.
     * @param chunk  The chunk being claimed.
     */
    private void tryToClaim(Player player, Chunk chunk) {
        long now = System.currentTimeMillis();

        /** Prevents claim spam by applying a short cooldown. */
        if (lastClaimAttempt.containsKey(player)
                && (now - lastClaimAttempt.get(player)) < CLAIM_COOLDOWN_MS) {
            return;
        }
        lastClaimAttempt.put(player, now);

        /** Prevents claiming in disabled worlds. */
        if (ChunksManager.isChunkInDisabledWorld(chunk)) {
            PlayerUtils.sendMessage(player, 20);
            return;
        }

        /** Prevents claiming inside WorldGuard protected areas if configured. */
        boolean wgEnabled = Homestead.config.get("worldguard.protect-existing-regions");
        if (wgEnabled && WorldGuardAPI.isChunkInWorldGuardRegion(chunk)) {
            PlayerUtils.sendMessage(player, 133);
            return;
        }

        /** Retrieves or creates a region for the player if none exists. */
        Region region = TargetRegionSession.getRegion(player);
        if (region == null) {
            if (!RegionsManager.getRegionsOwnedByPlayer(player).isEmpty()) {
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

                region = RegionsManager.createRegion(player.getName(), player, true);
                new TargetRegionSession(player, region);
            }
        }

        /** Verifies the player's permission to claim chunks for the region. */
        if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                RegionControlFlags.CLAIM_CHUNKS)) {
            return;
        }

        /** Prevents claiming chunks already owned by other regions. */
        Region owner = ChunksManager.getRegionOwnsTheChunk(chunk);
        if (owner != null) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("{region}", owner.getName());
            PlayerUtils.sendMessage(player, 21, replacements);
            return;
        }

        /** Prevents exceeding the maximum chunks-per-region limit. */
        if (PlayerLimits.hasReachedLimit(region.getOwner(), PlayerLimits.LimitType.CHUNKS_PER_REGION)) {
            PlayerUtils.sendMessage(player, 116);
            return;
        }

        /** Attempts to claim the chunk and confirms success by checking size difference. */
        int before = region.getChunks().size();

        boolean isClaimedSuccessfully = ChunksManager.claimChunk(region.getUniqueId(), chunk, player);

        int after = region.getChunks().size();

        if (isClaimedSuccessfully) {
            /** Sends a success message only if the claim was actually added. */
            if (after > before) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{region}", region.getName());
                PlayerUtils.sendMessage(player, 22, replacements);
            }

            /** Sets a default location for the region if not yet defined. */
            if (region.getLocation() == null) {
                region.setLocation(new SerializableLocation(player.getLocation()));
            }

            /** Starts the visual border particle display if not already active. */
            if (!ChunkParticlesSpawner.isTaskRunning(player)) {
                new ChunkParticlesSpawner(player);
            }
        }
    }
}
