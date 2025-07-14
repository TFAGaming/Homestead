package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RenameRegionSubCmd extends SubCommandBuilder {
    public RenameRegionSubCmd() {
        super("rename");
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

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            PlayerUtils.sendMessage(player, 4);
            return true;
        }

        if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                RegionControlFlags.RENAME_REGION)) {
            return true;
        }

        String regionName = args[1];

        if (!StringUtils.isValidRegionName(regionName)) {
            PlayerUtils.sendMessage(player, 1);
            return true;
        }

        if (regionName.equalsIgnoreCase(region.getName())) {
            PlayerUtils.sendMessage(player, 11);
            return true;
        }

        if (RegionsManager.isNameUsed(regionName)) {
            PlayerUtils.sendMessage(player, 2);
            return true;
        }

        final String oldName = region.getName();

        region.setName(regionName);

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("{oldname}", oldName);
        replacements.put("{newname}", regionName);

        PlayerUtils.sendMessage(player, 13, replacements);

        RegionsManager.addNewLog(region.getUniqueId(), 0, replacements);

        return true;
    }
}
