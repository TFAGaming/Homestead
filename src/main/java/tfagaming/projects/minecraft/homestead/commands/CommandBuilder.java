package tfagaming.projects.minecraft.homestead.commands;

import org.bukkit.command.*;

import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.*;

public abstract class CommandBuilder implements CommandExecutor, TabCompleter {
    private final String name;
    private String[] aliases = {};

    public final Homestead plugin = Homestead.getInstance();

    public CommandBuilder(String name) {
        this.name = name;
    }

    public CommandBuilder(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public abstract boolean onExecution(CommandSender sender, String[] args);

    public abstract List<String> onAutoComplete(CommandSender sender, String[] args);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return onExecution(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        return onAutoComplete(sender, args);
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public static void register(CommandBuilder command) {
        PluginCommand bukkitCommand = Homestead.getInstance().getCommand(command.getName());

        if (bukkitCommand != null) {
            bukkitCommand.setExecutor(command);
            bukkitCommand.setTabCompleter(command);
        }

        for (String alias : command.getAliases()) {
            PluginCommand bukkitCommandAlias = Homestead.getInstance().getCommand(alias);

            if (bukkitCommandAlias != null) {
                bukkitCommandAlias.setExecutor(command);
                bukkitCommandAlias.setTabCompleter(command);
            }
        }
    }
}