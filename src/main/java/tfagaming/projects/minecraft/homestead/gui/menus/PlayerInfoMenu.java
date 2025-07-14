package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.HashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class PlayerInfoMenu {
    public PlayerInfoMenu(Player player, OfflinePlayer target, Runnable backButton) {
        Menu gui = new Menu(MenuUtils.getTitle(4), 9 * 3);

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("{regions-count}", String.valueOf(RegionsManager.getRegionsOwnedByPlayer(target).size()
                + RegionsManager.getRegionsHasPlayerAsMember(target).size()));
        replacements.put("{playername}", target.getName());
        replacements.put("{player-status}", Formatters.getPlayerStatus(target));
        replacements.put("{player-balance}", Formatters.formatBalance(PlayerUtils.getBalance(target)));
        replacements.put("{player-ping}",
                String.valueOf(target.isOnline() ? ((Player) target).getPing() : 0));
        replacements.put("{player-joinedat}", Formatters.formatDate(target.getFirstPlayed()));
        replacements.put("{player-owned-regions}", Formatters.getPlayerOwnedRegions(target));
        replacements.put("{player-trusted-regions}", Formatters.getPlayerTrustedRegions(target));

        ItemStack playerInServerInfoButton = MenuUtils.getButton(21, replacements, target);

        gui.addItem(11, playerInServerInfoButton, (_player, event) -> {
            // Do nothing
        });

        ItemStack ownedRegionsButton = MenuUtils.getButton(22, replacements);

        gui.addItem(13, ownedRegionsButton, (_player, event) -> {
            // Do nothing
        });

        ItemStack trustedRegionsButton = MenuUtils.getButton(23, replacements);

        gui.addItem(15, trustedRegionsButton, (_player, event) -> {
            // Do nothing
        });

        gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            backButton.run();
        });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
