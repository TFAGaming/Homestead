package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.WarsManager;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class PluginSubCmd extends SubCommandBuilder {
    public PluginSubCmd() {
        super("plugin");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("{plugin-version}", Homestead.getVersion());
            replacements.put("{regions}", String.valueOf(RegionsManager.getAll().size()));
            replacements.put("{wars}", String.valueOf(WarsManager.getAll().size()));
            replacements.put("{provider}", Homestead.database.getSelectedProvider());
            replacements.put("{avg-response-db}", String.valueOf(Homestead.database.getLatency()));
            replacements.put("{avg-response-cache}", String.valueOf(Homestead.regionsCache.getLatency()));

            PlayerUtils.sendMessage(sender, 89, replacements);
        } else {
            sender.sendMessage("Please wait...");

            String[] headers = { "Property", "Value" };

            Object[][] data = {
                    { "Software", Bukkit.getName() },
                    { "Version", Bukkit.getVersion() },
                    { "Players", Bukkit.getOnlinePlayers().size() },
                    { "Homestead", "v" + Homestead.getVersion() },
                    { "Regions", RegionsManager.getAll().size() },
                    { "Database", Homestead.database.getSelectedProvider() },
                    { "Latency", Homestead.database.getLatency() },
                    { "Latency (cache)", Homestead.regionsCache.getLatency() }
            };

            ListUtils.printTable(headers, data);
        }

        return true;
    }
}
