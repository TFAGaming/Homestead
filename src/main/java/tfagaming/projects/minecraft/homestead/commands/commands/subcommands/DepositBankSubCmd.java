package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class DepositBankSubCmd extends SubCommandBuilder {
    public DepositBankSubCmd() {
        super("deposit");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("homestead.region.bank")) {
            PlayerUtils.sendMessage(player, 8);
            return true;
        }

        if (args.length < 2) {
            PlayerUtils.sendMessage(player, 0);
            return true;
        }

        if (!Homestead.vault.isEconomyReady()) {
            PlayerUtils.sendMessage(player, 69);
            Logger.warning("The player \"" + player.getName() + "\" (UUID: " + player.getUniqueId()
                    + ") executed a command that requires economy implementation, but it's disabled.");
            Logger.warning(
                    "The execution has been ignored, you may resolve this issue by installing a plugin that implements economy on the server.");

            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            PlayerUtils.sendMessage(player, 4);
            return true;
        }

        if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                RegionControlFlags.DEPOSIT_MONEY)) {
            return true;
        }

        String amountInput = args[1];

        if ((!amountInput.equalsIgnoreCase("all") && !NumberUtils.isValidDouble(amountInput))
                || (NumberUtils.isValidDouble(amountInput) && Double.parseDouble(amountInput) > 2147483647)) {
            PlayerUtils.sendMessage(player, 64);
            return true;
        }

        double amount = amountInput.equalsIgnoreCase("all") ? PlayerUtils.getBalance(player)
                : Double.parseDouble(amountInput);

        if (amount <= 0) {
            PlayerUtils.sendMessage(player, 64);
            return true;
        }

        if (amount > PlayerUtils.getBalance(player)) {
            PlayerUtils.sendMessage(player, 65);
            return true;
        }

        PlayerUtils.removeBalance(player, amount);
        region.addBalanceToBank(amount);

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("{region}", region.getName());
        replacements.put("{amount}", Formatters.formatBalance(amount));

        PlayerUtils.sendMessage(player, 66, replacements);

        return true;
    }
}
