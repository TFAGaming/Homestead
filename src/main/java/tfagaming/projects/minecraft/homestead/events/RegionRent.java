package tfagaming.projects.minecraft.homestead.events;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RegionRent {
    public RegionRent(Homestead instance) {
        for (Region region : RegionsManager.getAll()) {
            if (region.getRent() != null && System.currentTimeMillis() > region.getRent().getUntilAt()) {
                region.setRent(null);

                OfflinePlayer leaser = region.getRent().getBukkitOfflinePlayer();

                if (leaser.isOnline()) {
                    Player player = (Player) leaser;

                    Map<String, String> replacements = new HashMap<String, String>();
                    replacements.put("{region}", region.getName());

                    PlayerUtils.sendMessage(player, 130, replacements);
                }
            }
        }
    }
}
