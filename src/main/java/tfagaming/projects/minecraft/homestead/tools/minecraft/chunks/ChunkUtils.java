package tfagaming.projects.minecraft.homestead.tools.minecraft.chunks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;

public class ChunkUtils {
    public static List<Chunk> getChunksInArea(Block corner1, Block corner2) {
        World world = corner1.getWorld();
        List<Chunk> chunks = new ArrayList<>();
        
        int minX = Math.min(corner1.getX(), corner2.getX());
        int maxX = Math.max(corner1.getX(), corner2.getX());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;
        
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                Chunk chunk = world.getChunkAt(x, z);
                chunks.add(chunk);
            }
        }
        
        return chunks;
    }

    public static Chunk findNearbyUnclaimedChunk(Player player) {
        Chunk startChunk = player.getLocation().getChunk();
        World world = player.getWorld();
        int startX = startChunk.getX();
        int startZ = startChunk.getZ();

        int radius = 1;
        int maxRadius = 30;

        while (radius <= maxRadius) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }

                    Chunk currentChunk = world.getChunkAt(startX + x, startZ + z);

                    if (!ChunksManager.isChunkClaimed(currentChunk)) {
                        return currentChunk;
                    }
                }
            }

            radius++;
        }

        return null;
    }
}