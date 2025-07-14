package tfagaming.projects.minecraft.homestead.structure.serializable;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SerializableChunk {
    private String worldName;
    private int x;
    private int z;
    private long claimedAt;

    public SerializableChunk(Chunk chunk) {
        this.worldName = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.claimedAt = System.currentTimeMillis();
    }

    public SerializableChunk(String worldName, int x, int z) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
        this.claimedAt = System.currentTimeMillis();
    }

    public SerializableChunk(World world, int x, int z) {
        this.worldName = world.getName();
        this.x = x;
        this.z = z;
        this.claimedAt = System.currentTimeMillis();
    }

    public SerializableChunk(String worldName, int x, int z, long claimedAt) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
        this.claimedAt = claimedAt;
    }

    public String getWorldName() {
        return worldName;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public long getClaimedAt() {
        return claimedAt;
    }

    @Override
    public String toString() {
        return (worldName + "," + x + "," + z + "," + claimedAt);
    }

    public String toString(boolean withoutClaimTime) {
        return (worldName + "," + x + "," + z);
    }

    public static SerializableChunk fromString(String string) {
        String[] splitted = string.split(",");

        return new SerializableChunk(splitted[0], Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]),
                Long.parseLong(splitted[3]));
    }

    public static String convertToString(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ() + "," + System.currentTimeMillis();
    }

    public static String convertToString(Chunk chunk, boolean withoutClaimTime) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }

    public Location getBukkitLocation() {
        World world = Bukkit.getWorld(worldName);

        Location location = new Location(world, x * 16 + 8, 64,
                z * 16 + 8);

        location.setY(world.getHighestBlockYAt(location) + 2);

        if (world.getEnvironment().equals(World.Environment.NETHER)) {
            Location newLocation = findSafeNetherLocation(world, x * 16 + 8, z * 16 + 8);

            if (newLocation != null) {
                location = newLocation;
            }
        }
        return location;
    }

    public Chunk getBukkitChunk() {
        return getBukkitLocation().getChunk();
    }

    private Location findSafeNetherLocation(World world, int x, int z) {
        int minY = 32;
        int maxY = 124; // 127

        for (int y = maxY; y >= minY; y--) {
            Block block = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);
            Block aboveAbove = world.getBlockAt(x, y + 2, z);

            if ((block.getType() != Material.AIR && block.getType() != Material.LAVA) && above.getType() == Material.AIR
                    && aboveAbove.getType() == Material.AIR) {
                return new Location(world, x + 0.5, y + 1, z + 0.5);
            }
        }

        return null;
    }
}
