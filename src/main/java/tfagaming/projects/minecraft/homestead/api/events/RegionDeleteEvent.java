package tfagaming.projects.minecraft.homestead.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import tfagaming.projects.minecraft.homestead.structure.Region;

public class RegionDeleteEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Region region;
    private final OfflinePlayer player;

    public RegionDeleteEvent(Region region, OfflinePlayer player) {
        this.region = region;
        this.player = player;
    }
    
    public Region getRegion() {
        return region;
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
