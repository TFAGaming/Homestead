package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.particles.ChunkParticlesSpawner;
import tfagaming.projects.minecraft.homestead.particles.SelectedAreaParticlesSpawner;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableSubArea;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RegionBordersSubCmd extends SubCommandBuilder {
    public RegionBordersSubCmd() {
        super("borders");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 2 && args[1].equalsIgnoreCase("stop")) {
            ChunkParticlesSpawner.cancelTask(player);
            SelectedAreaParticlesSpawner.cancelTask(player);

            PlayerUtils.sendMessage(player, 26);

            return true;
        }

        Region region = ChunksManager.getRegionOwnsTheChunk(player.getLocation().getChunk());

        if (region == null) {
            new ChunkParticlesSpawner(player);
        } else {
            SerializableSubArea subArea = region.findSubAreaHasLocationInside(player.getLocation());

            if (subArea != null) {
                new SelectedAreaParticlesSpawner(player, subArea.getFirstPoint(), subArea.getSecondPoint());
            } else {
                new ChunkParticlesSpawner(player);
            }
        }

        PlayerUtils.sendMessage(player, 27);

        return true;
    }
}
