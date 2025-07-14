package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class DeleteRegionSubCmd extends SubCommandBuilder {
    public DeleteRegionSubCmd() {
        super("delete");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("homestead.region.delete")) {
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

        String confirmInput = args[1];

        if (!confirmInput.equalsIgnoreCase("confirm")) {
            PlayerUtils.sendMessage(player, 5);
            return true;
        }

        double amountToGive = region.getBank();

        RegionsManager.deleteRegion(region.getUniqueId(), player);

        if (Homestead.vault.isEconomyReady()) {
            PlayerUtils.addBalance(region.getOwner(), amountToGive);
        }

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("{region}", region.getDisplayName());
        replacements.put("{region-bank}", Formatters.formatBalance(amountToGive));

        PlayerUtils.sendMessage(player, 6, replacements);

        TargetRegionSession.randomizeRegion(player);

        return true;
    }
}
