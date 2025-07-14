package tfagaming.projects.minecraft.homestead.events;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.other.UpkeepUtils;

public class RegionUpkeep {
    public RegionUpkeep(Homestead instance) {
        for (Region region : RegionsManager.getAll()) {
            if (System.currentTimeMillis() > region.getUpkeepAt()) {
                double amountToPay = UpkeepUtils.getAmountToPay(region.getChunks().size());

                if (amountToPay > region.getBank()) {
                    int chunksToRemove = UpkeepUtils.getChunksToRemove(region);

                    if (chunksToRemove > 0) {
                        for (int i = 0; i < chunksToRemove; i++) {
                            ChunksManager.removeRandomChunk(region.getUniqueId());
                        }
                    }

                    if (region.getOwner().isOnline()) {
                        Player regionOwner = (Player) region.getOwner();

                        Map<String, String> replacements = new HashMap<String, String>();
                        replacements.put("{amount}", Formatters.formatBalance(amountToPay));
                        replacements.put("{region}", region.getName());
                        replacements.put("{chunks}", String.valueOf(chunksToRemove));

                        PlayerUtils.sendMessage(regionOwner, 111, replacements);
                        PlayerUtils.sendMessage(regionOwner, 112, replacements);
                    }
                } else {
                    region.removeBalanceFromBank(amountToPay);

                    region.setUpkeepAt(UpkeepUtils.getNewUpkeepAt());

                    if (region.getOwner().isOnline()) {
                        Player regionOwner = (Player) region.getOwner();

                        Map<String, String> replacements = new HashMap<String, String>();
                        replacements.put("{amount}", Formatters.formatBalance(amountToPay));
                        replacements.put("{region}", region.getName());
                        replacements.put("{bank}", Formatters.formatBalance(region.getBank()));

                        PlayerUtils.sendMessage(regionOwner, 109, replacements);
                        PlayerUtils.sendMessage(regionOwner, 110, replacements);
                    }
                }
            }
        }
    }
}
