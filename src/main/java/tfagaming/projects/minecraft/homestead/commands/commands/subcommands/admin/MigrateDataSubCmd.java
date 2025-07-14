package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.database.Database;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class MigrateDataSubCmd extends SubCommandBuilder {
    public MigrateDataSubCmd() {
        super("migratedata");
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

        String provider = args[1];

        if (Database.parseProviderFromString(provider) == null) {
            PlayerUtils.sendMessage(player, 84);
            return true;
        }

        String currentProvider = Homestead.database.getSelectedProvider();

        if (currentProvider.equalsIgnoreCase(provider)) {
            PlayerUtils.sendMessage(player, 85);
            return true;
        }

        try {
            Database instance = new Database(Database.parseProviderFromString(provider), true);

            instance.exportRegions();

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{regions}", String.valueOf(Homestead.cache.getAll().size()));
            replacements.put("{current-provider}", currentProvider);
            replacements.put("{selected-provider}", provider);

            PlayerUtils.sendMessage(player, 86, replacements);

            instance.closeConnection();
        } catch (Exception e) {
            PlayerUtils.sendMessage(player, 87);
        }

        return true;
    }
}
