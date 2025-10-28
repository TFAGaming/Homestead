package tfagaming.projects.minecraft.homestead.database.providers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;

public class YAML {
    private File regionsFolder;
    private File warsFolder;

    public YAML(File dataFolder) {
        this.regionsFolder = new File(dataFolder, "regions");
        if (!regionsFolder.exists()) {
            regionsFolder.mkdirs();
        }

        this.warsFolder = new File(dataFolder, "wars");
        if (!warsFolder.exists()) {
            warsFolder.mkdirs();
        }

        Logger.info("New database connection established, path: " + regionsFolder.getPath());
    }

    public void importRegions() {
        File[] regionFiles = regionsFolder
                .listFiles((dir, name) -> name.startsWith("region_") && name.endsWith(".yml"));

        if (regionFiles == null || regionFiles.length == 0) {
            Logger.info("No region files found to import.");
            return;
        }

        Homestead.regionsCache.clear();
        int importedCount = 0;

        for (File file : regionFiles) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                UUID id = UUID.fromString(config.getString("id"));
                String displayName = config.getString("displayName");
                String name = config.getString("name");
                String description = config.getString("description");
                OfflinePlayer owner = Homestead.getInstance()
                        .getOfflinePlayerSync(UUID.fromString(config.getString("ownerId")));
                SerializableLocation location = SerializableLocation.fromString(config.getString("location"));
                long createdAt = config.getLong("createdAt");
                long playerFlags = config.getLong("playerFlags");
                long worldFlags = config.getLong("worldFlags");
                double bank = config.getDouble("bank");
                int mapColor = config.getInt("mapColor");

                List<SerializableChunk> chunks = config.getStringList("chunks").stream()
                        .map(SerializableChunk::fromString)
                        .collect(Collectors.toList());

                List<SerializableMember> members = config.getStringList("members").stream()
                        .map(SerializableMember::fromString)
                        .collect(Collectors.toList());

                List<SerializableRate> rates = config.getStringList("rates").stream()
                        .map(SerializableRate::fromString)
                        .collect(Collectors.toList());

                List<OfflinePlayer> invitedPlayers = config.getStringList("invitedPlayers").stream()
                        .map(uuidString -> Homestead.getInstance().getOfflinePlayerSync(UUID.fromString(uuidString)))
                        .collect(Collectors.toList());

                List<SerializableBannedPlayer> bannedPlayers = config.getStringList("bannedPlayers").stream()
                        .map(SerializableBannedPlayer::fromString)
                        .collect(Collectors.toList());

                List<SerializableLog> logs = config.getStringList("logs").stream()
                        .map(SerializableLog::fromString)
                        .collect(Collectors.toList());

                List<SerializableSubArea> subAreas = config.getStringList("subAreas").stream()
                        .map(SerializableSubArea::fromString)
                        .collect(Collectors.toList());

                SerializableRent rent = config.getString("rent") != null
                        ? SerializableRent.fromString(config.getString("rent"))
                        : null;
                long upkeepAt = config.getLong("upkeepAt");
                double taxesAmount = config.getDouble("taxesAmount");
                int weather = config.getInt("weather");
                int time = config.getInt("time");
                SerializableLocation welcomeSign = config.getString("welcomeSign") != null
                        ? SerializableLocation.fromString(config.getString("welcomeSign"))
                        : null;
                String icon = config.getString("icon") == null ? null : config.getString("icon");

                if (owner == null) {
                    continue;
                }

                Region region = new Region(name, owner);
                region.id = id;
                region.displayName = displayName;
                region.description = description;
                region.location = location;
                region.createdAt = createdAt;
                region.playerFlags = playerFlags;
                region.worldFlags = worldFlags;
                region.bank = bank;
                region.mapColor = mapColor;
                region.setChunks(chunks);
                region.setMembers(members);
                region.setRates(rates);
                region.setInvitedPlayers(ListUtils.removeNullElements(invitedPlayers));
                region.setBannedPlayers(bannedPlayers);
                region.setLogs(logs);
                region.setSubAreas(subAreas);
                region.rent = rent;
                region.upkeepAt = upkeepAt;
                region.taxesAmount = taxesAmount;
                region.weather = weather;
                region.time = time;
                region.welcomeSign = welcomeSign;
                region.icon = icon;

                Homestead.regionsCache.putOrUpdate(region);
                importedCount++;
            } catch (Exception e) {
                Logger.error("Error loading region from file: " + file.getName());
                e.printStackTrace();
            }
        }

        Logger.info("Imported " + importedCount + " regions.");
    }

    public void importWars() {
        File[] warFiles = warsFolder
                .listFiles((dir, name) -> name.startsWith("war_") && name.endsWith(".yml"));

        if (warFiles == null || warFiles.length == 0) {
            Logger.info("No war files found to import.");
            return;
        }

        Homestead.warsCache.clear();
        int importedCount = 0;

        for (File file : warFiles) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                UUID id = UUID.fromString(config.getString("id"));
                String displayName = config.getString("displayName");
                String name = config.getString("name");
                String description = config.getString("description");
                List<UUID> regions = config.getStringList("regions").stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList());
                double prize = config.getDouble("prize");
                long startedAt = config.getLong("startedAt");

                War war = new War(name, regions);
                war.id = id;
                war.displayName = displayName;
                war.description = description;
                war.prize = prize;
                war.startedAt = startedAt;

                Homestead.warsCache.putOrUpdate(war);
                importedCount++;
            } catch (Exception e) {
                Logger.error("Error loading war from file: " + file.getName());
                e.printStackTrace();
            }
        }

        Logger.info("Imported " + importedCount + " wars.");
    }

    public void exportRegions() {
        int savedCount = 0;
        int deletedCount = 0;

        Set<UUID> existingFiles = new HashSet<>();
        File[] regionFiles = regionsFolder
                .listFiles((dir, name) -> name.startsWith("region_") && name.endsWith(".yml"));
        if (regionFiles != null) {
            for (File file : regionFiles) {
                try {
                    String uuidStr = file.getName().replace("region_", "").replace(".yml", "");
                    existingFiles.add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException e) {
                }
            }
        }

        Set<UUID> cacheRegionIds = new HashSet<>();
        for (Region region : Homestead.regionsCache.getAll()) {
            try {
                UUID regionId = region.id;
                cacheRegionIds.add(regionId);

                File regionFile = new File(regionsFolder, "region_" + regionId.toString() + ".yml");
                YamlConfiguration config = new YamlConfiguration();

                // Set all region properties
                config.set("id", regionId.toString());
                config.set("displayName", region.displayName);
                config.set("name", region.name);
                config.set("description", region.description);
                config.set("ownerId", region.getOwnerId().toString());
                config.set("location", region.location != null ? region.location.toString() : null);
                config.set("createdAt", region.createdAt);
                config.set("playerFlags", region.playerFlags);
                config.set("worldFlags", region.worldFlags);
                config.set("bank", region.bank);
                config.set("mapColor", region.mapColor);

                config.set("chunks", region.chunks.stream()
                        .map(SerializableChunk::toString)
                        .collect(Collectors.toList()));

                config.set("members", region.members.stream()
                        .map(SerializableMember::toString)
                        .collect(Collectors.toList()));

                config.set("rates", region.rates.stream()
                        .map(SerializableRate::toString)
                        .collect(Collectors.toList()));

                config.set("invitedPlayers", region.getInvitedPlayers().stream()
                        .map(OfflinePlayer::getUniqueId)
                        .map(UUID::toString)
                        .collect(Collectors.toList()));

                config.set("bannedPlayers", region.bannedPlayers.stream()
                        .map(SerializableBannedPlayer::toString)
                        .collect(Collectors.toList()));

                config.set("logs", region.logs.stream()
                        .map(SerializableLog::toString)
                        .collect(Collectors.toList()));

                config.set("subAreas", region.subAreas.stream()
                        .map(SerializableSubArea::toString)
                        .collect(Collectors.toList()));

                config.set("rent", region.rent != null ? region.rent.toString() : null);
                config.set("upkeepAt", region.upkeepAt);
                config.set("taxesAmount", region.taxesAmount);
                config.set("weather", region.weather);
                config.set("time", region.time);
                config.set("welcomeSign", region.welcomeSign != null ? region.welcomeSign.toString() : null);
                config.set("welcomeSign", region.icon != null ? region.icon.toString() : null);

                config.save(regionFile);
                savedCount++;
            } catch (IOException e) {
                Logger.error("Error saving region: " + region.id);
                e.printStackTrace();
            }
        }

        existingFiles.removeAll(cacheRegionIds);
        for (UUID deletedId : existingFiles) {
            File toDelete = new File(regionsFolder, "region_" + deletedId.toString() + ".yml");
            if (toDelete.delete()) {
                deletedCount++;
            } else {
                Logger.warning("Failed to delete region file: " + toDelete.getName());
            }
        }

        if (Homestead.config.isDebugEnabled()) {
            Logger.info(
                    "Exported " + savedCount + " regions and deleted " + deletedCount + " regions.");
        }
    }

    public void exportWars() {
        int savedCount = 0;
        int deletedCount = 0;

        Set<UUID> existingFiles = new HashSet<>();
        File[] warFiles = warsFolder
                .listFiles((dir, name) -> name.startsWith("war_") && name.endsWith(".yml"));
        if (warFiles != null) {
            for (File file : warFiles) {
                try {
                    String uuidStr = file.getName().replace("war_", "").replace(".yml", "");
                    existingFiles.add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException e) {
                }
            }
        }

        Set<UUID> cacheWarIds = new HashSet<>();
        for (War war : Homestead.warsCache.getAll()) {
            try {
                UUID warId = war.id;
                cacheWarIds.add(warId);

                File warFile = new File(warsFolder, "war_" + warId.toString() + ".yml");
                YamlConfiguration config = new YamlConfiguration();

                // Set all war properties
                config.set("id", warId.toString());
                config.set("displayName", war.displayName);
                config.set("name", war.name);
                config.set("description", war.description);
                config.set("regions", war.regions.stream()
                        .map(UUID::toString)
                        .collect(Collectors.toList()));
                config.set("prize", war.prize);
                config.set("startedAt", war.startedAt);

                config.save(warFile);
                savedCount++;
            } catch (IOException e) {
                Logger.error("Error saving war: " + war.id);
                e.printStackTrace();
            }
        }

        existingFiles.removeAll(cacheWarIds);
        for (UUID deletedId : existingFiles) {
            File toDelete = new File(warsFolder, "war_" + deletedId.toString() + ".yml");
            if (toDelete.delete()) {
                deletedCount++;
            } else {
                Logger.warning("Failed to delete war file: " + toDelete.getName());
            }
        }

        if (Homestead.config.isDebugEnabled()) {
            Logger.info(
                    "Exported " + savedCount + " wars and deleted " + deletedCount + " wars.");
        }
    }

    public void closeConnection() {
        Logger.warning("Connection for YAML has been closed.");
    }

    public long getLatency() {
        long before = System.currentTimeMillis();

        File[] regionFiles = regionsFolder
                .listFiles((dir, name) -> name.startsWith("region_") && name.endsWith(".yml"));

        if (regionFiles == null || regionFiles.length == 0) {
            return 0L;
        }

        for (@SuppressWarnings("unused")
        File file : regionFiles) {

        }

        long after = System.currentTimeMillis();

        return after - before;
    }
}
