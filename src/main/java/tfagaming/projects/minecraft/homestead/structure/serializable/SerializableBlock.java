package tfagaming.projects.minecraft.homestead.structure.serializable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SerializableBlock {
    private String worldName;
    private int x;
    private int y;
    private int z;

    public SerializableBlock(Block block) {
        this.worldName = block.getWorld().getName();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
    }

    public SerializableBlock(String worldName, int x, int y, int z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return (worldName + "," + x + "," + y + "," + z);
    }

    public static SerializableBlock fromString(String string) {
        String[] splitted = string.split(",");

        return new SerializableBlock(splitted[0], Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]));
    }

    public Block getBlock() {
        World world = getWorld();

        if (world == null) {
            return null;
        }

        return world.getBlockAt(x, y, z);
    }
}