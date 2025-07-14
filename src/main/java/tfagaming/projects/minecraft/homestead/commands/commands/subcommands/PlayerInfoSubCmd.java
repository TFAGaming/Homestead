package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.PlayerInfoMenu;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class PlayerInfoSubCmd extends SubCommandBuilder {
    public PlayerInfoSubCmd() {
        super("player");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            new PlayerInfoMenu(player, player, () -> {
                player.closeInventory();
            });
        } else {
            String playerName = args[1];

            OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

            if (target == null) {
                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{playername}", playerName);

                PlayerUtils.sendMessage(player, 29, replacements);
                return true;
            }

            new PlayerInfoMenu(player, target, () -> {
                player.closeInventory();
            });
        }

        return true;
    }
}
