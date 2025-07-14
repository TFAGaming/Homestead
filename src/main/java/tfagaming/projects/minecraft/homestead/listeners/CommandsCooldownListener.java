package tfagaming.projects.minecraft.homestead.listeners;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class CommandsCooldownListener implements Listener {
    private static HashSet<UUID> cooldown = new HashSet<UUID>();

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        PluginCommand pluginCommand = Homestead.getInstance().getServer().getPluginCommand(command);

        if (pluginCommand != null && pluginCommand.getPlugin().equals(Homestead.getInstance())) {
            Player player = event.getPlayer();

            if (cooldown.contains(player.getUniqueId())) {
                event.setCancelled(true);

                PlayerUtils.sendMessage(player, 118);
            } else {
                int cooldownPlayer = PlayerLimits.getLimitValue(player, PlayerLimits.LimitType.COMMANDS_COOLDOWN);

                if (cooldownPlayer > 0) {
                    cooldown.add(player.getUniqueId());

                    Homestead.getInstance().runAsyncTaskLater(() -> {
                        cooldown.remove(player.getUniqueId());
                    }, cooldownPlayer);
                }
            }
        }
    }
}
