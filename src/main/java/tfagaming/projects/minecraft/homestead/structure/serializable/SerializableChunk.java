package tfagaming.projects.minecraft.homestead.structure.serializable;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Represents a serializable Minecraft chunk with claim metadata.
 * <p>
 * Supports backward compatibility for older database formats (missing claimedAt).
 * </p>
 */
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
        return worldName + "," + x + "," + z + "," + claimedAt;
    }

    public String toString(boolean withoutClaimTime) {
        return worldName + "," + x + "," + z;
    }

    /**
     * Parses a chunk string into a SerializableChunk.
     * <p>
     * Backward-compatible with older database formats (handles missing or invalid claimedAt values).
     * </p>
     *
     * @param string The serialized chunk string (e.g. "world,-52,-310,1759002383683").
     * @return A valid SerializableChunk instance, or null if invalid.
     */
    public static SerializableChunk fromString(String string) {
        if (string == null || string.isEmpty()) return null;

        try {
            String[] split = string.split(",");

            // Must have at least world, x, z
            if (split.length < 3) return null;

            String world = split[0].trim();
            int x = Integer.parseInt(split[1].trim());
            int z = Integer.parseInt(split[2].trim());

            long claimedAt = System.currentTimeMillis();

            // Some old DB entries may lack or have invalid claimedAt
            if (split.length >= 4 && !split[3].trim().isEmpty()) {
                try {
                    claimedAt = Long.parseLong(split[3].trim());
                } catch (NumberFormatException ignored) {
                    // fallback to current timestamp
                }
            }

            return new SerializableChunk(world, x, z, claimedAt);
        } catch (Exception e) {
            return null;
        }
    }

    public static String convertToString(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ() + "," + System.currentTimeMillis();
    }

    public static String convertToString(Chunk chunk, boolean withoutClaimTime) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }

    public Location getBukkitLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        Location location = new Location(world, x * 16 + 8, 64, z * 16 + 8);
        location.setY(world.getHighestBlockYAt(location) + 2);

        if (world.getEnvironment() == World.Environment.NETHER) {
            Location newLocation = findSafeNetherLocation(world, x * 16 + 8, z * 16 + 8);
            if (newLocation != null) {
                location = newLocation;
            }
        }
        return location;
    }

    public Chunk getBukkitChunk() {
        Location loc = getBukkitLocation();
        return (loc != null) ? loc.getChunk() : null;
    }

    private Location findSafeNetherLocation(World world, int x, int z) {
        int minY = 32;
        int maxY = 124; // Prevents ceiling spawn issues

        for (int y = maxY; y >= minY; y--) {
            Block block = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);
            Block aboveAbove = world.getBlockAt(x, y + 2, z);

            if ((block.getType() != Material.AIR && block.getType() != Material.LAVA)
                    && above.getType() == Material.AIR
                    && aboveAbove.getType() == Material.AIR) {
                return new Location(world, x + 0.5, y + 1, z + 0.5);
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SerializableChunk other)) return false;
        return this.x == other.x && this.z == other.z &&
                this.worldName.equalsIgnoreCase(other.worldName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(worldName.toLowerCase(), x, z);
    }
}
