package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class UntrustPlayerSubCmd extends SubCommandBuilder {
    public UntrustPlayerSubCmd() {
        super("untrust");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("homestead.region.players.untrust")) {
            PlayerUtils.sendMessage(player, 8);
            return true;
        }

        if (args.length < 2) {
            PlayerUtils.sendMessage(player, 0);
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            PlayerUtils.sendMessage(player, 4);
            return true;
        }

        if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                RegionControlFlags.UNTRUST_PLAYERS)) {
            return true;
        }

        String targetName = args[1];

        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

        if (target == null) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{playername}", targetName);

            PlayerUtils.sendMessage(player, 29, replacements);
            return true;
        }

        if (region.isPlayerInvited(target)) {
            region.removePlayerInvite(target);

            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{playername}", target.getName());

            PlayerUtils.sendMessage(player, 37, replacements);
        } else if (region.isPlayerMember(target)) {
            region.removeMember(target);

            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{player}", target.getName());
            replacements.put("{region}", region.getName());

            PlayerUtils.sendMessage(player, 38, replacements);

            RegionsManager.addNewLog(region.getUniqueId(), 3, replacements);
        } else {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{playername}", target.getName());
            replacements.put("{region}", region.getName());

            PlayerUtils.sendMessage(player, 39, replacements);
        }

        return true;
    }
}
