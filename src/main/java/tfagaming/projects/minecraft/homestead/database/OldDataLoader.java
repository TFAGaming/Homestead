package tfagaming.projects.minecraft.homestead.database;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBannedPlayer;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLocation;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRate;

public class OldDataLoader {
    public static Region loadFromFile(UUID id) {
        File claimsFolder = new File(Homestead.getInstance().getDataFolder(), "claims");
        File file = new File(claimsFolder, "region_" + id.toString() + ".yml");

        if (!file.exists()) {
            Logger.error("Region file not found: region_" + id + ".yml");
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String name = config.getString("name");
        String description = config.getString("description");
        String ownerId = config.getString("ownerId");
        String location = config.getString("location");
        long createdAt = config.getLong("createdAt");
        long playerFlags = config.getLong("playerFlags");
        long worldFlags = config.getLong("worldFlags");
        double bank = config.getDouble("bank");
        int mapColor = config.getInt("mapColor");
        long taxAt = config.getLong("taxAt");
        int weather = config.getInt("clientSideWeather");
        int time = config.getInt("clientSideTime");
        String welcomeSign = config.getString("welcomeSign");

        OfflinePlayer owner = Homestead.getInstance().getOfflinePlayerSync(UUID.fromString(ownerId));

        if (owner == null) {
            return null;
        }

        Region region = new Region(name, owner);
        region.id = id;
        region.description = description;
        region.location = location.equals("~") ? null : SerializableLocation.fromString(location);
        region.createdAt = createdAt;
        region.playerFlags = playerFlags;
        region.worldFlags = worldFlags;
        region.bank = bank;
        region.mapColor = mapColor;
        region.upkeepAt = taxAt;
        region.weather = weather;
        region.time = time;
        region.welcomeSign = welcomeSign == null ? null : SerializableLocation.fromString(welcomeSign);

        List<String> chunks = config.getStringList("chunks");

        for (String chunkString : chunks) {
            region.chunks.add(SerializableChunk.fromString(chunkString));
        }

        List<String> members = config.getStringList("members");

        for (String memberString : members) {
            String[] splitted = memberString.split(",");

            region.members.add(new SerializableMember(UUID.fromString(splitted[0]), Long.parseLong(splitted[1]), 0,
                    Long.parseLong(splitted[2]), 0));
        }

        List<String> rates = config.getStringList("rates");

        for (String rateString : rates) {
            region.rates.add(SerializableRate.fromString(rateString));
        }

        List<String> invitedPlayers = config.getStringList("invitedPlayers");

        for (String playerUuidString : invitedPlayers) {
            region.invitedPlayers.add(UUID.fromString(playerUuidString));
        }

        List<String> bannedPlayers = config.getStringList("bannedPlayers");

        for (String playerUuidString : bannedPlayers) {
            OfflinePlayer bannedPlayer = Homestead.getInstance()
                    .getOfflinePlayerSync(UUID.fromString(playerUuidString));

            if (bannedPlayer != null) {
                region.bannedPlayers.add(new SerializableBannedPlayer(bannedPlayer));
            }
        }

        return region;
    }

    public static int loadRegions() {
        File claimsFolder = new File(Homestead.getInstance().getDataFolder(), "claims");
        File[] regionFiles = claimsFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (regionFiles == null) {
            return 0;
        }

        for (File file : regionFiles) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                UUID id = UUID.fromString(config.getString("id"));

                Region region = loadFromFile(id);

                if (region != null) {
                    Logger.warning("Loaded region (ID: " + region.getUniqueId().toString() + "), name = "
                            + region.getName() + ", owner = " + region.getOwner().getName());

                    Homestead.regionsCache.putOrUpdate(region);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Homestead.regionsCache.size();
    }

    public static boolean deleteDirectory(File file) {
        for (File subfile : file.listFiles()) {
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }

            subfile.delete();
        }

        return file.delete();
    }
}
