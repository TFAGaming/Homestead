package tfagaming.projects.minecraft.homestead.listeners;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.particles.SelectedAreaParticlesSpawner;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBlock;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class SelectionToolListener implements Listener {
    private static final HashSet<UUID> cooldowns = new HashSet<>();
    private static final HashMap<UUID, Selection> sessions = new HashMap<>();
    private static final HashMap<UUID, BukkitTask> tasks = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() == getSelectionToolType()) {
            UUID playerId = player.getUniqueId();

            sessions.putIfAbsent(playerId, new Selection());

            Selection selection = sessions.get(playerId);

            Map<String, String> replacements = new HashMap<>();

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);

                if (cooldowns.contains(player.getUniqueId())) {
                    return;
                }

                Block firstPosition = event.getClickedBlock();

                if (selection.getSecondPosition() != null && !firstPosition.getWorld().getName()
                        .equals(selection.getSecondPosition().getWorld().getName())) {
                    return;
                }

                selection.setFirstPosition(firstPosition);

                cooldowns.add(player.getUniqueId());

                Homestead.getInstance().runAsyncTaskLater(() -> {
                    cooldowns.remove(player.getUniqueId());
                }, 1);

                replacements.put("{location}", Formatters.formatLocation(firstPosition.getLocation()));

                sendActionBarMessage(player, "firstCorner");

                if (selection.getSecondPosition() != null && selection.getFirstPosition() != null) {
                    sendActionBarMessage(player, "selectionDone");

                    new SelectedAreaParticlesSpawner(player, new SerializableBlock(selection.getFirstPosition()),
                            new SerializableBlock(selection.getSecondPosition()));
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);

                if (cooldowns.contains(player.getUniqueId())) {
                    return;
                }

                Block secondPosition = event.getClickedBlock();

                if (selection.getFirstPosition() != null && !secondPosition.getWorld().getName()
                        .equals(selection.getFirstPosition().getWorld().getName())) {
                    return;
                }

                selection.setSecondPosition(secondPosition);

                cooldowns.add(player.getUniqueId());

                Homestead.getInstance().runAsyncTaskLater(() -> {
                    cooldowns.remove(player.getUniqueId());
                }, 1);

                replacements.put("{location}", Formatters.formatLocation(secondPosition.getLocation()));

                sendActionBarMessage(player, "secondCorner");

                if (selection.getSecondPosition() != null && selection.getFirstPosition() != null) {
                    sendActionBarMessage(player, "selectionDone");

                    new SelectedAreaParticlesSpawner(player, new SerializableBlock(selection.getFirstPosition()),
                            new SerializableBlock(selection.getSecondPosition()));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        UUID playerId = player.getUniqueId();

        if (newItem == null || newItem.getType() != getSelectionToolType()) {
            cancelPlayerSession(player);
        } else {
            sessions.putIfAbsent(playerId, new Selection());

            sendActionBarMessage(player, "none");
        }
    }

    public static void cancelPlayerSession(Player player) {
        if (sessions.containsKey(player.getUniqueId())) {
            sessions.remove(player.getUniqueId());

            SelectedAreaParticlesSpawner.cancelTask(player);
        }

        cancelTask(player);
    }

    public static void cancelTask(BukkitTask task, Player player) {
        if (task != null) {
            tasks.remove(player.getUniqueId());

            task.cancel();
            task = null;
        }
    }

    public static void cancelTask(Player player) {
        BukkitTask task = tasks.get(player.getUniqueId());

        if (task != null) {
            tasks.remove(player.getUniqueId());

            task.cancel();
            task = null;
        }
    }

    private void sendActionBarMessage(Player player, String path) {
        String message = Homestead.config.get("selection-tool.messages." + path);

        cancelTask(player);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColorTranslator
                                .translate(message)));
            }
        }.runTaskTimer(Homestead.getInstance(), 0L, 20L);

        tasks.put(player.getUniqueId(), task);
    }

    private Material getSelectionToolType() {
        String itemString = Homestead.config.get("selection-tool.item");

        return Material.getMaterial(itemString);
    }

    public static Selection getPlayerSession(Player player) {
        Selection selection = sessions.get(player.getUniqueId());

        if (selection == null || selection.getSecondPosition() == null || selection.getFirstPosition() == null) {
            return null;
        }

        return selection;
    }

    public static class Selection {
        private Block firstPosition;
        private Block secondPosition;

        public void setFirstPosition(Block firstPosition) {
            this.firstPosition = firstPosition;
        }

        public void setSecondPosition(Block secondPosition) {
            this.secondPosition = secondPosition;
        }

        public Block getFirstPosition() {
            return firstPosition;
        }

        public Block getSecondPosition() {
            return secondPosition;
        }
    }
}
