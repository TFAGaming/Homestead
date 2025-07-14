package tfagaming.projects.minecraft.homestead.structure.serializable;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;

public class SerializableBannedPlayer {
    private UUID playerId;
    private String reason;
    private long bannedAt;

    public SerializableBannedPlayer(OfflinePlayer player) {
        this.playerId = player.getUniqueId();
        this.reason = null;
        this.bannedAt = System.currentTimeMillis();
    }

    public SerializableBannedPlayer(OfflinePlayer player, String reason) {
        this.playerId = player.getUniqueId();
        this.reason = reason;
        this.bannedAt = System.currentTimeMillis();
    }

    public SerializableBannedPlayer(OfflinePlayer player, String reason, long bannedAt) {
        this.playerId = player.getUniqueId();
        this.reason = reason;
        this.bannedAt = bannedAt;
    }

    public SerializableBannedPlayer(UUID playerId, String reason, long bannedAt) {
        this.playerId = playerId;
        this.reason = reason;
        this.bannedAt = bannedAt;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getBannedAt() {
        return bannedAt;
    }

    @Override
    public String toString() {
        return (playerId + "," + bannedAt + "," + reason);
    }

    public static SerializableBannedPlayer fromString(String string) {
        String[] splitted = StringUtils.splitWithLimit(string, ",", 3);

        return new SerializableBannedPlayer(UUID.fromString(splitted[0]), splitted[2], Long.parseLong(splitted[1]));
    }

    public OfflinePlayer getBukkitOfflinePlayer() {
        return Homestead.getInstance().getOfflinePlayerSync(playerId);
    }
}
