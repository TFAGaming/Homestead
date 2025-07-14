package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.gui.Menu;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;

public class ManagePlayersMenu {
    public ManagePlayersMenu(Player player, Region region) {
        Menu gui = new Menu(MenuUtils.getTitle(4), 9 * 3);

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("{region-members}", String.valueOf(region.getMembers().size()));
        replacements.put("{region-banned-players}", String.valueOf(region.getBannedPlayers().size()));
        replacements.put("{region-invited-players}", String.valueOf(region.getInvitedPlayers().size()));

        ItemStack trustedMembersButton = MenuUtils.getButton(18, replacements);

        gui.addItem(11, trustedMembersButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new RegionMembersMenu(player, region);
        });

        ItemStack bannedPlayersButton = MenuUtils.getButton(19, replacements);

        gui.addItem(13, bannedPlayersButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new RegionBannedPlayersMenu(player, region);
        });

        ItemStack invitedPlayersButton = MenuUtils.getButton(20, replacements);

        gui.addItem(15, invitedPlayersButton, (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new RegionInvitedPlayersMenu(player, region);
        });

        gui.addItem(18, MenuUtils.getBackButton(), (_player, event) -> {
            if (!event.isLeftClick()) {
                return;
            }

            new RegionMenu(player, region);
        });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
