package tfagaming.projects.minecraft.homestead.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RegionProtectionListener implements Listener {
    // Blocks protection

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)
                && !event.getBlock().getType().equals(Material.FIRE)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
            SerializableSubArea subArea = region.findSubAreaHasLocationInside(event.getBlock().getLocation());

            if (subArea != null) {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                PlayerFlags.PLACE_BLOCKS)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PLACE_BLOCKS)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)
                && !event.getBlock().getType().equals(Material.FIRE)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
            SerializableSubArea subArea = region.findSubAreaHasLocationInside(event.getBlock().getLocation());

            if (subArea != null) {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                PlayerFlags.BREAK_BLOCKS)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.BREAK_BLOCKS)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        if (holder instanceof Villager) {
            Player player = (Player) event.getPlayer();
            Villager villager = (Villager) holder;
            Chunk chunk = villager.getLocation().getChunk();

            if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                SerializableSubArea subArea = region.findSubAreaHasLocationInside(villager.getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.TRADE_VILLAGERS)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                    PlayerFlags.TRADE_VILLAGERS)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (event.getInventory().getHolder() instanceof org.bukkit.entity.ChestBoat
                || event.getInventory().getHolder() instanceof org.bukkit.entity.ChestedHorse
                || event.getInventory().getHolder() instanceof org.bukkit.entity.minecart.StorageMinecart
                || event.getInventory().getHolder() instanceof org.bukkit.entity.minecart.HopperMinecart) {
            Player player = (Player) event.getPlayer();
            Chunk chunk = player.getLocation().getChunk();

            if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                SerializableSubArea subArea = region.findSubAreaHasLocationInside(player.getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.CONTAINERS)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.CONTAINERS)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Chunk chunk = event.getBlockClicked().getRelative(event.getBlockFace()).getChunk();
        Player player = event.getPlayer();

        if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
            SerializableSubArea subArea = region.findSubAreaHasLocationInside(event.getBlockClicked().getLocation());

            if (subArea != null) {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                PlayerFlags.PLACE_BLOCKS)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PLACE_BLOCKS)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Chunk chunk = event.getBlockClicked().getRelative(event.getBlockFace()).getChunk();
        Player player = event.getPlayer();

        if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
            SerializableSubArea subArea = region.findSubAreaHasLocationInside(event.getBlock().getLocation());

            if (subArea != null) {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                PlayerFlags.BREAK_BLOCKS)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.BREAK_BLOCKS)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onBreakCrop(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedblock = event.getClickedBlock();

        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                && player.getTargetBlock((Set<Material>) null, 5).getType().equals(Material.FIRE)) {
            Chunk chunk = player.getTargetBlock((Set<Material>) null, 5).getChunk();

            if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(event.getClickedBlock().getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.IGNITE)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.IGNITE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (event.getAction().equals(Action.PHYSICAL)) {
            if (clickedblock != null
                    && (clickedblock.getType() == Material.FARMLAND || clickedblock.getType() == Material.TURTLE_EGG)) {
                Chunk chunk = event.getClickedBlock().getLocation().getChunk();

                if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                    Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                    SerializableSubArea subArea = region
                            .findSubAreaHasLocationInside(event.getClickedBlock().getLocation());

                    if (subArea != null) {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                        PlayerFlags.BLOCK_TRAMPLING)) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                        PlayerFlags.BLOCK_TRAMPLING)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        } else if (clickedblock != null && isCropBlock(clickedblock)) {
            Chunk chunk = event.getClickedBlock().getLocation().getChunk();

            if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(event.getClickedBlock().getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.HARVEST_CROPS)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                    PlayerFlags.HARVEST_CROPS)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSpawnEggPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();

        ItemStack item = event.getItem();
        if (item == null)
            return;

        if (item.getType().toString().endsWith("_SPAWN_EGG")) {
            if (event.getClickedBlock() == null) {
                return;
            }

            Chunk chunk = event.getClickedBlock().getChunk();

            if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(event.getClickedBlock().getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.PLACE_BLOCKS)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PLACE_BLOCKS)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Handles most player interaction with blocks and certain placeable items in claimed chunks.
     * Uses Bukkit tags where available and centralizes permission gating to reduce branching and duplication.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block clicked = event.getClickedBlock();
        final Chunk chunk = (clicked != null ? clicked.getLocation().getChunk() : player.getLocation().getChunk());

        if (!ChunksManager.isChunkClaimed(chunk)) return;
        if (PlayerUtils.isOperator(player)) return;

        final Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
        if (region == null) return;

        final boolean isOwner = player.getUniqueId().equals(region.getOwnerId());
        if (isOwner) return;

        final SerializableSubArea subArea = (clicked != null) ? region.findSubAreaHasLocationInside(clicked.getLocation())
                : region.findSubAreaHasLocationInside(player.getLocation());

        if (event.getItem() != null) {
            final Material itemType = event.getItem().getType();
            final String itn = itemType.name();

            final boolean placeSpawnItem =
                    itn.contains("BOAT") ||
                            itn.contains("ARMOR_STAND") ||
                            itn.contains("MINECART") ||
                            itn.contains("PAINTING") ||
                            itemType == Material.BONE_MEAL;

            if (placeSpawnItem) {
                if (!requireFlag(region, subArea, player, PlayerFlags.PLACE_BLOCKS, event)) return;
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clicked != null) {
            final Material type = clicked.getType();

            if (isShulkerBox(type)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.CONTAINERS, event)) return;
            }

            if (isAnySign(type)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.GENERAL_INTERACTION, event)) return;
            }

            if (isContainerLike(type)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.CONTAINERS, event)) return;
                return;
            }

            if (isAnvil(type)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.USE_ANVIL, event)) return;
                return;
            }

            if (Tag.TRAPDOORS.isTagged(type)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.TRAP_DOORS, event)) return;
                return;
            }

            if (Tag.DOORS.isTagged(type) || type.name().contains("DOOR")) {
                if (!requireFlag(region, subArea, player, PlayerFlags.DOORS, event)) return;
                return;
            }

            if (isArchaeologyBlockWithBrush(type, player)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.BREAK_BLOCKS, event)) return;
                return;
            }

            if (Tag.BUTTONS.isTagged(type)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.BUTTONS, event)) return;
                return;
            }

            if (type.name().contains("FENCE_GATE")) {
                if (!requireFlag(region, subArea, player, PlayerFlags.FENCE_GATES, event)) return;
                return;
            }

            if (isSmallInteractable(type)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.GENERAL_INTERACTION, event)) return;
                return;
            }

            if (isLecternOrVaultWithKey(type, player)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.CONTAINERS, event)) return;
                return;
            }

            if (type.name().endsWith("_BED")) {
                if (!requireFlag(region, subArea, player, PlayerFlags.SLEEP, event)) return;
                return;
            }

            if (type == Material.LEVER) {
                if (!requireFlag(region, subArea, player, PlayerFlags.LEVERS, event)) return;
                return;
            }

            if (type == Material.BELL) {
                if (!requireFlag(region, subArea, player, PlayerFlags.USE_BELLS, event)) return;
                return;
            }

            if (isRedstoneInteraction(type)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.REDSTONE, event)) return;
            }
            return;
        }

        if (event.getAction() == Action.PHYSICAL && clicked != null) {
            final Material type = clicked.getType();

            if (Tag.PRESSURE_PLATES.isTagged(type)) {
                if (!requireFlag(region, subArea, player, PlayerFlags.PRESSURE_PLATES, event)) return;
                return;
            }

            if (type == Material.TRIPWIRE) {
                if (!requireFlag(region, subArea, player, PlayerFlags.TRIGGER_TRIPWIRE, event)) return;
            }
        }
    }

    /**
     * Centralized permission gate. Cancels the event if the player does not have the given flag.
     */
    private boolean requireFlag(Region region, SerializableSubArea subArea, Player player, long flag, Cancellable event) {
        final boolean allowed = (subArea != null)
                ? PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player, flag)
                : PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, flag);

        if (!allowed) {
            event.setCancelled(true);
            return false;
        }
        return true;
    }

    /**
     * Returns true for all shulker boxes using Bukkit tags with a simple fallback.
     */
    private boolean isShulkerBox(Material type) {
        if (Tag.SHULKER_BOXES.isTagged(type)) return true;
        final String n = type.name();
        return n.endsWith("SHULKER_BOX");
    }

    /**
     * Returns true for any kind of sign (standing, wall, hanging).
     */
    private boolean isAnySign(Material type) {
        if (Tag.SIGNS.isTagged(type)) return true;
        final String n = type.name();
        return n.endsWith("_HANGING_SIGN")
                || n.endsWith("_WALL_HANGING_SIGN")
                || n.endsWith("_SIGN")
                || n.endsWith("_WALL_SIGN");
    }

    /**
     * Returns true for blocks gated by the CONTAINERS flag in your logic.
     */
    private boolean isContainerLike(Material type) {
        if (type == Material.ENDER_CHEST) return false;
        if (Tag.CAMPFIRES.isTagged(type)) return true;
        if (isShulkerBox(type)) return true;

        switch (type) {
            case FURNACE:
            case SMOKER:
            case BLAST_FURNACE:
            case BREWING_STAND:
            case BARREL:
            case BEACON:
            case DROPPER:
            case DISPENSER:
            case CHISELED_BOOKSHELF:
            case CAULDRON:
            case LAVA_CAULDRON:
            case WATER_CAULDRON:
            case LODESTONE:
            case HOPPER:
                return true;
            default:
                final String n = type.name();
                if (n.contains("CHEST")) return true;
                return false;
        }
    }

    /**
     * Returns true if the block is an anvil variant.
     */
    private boolean isAnvil(Material type) {
        return type.name().contains("ANVIL");
    }

    /**
     * Returns true for archaeology brushing blocks when the player holds a brush.
     */
    private boolean isArchaeologyBlockWithBrush(Material type, Player player) {
        if (!(type == Material.SUSPICIOUS_GRAVEL || type == Material.SUSPICIOUS_SAND)) return false;
        return player.getInventory().getItemInMainHand().getType() == Material.BRUSH;
    }

    /**
     * Returns true for small interactables handled under GENERAL_INTERACTION.
     */
    private boolean isSmallInteractable(Material type) {
        if (type == Material.CAKE) return true;
        if (type == Material.DECORATED_POT) return true;
        if (type == Material.FLOWER_POT) return true;
        final String n = type.name();
        if (n.contains("POTTED")) return true;
        return false;
    }

    /**
     * Returns true if the block requires CONTAINERS based on item-in-hand logic (lectern with book, vault with trial key).
     */
    private boolean isLecternOrVaultWithKey(Material type, Player player) {
        if (type == Material.LECTERN) {
            final Material inHand = player.getInventory().getItemInMainHand().getType();
            return inHand == Material.WRITTEN_BOOK || inHand == Material.WRITABLE_BOOK;
        }
        if (type == Material.VAULT) {
            return player.getInventory().getItemInMainHand().getType().name().contains("TRIAL_KEY");
        }
        return false;
    }

    /**
     * Returns true for blocks considered redstone interaction in your logic.
     */
    private boolean isRedstoneInteraction(Material type) {
        switch (type) {
            case REPEATER:
            case COMPARATOR:
            case COMMAND_BLOCK:
            case COMMAND_BLOCK_MINECART:
            case REDSTONE:
            case REDSTONE_WIRE:
            case NOTE_BLOCK:
            case JUKEBOX:
            case COMPOSTER:
            case DAYLIGHT_DETECTOR:
                return true;
            default:
                return false;
        }
    }


    @EventHandler
    public void onPlayerPunchFrame(EntityDamageByEntityEvent event) {
        if (event.getEntityType() == EntityType.ITEM_FRAME || event.getEntityType() == EntityType.GLOW_ITEM_FRAME) {
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                Chunk chunk = event.getEntity().getLocation().getChunk();

                if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                    Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                    SerializableSubArea subArea = region.findSubAreaHasLocationInside(event.getEntity().getLocation());

                    if (subArea != null) {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                        PlayerFlags.CONTAINERS)) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                        PlayerFlags.CONTAINERS)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event) {
        Player player = event.getPlayer();
        Block block = event.getLectern().getLocation().getBlock();
        Chunk chunk = block.getLocation().getChunk();

        if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
            SerializableSubArea subArea = region.findSubAreaHasLocationInside(event.getLectern().getLocation());

            if (subArea != null) {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                PlayerFlags.CONTAINERS)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.CONTAINERS)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Chunk chunk = event.getBlock().getChunk();

            ItemStack boots = player.getEquipment().getBoots();

            if (boots != null && boots.getEnchantments().containsKey(Enchantment.FROST_WALKER)) {
                if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                    Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                    SerializableSubArea subArea = region.findSubAreaHasLocationInside(event.getBlock().getLocation());

                    if (subArea != null) {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                        PlayerFlags.FROST_WALKER)) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                        PlayerFlags.FROST_WALKER)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getLocation().getChunk();

        if (player == null) {
            event.setCancelled(true);
            return;
        } else {
            if (ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                SerializableSubArea subArea = region.findSubAreaHasLocationInside(event.getBlock().getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.IGNITE)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.IGNITE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getEntity().getType().name().contains("PAINTING")
                || event.getEntity().getType().name().contains("ITEM_FRAME")) {
            if (event.getRemover() instanceof Player) {
                Player player = (Player) event.getRemover();
                Chunk chunk = event.getEntity().getLocation().getChunk();

                if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                    Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                    SerializableSubArea subArea = region.findSubAreaHasLocationInside(event.getEntity().getLocation());

                    if (subArea != null) {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                        PlayerFlags.BREAK_BLOCKS)) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                        PlayerFlags.BREAK_BLOCKS)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            } else if (event.getRemover() instanceof Creeper || event.getRemover() instanceof TNTPrimed
                    || event.getRemover() instanceof Fireball
                    || event.getRemover() instanceof EnderCrystal
                    || event.getRemover().getType() == EntityType.END_CRYSTAL
                    || event.getRemover().getType() == EntityType.TNT_MINECART) {
                Chunk chunk = event.getEntity().getLocation().getChunk();

                if (ChunksManager.isChunkClaimed(chunk)) {
                    Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                    if (!region.isWorldFlagSet(WorldFlags.EXPLOSIONS_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity entity = event.getEntity();
        ProjectileSource source = event.getEntity().getShooter();
        Block hitBlock = event.getHitBlock();

        if (hitBlock != null && hitBlock.getType().equals(Material.DECORATED_POT)) {
            Chunk chunk = hitBlock.getChunk();

            if (ChunksManager.isChunkClaimed(chunk)) {
                if (entity instanceof Arrow || entity instanceof WindCharge || entity instanceof Egg
                        || entity instanceof Snowball) {
                    Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                    if (source instanceof Player) {
                        Player player = (Player) source;

                        SerializableSubArea subArea = region
                                .findSubAreaHasLocationInside(player.getLocation());

                        if (subArea != null) {
                            if (!player.getUniqueId().equals(region.getOwnerId())
                                    && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                            PlayerFlags.BREAK_BLOCKS)) {
                                event.setCancelled(true);
                                return;
                            }
                        } else {
                            if (!player.getUniqueId().equals(region.getOwnerId())
                                    && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                            PlayerFlags.BREAK_BLOCKS)) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    } else {
                        if (!region.isWorldFlagSet(WorldFlags.WILDERNESS_DISPENSERS)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    // Entities protection

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        Chunk chunk = entity.getLocation().getChunk();

        if (ChunksManager.isChunkClaimed(chunk)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            SerializableSubArea subArea = region
                    .findSubAreaHasLocationInside(event.getEntity().getLocation());

            if (subArea != null) {
                if (damager instanceof Player && entity instanceof ArmorStand) {
                    if (!PlayerUtils.isOperator((Player) damager)
                            && !((Player) damager).getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(subArea.getId(), subArea.getId(), (Player) damager,
                                    PlayerFlags.BREAK_BLOCKS)) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (entity instanceof Player && damager instanceof Player) {
                    if (!PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), (Player) damager,
                            PlayerFlags.PVP)) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (damager instanceof Player && (entity instanceof Monster || entity instanceof IronGolem)) {
                    if (!((Player) damager).getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.isOperator((Player) damager)
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), (Player) damager,
                                    PlayerFlags.DAMAGE_HOSTILE_ENTITIES)) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (damager instanceof Player && (entity instanceof Animals || entity instanceof Mob)) {
                    if (!((Player) damager).getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.isOperator((Player) damager)
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), (Player) damager,
                                    PlayerFlags.DAMAGE_PASSIVE_ENTITIES)) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (damager instanceof Creeper || damager instanceof TNTPrimed || damager instanceof Fireball
                        || damager instanceof EnderCrystal || damager.getType() == EntityType.END_CRYSTAL
                        || damager.getType() == EntityType.TNT_MINECART) {
                    if (!region.isWorldFlagSet(WorldFlags.EXPLOSIONS_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            } else {
                if (damager instanceof Player && entity instanceof ArmorStand) {
                    if (!PlayerUtils.isOperator((Player) damager)
                            && !((Player) damager).getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), (Player) damager,
                                    PlayerFlags.BREAK_BLOCKS)) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (entity instanceof Player && damager instanceof Player) {
                    if (!PlayerUtils.hasPermissionFlag(region.getUniqueId(), (Player) damager, PlayerFlags.PVP)) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (damager instanceof Player && (entity instanceof Monster || entity instanceof IronGolem)) {
                    if (!((Player) damager).getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.isOperator((Player) damager)
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), (Player) damager,
                                    PlayerFlags.DAMAGE_HOSTILE_ENTITIES)) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (damager instanceof Player && (entity instanceof Animals || entity instanceof Mob)) {
                    if (!((Player) damager).getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.isOperator((Player) damager)
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), (Player) damager,
                                    PlayerFlags.DAMAGE_PASSIVE_ENTITIES)) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (damager instanceof Creeper || damager instanceof TNTPrimed || damager instanceof Fireball
                        || damager instanceof EnderCrystal || damager.getType() == EntityType.END_CRYSTAL
                        || damager.getType() == EntityType.TNT_MINECART) {
                    if (!region.isWorldFlagSet(WorldFlags.EXPLOSIONS_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getEntity().getLocation().getChunk();

        if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            SerializableSubArea subArea = region
                    .findSubAreaHasLocationInside(event.getEntity().getLocation());

            if (subArea != null) {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                PlayerFlags.INTERACT_ENTITIES)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                PlayerFlags.INTERACT_ENTITIES)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract2(PlayerInteractEvent event) {
        Player player = (Player) event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();

        if (event.getItem() != null) {
            if (event.getItem().getType().equals(Material.ENDER_PEARL)) {
                if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                    Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                    SerializableSubArea subArea = region
                            .findSubAreaHasLocationInside(player.getLocation());

                    if (subArea != null) {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                        PlayerFlags.TELEPORT)) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TELEPORT)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            } else if (event.getItem().getType().equals(Material.SPLASH_POTION)
                    || event.getItem().getType().equals(Material.LINGERING_POTION)) {
                if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                    Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                    SerializableSubArea subArea = region
                            .findSubAreaHasLocationInside(player.getLocation());

                    if (subArea != null) {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                        PlayerFlags.THROW_POTIONS)) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                        PlayerFlags.THROW_POTIONS)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerEatChorusFruit(PlayerItemConsumeEvent event) {
        Player player = (Player) event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();

        if (event.getItem() != null && event.getItem().getType().equals(Material.CHORUS_FRUIT)) {
            if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(player.getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.TELEPORT)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TELEPORT)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionSplash(PotionSplashEvent event) {
        Projectile entity = event.getEntity();
        ProjectileSource shooter = entity.getShooter();

        if (shooter instanceof Player && entity instanceof ThrownPotion) {
            Player player = (Player) shooter;

            Chunk chunk = entity.getLocation().getChunk();

            if (ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(entity.getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.THROW_POTIONS)) {
                        event.setCancelled(true);
                        entity.remove();
                        return;
                    }
                } else {
                    if (!PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.THROW_POTIONS)) {
                        event.setCancelled(true);
                        entity.remove();
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        Projectile entity = event.getEntity();
        ProjectileSource shooter = entity.getShooter();

        if (shooter instanceof Player && entity instanceof ThrownPotion) {
            Player player = (Player) shooter;

            Chunk chunk = entity.getLocation().getChunk();

            if (ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(entity.getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.THROW_POTIONS)) {
                        event.setCancelled(true);
                        event.getEntity().remove();
                        return;
                    }
                } else {
                    if (!PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.THROW_POTIONS)) {
                        event.setCancelled(true);
                        event.getEntity().remove();
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit2(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        ProjectileSource shooter = entity.getShooter();

        Chunk chunk;
        Entity entityhit = event.getHitEntity();
        // Block blockHit = event.getHitBlock();

        if (shooter instanceof Player && entity instanceof ThrownPotion) {
            Player player = (Player) shooter;

            chunk = entity.getLocation().getChunk();

            if (ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(entity.getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.THROW_POTIONS)) {
                        event.setCancelled(true);
                        event.getEntity().remove();
                        return;
                    }
                } else {
                    if (!PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.THROW_POTIONS)) {
                        event.setCancelled(true);
                        event.getEntity().remove();
                        return;
                    }
                }
            }
        } else if (shooter instanceof Player && entityhit != null) {
            Player player = (Player) shooter;

            chunk = entityhit.getLocation().getChunk();

            if (ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(entityhit.getLocation());

                if (entityhit instanceof Player) {
                    if (subArea != null) {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                        PlayerFlags.PVP)) {
                            event.setCancelled(true);
                            event.getEntity().remove();
                            return;
                        }
                    } else {
                        if (!PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PVP)) {
                            event.setCancelled(true);
                            event.getEntity().remove();
                            return;
                        }
                    }
                } else if (entityhit instanceof Monster || entityhit instanceof IronGolem) {
                    if (subArea != null) {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                        PlayerFlags.DAMAGE_HOSTILE_ENTITIES)) {
                            event.setCancelled(true);
                            event.getEntity().remove();
                            return;
                        }
                    } else {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                        PlayerFlags.DAMAGE_HOSTILE_ENTITIES)) {
                            event.setCancelled(true);
                            event.getEntity().remove();
                            return;
                        }
                    }
                } else if (entityhit instanceof Animals || entityhit instanceof Mob) {
                    if (subArea != null) {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                        PlayerFlags.DAMAGE_PASSIVE_ENTITIES)) {
                            event.setCancelled(true);
                            event.getEntity().remove();
                            return;
                        }
                    } else {
                        if (!player.getUniqueId().equals(region.getOwnerId())
                                && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                        PlayerFlags.DAMAGE_PASSIVE_ENTITIES)) {
                            event.setCancelled(true);
                            event.getEntity().remove();
                            return;
                        }
                    }
                }
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (shouldCancelItemTransfer(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (shouldCancelItemTransfer(player)) {
            event.setCancelled(true);
        }
    }

    /**
     * Returns true if the player's item transfer (drop or pickup) should be cancelled
     * based on region ownership, operator status, sub-area membership and the
     * PICKUP_ITEMS flag resolution.
     *
     * @param player the player performing the action
     * @return true to cancel the event, false to allow
     */
    private boolean shouldCancelItemTransfer(Player player) {
        if (player == null) return false;
        if (PlayerUtils.isOperator(player)) return false;

        Chunk chunk = player.getLocation().getChunk();
        if (!ChunksManager.isChunkClaimed(chunk)) return false;

        Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
        if (region == null) return false;
        if (player.getUniqueId().equals(region.getOwnerId())) return false;

        SerializableSubArea subArea = region.findSubAreaHasLocationInside(player.getLocation());

        boolean allowed = (subArea != null)
                ? PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player, PlayerFlags.PICKUP_ITEMS)
                : PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PICKUP_ITEMS);

        return !allowed;
    }


    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Vehicle vehicle = event.getVehicle();
        Chunk chunk = vehicle.getLocation().getChunk();
        Entity entity = event.getEntered();

        if (vehicle != null) {
            if (entity instanceof Player && ChunksManager.isChunkClaimed(chunk)
                    && !PlayerUtils.isOperator((Player) entity)) {
                Player player = (Player) entity;

                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(vehicle.getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.VEHICLES)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.VEHICLES)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        Vehicle vehicle = event.getVehicle();
        Chunk chunk = vehicle.getLocation().getChunk();
        Entity entity = event.getAttacker();

        if (vehicle != null) {
            if (entity instanceof Player && ChunksManager.isChunkClaimed(chunk)
                    && !PlayerUtils.isOperator((Player) entity)) {
                Player player = (Player) entity;

                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(vehicle.getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.BREAK_BLOCKS)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.BREAK_BLOCKS)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        Player player = (Player) event.getPlayer();
        Entity entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();

        if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            SerializableSubArea subArea = region
                    .findSubAreaHasLocationInside(entity.getLocation());

            if (subArea != null) {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                PlayerFlags.INTERACT_ENTITIES)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                PlayerFlags.INTERACT_ENTITIES)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onLeashEvent(PlayerLeashEntityEvent event) {
        Player player = event.getPlayer();
        Block block = event.getEntity().getLocation().getBlock();
        Chunk chunk = block.getLocation().getChunk();

        if (block.getType().name().contains("FENCE")) {
            if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(block.getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.INTERACT_ENTITIES)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                    PlayerFlags.INTERACT_ENTITIES)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        Player player = (Player) event.getPlayer();
        Entity entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();

        if (player != null && ChunksManager.isChunkClaimed(chunk) && !PlayerUtils.isOperator(player)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            SerializableSubArea subArea = region
                    .findSubAreaHasLocationInside(entity.getLocation());

            if (subArea != null) {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                PlayerFlags.INTERACT_ENTITIES)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player,
                                PlayerFlags.INTERACT_ENTITIES)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (shouldCancelEntityInteraction(player, entity)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (shouldCancelEntityInteraction(player, entity)) {
            event.setCancelled(true);
        }
    }

    /**
     * Returns true if the interaction should be cancelled based on region ownership,
     * sub-area membership and the required flag for the clicked entity type.
     * <p>
     * Required flags:
     * <ul>
     *   <li>ArmorStand → {@code PlayerFlags.ARMOR_STANDS}</li>
     *   <li>Item Frames → {@code PlayerFlags.ITEM_FRAME_ROTATION}</li>
     *   <li>Villager trading → {@code PlayerFlags.TRADE_VILLAGERS}</li>
     *   <li>All other entities → {@code PlayerFlags.INTERACT_ENTITIES}</li>
     * </ul>
     *
     * @param player the interacting player
     * @param entity the clicked entity
     * @return true if the event should be cancelled
     */
    private boolean shouldCancelEntityInteraction(Player player, Entity entity) {
        if (player == null || entity == null) return false;
        if (PlayerUtils.isOperator(player)) return false;

        Chunk chunk = entity.getLocation().getChunk();
        if (!ChunksManager.isChunkClaimed(chunk)) return false;

        Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
        if (region == null) return false;

        if (player.getUniqueId().equals(region.getOwnerId())) return false;

        long requiredFlag;
        EntityType type = entity.getType();

        if (type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME) {
            requiredFlag = PlayerFlags.ITEM_FRAME_ROTATION;
        } else if (entity instanceof org.bukkit.entity.Villager) {
            requiredFlag = PlayerFlags.TRADE_VILLAGERS;
        } else if (entity instanceof org.bukkit.entity.ArmorStand) {
            requiredFlag = PlayerFlags.ARMOR_STANDS;
        } else {
            requiredFlag = PlayerFlags.INTERACT_ENTITIES;
        }

        SerializableSubArea subArea = region.findSubAreaHasLocationInside(entity.getLocation());
        boolean allowed = (subArea != null)
                ? PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player, requiredFlag)
                : PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, requiredFlag);

        return !allowed;
    }


    @EventHandler
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Chunk chunk = player.getLocation().getChunk();

            if (event.isGliding() && isWearingElytra(player) && ChunksManager.isChunkClaimed(chunk)
                    && !PlayerUtils.isOperator(player)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                SerializableSubArea subArea = region
                        .findSubAreaHasLocationInside(player.getLocation());

                if (subArea != null) {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                                    PlayerFlags.ELYTRA)) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.getUniqueId().equals(region.getOwnerId())
                            && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.ELYTRA)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        if (event.getCause() != DamageCause.FALL && event.getCause() != DamageCause.FLY_INTO_WALL) return;

        Player player = (Player) event.getEntity();
        Chunk chunk = player.getLocation().getChunk();

        if (!ChunksManager.isChunkClaimed(chunk)) return;

        Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
        SerializableSubArea subArea = region.findSubAreaHasLocationInside(player.getLocation());

        if (subArea != null) {
            boolean allowed = PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player, PlayerFlags.TAKE_FALL_DAMAGE);
            if (!allowed) {
                event.setCancelled(true);
            }
        } else {
            boolean allowed = PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TAKE_FALL_DAMAGE);
            if (!allowed) {
                event.setCancelled(true);
            }
        }
    }


    // World protection

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity().getType() == EntityType.WIND_CHARGE) {
            Chunk chunk = event.getLocation().getChunk();

            if (ChunksManager.isChunkClaimed(chunk)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                if (!region.isWorldFlagSet(WorldFlags.WINDCHARGE_BURST)) {
                    event.getEntity().remove();

                    event.setCancelled(true);
                }
            }
        } else if (event.getEntity().getType() == EntityType.WITHER
                || event.getEntity().getType() == EntityType.WITHER_SKULL) {
            event.blockList().removeIf((block) -> {
                Chunk chunk = block.getChunk();

                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                if (region != null && region.isWorldFlagSet(WorldFlags.WITHER_DAMAGE)) {
                    return true;
                }

                return false;
            });
        } else if (event.getEntity() instanceof TNTPrimed || event.getEntity() instanceof Creeper
                || event.getEntity() instanceof Fireball || event.getEntity() instanceof EnderCrystal
                || event.getEntity().getType() == EntityType.END_CRYSTAL
                || event.getEntity().getType() == EntityType.TNT_MINECART) {
            Chunk chunk = event.getLocation().getChunk();

            if (ChunksManager.isChunkClaimed(chunk)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                if (!region.isWorldFlagSet(WorldFlags.EXPLOSIONS_DAMAGE)) {
                    event.setCancelled(true);
                }
            } else {
                List<Block> allowedblocks = new ArrayList<Block>();

                for (Block block : event.blockList()) {
                    Location blocklocation = block.getLocation();
                    Chunk blockchunk = blocklocation.getChunk();

                    if (!ChunksManager.isChunkClaimed(blockchunk)) {
                        allowedblocks.add(block);
                    }
                }

                event.blockList().clear();
                event.blockList().addAll(allowedblocks);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.getBlock().getType().equals(Material.AIR)) {
            Chunk chunk = event.getBlock().getLocation().getChunk();

            if (ChunksManager.isChunkClaimed(chunk)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                if (!region.isWorldFlagSet(WorldFlags.EXPLOSIONS_DAMAGE)) {
                    event.setCancelled(true);
                }
            } else {
                List<Block> allowedblocks = new ArrayList<Block>();

                for (Block block : event.blockList()) {
                    Location blocklocation = block.getLocation();
                    Chunk blockchunk = blocklocation.getChunk();

                    if (!ChunksManager.isChunkClaimed(blockchunk)) {
                        allowedblocks.add(block);
                    }
                }

                event.blockList().clear();
                event.blockList().addAll(allowedblocks);
            }
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (event.getNewState().getType() == Material.FIRE) {
            Chunk chunk = event.getBlock().getLocation().getChunk();

            if (ChunksManager.isChunkClaimed(chunk)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                if (!region.isWorldFlagSet(WorldFlags.FIRE_SPREAD)) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getSource().getType() == Material.GRASS_BLOCK
                || event.getSource().getType() == Material.MYCELIUM) {
            Chunk chunk = event.getBlock().getChunk();

            if (ChunksManager.isChunkClaimed(chunk)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                if (!region.isWorldFlagSet(WorldFlags.GRASS_GROWTH)) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getSource().getType() == Material.SCULK_CATALYST) {
            Chunk chunk = event.getBlock().getChunk();

            if (ChunksManager.isChunkClaimed(chunk)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                if (!region.isWorldFlagSet(WorldFlags.SCULK_SPREAD)) {
                    event.setCancelled(true);
                }
            }
        } else {
            Chunk chunk = event.getBlock().getLocation().getChunk();

            if (ChunksManager.isChunkClaimed(chunk)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                if (!region.isWorldFlagSet(WorldFlags.PLANT_GROWTH)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public void onBlockGrow(BlockGrowEvent event) {
        Chunk chunk = event.getBlock().getLocation().getChunk();

        if (ChunksManager.isChunkClaimed(chunk)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            if (!region.isWorldFlagSet(WorldFlags.PLANT_GROWTH)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        Chunk chunk = event.getBlock().getLocation().getChunk();

        if (ChunksManager.isChunkClaimed(chunk)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            if (!region.isWorldFlagSet(WorldFlags.LEAVES_DECAY)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Chunk chunk = event.getBlock().getLocation().getChunk();

        if (ChunksManager.isChunkClaimed(chunk)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            if (!region.isWorldFlagSet(WorldFlags.FIRE_SPREAD)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent event) {
        Chunk fromChunk = event.getBlock().getChunk();
        Chunk toChunk = event.getToBlock().getChunk();

        if (!fromChunk.equals(toChunk)) {
            if (ChunksManager.isChunkClaimed(toChunk) && ChunksManager.isChunkClaimed(fromChunk)) {
                event.setCancelled(false);
            } else if (ChunksManager.isChunkClaimed(toChunk)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(toChunk);

                if (!region.isWorldFlagSet(WorldFlags.LIQUID_FLOW)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block piston = event.getBlock();
        @SuppressWarnings({ "rawtypes", "unchecked" })
        List<Block> affectedBlocks = new ArrayList(event.getBlocks());
        BlockFace direction = event.getDirection();

        if (!affectedBlocks.isEmpty()) {
            affectedBlocks.add(piston.getRelative(direction));
        }

        if (!this.canPistonMoveBlock(affectedBlocks, direction, piston.getLocation().getChunk(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Block piston = event.getBlock();
        @SuppressWarnings({ "rawtypes", "unchecked" })
        List<Block> affectedBlocks = new ArrayList(event.getBlocks());
        BlockFace direction = event.getDirection();

        if (event.isSticky() && !affectedBlocks.isEmpty()) {
            affectedBlocks.add(piston.getRelative(direction));
        }

        if (!this.canPistonMoveBlock(affectedBlocks, direction, piston.getLocation().getChunk(), true)) {
            event.setCancelled(true);
        }
    }

    private boolean canPistonMoveBlock(List<Block> blocks, BlockFace direction, Chunk pistonChunk,
            boolean retractOrNot) {
        @SuppressWarnings("rawtypes")
        Iterator var5;
        Block block;
        Chunk chunk;

        if (retractOrNot) {
            var5 = blocks.iterator();

            while (var5.hasNext()) {
                block = (Block) var5.next();
                chunk = block.getLocation().getChunk();

                if (!chunk.equals(pistonChunk) && ChunksManager.isChunkClaimed(chunk)) {
                    Region pistonChunkRegion = ChunksManager.getRegionOwnsTheChunk(pistonChunk);
                    UUID pistonChunkOwner = pistonChunkRegion == null ? null : pistonChunkRegion.getOwnerId();
                    UUID targetChunkOwner = ChunksManager.getRegionOwnsTheChunk(chunk).getOwnerId();

                    if (pistonChunkRegion != null && pistonChunkOwner != null && targetChunkOwner != null
                            && pistonChunkOwner.equals(targetChunkOwner)) {
                        return true;
                    }

                    Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                    if (!region.isWorldFlagSet(WorldFlags.WILDERNESS_PISTONS)) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            var5 = blocks.iterator();

            while (var5.hasNext()) {
                block = (Block) var5.next();
                chunk = block.getRelative(direction).getLocation().getChunk();

                if (!chunk.equals(pistonChunk) && ChunksManager.isChunkClaimed(chunk)) {
                    Region pistonChunkRegion = ChunksManager.getRegionOwnsTheChunk(pistonChunk);
                    UUID pistonChunkOwner = pistonChunkRegion == null ? null : pistonChunkRegion.getOwnerId();
                    UUID targetChunkOwner = ChunksManager.getRegionOwnsTheChunk(chunk).getOwnerId();

                    if (pistonChunkRegion != null && pistonChunkOwner != null && targetChunkOwner != null
                            && pistonChunkOwner.equals(targetChunkOwner)) {
                        return true;
                    }

                    Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                    if (!region.isWorldFlagSet(WorldFlags.WILDERNESS_PISTONS)) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        BlockData blockdata = event.getBlock().getBlockData();
        Chunk targetChunk = block.getRelative(((Directional) blockdata).getFacing()).getLocation().getChunk();

        if (!block.getLocation().getChunk().equals(targetChunk)) {
            if (ChunksManager.isChunkClaimed(targetChunk)) {
                Region dispenserChunkRegion = ChunksManager.getRegionOwnsTheChunk(block.getLocation().getChunk());
                UUID dispenserChunkOwner = dispenserChunkRegion == null ? null : dispenserChunkRegion.getOwnerId();
                UUID targetChunkOwner = ChunksManager.getRegionOwnsTheChunk(targetChunk).getOwnerId();

                if (dispenserChunkRegion != null && dispenserChunkOwner != null && targetChunkOwner != null
                        && dispenserChunkOwner.equals(targetChunkOwner)) {
                    return;
                }

                Region region = ChunksManager.getRegionOwnsTheChunk(targetChunk);

                if (!region.isWorldFlagSet(WorldFlags.WILDERNESS_DISPENSERS)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();

        if (entity == null) return;

        if (entity instanceof Sheep || entity instanceof Goat || entity instanceof Cow) return;

        if (ChunksManager.isChunkClaimed(chunk)) {
            if (!(entity instanceof Player || entity instanceof Wither || entity instanceof Villager || entity instanceof Bee)
                    && entity instanceof Mob) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);
                if (!region.isWorldFlagSet(WorldFlags.ENTITIES_GRIEF)) {
                    event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Chunk chunk = event.getLocation().getChunk();
        Entity entity = event.getEntity();

        if (ChunksManager.isChunkClaimed(chunk)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            if (entity instanceof Monster || entity instanceof IronGolem) {
                if (!region.isWorldFlagSet(WorldFlags.HOSTILE_ENTITIES_SPAWN)) {
                    event.setCancelled(true);
                }
            } else if (entity instanceof Animals || entity instanceof Mob) {
                if (!region.isWorldFlagSet(WorldFlags.PASSIVE_ENTITIES_SPAWN)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity2(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        Chunk chunk = entity.getLocation().getChunk();

        if (ChunksManager.isChunkClaimed(chunk)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            if ((damager instanceof Entity && !(damager instanceof Player)) && entity instanceof Entity) {
                if (!region.isWorldFlagSet(WorldFlags.ENTITIES_DAMAGE_ENTITIES)) {
                    event.setCancelled(true);
                }
            }

        }
    }

    @EventHandler
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        Entity entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();

        if (ChunksManager.isChunkClaimed(chunk)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            if (!(entity instanceof Player)) {
                if (!region.isWorldFlagSet(WorldFlags.ENTITIES_GRIEF)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onRaidTrigger(RaidTriggerEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getRaid().getLocation().getChunk();

        if (ChunksManager.isChunkClaimed(chunk)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            SerializableSubArea subArea = region.findSubAreaHasLocationInside(player.getLocation());

            if (subArea != null) {
                if (!PlayerUtils.hasPermissionFlag(region.getUniqueId(), subArea.getId(), player,
                        PlayerFlags.TRIGGER_RAID)) {
                    event.setCancelled(true);

                    PotionEffect effect = event.getPlayer().getPotionEffect(PotionEffectType.BAD_OMEN);

                    if (effect != null) {
                        event.getPlayer().removePotionEffect(PotionEffectType.BAD_OMEN);
                    }
                }
            } else {
                if (!PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TRIGGER_RAID)) {
                    event.setCancelled(true);

                    PotionEffect effect = event.getPlayer().getPotionEffect(PotionEffectType.BAD_OMEN);

                    if (effect != null) {
                        event.getPlayer().removePotionEffect(PotionEffectType.BAD_OMEN);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        Material blockType = event.getBlock().getType();
        Chunk chunk = event.getBlock().getLocation().getChunk();

        if (ChunksManager.isChunkClaimed(chunk)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            if (blockType == Material.SNOW) {
                if (!region.isWorldFlagSet(WorldFlags.SNOW_MELTING)) {
                    event.setCancelled(true);
                }
            } else if (blockType == Material.ICE) {
                if (!region.isWorldFlagSet(WorldFlags.ICE_MELTING)) {
                    event.setCancelled(true);
                }
            }
        }

    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        Chunk fromChunk = from.getChunk();
        Chunk toChunk = to.getChunk();

        if (fromChunk.equals(toChunk)) {
            return;
        }

        if (ChunksManager.isChunkClaimed(toChunk)) {
            Region fromRegion = ChunksManager.getRegionOwnsTheChunk(fromChunk);
            Region toRegion = ChunksManager.getRegionOwnsTheChunk(toChunk);

            if (fromRegion == null) {
                if (!toRegion.isWorldFlagSet(WorldFlags.WILDERNESS_MINECARTS)) {
                    event.getVehicle().remove();
                }
            } else if (!fromRegion.getUniqueId().equals(toRegion.getUniqueId())) {
                if (!toRegion.isWorldFlagSet(WorldFlags.WILDERNESS_MINECARTS)) {
                    event.getVehicle().remove();
                }
            }
        }
    }

    @EventHandler
    public void onWitherBlockChange(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();

        if (entity.getType() == EntityType.WITHER || entity.getType() == EntityType.WITHER_SKULL) {
            Block block = event.getBlock();
            Chunk chunk = block.getChunk();

            if (ChunksManager.isChunkClaimed(chunk)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                if (region != null && !region.isWorldFlagSet(WorldFlags.WITHER_DAMAGE)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onSnowGolemTrail(EntityBlockFormEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Snowman) {
            Block block = event.getBlock();
            Chunk chunk = block.getChunk();

            if (ChunksManager.isChunkClaimed(chunk)) {
                Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

                if (region != null && !region.isWorldFlagSet(WorldFlags.SNOWMAN_TRAILS)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onTreeGrow(StructureGrowEvent event) {
        Chunk chunk = event.getLocation().getChunk();

        if (ChunksManager.isChunkClaimed(chunk)) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            if (!region.isWorldFlagSet(WorldFlags.PLANT_GROWTH)) {
                event.setCancelled(true);
            }
        }
    }

    // Helper functions

    private boolean isWearingElytra(Player player) {
        return player.getInventory().getChestplate() != null &&
                player.getInventory().getChestplate().getType() == Material.ELYTRA;
    }

    private boolean isCropBlock(Block block) {
        Material type = block.getType();

        return type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES
                || type == Material.BEETROOTS || type == Material.PITCHER_PLANT || type == Material.NETHER_WART
                || type == Material.KELP || type == Material.CACTUS || type == Material.SEA_PICKLE
                || type == Material.RED_MUSHROOM || type == Material.BROWN_MUSHROOM || type == Material.SWEET_BERRIES
                || type == Material.SWEET_BERRY_BUSH;
    }
}
