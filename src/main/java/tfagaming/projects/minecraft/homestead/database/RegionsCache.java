package tfagaming.projects.minecraft.homestead.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Region;

public class RegionsCache extends HashMap<UUID, Region> {
    public RegionsCache(int interval) {
        Homestead.getInstance().runAsyncTimerTask(() -> {
            Homestead.database.exportRegions();
        }, 10, interval);
    }

    public List<Region> getAll() {
        List<Region> regions = new ArrayList<>();

        for (Region region : this.values()) {
            regions.add(region);
        }

        return regions;
    }

    public void putOrUpdate(Region region) {
        this.put(region.getUniqueId(), region);
    }

    public long getLatency() {
        long before = System.currentTimeMillis();

        this.getAll();

        long after = System.currentTimeMillis();

        return after - before;
    }
}
