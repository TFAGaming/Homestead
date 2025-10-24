package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import java.util.*;

import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.ChunkCoordinate;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;

import biz.princeps.landlord.api.ILandLord;
import biz.princeps.landlord.api.IOwnedLand;
import me.ryanhamshire.GriefPrevention.*;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class ImportDataSubCmd extends SubCommandBuilder {
    public ImportDataSubCmd() {
        super("importdata");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (args.length < 2) {
            PlayerUtils.sendMessage(sender, 0);
            return true;
        }

        String pluginInput = args[1];

        switch (pluginInput.toLowerCase()) {
            case "griefprevention": {
                if (!isGriefPreventionInstalled()) {
                    PlayerUtils.sendMessage(sender, 114);
                    return true;
                }

                int imported = 0;

                Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims();

                for (Claim claim : claims) {
                    OfflinePlayer owner = Homestead.getInstance().getOfflinePlayerSync(claim.getOwnerID());

                    if (owner == null) {
                        continue;
                    }

                    Region region = RegionsManager.createRegion(owner.getName(),
                            Bukkit.getOfflinePlayer(claim.getOwnerID()),
                            true);

                    for (Chunk chunk : claim.getChunks()) {
                        if (!ChunksManager.isChunkClaimed(chunk)) {
                            ChunksManager.claimChunk(region.getUniqueId(), chunk);
                        }
                    }

                    Logger.info("Imported a region; Name = " + region.getName() + ", ID = "
                            + region.getUniqueId().toString() + ", Owner = " + owner.getName() + " ("
                            + owner.getUniqueId().toString() + ")");

                    imported++;
                }

                Map<String, String> replacements = new HashMap<>();
                replacements.put("{regions}", String.valueOf(imported));

                PlayerUtils.sendMessage(sender, 115, replacements);

                break;
            }
            case "landlord": {
                if (!isLandLordInstalled()) {
                    PlayerUtils.sendMessage(sender, 114);
                    return true;
                }

                int imported = 0;

                ILandLord landlord = getLandLordInstance();

                Set<IOwnedLand> chunks = landlord.getWGManager().getRegions();

                for (IOwnedLand chunk : chunks) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(chunk.getOwner());

                    if (RegionsManager.getRegionsOwnedByPlayer(owner).size() == 0) {
                        Region region = RegionsManager.createRegion(owner.getName(),
                                owner, true);

                        if (!ChunksManager.isChunkClaimed(chunk.getChunk())) {
                            ChunksManager.claimChunk(region.getUniqueId(), chunk.getChunk());
                        }

                        for (UUID friendUuid : chunk.getFriends()) {
                            OfflinePlayer friend = Homestead.getInstance().getOfflinePlayerSync(friendUuid);

                            if (friend == null) {
                                continue;
                            }

                            if (!region.isPlayerMember(friend)) {
                                region.addMember(friend);

                                region.setMemberFlags(region.getMember(friend), region.getPlayerFlags());
                            }
                        }

                        Logger.info("Imported a region; Name = " + region.getName() + ", ID = "
                                + region.getUniqueId().toString() + ", Owner = " + owner.getName() + " ("
                                + owner.getUniqueId().toString() + ")");

                        imported++;
                    } else {
                        Region region = RegionsManager.getRegionsOwnedByPlayer(owner).get(0);

                        for (UUID friendUuid : chunk.getFriends()) {
                            OfflinePlayer friend = Homestead.getInstance().getOfflinePlayerSync(friendUuid);

                            if (friend == null) {
                                continue;
                            }

                            if (!region.isPlayerMember(friend)) {
                                region.addMember(friend);

                                region.setMemberFlags(region.getMember(friend), region.getPlayerFlags());
                            }
                        }

                        if (!ChunksManager.isChunkClaimed(chunk.getChunk())) {
                            ChunksManager.claimChunk(region.getUniqueId(), chunk.getChunk());
                        }
                    }
                }

                Map<String, String> replacements = new HashMap<>();
                replacements.put("{regions}", String.valueOf(imported));

                PlayerUtils.sendMessage(sender, 115, replacements);

                break;
            }
            case "claimchunk": {
                if (!isClaimChunkInstalled()) {
                    PlayerUtils.sendMessage(sender, 114);
                    return true;
                }

                int imported = 0;

                ClaimChunk claimChunk = ClaimChunk.getInstance();

                for (OfflinePlayer offlinePlayer : Homestead.getInstance().getOfflinePlayersSync()) {
                    if (offlinePlayer.getName() == null) {
                        continue;
                    }

                    ChunkPos[] chunkPositions = claimChunk.getChunkHandler()
                            .getClaimedChunks(offlinePlayer.getUniqueId());

                    Region region = RegionsManager.createRegion(offlinePlayer.getName(), offlinePlayer, true);

                    for (ChunkPos chunkPos : chunkPositions) {
                        Chunk chunk = ChunksManager.getFromLocation(Bukkit.getWorld(chunkPos.world()), chunkPos.x(),
                                chunkPos.z());

                        if (!ChunksManager.isChunkClaimed(chunk)) {
                            ChunksManager.claimChunk(region.getUniqueId(), chunk);
                        }
                    }

                    Logger.info("Imported a region; Name = " + region.getName() + ", ID = "
                            + region.getUniqueId().toString() + ", Owner = " + offlinePlayer.getName() + " ("
                            + offlinePlayer.getUniqueId().toString() + ")");

                    imported++;
                }

                Map<String, String> replacements = new HashMap<>();
                replacements.put("{regions}", String.valueOf(imported));

                PlayerUtils.sendMessage(sender, 115, replacements);

                break;
            }
            case "lands": {
                if (!isLandsInstalled()) {
                    PlayerUtils.sendMessage(sender, 114);
                    return true;
                }

                int imported = 0;

                LandsIntegration landsApi = getLandsInstance();

                for (Land land : landsApi.getLands()) {
                    OfflinePlayer owner = Homestead.getInstance().getOfflinePlayerSync(land.getOwnerUID());

                    if (owner == null) {
                        continue;
                    }

                    Region region = RegionsManager.createRegion(owner.getName(), owner, true);

                    for (World world : Bukkit.getWorlds()) {
                        for (ChunkCoordinate chunkCoord : Objects.requireNonNull(land.getChunks(world))) {
                            Chunk chunk = ChunksManager.getFromLocation(world, chunkCoord.getX(), chunkCoord.getZ());

                            if (!ChunksManager.isChunkClaimed(chunk)) {
                                ChunksManager.claimChunk(region.getUniqueId(), chunk);
                            }
                        }
                    }

                    Logger.info("Imported a region; Name = " + region.getName() + ", ID = "
                            + region.getUniqueId().toString() + ", Owner = " + owner.getName() + " ("
                            + owner.getUniqueId().toString() + ")");

                    imported++;
                }

                Map<String, String> replacements = new HashMap<>();
                replacements.put("{regions}", String.valueOf(imported));

                PlayerUtils.sendMessage(sender, 115, replacements);

                break;
            }
            default:
                PlayerUtils.sendMessage(sender, 113);
                break;
        }

        return true;
    }

    public static boolean isGriefPreventionInstalled() {
        try {
            GriefPrevention.instance.getClass();

            return Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention") != null
                    && Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention").isEnabled();
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public static boolean isLandLordInstalled() {
        try {
            return Bukkit.getServer().getPluginManager().getPlugin("LandLord") != null
                    && Bukkit.getServer().getPluginManager().getPlugin("LandLord").isEnabled();
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public static boolean isClaimChunkInstalled() {
        try {
            ClaimChunk.getInstance();

            return Bukkit.getServer().getPluginManager().getPlugin("ClaimChunk") != null
                    && Bukkit.getServer().getPluginManager().getPlugin("ClaimChunk").isEnabled();
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public static boolean isLandsInstalled() {
        try {
            return Bukkit.getServer().getPluginManager().getPlugin("Lands") != null
                    && Bukkit.getServer().getPluginManager().getPlugin("Lands").isEnabled();
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }


    public ILandLord getLandLordInstance() {
        return (ILandLord) Bukkit.getServer().getPluginManager().getPlugin("Landlord");
    }

    public LandsIntegration getLandsInstance() {
        return new LandsIntegration(Homestead.getInstance());
    }
}
