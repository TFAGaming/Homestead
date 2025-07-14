package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class DenyInviteSubCmd extends SubCommandBuilder {
    public DenyInviteSubCmd() {
        super("deny");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            PlayerUtils.sendMessage(player, 0);
            return true;
        }

        String regionName = args[1];

        Region region = RegionsManager.findRegion(regionName);

        if (region == null) {
            PlayerUtils.sendMessage(player, 9);
            return false;
        }

        if (!region.isPlayerInvited(player)) {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{region}", region.getName());

            PlayerUtils.sendMessage(player, 45, replacements);
            return true;
        }

        region.removePlayerInvite(player);

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("{region}", region.getName());

        PlayerUtils.sendMessage(player, 47, replacements);

        return true;
    }
}
