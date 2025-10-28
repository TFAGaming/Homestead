package tfagaming.projects.minecraft.homestead.managers;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarsManager {
    public static void declareWar(String name, double prize, List<Region> regions) {
        War war = new War(name);

        for (Region region : regions) {
            war.addRegion(region);
        }
    }

    public static List<War> getAll() {
        return Homestead.warsCache.getAll();
    }

    public static War findWar(UUID id) {
        for (War war : Homestead.warsCache.getAll()) {
            if (war.getUniqueId().equals(id)) {
                return war;
            }
        }

        return null;
    }

    public static War findWar(String name) {
        for (War war : Homestead.warsCache.getAll()) {
            if (war.getName().equals(name)) {
                return war;
            }
        }

        return null;
    }

    public static void endWar(UUID id) {
        War war = findWar(id);

        if (war == null) {
            return;
        }

        Homestead.warsCache.remove(war);
    }

    public static void removeRegionFromAnyWar(UUID id) {
        for (War war : Homestead.warsCache.getAll()) {
            for (Region region : war.getRegions()) {
                if (region.getUniqueId().equals(id)) {
                    war.removeRegion(region);

                    break;
                }
            }
        }
    }

    public static boolean isNameUsed(String name) {
        for (War war : Homestead.warsCache.getAll()) {
            if (war.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isRegionInWar(UUID id) {
        for (War war : Homestead.warsCache.getAll()) {
            for (Region region : war.getRegions()) {
                if (region.getUniqueId().equals(id)) {
                    return true;
                }
            }
        }

        return false;
    }
}
