package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

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
        if (args.length < 2) {
            PlayerUtils.sendMessage(sender, 0);
            return true;
        }

        String provider = args[1];

        if (Database.parseProviderFromString(provider) == null) {
            PlayerUtils.sendMessage(sender, 84);
            return true;
        }

        String currentProvider = Homestead.database.getSelectedProvider();

        if (currentProvider.equalsIgnoreCase(provider)) {
            PlayerUtils.sendMessage(sender, 85);
            return true;
        }

        try {
            Database instance = new Database(Database.parseProviderFromString(provider), true);

            instance.exportRegions();

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{regions}", String.valueOf(Homestead.regionsCache.getAll().size()));
            replacements.put("{current-provider}", currentProvider);
            replacements.put("{selected-provider}", provider);

            PlayerUtils.sendMessage(sender, 86, replacements);

            instance.closeConnection();
        } catch (Exception e) {
            PlayerUtils.sendMessage(sender, 87);
        }

        return true;
    }
}
