package tfagaming.projects.minecraft.homestead.structure.serializable;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

import tfagaming.projects.minecraft.homestead.Homestead;

public class SerializableRent {
    private UUID playerId;
    private double price;
    private long startAt;
    private long untilAt;

    public SerializableRent(OfflinePlayer player, double price, long untilAt) {
        this.playerId = player.getUniqueId();
        this.price = price;
        this.startAt = System.currentTimeMillis();
        this.untilAt = untilAt;
    }

    public SerializableRent(UUID playerId, double price, long startedAt, long untilAt) {
        this.playerId = playerId;
        this.price = price;
        this.startAt = startedAt;
        this.untilAt = untilAt;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public OfflinePlayer getPlayer() {
        return Homestead.getInstance().getOfflinePlayerSync(playerId);
    }

    public double getPrice() {
        return price;
    }

    public long getStartAt() {
        return startAt;
    }

    public long getUntilAt() {
        return untilAt;
    }

    @Override
    public String toString() {
        return (playerId + "," + price + "," + startAt + "," + untilAt);
    }

    public static SerializableRent fromString(String string) {
        if (string == null) {
            return null;
        }
        
        String[] splitted = string.split(",");

        return new SerializableRent(UUID.fromString(splitted[0]), Double.parseDouble(splitted[1]), Long.parseLong(splitted[2]), Long.parseLong(splitted[3]));
    }

    public OfflinePlayer getBukkitOfflinePlayer() {
        return Homestead.getInstance().getOfflinePlayerSync(playerId);
    }
}
