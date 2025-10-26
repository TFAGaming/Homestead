package tfagaming.projects.minecraft.homestead.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionCreateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionDeleteEvent;
import tfagaming.projects.minecraft.homestead.flags.*;
import tfagaming.projects.minecraft.homestead.integrations.WorldEditAPI;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.other.UpkeepUtils;

public class RegionsManager {
    public enum RegionSorting {
        BANK,
        CHUNKS_COUNT,
        MEMBERS_COUNT,
        RATING,
        CREATION_DATE
    }

    public static Region createRegion(String name, OfflinePlayer player) {
        Region region = new Region(name, player);

        boolean isEnabled = Homestead.config.get("upkeep.enabled");
        int delay = Homestead.config.get("upkeep.start-upkeep");

        if (isEnabled) {
            region.setUpkeepAt(UpkeepUtils.getNewUpkeepAt() + (delay != 0 ? delay * 1000 : 0));
        }

        Homestead.cache.putOrUpdate(region);

        RegionCreateEvent event = new RegionCreateEvent(region, player);
        Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));

        return region;
    }

    public static Region createRegion(String name, OfflinePlayer player, boolean verifyName) {
        if (verifyName) {
            String newname = name;
            int counter = 1;

            if (verifyName) {
                while (RegionsManager.isNameUsed(newname)) {
                    newname = name + counter;
                    counter++;
                }
            }

            Region region = new Region(newname, player);

            boolean isEnabled = Homestead.config.get("upkeep.enabled");
            int delay = Homestead.config.get("upkeep.start-upkeep");

            if (isEnabled) {
                region.setUpkeepAt(UpkeepUtils.getNewUpkeepAt() + (delay != 0 ? delay * 1000 : 0));
            }

            Homestead.cache.putOrUpdate(region);

            return region;
        } else {
            return createRegion(name, player);
        }
    }

    public static List<Region> getAll() {
        return Homestead.cache.getAll();
    }

    public static Region findRegion(UUID id) {
        for (Region region : Homestead.cache.getAll()) {
            if (region.getUniqueId().equals(id)) {
                return region;
            }
        }

        return null;
    }

    public static Region findRegion(String name) {
        for (Region region : Homestead.cache.getAll()) {
            if (region.getName().equals(name)) {
                return region;
            }
        }

        return null;
    }

    public static void deleteRegion(UUID id, OfflinePlayer... player) {
        Region region = findRegion(id);

        if (region == null) {
            return;
        }

        boolean isRegeneratingChunksEnabled = Homestead.config.get("worldedit.regenerate-chunks");

        if (isRegeneratingChunksEnabled) {
            for (SerializableChunk chunk : region.getChunks()) {
                Homestead.getInstance().runAsyncTask(() -> {
                    WorldEditAPI.regenerateChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
                });
            }
        }

        Homestead.cache.remove(id);

        RegionDeleteEvent event = new RegionDeleteEvent(region, player.length > 0 ? player[0] : null);
        Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));
    }

    public static void addNewLog(UUID id, int messagePath) {
        Region region = findRegion(id);

        if (region == null) {
            return;
        }

        String message = Homestead.language.get("logs." + messagePath);

        region.addLog(new SerializableLog(Homestead.language.get("default.author"), message));
    }

    public static void addNewLog(UUID id, int messagePath, Map<String, String> replacements) {
        Region region = findRegion(id);

        if (region == null) {
            return;
        }

        String message = Homestead.language.get("logs." + messagePath);

        region.addLog(new SerializableLog(Homestead.language.get("default.author"),
                Formatters.replace(message, replacements)));
    }

    public static List<OfflinePlayer> getAllOwners() {
        List<OfflinePlayer> players = new ArrayList<OfflinePlayer>();

        for (Region region : Homestead.cache.getAll()) {
            players.add(region.getOwner());
        }

        return ListUtils.removeDuplications(players);
    }

    public static List<Region> sortRegionsAlpha() {
        List<Region> regions = Homestead.cache.getAll();

        Collections.sort(regions, new Comparator<Region>() {
            @Override
            public int compare(Region r1, Region r2) {
                return r1.getName().compareToIgnoreCase(r2.getName());
            }
        });

        return regions;
    }

    public static List<Region> getRegionsWithWelcomeSigns() {
        List<Region> filtered = new ArrayList<>();

        for (Region region : getAll()) {
            if (region.getWelcomeSign() != null) {
                filtered.add(region);
            }
        }

        return filtered;
    }

    public static List<OfflinePlayer> getPlayersWithRegionsHasWelcomeSigns() {
        List<OfflinePlayer> filtered = new ArrayList<>();

        for (Region region : getAll()) {
            if (region.getWelcomeSign() != null) {
                filtered.add(region.getOwner());
            }
        }

        return filtered;
    }

    public static List<Region> getRegionsOwnedByPlayer(OfflinePlayer player) {
        List<Region> regions = new ArrayList<Region>();

        for (Region region : Homestead.cache.getAll()) {
            if (region.getOwner().getUniqueId().equals(player.getUniqueId())) {
                regions.add(region);
            }
        }

        return regions;
    }

    public static List<Region> getRegionsHasPlayerAsMember(OfflinePlayer player) {
        List<Region> regions = new ArrayList<Region>();

        for (Region region : Homestead.cache.getAll()) {
            if (region.isPlayerMember(player)) {
                regions.add(region);
            }
        }

        return regions;
    }

    public static List<Region> getPublicRegions() {
        List<Region> regions = new ArrayList<Region>();

        for (Region region : Homestead.cache.getAll()) {
            long flags = region.getPlayerFlags();

            if (FlagsCalculator.isFlagSet(flags, PlayerFlags.PASSTHROUGH)
                    && FlagsCalculator.isFlagSet(flags, PlayerFlags.TELEPORT_SPAWN)) {
                regions.add(region);
            }
        }

        return regions;
    }

    public static List<Region> getRegionsInvitedPlayer(OfflinePlayer player) {
        List<Region> regions = new ArrayList<Region>();

        for (Region region : Homestead.cache.getAll()) {
            if (region.isPlayerInvited(player)) {
                regions.add(region);
            }
        }

        return regions;
    }

    public static List<Region> sortRegions(RegionSorting type) {
        switch (type) {
            case BANK:
                return Homestead.cache.getAll().stream()
                        .sorted(Comparator.comparingDouble(Region::getBank).reversed())
                        .collect(Collectors.toList());
            case CHUNKS_COUNT:
                return Homestead.cache.getAll().stream()
                        .sorted(Comparator.comparingInt(region -> ((Region) region).getChunks().size()).reversed())
                        .collect(Collectors.toList());
            case MEMBERS_COUNT:
                return Homestead.cache.getAll().stream()
                        .sorted(Comparator.comparingInt((region) -> ((Region) region).getMembers().size()).reversed())
                        .collect(Collectors.toList());
            case RATING:
                return Homestead.cache.getAll().stream()
                        .sorted(Comparator
                                .comparingDouble((region) -> getAverageRating((Region) region))
                                .reversed())
                        .collect(Collectors.toList());
            case CREATION_DATE:
                return Homestead.cache.getAll().stream()
                        .sorted(Comparator.comparingLong((region) -> ((Region) region).getCreatedAt()))
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    public static int getRank(RegionSorting type, UUID id) {
        List<Region> regions = sortRegions(type);

        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);

            if (region.getUniqueId().equals(id)) {
                return i + 1;
            }
        }

        return 0;
    }

    public static int getGlobalRank(UUID id) {
        return (getRank(RegionSorting.BANK, id) + getRank(RegionSorting.CHUNKS_COUNT, id)
                + getRank(RegionSorting.MEMBERS_COUNT, id)
                + getRank(RegionSorting.RATING, id)) / 4;
    }

    public static boolean isNameUsed(String name) {
        for (Region region : Homestead.cache.getAll()) {
            if (region.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPlayerInsideRegion(Player player, Region region) {
        Chunk location = player.getLocation().getChunk();

        for (SerializableChunk chunk : region.getChunks()) {
            if (chunk.getX() == location.getX() && chunk.getZ() == location.getZ()) {
                return true;
            }
        }

        return false;
    }

    public static double getAverageRating(Region region) {
        List<SerializableRate> rates = region.getRates();

        if (rates == null || rates.isEmpty()) {
            return 0.0;
        }

        int totalRate = 0;
        for (SerializableRate rate : rates) {
            totalRate += rate.getRate();
        }

        return (double) totalRate / rates.size();
    }

    public static int deleteRegionsWithInvalidPlayerIds() {
        int count = 0;

        for (Region region : Homestead.cache.getAll()) {
            OfflinePlayer regionOwner = region.getOwner();

            if (regionOwner.getName() == null) {
                deleteRegion(region.getUniqueId());

                count++;
            } else {
                for (SerializableMember serializableMember : region.getMembers()) {
                    OfflinePlayer member = serializableMember.getBukkitOfflinePlayer();

                    if (member.getName() == null) {
                        region.removeMember(serializableMember);

                        count++;
                    }
                }
            }
        }

        return count;
    }

    public static void setPlayerFlagForAllRegions(long flag, boolean state) {
        for (Region region : getAll()) {
            long flags = region.getPlayerFlags();
            long newFlags;

            if (state) {
                newFlags = FlagsCalculator.addFlag(flags, flag);
            } else {
                newFlags = FlagsCalculator.removeFlag(flags, flag);
            }

            region.setPlayerFlags(newFlags);
        }
    }

    public static void setWorldFlagForAllRegions(long flag, boolean state) {
        for (Region region : getAll()) {
            long flags = region.getWorldFlags();
            long newFlags;

            if (state) {
                newFlags = FlagsCalculator.addFlag(flags, flag);
            } else {
                newFlags = FlagsCalculator.removeFlag(flags, flag);
            }

            region.setWorldFlags(newFlags);
        }
    }
}
