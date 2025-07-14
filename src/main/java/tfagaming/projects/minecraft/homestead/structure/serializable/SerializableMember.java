package tfagaming.projects.minecraft.homestead.structure.serializable;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

import tfagaming.projects.minecraft.homestead.Homestead;

public class SerializableMember {
    private UUID playerId;
    private long flags;
    private long regionControlFlags;
    private long joinedAt;
    private long taxesAt;

    public SerializableMember(OfflinePlayer player) {
        this.playerId = player.getUniqueId();
        this.flags = 0;
        this.regionControlFlags = 0;
        this.joinedAt = System.currentTimeMillis();
        this.taxesAt = 0;
    }

    public SerializableMember(OfflinePlayer player, long flags, long regionControlFlags) {
        this.playerId = player.getUniqueId();
        this.flags = flags;
        this.regionControlFlags = regionControlFlags;
        this.joinedAt = System.currentTimeMillis();
        this.taxesAt = 0;
    }

    public SerializableMember(OfflinePlayer player, long flags, long regionControlFlags, long joinedAt, long taxesAt) {
        this.playerId = player.getUniqueId();
        this.flags = flags;
        this.regionControlFlags = regionControlFlags;
        this.joinedAt = joinedAt;
        this.taxesAt = taxesAt;
    }

    public SerializableMember(UUID playerId, long flags, long regionControlFlags, long joinedAt, long taxesAt) {
        this.playerId = playerId;
        this.flags = flags;
        this.regionControlFlags = regionControlFlags;
        this.joinedAt = joinedAt;
        this.taxesAt = taxesAt;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public long getFlags() {
        return flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public long getTaxesAt() {
        return taxesAt;
    }

    public void setTaxesAt(long taxesAt) {
        this.taxesAt = taxesAt;
    }

    public long getRegionControlFlags() {
        return regionControlFlags;
    }

    public void setRegionControlFlags(long flags) {
        this.regionControlFlags = flags;
    }

    @Override
    public String toString() {
        return (playerId + "," + flags + "," + regionControlFlags + "," + joinedAt + "," + taxesAt);
    }

    public static SerializableMember fromString(String string) {
        String[] splitted = string.split(",");
        
        return new SerializableMember(UUID.fromString(splitted[0]), Long.parseLong(splitted[1]),
                Long.parseLong(splitted[2]), Long.parseLong(splitted[3]), Long.parseLong(splitted[4]));
    }

    public OfflinePlayer getBukkitOfflinePlayer() {
        return Homestead.getInstance().getOfflinePlayerSync(playerId);
    }
}
