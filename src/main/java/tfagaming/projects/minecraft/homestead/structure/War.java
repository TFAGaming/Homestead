package tfagaming.projects.minecraft.homestead.structure;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class War {
    public UUID id;
    public String name;
    public String description;
    public String displayName;
    public ArrayList<UUID> regions = new ArrayList<>();
    public double prize;
    public long startedAt;

    public War(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.prize = 0.0;
        this.startedAt = System.currentTimeMillis();
    }

    public War(String name, List<UUID> regions) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.prize = 0.0;
        this.startedAt = System.currentTimeMillis();

        this.regions.addAll(regions);
    }

    public UUID getUniqueId() {
        return id;
    }

    // Name and displayname
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        updateCache();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateCache();
    }

    // Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        updateCache();
    }

    // Prize
    public double getPrize() {
        return prize;
    }

    public void setPrize(double prize) {
        this.prize = prize;
        updateCache();
    }

    // Started At
    public long getStartedAt() {
        return startedAt;
    }

    // Regions
    public ArrayList<UUID> getRegionUniqueIds() {
        return regions;
    }

    public ArrayList<Region> getRegions() {
        ArrayList<Region> regions = new ArrayList<>();

        for (UUID uuid : this.regions) {
            Region region = RegionsManager.findRegion(uuid);

            if (region != null) {
                regions.add(region);
            }
        }

        return regions;
    }

    public void addRegion(Region region) {
        if (this.regions.contains(region.id)) {
            return;
        }

        this.regions.add(region.id);
        updateCache();
    }

    public void removeRegion(Region region) {
        if (!this.regions.contains(region.id)) {
            return;
        }

        this.regions.remove(region.id);
        updateCache();
    }

    public void updateCache() {
        Homestead.warsCache.putOrUpdate(this);
    }
}
