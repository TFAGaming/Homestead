package tfagaming.projects.minecraft.homestead.integrations;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import org.bukkit.Bukkit;
import org.bukkit.World;

import tfagaming.projects.minecraft.homestead.Homestead;

public class WorldEditAPI {
    public static void regenerateChunk(World world, int x, int z) {
        Bukkit.getScheduler().runTaskAsynchronously(Homestead.getInstance(), () -> {
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
            CuboidRegion region = new CuboidRegion(BlockVector3.at(x << 4, weWorld.getMinY(), z << 4),
                    BlockVector3.at((x << 4) + 15, weWorld.getMaxY(), (z << 4) + 15));
            WorldEdit worldEdit = WorldEdit.getInstance();

            @SuppressWarnings("deprecation")
            EditSession editSession = worldEdit.getEditSessionFactory().getEditSession(weWorld, -1);
            weWorld.regenerate(region, editSession);

            editSession.close();
        });
    }
}
