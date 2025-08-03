package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import org.bukkit.command.CommandSender;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.tools.https.UpdateChecker;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class CheckUpdatesSubCmd extends SubCommandBuilder {
    public CheckUpdatesSubCmd() {
        super("updates");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        PlayerUtils.sendMessage(sender, 98);

        Homestead.getInstance().runAsyncTask(() -> {
            new UpdateChecker(Homestead.getInstance());

            boolean foundUpdate = UpdateChecker.foundUpdate;

            if (foundUpdate) {
                PlayerUtils.sendMessage(sender, 97);
            } else {
                PlayerUtils.sendMessage(sender, 96);
            }
        });

        return true;
    }
}
