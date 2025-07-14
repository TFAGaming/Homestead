package tfagaming.projects.minecraft.homestead.tools.other;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Region;

public class UpkeepUtils {
    public static long getNewUpkeepAt() {
        if ((boolean) Homestead.config.get("upkeep.enabled")) {
            return System.currentTimeMillis() + ((int) Homestead.config.get("upkeep.upkeep-timer") * 1000);
        }

        return 0;
    }

    public static double getAmountToPay(int chunks) {
        double amountPerChunk = Homestead.config.get("upkeep.per-chunk");

        return amountPerChunk * chunks;
    }

    public static int getChunksToRemove(Region region) {
        if (region.getBank() >= getAmountToPay(region.getChunks().size())) {
            return 0;
        }

        double amountPerChunk = Homestead.config.get("upkeep.per-chunk");
        double amountToPay = 0.0;
        int chunksToRemove = 1;

        for (int i = 0; i < region.getChunks().size(); i++) {
            if (amountToPay > region.getBank()) {
                chunksToRemove++;
            } else {
                amountToPay += amountPerChunk;
            }
        }

        return chunksToRemove;
    }
}
