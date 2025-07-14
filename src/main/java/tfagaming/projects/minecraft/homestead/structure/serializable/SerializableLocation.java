package tfagaming.projects.minecraft.homestead.structure.serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializableLocation {
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public SerializableLocation(Location location) {
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public SerializableLocation(World world, double x, double y, double z, float yaw, float pitch) {
        this.worldName = world.getName();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public SerializableLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public void setWorld(World world) {
        this.worldName = world.getName();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public String toString() {
        return (worldName + "," + x + "," + y + "," + z + "," + yaw + "," + pitch);
    }

    public static SerializableLocation fromString(String string) {
        if (string == null) {
            return null;
        }

        String[] splitted = string.split(",");

        return new SerializableLocation(splitted[0], Double.parseDouble(splitted[1]), Double.parseDouble(splitted[2]),
                Double.parseDouble(splitted[3]), Float.parseFloat(splitted[4]), Float.parseFloat(splitted[5]));
    }

    public static String toString(Location location) {
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getYaw() + "," + location.getPitch();
    }

    public Location getBukkitLocation() {
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return null;
        }

        Location location = new Location(world, x, y, z);

        location.setYaw(yaw);
        location.setPitch(pitch);

        return location;
    }
}
