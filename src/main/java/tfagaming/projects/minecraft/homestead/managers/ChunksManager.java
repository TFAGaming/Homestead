package tfagaming.projects.minecraft.homestead.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.ChunkClaimEvent;
import tfagaming.projects.minecraft.homestead.api.events.ChunkUnclaimEvent;
import tfagaming.projects.minecraft.homestead.integrations.WorldEditAPI;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableSubArea;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

/**
 * Handles all logic related to claiming, unclaiming, and managing chunks in regions.
 * <p>
 * Includes adjacency enforcement, anti-split protection, and performance optimizations
 * to avoid synchronous chunk loading.
 * </p>
 */
public class ChunksManager {

    /**
     * Claims a chunk for a specific region.
     * <p>
     * A chunk can only be claimed if:
     * <ul>
     *     <li>The region exists.</li>
     *     <li>The chunk is adjacent to at least one already owned chunk.</li>
     *     <li>If it’s the first chunk, adjacency is not required.</li>
     * </ul>
     * </p>
     *
     * @param id     The region UUID.
     * @param chunk  The chunk to claim.
     * @param player The player performing the claim (optional).
     */
    public static void claimChunk(UUID id, Chunk chunk, OfflinePlayer... player) {
        Region region = RegionsManager.findRegion(id);
        if (region == null) return;

        // Prevent claiming isolated chunks (except the first)
        if (!region.getChunks().isEmpty() && !hasAdjacentOwnedChunk(region, chunk)) {
            if (player.length > 0 && player[0] instanceof Player target && target.isOnline()) {
                PlayerUtils.sendMessage(target, 140); // "&cYou can only claim chunks that are next to your existing region!"
            }
            return;
        }

        // Add chunk to region
        region.addChunk(new SerializableChunk(chunk));

        // Fire event only if chunk was added
        ChunkClaimEvent event = new ChunkClaimEvent(chunk, player.length > 0 ? player[0] : null);
        Homestead.getInstance().runSyncTask(() ->
                Bukkit.getPluginManager().callEvent(event)
        );
    }

    /**
     * Unclaims a chunk from a region.
     * <p>
     * The removal is prevented if doing so would cause the region
     * to split into multiple disconnected parts.
     * </p>
     *
     * @param id     The region UUID.
     * @param chunk  The chunk to unclaim.
     * @param player The player performing the unclaim (optional).
     */
    public static void unclaimChunk(UUID id, Chunk chunk, OfflinePlayer... player) {
        Region region = RegionsManager.findRegion(id);
        if (region == null) return;

        SerializableChunk target = new SerializableChunk(chunk);

        // Prevent splitting the region into multiple disconnected areas
        if (wouldSplitRegion(region, target)) {
            if (player.length > 0 && player[0] instanceof Player p && p.isOnline()) {
                PlayerUtils.sendMessage(p, 141); // "&cYou cannot unclaim this chunk because it would split your region!"
            }
            return;
        }

        removeChunk(id, target);

        boolean regenerate = Homestead.config.get("worldedit.regenerate-chunks");
        if (regenerate) {
            Homestead.getInstance().runAsyncTask(() ->
                    WorldEditAPI.regenerateChunk(chunk.getWorld(), chunk.getX(), chunk.getZ())
            );
        }

        ChunkUnclaimEvent event = new ChunkUnclaimEvent(chunk, player.length > 0 ? player[0] : null);
        Homestead.getInstance().runSyncTask(() ->
                Bukkit.getPluginManager().callEvent(event)
        );
    }

    /**
     * Removes a claimed chunk from a region and its sub-areas.
     *
     * @param id    The region UUID.
     * @param chunk The chunk to remove.
     */
    public static void removeChunk(UUID id, SerializableChunk chunk) {
        Region region = RegionsManager.findRegion(id);
        if (region == null) return;

        region.removeChunk(chunk);

        for (SerializableSubArea sub : region.getSubAreas()) {
            for (Chunk subChunk : ChunkUtils.getChunksInArea(sub.getFirstPoint(), sub.getSecondPoint())) {
                if (new SerializableChunk(subChunk).toString(true).equals(chunk.toString(true))) {
                    region.removeSubArea(sub.getId());
                    break;
                }
            }
        }
    }

    /**
     * Determines whether removing the specified chunk would split the region
     * into multiple disconnected areas.
     *
     * @param region        The region to check.
     * @param chunkToRemove The chunk to hypothetically remove.
     * @return True if removal would split the region, false otherwise.
     */
    public static boolean wouldSplitRegion(Region region, SerializableChunk chunkToRemove) {
        List<SerializableChunk> chunks = new ArrayList<>(region.getChunks());
        chunks.removeIf(c -> c.toString(true).equals(chunkToRemove.toString(true)));

        if (chunks.isEmpty()) return false; // Removing the last chunk is always allowed

        // Breadth-First Search to check connectivity
        List<SerializableChunk> visited = new ArrayList<>();
        List<SerializableChunk> queue = new ArrayList<>();
        queue.add(chunks.get(0));

        while (!queue.isEmpty()) {
            SerializableChunk current = queue.remove(0);
            visited.add(current);

            for (SerializableChunk neighbor : chunks) {
                if (!visited.contains(neighbor) && areAdjacent(current, neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        // If not all chunks are reachable, region would split
        return visited.size() != chunks.size();
    }

    /**
     * Checks whether two chunks are directly adjacent.
     *
     * @param a The first chunk.
     * @param b The second chunk.
     * @return True if the chunks are directly adjacent (N, S, E, W).
     */
    private static boolean areAdjacent(SerializableChunk a, SerializableChunk b) {
        if (!a.getWorldName().equals(b.getWorldName())) return false;
        int dx = Math.abs(a.getX() - b.getX());
        int dz = Math.abs(a.getZ() - b.getZ());
        return (dx == 1 && dz == 0) || (dx == 0 && dz == 1);
    }

    /**
     * Checks if a chunk belongs to a disabled world.
     *
     * @param chunk The chunk to check.
     * @return True if the chunk’s world is disabled.
     */
    public static boolean isChunkInDisabledWorld(Chunk chunk) {
        List<String> disabledWorlds = Homestead.config.get("disabled-worlds");
        return disabledWorlds.contains(chunk.getWorld().getName());
    }

    /**
     * Checks if the given chunk is already claimed by any region.
     *
     * @param chunk The chunk to check.
     * @return True if already claimed.
     */
    public static boolean isChunkClaimed(Chunk chunk) {
        for (Region region : RegionsManager.getAll()) {
            for (SerializableChunk serialized : region.getChunks()) {
                String chunkString = SerializableChunk.convertToString(chunk, true);
                if (serialized.toString(true).equals(chunkString)) return true;
            }
        }
        return false;
    }

    /**
     * Returns the region that owns the specified chunk.
     *
     * @param chunk The chunk to check.
     * @return The owning region, or null if unclaimed.
     */
    public static Region getRegionOwnsTheChunk(Chunk chunk) {
        for (Region region : RegionsManager.getAll()) {
            for (SerializableChunk serialized : region.getChunks()) {
                String chunkString = SerializableChunk.convertToString(chunk, true);
                if (serialized.toString(true).equals(chunkString)) return region;
            }
        }
        return null;
    }

    /**
     * Checks if the specified chunk is adjacent to any chunk owned by the same region.
     *
     * @param region The region to check.
     * @param chunk  The chunk being considered.
     * @return True if adjacent, false otherwise.
     */
    public static boolean hasAdjacentOwnedChunk(Region region, Chunk chunk) {
        World world = chunk.getWorld();
        int x = chunk.getX();
        int z = chunk.getZ();

        int[][] directions = {
                {x + 1, z},
                {x - 1, z},
                {x, z + 1},
                {x, z - 1}
        };

        for (int[] dir : directions) {
            int nx = dir[0];
            int nz = dir[1];

            if (!world.isChunkLoaded(nx, nz)) continue;

            Chunk neighbor = world.getChunkAt(nx, nz);
            Region neighborRegion = getRegionOwnsTheChunk(neighbor);

            if (neighborRegion != null && neighborRegion.getUniqueId().equals(region.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds a nearby unclaimed chunk within a 30-chunk radius.
     *
     * @param player The player searching.
     * @return The first unclaimed chunk found, or null.
     */
    public static Chunk findNearbyUnclaimedChunk(Player player) {
        Chunk start = player.getLocation().getChunk();
        World world = player.getWorld();
        int sx = start.getX();
        int sz = start.getZ();

        int radius = 1;
        int maxRadius = 30;

        while (radius <= maxRadius) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) continue;
                    if (!world.isChunkLoaded(sx + x, sz + z)) continue;

                    Chunk current = world.getChunkAt(sx + x, sz + z);
                    if (!isChunkClaimed(current)) return current;
                }
            }
            radius++;
        }

        return null;
    }

    /**
     * Checks if the player has any neighboring chunks that belong to another region.
     *
     * @param player The player to check.
     * @return True if any neighboring chunk belongs to a different owner.
     */
    public static boolean hasNeighbor(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        World world = player.getWorld();
        int x = chunk.getX();
        int z = chunk.getZ();

        Chunk[] neighbors = {
                world.getChunkAt(x, z - 1),
                world.getChunkAt(x, z + 1),
                world.getChunkAt(x - 1, z),
                world.getChunkAt(x + 1, z)
        };

        for (Chunk neighbor : neighbors) {
            if (isChunkClaimed(neighbor)) {
                Region region = getRegionOwnsTheChunk(neighbor);
                if (region != null && !region.getOwnerId().equals(player.getUniqueId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets a chunk based on X/Z coordinates.
     *
     * @param world The world.
     * @param x     The chunk X coordinate.
     * @param z     The chunk Z coordinate.
     * @return The chunk.
     */
    public static Chunk getFromLocation(World world, int x, int z) {
        Location loc = new Location(world, x * 16 + 8, 64, z * 16 + 8);
        return loc.getChunk();
    }

    /**
     * Returns a central location inside the given chunk.
     *
     * @param player The player for pitch/yaw orientation.
     * @param chunk  The chunk to use.
     * @return A location inside the chunk.
     */
    public static Location getLocation(Player player, Chunk chunk) {
        Location loc = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, 64, chunk.getZ() * 16 + 8);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc) + 2);
        loc.setPitch(player.getLocation().getPitch());
        loc.setYaw(player.getLocation().getYaw());
        return loc;
    }

    /**
     * Converts a SerializableChunk to a valid Bukkit Location.
     *
     * @param player The player for orientation.
     * @param chunk  The serializable chunk.
     * @return A valid location.
     */
    public static Location getLocation(Player player, SerializableChunk chunk) {
        World world = chunk.getWorld();
        int x = chunk.getX() * 16 + 8;
        int z = chunk.getZ() * 16 + 8;
        if (world == null) return null;

        Location loc;
        if (world.getEnvironment() == World.Environment.NETHER) {
            loc = findSafeNetherLocation(world, x, z);
        } else {
            int highest = world.getHighestBlockYAt(x, z);
            loc = new Location(world, x, highest + 2, z);
        }

        if (loc != null) {
            loc.setPitch(player.getLocation().getPitch());
            loc.setYaw(player.getLocation().getYaw());
        }
        return loc;
    }

    /**
     * Finds a safe teleportable location in the Nether.
     *
     * @param world The world.
     * @param x     X coordinate.
     * @param z     Z coordinate.
     * @return A safe location or null if none found.
     */
    private static Location findSafeNetherLocation(World world, int x, int z) {
        for (int y = 32; y < 127; y++) {
            Block block = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);
            if (block.getType() == Material.AIR && above.getType() == Material.AIR) {
                return new Location(world, x + 0.5, y, z + 0.5);
            }
        }
        return null;
    }

    /**
     * Removes a random chunk from a region.
     *
     * @param id The region UUID.
     */
    public static void removeRandomChunk(UUID id) {
        Region region = RegionsManager.findRegion(id);
        if (region == null) return;

        List<SerializableChunk> chunks = region.getChunks();
        if (chunks.isEmpty()) return;

        int index = new Random().nextInt(chunks.size());
        region.removeChunk(chunks.get(index));
    }

    /**
     * Removes chunks belonging to deleted worlds.
     *
     * @return Number of removed invalid chunks.
     */
    public static int deleteInvalidChunks() {
        int count = 0;
        List<String> worlds = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }

        for (Region region : RegionsManager.getAll()) {
            if (region == null || region.getChunks().isEmpty()) continue;

            for (SerializableChunk serialized : region.getChunks()) {
                if (!worlds.contains(serialized.getWorldName())) {
                    region.removeChunk(serialized);
                    count++;
                }
            }
        }
        return count;
    }
}
