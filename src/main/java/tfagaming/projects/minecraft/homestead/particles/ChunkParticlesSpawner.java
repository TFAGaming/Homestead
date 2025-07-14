package tfagaming.projects.minecraft.homestead.particles;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

public class ChunkParticlesSpawner {
    private static final Map<UUID, BukkitTask> tasks = new HashMap<>();

    private final Player player;

    public ChunkParticlesSpawner(Player player) {
        this.player = player;

        if (tasks.containsKey(player.getUniqueId())) {
            BukkitTask taskFromMap = tasks.get(player.getUniqueId());

            cancelTask(taskFromMap, player);
        }

        startRepeatingEffect(15L);
    }

    public void spawnParticles() {
        for (Region region : RegionsManager.getAll()) {
            spawnParticlesForRegion(region);
        }
    }

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

            if (!player.getLocation().getWorld().getName().equals(chunkWorldName)) {
                continue;
            }

            DustOptions dustoptions;

            region = RegionsManager.findRegion(region.getUniqueId());

            if (region.getOwnerId().equals(player.getUniqueId())) {
                dustoptions = new DustOptions(Color.fromRGB(0, 255, 0), 2.0F);
            } else if (region.isPlayerMember(player)) {
                dustoptions = new DustOptions(Color.fromRGB(255, 255, 0), 2.0F);
            } else {
                dustoptions = new DustOptions(Color.fromRGB(255, 0, 0), 2.0F);
            }
            
            Chunk north = world.getChunkAt(chunkX, chunkZ - 1);
            if (ChunksManager.getRegionOwnsTheChunk(north) == null || (ChunksManager.getRegionOwnsTheChunk(north) != null && !ChunksManager.getRegionOwnsTheChunk(north).getUniqueId().equals(region.getUniqueId()))) {
                for (int x = minX; x < minX + 16; x++) {
                    player.spawnParticle(Particle.DUST, x, yOffset, minZ, 5, dustoptions);
                }
            }

            Chunk south = world.getChunkAt(chunkX, chunkZ + 1);
            if (ChunksManager.getRegionOwnsTheChunk(south) == null || (ChunksManager.getRegionOwnsTheChunk(south) != null && !ChunksManager.getRegionOwnsTheChunk(south).getUniqueId().equals(region.getUniqueId()))) {
                for (int x = minX; x < minX + 16; x++) {
                    player.spawnParticle(Particle.DUST, x, yOffset, minZ + 16, 5, dustoptions);
                }
            }

            Chunk west = world.getChunkAt(chunkX - 1, chunkZ);
            if (ChunksManager.getRegionOwnsTheChunk(west) == null || (ChunksManager.getRegionOwnsTheChunk(west) != null && !ChunksManager.getRegionOwnsTheChunk(west).getUniqueId().equals(region.getUniqueId()))) {
                for (int z = minZ; z < minZ + 16; z++) {
                    player.spawnParticle(Particle.DUST, minX, yOffset, z, 5, dustoptions);
                }
            }

            Chunk east = world.getChunkAt(chunkX + 1, chunkZ);
            if (ChunksManager.getRegionOwnsTheChunk(east) == null || (ChunksManager.getRegionOwnsTheChunk(east) != null && !ChunksManager.getRegionOwnsTheChunk(east).getUniqueId().equals(region.getUniqueId()))) {
                for (int z = minZ; z < minZ + 16; z++) {
                    player.spawnParticle(Particle.DUST, minX + 16, yOffset, z, 5, dustoptions);
                }
            }
        }
    }

    public void startRepeatingEffect(long intervalTicks) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                spawnParticles();
            }
        }.runTaskTimer(Homestead.getInstance(), 0L, intervalTicks);

        tasks.put(player.getUniqueId(), task);

        Bukkit.getScheduler().runTaskLater(Homestead.getInstance(),
                () -> cancelTask(task, player), 60 * 20L);
    }

    public static void cancelTask(BukkitTask task, Player player) {
        if (task != null) {
            tasks.remove(player.getUniqueId());

            task.cancel();
            task = null;
        }
    }

    public static void cancelTask(Player player) {
        BukkitTask task = tasks.get(player.getUniqueId());

        if (task != null) {
            tasks.remove(player.getUniqueId());

            task.cancel();
            task = null;
        }
    }
}