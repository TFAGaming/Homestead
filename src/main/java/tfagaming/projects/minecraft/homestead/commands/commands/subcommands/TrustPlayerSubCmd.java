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
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class TrustPlayerSubCmd extends SubCommandBuilder {
    public TrustPlayerSubCmd() {
        super("trust");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("homestead.region.players.trust")) {
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

        String targetName = args[1];

        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

        if (target == null) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{playername}", targetName);

            PlayerUtils.sendMessage(player, 29, replacements);
            return true;
        }

        if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                RegionControlFlags.TRUST_PLAYERS)) {
            return true;
        }

        if (region.isPlayerBanned(target)) {
            PlayerUtils.sendMessage(player, 74);
            return true;
        }

        if (region.isPlayerMember(target)) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{playername}", target.getName());

            PlayerUtils.sendMessage(player, 48, replacements);
            return true;
        }

        if (region.isPlayerInvited(target)) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{playername}", target.getName());

            PlayerUtils.sendMessage(player, 35, replacements);
            return true;
        }

        if (target.getUniqueId().equals(region.getOwnerId())) {
            PlayerUtils.sendMessage(player, 30);
            return true;
        }

        if (PlayerLimits.hasReachedLimit(region.getOwner(), PlayerLimits.LimitType.MEMBERS_PER_REGION)) {
            PlayerUtils.sendMessage(player, 116);
            return true;
        }

        region.addPlayerInvite(target);

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("{playername}", target.getName());
        replacements.put("{region}", region.getName());
        replacements.put("{ownername}", region.getOwner().getName());

        PlayerUtils.sendMessage(player, 36, replacements);

        if (target.isOnline()) {
            PlayerUtils.sendMessage(target.getPlayer(), 139, replacements);
        }

        RegionsManager.addNewLog(region.getUniqueId(), 2, replacements);

        return true;
    }
}
