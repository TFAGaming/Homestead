package tfagaming.projects.minecraft.homestead.structure.serializable;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

import tfagaming.projects.minecraft.homestead.Homestead;

public class SerializableRate {
    private UUID playerId;
    private int rate;
    private long ratedAt;

    public SerializableRate(OfflinePlayer player) {
        this.playerId = player.getUniqueId();
        this.rate = 0;
        this.ratedAt = System.currentTimeMillis();
    }

    public SerializableRate(OfflinePlayer player, int rate) {
        this.playerId = player.getUniqueId();
        this.rate = rate;
        this.ratedAt = System.currentTimeMillis();
    }

    public SerializableRate(OfflinePlayer player, int rate, long ratedAt) {
        this.playerId = player.getUniqueId();
        this.rate = rate;
        this.ratedAt = ratedAt;
    }

    public SerializableRate(UUID playerId, int rate, long ratedAt) {
        this.playerId = playerId;
        this.rate = rate;
        this.ratedAt = ratedAt;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public long getRatedAt() {
        return ratedAt;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return (playerId + "," + rate + "," + ratedAt);
    }

    public static SerializableRate fromString(String string) {
        String[] splitted = string.split(",");

        return new SerializableRate(UUID.fromString(splitted[0]), Integer.parseInt(splitted[1]), Long.parseLong(splitted[2]));
    }

    public OfflinePlayer getBukkitOfflinePlayer() {
        return Homestead.getInstance().getOfflinePlayerSync(playerId);
    }
}
