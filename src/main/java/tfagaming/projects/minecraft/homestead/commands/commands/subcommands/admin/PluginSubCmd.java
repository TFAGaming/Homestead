package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class PluginSubCmd extends SubCommandBuilder {
    public PluginSubCmd() {
        super("plugin");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{plugin-version}", Homestead.getVersion());
        replacements.put("{regions}", String.valueOf(RegionsManager.getAll().size()));
        replacements.put("{provider}", Homestead.database.getSelectedProvider());
        replacements.put("{avg-response-db}", String.valueOf(Homestead.database.getLatency()));
        replacements.put("{avg-response-cache}", String.valueOf(Homestead.cache.getLatency()));

        PlayerUtils.sendMessage(player, 89, replacements);

        return true;
    }
}
