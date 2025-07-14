package tfagaming.projects.minecraft.homestead.commands.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.particles.ChunkParticlesSpawner;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class UnclaimCommand extends CommandBuilder {
    public UnclaimCommand() {
        super("unclaim");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        Chunk chunk = player.getLocation().getChunk();

        if (ChunksManager.isChunkInDisabledWorld(chunk)) {
            PlayerUtils.sendMessage(player, 20);
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            PlayerUtils.sendMessage(player, 4);
            return true;
        }

        if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                RegionControlFlags.UNCLAIM_CHUNKS)) {
            return true;
        }

        Region regionOwnsThisChunk = ChunksManager.getRegionOwnsTheChunk(chunk);

        if (regionOwnsThisChunk == null) {
            PlayerUtils.sendMessage(player, 25);
            return true;
        }

        if (!regionOwnsThisChunk.getUniqueId().equals(region.getUniqueId())) {
            PlayerUtils.sendMessage(player, 23);
            return true;
        }

        ChunksManager.unclaimChunk(region.getUniqueId(), chunk, player);

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("{region}", region.getName());

        PlayerUtils.sendMessage(player, 24, replacements);

        if (region.getLocation() != null && region.getLocation().getBukkitLocation().getChunk().equals(chunk)) {
            region.setLocation(null);
        }

        new ChunkParticlesSpawner(player);

        return true;
    }

    @Override
    public List<String> onAutoComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
