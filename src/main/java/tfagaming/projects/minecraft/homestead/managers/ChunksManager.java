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
import tfagaming.projects.minecraft.homestead.api.events.*;
import tfagaming.projects.minecraft.homestead.integrations.WorldEditAPI;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtils;

public class ChunksManager {
    public static void claimChunk(UUID id, Chunk chunk, OfflinePlayer... player) {
        Region region = RegionsManager.findRegion(id);

        if (region == null) {
            return;
        }

        region.addChunk(new SerializableChunk(chunk));

        ChunkClaimEvent event = new ChunkClaimEvent(chunk, player.length > 0 ? player[0] : null);
        Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));
    }

    public static void unclaimChunk(UUID id, Chunk chunk, OfflinePlayer... player) {
        removeChunk(id, new SerializableChunk(chunk));

        boolean isRegeneratingChunksEnabled = Homestead.config.get("worldedit.regenerate-chunks");

        if (isRegeneratingChunksEnabled) {
            Homestead.getInstance().runAsyncTask(() -> {
                WorldEditAPI.regenerateChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
            });
        }

        ChunkUnclaimEvent event = new ChunkUnclaimEvent(chunk, player.length > 0 ? player[0] : null);
        Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));
    }

    public static void removeChunk(UUID id, SerializableChunk chunk) {
        Region region = RegionsManager.findRegion(id);

        if (region == null) {
            return;
        }

        region.removeChunk(chunk);

        for (SerializableSubArea subArea : region.getSubAreas()) {
            for (Chunk subAreaChunk : ChunkUtils.getChunksInArea(subArea.getFirstPoint(), subArea.getSecondPoint())) {
                if (new SerializableChunk(subAreaChunk).toString(true).equals(chunk.toString(true))) {
                    region.removeSubArea(subArea.getId());
                    break;
                }
            }
        }
    }

    public static boolean isChunkInDisabledWorld(Chunk chunk) {
        List<String> disabledWorlds = Homestead.config.get("disabled-worlds");

        return disabledWorlds.contains(chunk.getWorld().getName());
    }

    public static boolean isChunkClaimed(Chunk chunk) {
        for (Region region : RegionsManager.getAll()) {
            for (SerializableChunk serializedChunk : region.getChunks()) {
                String chunkString = SerializableChunk.convertToString(chunk, true);

                if (serializedChunk.toString(true).equals(chunkString)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Region getRegionOwnsTheChunk(Chunk chunk) {
        for (Region region : RegionsManager.getAll()) {
            for (SerializableChunk serializedChunk : region.getChunks()) {
                String chunkString = SerializableChunk.convertToString(chunk, true);

                if (serializedChunk.toString(true).equals(chunkString)) {
                    return region;
                }
            }
        }

        return null;
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

    public static boolean hasNeighbor(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        World world = player.getWorld();
        int x = chunk.getX();
        int z = chunk.getZ();

        Chunk north = world.getChunkAt(x, z - 1);
        Chunk south = world.getChunkAt(x, z + 1);
        Chunk west = world.getChunkAt(x - 1, z);
        Chunk east = world.getChunkAt(x + 1, z);

        if (isChunkClaimed(north)) {
            Region region = getRegionOwnsTheChunk(north);

            if (!region.getOwnerId().equals(player.getUniqueId())) {
                return true;
            }
        }

        if (isChunkClaimed(south)) {
            Region region = getRegionOwnsTheChunk(south);

            if (!region.getOwnerId().equals(player.getUniqueId())) {
                return true;
            }
        }

        if (isChunkClaimed(west)) {
            Region region = getRegionOwnsTheChunk(west);

            if (!region.getOwnerId().equals(player.getUniqueId())) {
                return true;
            }
        }

        if (isChunkClaimed(east)) {
            Region region = getRegionOwnsTheChunk(east);

            if (!region.getOwnerId().equals(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public static Chunk getFromLocation(World world, int x, int z) {
        Location location = new Location(world, x * 16 + 8, 64,
                z * 16 + 8);

        return location.getChunk();
    }

    public static Location getLocation(Player player, Chunk chunk) {
        Location location = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, 64,
                chunk.getZ() * 16 + 8);

        location.setY(location.getWorld().getHighestBlockYAt(location) + 2);
        location.setPitch(player.getLocation().getPitch());
        location.setYaw(player.getLocation().getYaw());

        return location;
    }

    public static Location getLocation(Player player, SerializableChunk chunk) {
        World world = chunk.getWorld();
        int x = chunk.getX() * 16 + 8;
        int z = chunk.getZ() * 16 + 8;

        if (world == null) {
            return null;
        }

        Location location;

        if (world.getEnvironment() == World.Environment.NETHER) {
            location = findSafeNetherLocation(world, x, z);
        } else {
            int highestY = world.getHighestBlockYAt(x, z);
            location = new Location(world, x, highestY + 2, z);
        }

        if (location != null) {
            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());
        }

        return location;
    }

    private static Location findSafeNetherLocation(World world, int x, int z) {
        int minY = 32;
        int maxY = 127;

        for (int y = minY; y < maxY; y++) {
            Block block = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);

            if (block.getType() == Material.AIR && above.getType() == Material.AIR) {
                return new Location(world, x + 0.5, y, z + 0.5);
            }
        }

        return null;
    }

    public static void removeRandomChunk(UUID id) {
        Region region = RegionsManager.findRegion(id);

        if (region == null) {
            return;
        }

        List<SerializableChunk> chunks = region.getChunks();

        if (chunks.size() > 0) {
            Random random = new Random();
            int randomIndex = random.nextInt(chunks.size());

            region.removeChunk(chunks.get(randomIndex));
        }
    }

    public static int deleteInvalidChunks() {
        int count = 0;

        List<String> worlds = new ArrayList<String>();

        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }

        for (Region region : RegionsManager.getAll()) {
            if (region == null || region.getChunks().size() == 0) {
                continue;
            }

            for (SerializableChunk serializedChunk : region.getChunks()) {
                if (!worlds.contains(serializedChunk.getWorldName())) {
                    region.removeChunk(serializedChunk);

                    count++;
                }
            }
        }

        return count;
    }
}
