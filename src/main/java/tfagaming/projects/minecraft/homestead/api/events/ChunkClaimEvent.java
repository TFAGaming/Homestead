package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChunkClaimEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Chunk chunk;
    private final OfflinePlayer player;

    public ChunkClaimEvent(Chunk chunk, OfflinePlayer player) {
        this.chunk = chunk;
        this.player = player;
    }
    
    public Chunk getChunk() {
        return chunk;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
