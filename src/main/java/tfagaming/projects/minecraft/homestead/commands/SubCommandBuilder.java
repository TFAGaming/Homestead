package tfagaming.projects.minecraft.homestead.commands;

import org.bukkit.command.*;

import tfagaming.projects.minecraft.homestead.Homestead;

public abstract class SubCommandBuilder implements CommandExecutor {
    private final String name;
    public final Homestead plugin = Homestead.getInstance();

    public SubCommandBuilder(String name) {
        this.name = name;
    }

    public abstract boolean onExecution(CommandSender sender, String[] args);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return onExecution(sender, args);
    }

    public String getName() {
        return name;
    }
}