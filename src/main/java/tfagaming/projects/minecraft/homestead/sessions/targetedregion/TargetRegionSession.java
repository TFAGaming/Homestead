package tfagaming.projects.minecraft.homestead.sessions.targetedregion;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;

public class TargetRegionSession {
    public static final HashMap<UUID, Region> sessions = new HashMap<UUID, Region>();

    public TargetRegionSession(Player player, Region region) {
        sessions.put(player.getUniqueId(), region);
    }

    public TargetRegionSession(Player player) {
        List<Region> regions = RegionsManager.getRegionsOwnedByPlayer(player);

        if (regions.size() > 0) {
            sessions.putIfAbsent(player.getUniqueId(), regions.get(0));
        } else {
            sessions.putIfAbsent(player.getUniqueId(), null);
        }
    }

    public static Region getRegion(OfflinePlayer player) {
        return sessions.get(player.getUniqueId());
    }

    public static void setRegion(OfflinePlayer player, Region region) {
        sessions.put(player.getUniqueId(), region);
    }

    public static void setRegion(OfflinePlayer player, String regionName) {
        Region region = RegionsManager.findRegion(regionName);

        sessions.put(player.getUniqueId(), region);
    }

    public static void randomizeRegion(
            Player player) {
        List<Region> regions = RegionsManager.getRegionsOwnedByPlayer(player);

        if (regions.size() == 0) {
            sessions.put(player.getUniqueId(), null);
        } else {
            Random random = new Random();
            int randomIndex = random.nextInt(regions.size());

            setRegion(player, regions.get(randomIndex));
        }
    }

    public static boolean hasSession(Player player) {
        return sessions.containsKey(player.getUniqueId()) && getRegion(player) != null;
    }

    public static void removeSession(Player player) {
        sessions.remove(player.getUniqueId());
    }
}
