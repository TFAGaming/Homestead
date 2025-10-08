package tfagaming.projects.minecraft.homestead.managers;

import java.util.*;
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
 * Includes adjacency enforcement, anti-split protection, and fault-tolerant data handling.
 */
public class ChunksManager {

    /** Claims a chunk for a specific region. */
    public static void claimChunk(UUID id, Chunk chunk, OfflinePlayer... player) {
        Region region = RegionsManager.findRegion(id);
        if (region == null) return;

        // Prevent claiming isolated chunks (except the first)
        if (!region.getChunks().isEmpty() && !hasAdjacentOwnedChunk(region, chunk)) {
            if (player.length > 0 && player[0] instanceof Player target && target.isOnline()) {
                PlayerUtils.sendMessage(target, 140);
            }
            return;
        }

        region.addChunk(new SerializableChunk(chunk));

        ChunkClaimEvent event = new ChunkClaimEvent(chunk, player.length > 0 ? player[0] : null);
        Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));
    }

    /** Unclaims a chunk from a region. Prevents removal if it would split the region. */
    public static void unclaimChunk(UUID id, Chunk chunk, OfflinePlayer... player) {
        Region region = RegionsManager.findRegion(id);
        if (region == null) return;

        SerializableChunk target = new SerializableChunk(chunk);

        if (wouldSplitRegion(region, target)) {
            if (player.length > 0 && player[0] instanceof Player p && p.isOnline()) {
                PlayerUtils.sendMessage(p, 141);
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
        Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));
    }

    /** Removes a claimed chunk from a region and its sub-areas. */
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
     */
    public static boolean wouldSplitRegion(Region region, SerializableChunk chunkToRemove) {
        if (region == null || region.getChunks() == null || region.getChunks().isEmpty()) return false;

        // Normalize and deduplicate chunks
        List<SerializableChunk> normalized = new ArrayList<>();
        for (SerializableChunk c : region.getChunks()) {
            if (c == null || c.getWorldName() == null || c.getWorldName().isBlank()) continue;
            c.setWorldName(c.getWorldName().trim());
            normalized.add(c);
        }

        Map<String, SerializableChunk> byKey = new LinkedHashMap<>();
        for (SerializableChunk c : normalized) {
            byKey.putIfAbsent(c.toString(true), c);
        }

        List<SerializableChunk> chunks = new ArrayList<>(byKey.values());
        chunks.removeIf(c -> c.toString(true).equals(chunkToRemove.toString(true)));

        if (chunks.isEmpty()) return false;

        // BFS traversal using Set for correct connectivity check
        String worldName = chunks.get(0).getWorldName();
        Set<SerializableChunk> visited = new HashSet<>();
        Queue<SerializableChunk> queue = new LinkedList<>();

        queue.add(chunks.get(0));
        visited.add(chunks.get(0));

        while (!queue.isEmpty()) {
            SerializableChunk current = queue.poll();

            for (SerializableChunk neighbor : chunks) {
                if (neighbor == null) continue;
                if (!neighbor.getWorldName().equals(worldName)) continue;
                if (visited.contains(neighbor)) continue;

                if (areAdjacent(current, neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited.size() != chunks.size();
    }

    /** Returns whether two chunks are directly adjacent (N, S, E, W) in the same world. */
    private static boolean areAdjacent(SerializableChunk a, SerializableChunk b) {
        if (!a.getWorldName().equals(b.getWorldName())) return false;
        int dx = Math.abs(a.getX() - b.getX());
        int dz = Math.abs(a.getZ() - b.getZ());
        return (dx == 1 && dz == 0) || (dx == 0 && dz == 1);
    }

    /** Returns whether the chunk's world is disabled. */
    public static boolean isChunkInDisabledWorld(Chunk chunk) {
        List<String> disabledWorlds = Homestead.config.get("disabled-worlds");
        return disabledWorlds.contains(chunk.getWorld().getName());
    }

    /** Returns whether a given chunk is already claimed by any region. */
    public static boolean isChunkClaimed(Chunk chunk) {
        String key = SerializableChunk.convertToString(chunk, true);
        for (Region region : RegionsManager.getAll()) {
            for (SerializableChunk serialized : region.getChunks()) {
                if (serialized == null) continue;
                if (serialized.toString(true).equals(key)) return true;
            }
        }
        return false;
    }

    /** Returns the region that owns a given chunk, or null if it is unclaimed. */
    public static Region getRegionOwnsTheChunk(Chunk chunk) {
        String key = SerializableChunk.convertToString(chunk, true);
        for (Region region : RegionsManager.getAll()) {
            for (SerializableChunk serialized : region.getChunks()) {
                if (serialized == null) continue;
                if (serialized.toString(true).equals(key)) return region;
            }
        }
        return null;
    }

    /** Returns true if the provided chunk is adjacent to any chunk owned by the same region. */
    public static boolean hasAdjacentOwnedChunk(Region region, Chunk chunk) {
        World world = chunk.getWorld();
        int x = chunk.getX();
        int z = chunk.getZ();

        int[][] dirs = {{x + 1, z}, {x - 1, z}, {x, z + 1}, {x, z - 1}};
        for (int[] dir : dirs) {
            int nx = dir[0], nz = dir[1];
            if (!world.isChunkLoaded(nx, nz)) continue;

            Chunk neighbor = world.getChunkAt(nx, nz);
            Region neighborRegion = getRegionOwnsTheChunk(neighbor);
            if (neighborRegion != null && neighborRegion.getUniqueId().equals(region.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    /** Finds a nearby unclaimed chunk around the player's current chunk within a radius up to 30. */
    public static Chunk findNearbyUnclaimedChunk(Player player) {
        Chunk start = player.getLocation().getChunk();
        World world = player.getWorld();
        int sx = start.getX(), sz = start.getZ();

        for (int radius = 1; radius <= 30; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) continue;
                    if (!world.isChunkLoaded(sx + x, sz + z)) continue;

                    Chunk current = world.getChunkAt(sx + x, sz + z);
                    if (!isChunkClaimed(current)) return current;
                }
            }
        }
        return null;
    }

    /** Returns true if the player has any neighboring claimed chunks that belong to another region. */
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
                Region r = getRegionOwnsTheChunk(neighbor);
                if (r != null && !r.getOwnerId().equals(player.getUniqueId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns the chunk object for the given world and chunk coordinates. */
    public static Chunk getFromLocation(World world, int x, int z) {
        Location location = new Location(world, x * 16 + 8, 64, z * 16 + 8);
        return location.getChunk();
    }

    /** Returns a center-ish, safe location inside the given chunk (overworld/end). */
    public static Location getLocation(Player player, Chunk chunk) {
        Location loc = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, 64, chunk.getZ() * 16 + 8);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc) + 2);
        loc.setPitch(player.getLocation().getPitch());
        loc.setYaw(player.getLocation().getYaw());
        return loc;
    }

    /** Returns a safe location for a SerializableChunk. Uses a Nether-safe finder if needed. */
    public static Location getLocation(Player player, SerializableChunk chunk) {
        World world = chunk.getWorld();
        if (world == null) return null;

        int x = chunk.getX() * 16 + 8;
        int z = chunk.getZ() * 16 + 8;

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

    /** Finds a safe standable location in the Nether near (x, z). */
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

    /** Removes a random chunk from the region (used by upkeep logic). */
    public static void removeRandomChunk(UUID id) {
        Region region = RegionsManager.findRegion(id);
        if (region == null) return;

        List<SerializableChunk> chunks = region.getChunks();
        if (chunks == null || chunks.isEmpty()) return;

        int index = new Random().nextInt(chunks.size());
        region.removeChunk(chunks.get(index));
    }

    /** Removes chunks belonging to worlds that no longer exist on the server. */
    public static int deleteInvalidChunks() {
        int count = 0;
        Set<String> worlds = new HashSet<>();
        for (World w : Bukkit.getWorlds()) worlds.add(w.getName());

        for (Region region : RegionsManager.getAll()) {
            if (region == null || region.getChunks() == null || region.getChunks().isEmpty()) continue;

            Iterator<SerializableChunk> it = region.getChunks().iterator();
            while (it.hasNext()) {
                SerializableChunk sc = it.next();
                if (sc == null || sc.getWorldName() == null || !worlds.contains(sc.getWorldName().trim())) {
                    it.remove();
                    count++;
                }
            }
        }
        return count;
    }
}
