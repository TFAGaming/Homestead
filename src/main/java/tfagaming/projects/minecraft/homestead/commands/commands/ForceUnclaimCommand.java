package tfagaming.projects.minecraft.homestead.commands.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.particles.ChunkParticlesSpawner;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

/**
 * OP-only admin command to unclaim the chunk the executor is standing in,
 * regardless of region ownership and split protection.
 */
public class ForceUnclaimCommand extends CommandBuilder {

    /**
     * Creates a new force-unclaim command instance.
     */
    public ForceUnclaimCommand() {
        super("forceunclaim");
    }

    /**
     * Executes the command.
     *
     * @param sender the command sender
     * @param args   the command arguments
     * @return true if execution was handled
     */
    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            PlayerUtils.sendMessage(sender, 8);
            return false;
        }

        Player player = (Player) sender;

        if (!PlayerUtils.isOperator(player)) {
            PlayerUtils.sendMessage(sender, 8);
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();

        if (ChunksManager.isChunkInDisabledWorld(chunk)) {
            PlayerUtils.sendMessage(player, 20);
            return true;
        }

        Region owningRegion = ChunksManager.getRegionOwnsTheChunk(chunk);

        if (owningRegion == null) {
            PlayerUtils.sendMessage(player, 25);
            return true;
        }

        ChunksManager.forceUnclaimChunk(owningRegion.getUniqueId(), chunk, player);

        if (owningRegion.getLocation() != null
                && owningRegion.getLocation().getBukkitLocation() != null
                && owningRegion.getLocation().getBukkitLocation().getChunk().equals(chunk)) {
            owningRegion.setLocation(null);
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{region}", owningRegion.getName());
        PlayerUtils.sendMessage(player, 24, replacements);

        new ChunkParticlesSpawner(player);

        return true;
    }

    /**
     * Provides tab completion options.
     *
     * @param sender the command sender
     * @param args   the command arguments
     * @return a list of completion suggestions
     */
    @Override
    public List<String> onAutoComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
