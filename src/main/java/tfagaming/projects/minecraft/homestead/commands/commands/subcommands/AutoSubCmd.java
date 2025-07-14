package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.sessions.autoclaim.AutoClaimSession;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class AutoSubCmd extends SubCommandBuilder {
    public AutoSubCmd() {
        super("auto");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (AutoClaimSession.hasSession(player)) {
            AutoClaimSession.removeSession(player);

            PlayerUtils.sendMessage(player, 136);
        } else {
            new AutoClaimSession(player);

            PlayerUtils.sendMessage(player, 135);
        }
        
        return true;
    }
}
