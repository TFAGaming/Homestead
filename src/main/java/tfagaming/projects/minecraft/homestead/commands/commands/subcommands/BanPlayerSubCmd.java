package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class BanPlayerSubCmd extends SubCommandBuilder {
    public BanPlayerSubCmd() {
        super("ban");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("homestead.region.players.ban")) {
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
                RegionControlFlags.BAN_PLAYERS)) {
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

        if (region.isPlayerBanned(target)) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{playername}", target.getName());

            PlayerUtils.sendMessage(player, 32, replacements);
            return true;
        }

        if (target.getUniqueId().equals(region.getOwnerId())) {
            PlayerUtils.sendMessage(player, 30);
            return true;
        }

        String reason = Homestead.language.get("default.reason");

        if (args.length > 2) {
            List<String> reasonList = Arrays.asList(args).subList(2, args.length);
            reason = String.join(" ", reasonList);
        }

        region.banPlayer(target, reason);

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("{playername}", target.getName());
        replacements.put("{region}", region.getName());
        replacements.put("{reason}", reason);

        PlayerUtils.sendMessage(player, 31, replacements);

        if (region.isPlayerMember(target)) {
            region.removeMember(target);
        }

        if (region.isPlayerInvited(target)) {
            region.removePlayerInvite(target);
        }

        return true;
    }
}
