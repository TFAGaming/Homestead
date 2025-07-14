package tfagaming.projects.minecraft.homestead.structure.serializable;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SerializableSubArea {
    private UUID id;
    private UUID regionId;
    private String name;
    private String worldName;
    private int[] point1;
    private int[] point2;
    private long flags;
    private long createdAt;

    public SerializableSubArea(UUID regionId, String name, World world, Block point1, Block point2, long flags) {
        this.id = UUID.randomUUID();
        this.regionId = regionId;
        this.name = name;
        this.worldName = world.getName();
        this.point1 = getBlockLocation(point1);
        this.point2 = getBlockLocation(point2);
        this.flags = flags;
        this.createdAt = System.currentTimeMillis();
    }

    public SerializableSubArea(UUID id, UUID regionId, String name, String worldName, Block point1, Block point2,
            long flags,
            long createdAt) {
        this.id = UUID.randomUUID();
        this.regionId = regionId;
        this.name = name;
        this.worldName = worldName;
        this.point1 = getBlockLocation(point1);
        this.point2 = getBlockLocation(point2);
        this.flags = flags;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRegionId() {
        return regionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Block getFirstPoint() {
        return parseBlockLocation(getWorld(), point1);
    }

    public Block getSecondPoint() {
        return parseBlockLocation(getWorld(), point2);
    }

    public long getFlags() {
        return flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    private int getMinX() {
        Block firstPoint = parseBlockLocation(getWorld(), point1), secondPoint = parseBlockLocation(getWorld(), point2);

        return Math.min(firstPoint.getX(), secondPoint.getX());
    }

    private int getMaxX() {
        Block firstPoint = parseBlockLocation(getWorld(), point1), secondPoint = parseBlockLocation(getWorld(), point2);

        return Math.max(firstPoint.getX(), secondPoint.getX());
    }

    private int getMinY() {
        Block firstPoint = parseBlockLocation(getWorld(), point1), secondPoint = parseBlockLocation(getWorld(), point2);

        return Math.min(firstPoint.getY(), secondPoint.getY());
    }

    private int getMaxY() {
        Block firstPoint = parseBlockLocation(getWorld(), point1), secondPoint = parseBlockLocation(getWorld(), point2);

        return Math.max(firstPoint.getY(), secondPoint.getY());
    }

    private int getMinZ() {
        Block firstPoint = parseBlockLocation(getWorld(), point1), secondPoint = parseBlockLocation(getWorld(), point2);

        return Math.min(firstPoint.getZ(), secondPoint.getZ());
    }

    private int getMaxZ() {
        Block firstPoint = parseBlockLocation(getWorld(), point1), secondPoint = parseBlockLocation(getWorld(), point2);

        return Math.max(firstPoint.getZ(), secondPoint.getZ());
    }

    public static int getMinX(SerializableBlock firstPoint, SerializableBlock secondPoint) {
        return Math.min(firstPoint.getX(), secondPoint.getX());
    }

    public static int getMaxX(SerializableBlock firstPoint, SerializableBlock secondPoint) {
        return Math.max(firstPoint.getX(), secondPoint.getX());
    }

    public static int getMinY(SerializableBlock firstPoint, SerializableBlock secondPoint) {
        return Math.min(firstPoint.getY(), secondPoint.getY());
    }

    public static int getMaxY(SerializableBlock firstPoint, SerializableBlock secondPoint) {
        return Math.max(firstPoint.getY(), secondPoint.getY());
    }

    public static int getMinZ(SerializableBlock firstPoint, SerializableBlock secondPoint) {
        return Math.min(firstPoint.getZ(), secondPoint.getZ());
    }

    public static int getMaxZ(SerializableBlock firstPoint, SerializableBlock secondPoint) {
        return Math.max(firstPoint.getZ(), secondPoint.getZ());
    }

    public int getVolume() {
        int width = getMaxX() - getMinX() + 1;
        int height = getMaxY() - getMinY() + 1;
        int depth = getMaxZ() - getMinZ() + 1;

        return width * height * depth;
    }

    public static int getVolume(SerializableBlock firstPoint, SerializableBlock secondPoint) {
        int width = getMaxX(firstPoint, secondPoint) - getMinX(firstPoint, secondPoint) + 1;
        int height = getMaxY(firstPoint, secondPoint) - getMinY(firstPoint, secondPoint) + 1;
        int depth = getMaxZ(firstPoint, secondPoint) - getMinZ(firstPoint, secondPoint) + 1;

        return width * height * depth;
    }

    public boolean isBlockInside(Block block) {
        return (block.getX() >= getMinX() && block.getX() <= getMaxX()) &&
                (block.getY() >= getMinY() && block.getY() <= getMaxY()) &&
                (block.getZ() >= getMinZ() && block.getZ() <= getMaxZ());
    }

    public boolean isLocationInside(Location location) {
        if (location == null || location.getWorld() == null)
            return false;
        World subAreaWorld = getFirstPoint().getWorld();

        if (!location.getWorld().equals(subAreaWorld))
            return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return isInsideBounds(x, y, z);
    }

    public boolean isIntersectingOtherSubArea(SerializableBlock firstPoint, SerializableBlock secondPoint) {
        if (!this.getFirstPoint().getWorld().equals(firstPoint.getWorld())) return false;

        Block thisFirstPoint = parseBlockLocation(getWorld(), point1), thisSecondPoint = parseBlockLocation(getWorld(), point2);

        int minX1 = Math.min(thisFirstPoint.getX(), thisSecondPoint.getX());
        int minY1 = Math.min(thisFirstPoint.getY(), thisSecondPoint.getY());
        int minZ1 = Math.min(thisFirstPoint.getZ(), thisSecondPoint.getZ());
        int maxX1 = Math.max(thisFirstPoint.getX(), thisSecondPoint.getX());
        int maxY1 = Math.max(thisFirstPoint.getY(), thisSecondPoint.getY());
        int maxZ1 = Math.max(thisFirstPoint.getZ(), thisSecondPoint.getZ());

        int minX2 = Math.min(firstPoint.getX(), secondPoint.getX());
        int minY2 = Math.min(firstPoint.getY(), secondPoint.getY());
        int minZ2 = Math.min(firstPoint.getZ(), secondPoint.getZ());
        int maxX2 = Math.max(firstPoint.getX(), secondPoint.getX());
        int maxY2 = Math.max(firstPoint.getY(), secondPoint.getY());
        int maxZ2 = Math.max(firstPoint.getZ(), secondPoint.getZ());

        return (minX1 <= maxX2 && maxX1 >= minX2) &&
               (minY1 <= maxY2 && maxY1 >= minY2) &&
               (minZ1 <= maxZ2 && maxZ1 >= minZ2);
    }

    private boolean isInsideBounds(int x, int y, int z) {
        Block firstPoint = parseBlockLocation(getWorld(), point1), secondPoint = parseBlockLocation(getWorld(), point2);

        int minX = Math.min(firstPoint.getX(), secondPoint.getX());
        int minY = Math.min(firstPoint.getY(), secondPoint.getY());
        int minZ = Math.min(firstPoint.getZ(), secondPoint.getZ());
        int maxX = Math.max(firstPoint.getX(), secondPoint.getX());
        int maxY = Math.max(firstPoint.getY(), secondPoint.getY());
        int maxZ = Math.max(firstPoint.getZ(), secondPoint.getZ());

        return (x >= minX && x <= maxX) &&
                (y >= minY && y <= maxY) &&
                (z >= minZ && z <= maxZ);
    }

    @Override
    public String toString() {
        return (id + "," + regionId + "," + name + "," + worldName + "," + toStringBlockLocation(getWorld(), point1) + ","
                + toStringBlockLocation(getWorld(), point2) + "," + flags + ","
                + createdAt);
    }

    public static String toStringBlockLocation(World world, int[] coords) {
        return toStringBlockLocation(world, parseBlockLocation(world, coords));
    }

    public static String toStringBlockLocation(World world, Block block) {
        return (world.getName() + ";" + block.getX() + ";" + block.getY() + ";" + block.getZ());
    }

    public static SerializableSubArea fromString(String string) {
        if (string == null) {
            return null;
        }

        String[] splitted = string.split(",");
        
        World world = Bukkit.getWorld(splitted[3]);

        return new SerializableSubArea(UUID.fromString(splitted[0]), UUID.fromString(splitted[1]), splitted[2],
                splitted[3],
                parseBlockLocation(world, splitted[4]), parseBlockLocation(world, splitted[5]), Long.parseLong(splitted[6]),
                Long.parseLong(splitted[7]));
    }

    public static Block parseBlockLocation(World world, int[] coords) {
        return new Location(world, coords[0], coords[1], coords[2]).getBlock();
    }

    public static Block parseBlockLocation(World world, String coordsString) {
        String[] splitted = coordsString.split(";");

        int[] coords = { Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]),
                Integer.parseInt(splitted[3]) };

        return parseBlockLocation(world, coords);
    }

    public static int[] getBlockLocation(Block block) {
        return new int[] { block.getX(), block.getY(), block.getZ() };
    }
}
