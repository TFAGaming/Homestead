package tfagaming.projects.minecraft.homestead.commands.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin.*;
import tfagaming.projects.minecraft.homestead.tools.commands.AutoCompleteFilter;
import tfagaming.projects.minecraft.homestead.tools.java.StringSimilarity;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class HomesteadAdminCommand extends CommandBuilder {
    public HomesteadAdminCommand() {
        super("homesteadadmin", "hsadmin");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use this command via the console.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            PlayerUtils.sendMessage(player, 0);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (getSubcommands().contains(subCommand)) {
            if (!player.hasPermission("homestead.commands.homesteadadmin." + subCommand)) {
                PlayerUtils.sendMessage(player, 8);
                return true;
            }
        }

        switch (subCommand) {
            case "migratedata":
                new MigrateDataSubCmd().onExecution(sender, args);
                break;
            case "plugin":
                new PluginSubCmd().onExecution(sender, args);
                break;
            case "reload":
                new ReloadSubCmd().onExecution(sender, args);
                break;
            case "updates":
                new CheckUpdatesSubCmd().onExecution(sender, args);
                break;
            case "importdata":
                new ImportDataSubCmd().onExecution(sender, args);
                break;
            default:
                String similaritySubCmds = StringSimilarity.findTopSimilarStrings(getSubcommands(), subCommand).stream()
                        .collect(Collectors.joining(", "));

                if (sender instanceof Player) {
                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("{similarity-subcmds}", similaritySubCmds);

                    PlayerUtils.sendMessage(player, 7, replacements);
                } else {
                    sender.sendMessage("Unknown sub-command, maybe you meant...", similaritySubCmds);
                }
                break;
        }

        return true;
    }

    @Override
    public List<String> onAutoComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Lists.newArrayList();
        }

        Player player = (Player) sender;

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = getSubcommands().stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());

            for (String subcommand : subcommands) {
                if (player.hasPermission("homestead.commands.homesteadadmin." + subcommand)) {
                    suggestions.add(subcommand);
                }
            }

            return suggestions;
        }

        if (getSubcommands().contains(args[0].toLowerCase())) {
            if (!player.hasPermission("homestead.commands.homesteadadmin." + args[0].toLowerCase())) {
                return new ArrayList<>();
            }
        }

        switch (args[0].toLowerCase()) {
            case "migratedata": {
                if (args.length == 2)
                    suggestions.addAll(List.of("SQLite", "MySQL", "YAML", "PostgreSQL"));
                break;
            }
            case "importdata": {
                if (args.length == 2)
                    suggestions.addAll(List.of("GriefPrevention", "LandLord", "ClaimChunk"));
                break;
            }
        }

        return AutoCompleteFilter.filter(suggestions, args);
    }

    public List<String> getSubcommands() {
        return Lists.newArrayList("migratedata", "plugin", "reload", "updates", "importdata");
    }
}
