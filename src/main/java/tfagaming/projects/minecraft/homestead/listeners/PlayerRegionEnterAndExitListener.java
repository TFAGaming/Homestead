package tfagaming.projects.minecraft.homestead.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.weatherandtime.TimeType;

public class PlayerRegionEnterAndExitListener implements Listener {
    private static final Map<UUID, UUID> sessions = new HashMap<UUID, UUID>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = (Player) event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();

        if (!(event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()
                || event.getFrom().getBlockY() != event.getTo().getBlockY())) {
            return;
        }

        boolean isFeatureEnabled = Homestead.config.get("enter-exit-region-message.enabled");

        if (ChunksManager.isChunkClaimed(chunk)) {
            // Player enters a region

            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            if (sessions.containsKey(player.getUniqueId())
                    && sessions.get(player.getUniqueId()).equals(region.getUniqueId())) {
                return;
            }

            if (region.isPlayerBanned(player)) {
                Chunk nearbyChunk = ChunksManager.findNearbyUnclaimedChunk(player);

                if (nearbyChunk != null) {
                    PlayerUtils.teleportPlayerToChunk(player, nearbyChunk);
                }

                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{region}", region.getName());
                replacements.put("{ban-reason}", region.getBannedPlayer(player).getReason());

                PlayerUtils.sendMessage(player, 28, replacements);

                return;
            }

            if (!PlayerUtils.isOperator(player) && !player.getUniqueId().equals(region.getOwnerId())
                    && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH)) {
                Chunk nearbyChunk = ChunksManager.findNearbyUnclaimedChunk(player);

                if (nearbyChunk != null) {
                    PlayerUtils.teleportPlayerToChunk(player, nearbyChunk);
                }

                return;
            }

            if (isFeatureEnabled) {
                Map<String, String> replacements = new HashMap<String, String>();
                replacements.put("{region-displayname}", region.getDisplayName());
                replacements.put("{region-owner}", region.getOwner().getName());
                replacements.put("{region-description}", region.getDescription().replace("%player%", player.getName()));

                PlayerUtils.sendMessageRegionEnter(player, replacements);
            }

            sessions.put(player.getUniqueId(), region.getUniqueId());

            // Weather and Time
            if (region.getWeather() != tfagaming.projects.minecraft.homestead.weatherandtime.WeatherType.SERVER) {
                switch (region.getWeather()) {
                    case tfagaming.projects.minecraft.homestead.weatherandtime.WeatherType.CLEAR:
                        player.setPlayerWeather(WeatherType.CLEAR);
                        break;
                    case tfagaming.projects.minecraft.homestead.weatherandtime.WeatherType.RAIN:
                        player.setPlayerWeather(WeatherType.DOWNFALL);
                    default:
                        break;
                }
            }

            if (region.getTime() != TimeType.SERVER) {
                player.setPlayerTime(region.getTime(), false);
            }

            // Glowing
            if (region.isWorldFlagSet(WorldFlags.PLAYER_GLOWING)) {
                if (!player.hasPotionEffect(PotionEffectType.GLOWING)) {
                    player.addPotionEffect(
                            new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1, false, false));
                }
            }

            // Checking if player has an elytra
            if (player.isGliding() && isWearingElytra(player) && !PlayerUtils.isOperator(player)) {
                if (!player.getUniqueId().equals(region.getOwnerId())
                        && !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.ELYTRA)) {
                    player.setGliding(false);
                }
            }
        } else {
            // Player leaves a region

            if (!sessions.containsKey(player.getUniqueId())) {
                return;
            }

            Region region = RegionsManager.findRegion(sessions.get(player.getUniqueId()));

            if (isFeatureEnabled) {
                Map<String, String> replacements = new HashMap<String, String>();

                if (region != null) {
                    replacements.put("{region-displayname}", region.getDisplayName());
                    replacements.put("{region-owner}", region.getOwner().getName());
                    replacements.put("{region-description}", region.getDescription());
                }

                PlayerUtils.sendMessageRegionExit(player, replacements);
            }

            sessions.remove(player.getUniqueId());

            if (player.getPlayerWeather() != null) {
                player.resetPlayerWeather();
            }

            if (player.getPlayerTimeOffset() != 0) {
                player.resetPlayerTime();
            }

            if (player.hasPotionEffect(PotionEffectType.GLOWING)) {
                player.removePotionEffect(PotionEffectType.GLOWING);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (sessions.containsKey(player.getUniqueId())) {
            sessions.remove(player.getUniqueId());
        }
    }

    private boolean isWearingElytra(Player player) {
        return player.getInventory().getChestplate() != null &&
                player.getInventory().getChestplate().getType() == Material.ELYTRA;
    }
}
