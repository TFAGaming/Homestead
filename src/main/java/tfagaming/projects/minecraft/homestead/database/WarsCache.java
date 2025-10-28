package tfagaming.projects.minecraft.homestead.database;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WarsCache extends HashMap<UUID, War> {
    public WarsCache(int interval) {
        Homestead.getInstance().runAsyncTimerTask(() -> {
            Homestead.database.exportWars();
        }, 10, interval);
    }

    public List<War> getAll() {
        List<War> wars = new ArrayList<>();

        for (War war : this.values()) {
            wars.add(war);
        }

        return wars;
    }

    public void putOrUpdate(War war) {
        this.put(war.getUniqueId(), war);
    }

    public long getLatency() {
        long before = System.currentTimeMillis();

        this.getAll();

        long after = System.currentTimeMillis();

        return after - before;
    }
}