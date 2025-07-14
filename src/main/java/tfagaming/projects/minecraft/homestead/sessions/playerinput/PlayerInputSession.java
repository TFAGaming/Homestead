package tfagaming.projects.minecraft.homestead.sessions.playerinput;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Function;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PlayerInputSession implements Listener {
    private final Player player;
    private final BiConsumer<Player, String> callback;
    private final Function<String, Boolean> validator;
    private final Consumer<Player> onCancel;
    private static final Map<UUID, PlayerInputSession> activeInputs = new HashMap<>();
    private static final HashMap<UUID, BukkitTask> tasks = new HashMap<>();

    public PlayerInputSession(Homestead plugin, Player player,
            BiConsumer<Player, String> callback,
            Function<String, Boolean> validator, Consumer<Player> onCancel, int messagePath) {
        this.player = player;
        this.callback = callback;
        this.validator = validator;
        this.onCancel = onCancel;

        if (!activeInputs.containsKey(player.getUniqueId())) {
            Bukkit.getPluginManager().registerEvents(this, plugin);

            activeInputs.put(player.getUniqueId(), this);

            sendActionBarMessage(player, messagePath);

            Homestead.getInstance().runAsyncTaskLater(() -> {
                if (activeInputs.containsKey(player.getUniqueId())) {
                    destroy(player);
                }
            }, 60);
        }
    }

    public void destroy(Player player) {
        HandlerList.unregisterAll(activeInputs.get(player.getUniqueId()));

        activeInputs.remove(player.getUniqueId());

        cancelTask(player);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!isWaitingForInput(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (isWaitingForInput(player)) {
            destroy(player);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player)) {
            return;
        }

        if (!isWaitingForInput(player)) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancel")) {
            this.onCancel.accept(player);

            destroy(player);

            return;
        }

        boolean response = validator.apply(message);

        if (response) {
            callback.accept(player, message);

            destroy(player);
        }
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

    private void sendActionBarMessage(Player player, int path) {
        String message = Homestead.language.get(String.valueOf(path));

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

    public static boolean isWaitingForInput(Player player) {
        return activeInputs.containsKey(player.getUniqueId());
    }
}