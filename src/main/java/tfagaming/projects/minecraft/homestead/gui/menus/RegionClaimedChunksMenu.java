package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
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

/**
 * GUI menu that lists all claimed chunks of a region.
 * <p>
 * Allows teleporting to or unclaiming specific chunks directly from the menu.
 * </p>
 */
public class RegionClaimedChunksMenu {
    private List<SerializableChunk> chunks;

    /**
     * Builds the item list for the GUI pagination menu.
     *
     * @param player The player viewing the menu.
     * @param region The region whose chunks are displayed.
     * @return The list of formatted item buttons.
     */
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
                    case NORMAL -> data.originalType = Homestead.menusConfig.get("button-types.world.overworld");
                    case NETHER -> data.originalType = Homestead.menusConfig.get("button-types.world.nether");
                    case THE_END -> data.originalType = Homestead.menusConfig.get("button-types.world.the_end");
                    default -> data.originalType = Homestead.menusConfig.get("button-types.world.overworld");
                }
            }

            items.add(MenuUtils.getButton(data, replacements));
        }

        return items;
    }

    /**
     * Opens the menu that lists all claimed chunks of the region.
     * <p>
     * Left-clicking unclaims a chunk (if allowed).
     * Right-clicking teleports the player to that chunk.
     * Updates automatically after unclaiming.
     * </p>
     *
     * @param player The player opening the menu.
     * @param region The target region.
     */
    public RegionClaimedChunksMenu(Player player, Region region) {
        this.chunks = region.getChunks();

        PaginationMenu gui = new PaginationMenu(
                MenuUtils.getTitle(11),
                9 * 5,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(),
                getItems(player, region),
                (_player, event) -> new RegionMenu(player, region),
                (_player, context) -> {

                    if (context.getIndex() >= chunks.size()) return;

                    SerializableChunk chunk = chunks.get(context.getIndex());

                    // Right-click → teleport
                    if (context.getEvent().isRightClick()) {
                        new DelayedTeleport(player, chunk.getBukkitLocation());
                        return;
                    }

                    // Left-click → unclaim
                    if (context.getEvent().isLeftClick()) {
                        if (ChunksManager.isChunkClaimed(chunk.getBukkitChunk())
                                && ChunksManager.getRegionOwnsTheChunk(chunk.getBukkitChunk())
                                .getUniqueId().equals(region.getUniqueId())) {

                            if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                                    RegionControlFlags.UNCLAIM_CHUNKS)) {
                                return;
                            }

                            int before = region.getChunks().size();
                            ChunksManager.unclaimChunk(region.getUniqueId(), chunk.getBukkitChunk(), player);
                            int after = region.getChunks().size();

                            /** Only send success message if the unclaim was successful. */
                            if (after < before) {
                                Map<String, String> replacements = new HashMap<>();
                                replacements.put("{region}", region.getName());
                                PlayerUtils.sendMessage(player, 24, replacements);
                            }

                            /** Reset region home if it was inside the unclaimed chunk. */
                            if (region.getLocation() != null
                                    && region.getLocation().getBukkitLocation().getChunk()
                                    .equals(chunk.getBukkitChunk())) {
                                region.setLocation(null);
                            }

                            new ChunkParticlesSpawner(player);

                            /** Update pagination items to reflect changes. */
                            PaginationMenu instance = context.getInstance();
                            chunks = region.getChunks();
                            instance.setItems(getItems(player, region));
                        }
                    }
                });

        gui.open(player, MenuUtils.getEmptySlot());
    }
}
