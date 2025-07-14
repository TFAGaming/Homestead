package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.particles.ChunkParticlesSpawner;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils.ButtonData;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

public class RegionClaimedChunksMenu {
    List<SerializableChunk> chunks;

    public List<ItemStack> getItems(Player player, Region region) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            SerializableChunk chunk = chunks.get(i);

            HashMap<String, String> replacements = new HashMap<>();

            replacements.put("{region}", region.getName());
            replacements.put("{index}", String.valueOf(i + 1));
            replacements.put("{chunk-claimedat}", Formatters.formatDate(chunk.getClaimedAt()));
            replacements.put("{chunk-location}", Formatters.formatLocation(chunk.getBukkitLocation()));

            ButtonData data = MenuUtils.getButtonData(33);

            if (data.getOriginalType().equals("CUSTOM::GETBYWORLD")) {
                switch (chunk.getBukkitLocation().getWorld().getEnvironment()) {
                    case NORMAL:
                        data.originalType = Homestead.menusConfig.get("button-types.world.overworld");
                        break;
                    case NETHER:
                        data.originalType = Homestead.menusConfig.get("button-types.world.nether");
                        break;
                    case THE_END:
                        data.originalType = Homestead.menusConfig.get("button-types.world.the_end");
                        break;
                    default:
                        data.originalType = Homestead.menusConfig.get("button-types.world.overworld");
                        break;
                }
            }

            items.add(MenuUtils.getButton(data, replacements));
        }

        return items;
    }

    public RegionClaimedChunksMenu(Player player, Region region) {
        chunks = region.getChunks();

        PaginationMenu gui = new PaginationMenu(MenuUtils.getTitle(11), 9 * 5,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(), getItems(player, region), (_player, event) -> {
                    new RegionMenu(player, region);
                }, (_player, context) -> {
                    if (context.getIndex() >= chunks.size()) {
                        return;
                    }

                    SerializableChunk chunk = chunks.get(context.getIndex());

                    if (context.getEvent().isRightClick()) {
                        new DelayedTeleport(player, chunk.getBukkitLocation());
                    } else if (context.getEvent().isLeftClick()) {
                        if (ChunksManager.isChunkClaimed(chunk.getBukkitChunk())
                                && ChunksManager.getRegionOwnsTheChunk(chunk.getBukkitChunk()).getUniqueId()
                                        .equals(region.getUniqueId())) {
                            ChunksManager.unclaimChunk(region.getUniqueId(), chunk.getBukkitChunk(), player);

                            Map<String, String> replacements = new HashMap<String, String>();
                            replacements.put("{region}", region.getName());

                            PlayerUtils.sendMessage(player, 24, replacements);

                            if (region.getLocation() != null
                                    && region.getLocation().getBukkitLocation().getChunk()
                                            .equals(chunk.getBukkitChunk())) {
                                region.setLocation(null);
                            }

                            new ChunkParticlesSpawner(player);

                            PaginationMenu instance = context.getInstance();

                            chunks = region.getChunks();

                            instance.setItems(getItems(player, region));
                        }
                    }
                });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
