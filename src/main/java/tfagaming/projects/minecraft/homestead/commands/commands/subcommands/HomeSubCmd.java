package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

public class HomeSubCmd extends SubCommandBuilder {
    public HomeSubCmd() {
        super("home");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            PlayerUtils.sendMessage(player, 4);
            return true;
        }

        if (region.getLocation() == null) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("{region}", region.getName());

            PlayerUtils.sendMessage(player, 71, replacements);
            return true;
        }

        if (!PlayerUtils.isOperator(player)
                && !player.getUniqueId().equals(region.getOwnerId())
                && !(PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TELEPORT_SPAWN)
                        && PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH))) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{region}", region.getName());

            PlayerUtils.sendMessage(player, 45, replacements);
            return true;
        }

        new DelayedTeleport(player, region.getLocation().getBukkitLocation());

        return true;
    }
}
