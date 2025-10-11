package tfagaming.projects.minecraft.homestead.particles;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles particle spawning around claimed region chunks for a specific player.
 * <p>
 * This class ensures that no server lag occurs by avoiding synchronous chunk loading.
 * Only already loaded chunks are used for particle effects.
 * </p>
 */
public class ChunkParticlesSpawner {

    /** Keeps track of active particle tasks per player. */
    private static final Map<UUID, BukkitTask> tasks = new HashMap<>();

    /** The player who triggered the particle effect. */
    private final Player player;

    /**
     * Creates a new ChunkParticlesSpawner for the given player.
     * Cancels any existing running task for the player and starts a new repeating effect.
     *
     * @param player the player to show region particles for
     */
    public ChunkParticlesSpawner(Player player) {
        this.player = player;

        // Cancel any previously running particle task for this player
        if (tasks.containsKey(player.getUniqueId())) {
            BukkitTask taskFromMap = tasks.get(player.getUniqueId());
            cancelTask(taskFromMap, player);
        }

        // Start repeating particle effect every 15 ticks (0.75s)
        startRepeatingEffect(15L);
    }

    /**
     * Iterates through all regions and spawns border particles visible to this player.
     */
    public void spawnParticles() {
        for (Region region : RegionsManager.getAll()) {
            spawnParticlesForRegion(region);
        }
    }

    /**
     * Spawns particles for a specific region around the chunk borders.
     * <p>
     * Chunks are only processed if they are already loaded in memory,
     * preventing any synchronous chunk loading that can cause server lag.
     * </p>
     *
     * @param region the region for which to display particles
     */
    public void spawnParticlesForRegion(Region region) {
        List<SerializableChunk> chunks = region.getChunks();

        for (SerializableChunk chunk : chunks) {
            World world = player.getWorld();
            double yOffset = player.getLocation().getY() + 1;

            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            String chunkWorldName = chunk.getWorldName();

            int minX = chunkX * 16;
            int minZ = chunkZ * 16;

            // Skip chunks in different worlds
            if (!world.getName().equals(chunkWorldName)) {
                continue;
            }

            // Get updated region reference
            region = RegionsManager.findRegion(region.getUniqueId());

            // Determine particle color based on player relation
            DustOptions dustOptions;
            if (region.getOwnerId().equals(player.getUniqueId())) {
                dustOptions = new DustOptions(Color.fromRGB(0, 255, 0), 2.0F); // green - owner
            } else if (region.isPlayerMember(player)) {
                dustOptions = new DustOptions(Color.fromRGB(255, 255, 0), 2.0F); // yellow - member
            } else {
                dustOptions = new DustOptions(Color.fromRGB(255, 0, 0), 2.0F); // red - others
            }

            // Check and render chunk borders safely (no sync loading)
            checkAndSpawn(world, chunkX, chunkZ - 1, region, minX, minZ, yOffset, dustOptions, Direction.NORTH);
            checkAndSpawn(world, chunkX, chunkZ + 1, region, minX, minZ + 16, yOffset, dustOptions, Direction.SOUTH);
            checkAndSpawn(world, chunkX - 1, chunkZ, region, minX, minZ, yOffset, dustOptions, Direction.WEST);
            checkAndSpawn(world, chunkX + 1, chunkZ, region, minX + 16, minZ, yOffset, dustOptions, Direction.EAST);
        }
    }

    /**
     * Checks if a neighboring chunk is loaded and spawns border particles if needed.
     *
     * @param world       the world instance
     * @param chunkX      X coordinate of the neighbor chunk
     * @param chunkZ      Z coordinate of the neighbor chunk
     * @param region      the current region
     * @param minX        minimum X coordinate in world space
     * @param minZ        minimum Z coordinate in world space
     * @param yOffset     Y level for particle display
     * @param dustOptions color and size of the dust particle
     * @param direction   which border direction to render
     */
    private void checkAndSpawn(World world, int chunkX, int chunkZ, Region region,
                               int minX, int minZ, double yOffset, DustOptions dustOptions, Direction direction) {

        // Skip chunks that are not loaded to avoid blocking the main thread
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            return;
        }

        Chunk neighbor = world.getChunkAt(chunkX, chunkZ);
        Region neighborRegion = ChunksManager.getRegionOwnsTheChunk(neighbor);

        // If there is no neighboring region or it belongs to a different region, draw the border
        if (neighborRegion == null || !neighborRegion.getUniqueId().equals(region.getUniqueId())) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                for (int x = minX; x < minX + 16; x++) {
                    player.spawnParticle(Particle.DUST, x, yOffset, minZ, 5, dustOptions);
                }
            } else {
                for (int z = minZ; z < minZ + 16; z++) {
                    player.spawnParticle(Particle.DUST, minX, yOffset, z, 5, dustOptions);
                }
            }
        }
    }

    /**
     * Starts the repeating task that spawns the particle effects.
     *
     * @param intervalTicks interval between each update in ticks
     */
    public void startRepeatingEffect(long intervalTicks) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                spawnParticles();
            }
        }.runTaskTimer(Homestead.getInstance(), 0L, intervalTicks);

        tasks.put(player.getUniqueId(), task);

        // Automatically cancel task after 60 seconds
        Bukkit.getScheduler().runTaskLater(Homestead.getInstance(),
                () -> cancelTask(task, player), 60 * 20L);
    }

    /**
     * Cancels the particle task for the given player and removes it from the map.
     *
     * @param task   the Bukkit task to cancel
     * @param player the player associated with the task
     */
    public static void cancelTask(BukkitTask task, Player player) {
        if (task != null) {
            tasks.remove(player.getUniqueId());
            task.cancel();
        }
    }

    /**
     * Checks if a particle task is already running for the given player.
     *
     * @param player the player to check
     * @return true if a task is currently running for this player
     */
    public static boolean isTaskRunning(Player player) {
        return tasks.containsKey(player.getUniqueId());
    }

    /**
     * Cancels the active particle task for the given player (if any).
     *
     * @param player the player whose task should be cancelled
     */
    public static void cancelTask(Player player) {
        BukkitTask task = tasks.get(player.getUniqueId());
        if (task != null) {
            tasks.remove(player.getUniqueId());
            task.cancel();
        }
    }

    /**
     * Enum representing the direction of a neighboring chunk border.
     */
    private enum Direction {
        NORTH, SOUTH, EAST, WEST
    }
}
