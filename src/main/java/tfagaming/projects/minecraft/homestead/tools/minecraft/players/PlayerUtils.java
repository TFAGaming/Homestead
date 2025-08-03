package tfagaming.projects.minecraft.homestead.tools.minecraft.players;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableSubArea;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits.LimitMethod;

public class PlayerUtils {
    private static HashSet<UUID> cooldown = new HashSet<UUID>();

    public static boolean hasAvailableSlot(Player player) {
        return player.getInventory().firstEmpty() == -1 ? false : true;
    }

    public static double getBalance(OfflinePlayer player) {
        if (!Homestead.vault.isEconomyReady()) {
            return 0.0;
        }

        return Homestead.vault.getEconomy().getBalance(player);
    }

    public static void addBalance(OfflinePlayer player, double amount) {
        if (!Homestead.vault.isEconomyReady()) {
            return;
        }

        Homestead.vault.getEconomy().depositPlayer(player, amount);
    }

    public static void removeBalance(OfflinePlayer player, double amount) {
        if (!Homestead.vault.isEconomyReady()) {
            return;
        }

        Homestead.vault.getEconomy().withdrawPlayer(player, amount);
    }

    public static void sendMessage(Player player, String... messages) {
        player.sendMessage(ChatColorTranslator.translate(String.join("", messages)));
    }

    public static void sendMessage(CommandSender sender, String path, Map<String, String> replacements) {
        String message = Homestead.language.get(path);

        if (message == null) {
            sender.sendMessage("String not found from the language file: " + path);
            return;
        }

        message = Formatters.replace(message, replacements);

        sender.sendMessage(ChatColorTranslator.translate(Homestead.config.getPrefix() + message));
    }

    public static void sendMessage(CommandSender sender, String path) {
        String message = Homestead.language.get(path);

        if (message == null) {
            sender.sendMessage("String not found from the language file: " + path);
            return;
        }

        sender.sendMessage(ChatColorTranslator.translate(Homestead.config.getPrefix() + message));
    }

    public static void sendMessage(Player player, int path, Map<String, String> replacements) {
        sendMessage(player, String.valueOf(path), replacements);
    }

    public static void sendMessage(CommandSender sender, int path, Map<String, String> replacements) {
        sendMessage(sender, String.valueOf(path), replacements);
    }

    public static void sendMessage(CommandSender sender, int path) {
        sendMessage(sender, String.valueOf(path));
    }

    public static void sendMessage(Player player, int path) {
        sendMessage(player, String.valueOf(path));
    }

    public static void sendMessageRegionEnter(Player player, Map<String, String> replacements) {
        switch (((String) Homestead.config.get("enter-exit-region-message.type")).toLowerCase()) {
            case "title":
                List<String> titleData = Homestead.config.get("enter-exit-region-message.messages.enter.title");

                player.sendTitle(ChatColorTranslator.translate(Formatters.replace(titleData.get(0), replacements)),
                        ChatColorTranslator.translate(Formatters.replace(titleData.get(1), replacements)), 10, 70,
                        20);

                break;
            case "actionbar":
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColorTranslator
                                .translate(Formatters.replace(
                                        Homestead.config.get("enter-exit-region-message.messages.enter.actionbar"),
                                        replacements))));

                break;
            default:
                player.sendMessage(ChatColorTranslator.translate(Formatters.replace(
                        Homestead.config.get("enter-exit-region-message.messages.enter.chat"),
                        replacements)));
                break;
        }
    }

    public static void sendMessageRegionExit(Player player, Map<String, String> replacements) {
        switch (((String) Homestead.config.get("enter-exit-region-message.type")).toLowerCase()) {
            case "title":
                List<String> titleData = Homestead.config.get("enter-exit-region-message.messages.exit.title");

                player.sendTitle(ChatColorTranslator.translate(Formatters.replace(titleData.get(0), replacements)),
                        ChatColorTranslator.translate(Formatters.replace(titleData.get(1), replacements)), 10, 70,
                        20);

                break;
            case "actionbar":
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColorTranslator
                                .translate(Formatters.replace(
                                        Homestead.config.get("enter-exit-region-message.messages.exit.actionbar"),
                                        replacements))));

                break;
            default:
                player.sendMessage(ChatColorTranslator.translate(Formatters.replace(
                        Homestead.config.get("enter-exit-region-message.messages.exit.chat"),
                        replacements)));
                break;
        }
    }

    public static void teleportPlayerToChunk(Player player, Chunk chunk) {
        Location location = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, 64,
                chunk.getZ() * 16 + 8);

        location.setY(location.getWorld().getHighestBlockYAt(location) + 2);
        location.setPitch(player.getLocation().getPitch());
        location.setYaw(player.getLocation().getYaw());

        player.teleport(location);
    }

    public static boolean isOperator(Player player) {
        if (player.isOp()) {
            return true;
        } else if (player.hasPermission("homestead.operator")) {
            return true;
        }

        return false;
    }

    public static boolean isOperator(OfflinePlayer player) {
        if (player.isOp()) {
            return true;
        }

        return false;
    }

    public static boolean hasPermissionFlag(UUID regionId, Player player, long flag) {
        Region region = RegionsManager.findRegion(regionId);

        if (region != null) {
            boolean response = true;

            SerializableRent rent = region.getRent();

            if (rent != null && rent.getPlayerId() != null
                    && rent.getPlayerId().equals(player.getUniqueId()) && flag != PlayerFlags.PVP) {
                response = true;
            } else {
                if (region.isPlayerMember(player)) {
                    SerializableMember member = region.getMember(player);

                    response = FlagsCalculator.isFlagSet(member.getFlags(), flag);
                } else {
                    response = FlagsCalculator.isFlagSet(region.getPlayerFlags(), flag);
                }
            }

            if (!response && !cooldown.contains(player.getUniqueId())) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{flag}", PlayerFlags.from(flag));
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 50, replacements);

                cooldown.add(player.getUniqueId());

                Homestead.getInstance().runAsyncTaskLater(() -> {
                    cooldown.remove(player.getUniqueId());
                }, 3);
            }

            return response;
        }

        return true;
    }

    public static boolean hasPermissionFlag(UUID regionId, UUID subAreaId, Player player, long flag) {
        Region region = RegionsManager.findRegion(regionId);

        if (region != null) {
            SerializableSubArea subArea = region.getSubArea(subAreaId);

            boolean response = true;

            SerializableRent rent = region.getRent();

            if (rent != null && rent.getPlayerId() != null
                    && rent.getPlayerId().equals(player.getUniqueId()) && flag != PlayerFlags.PVP) {
                response = true;
            } else {
                if (region.isPlayerMember(player)) {
                    SerializableMember member = region.getMember(player);

                    response = FlagsCalculator.isFlagSet(member.getFlags(), flag);
                } else {
                    response = FlagsCalculator.isFlagSet(subArea.getFlags(), flag);
                }
            }

            if (!response && !cooldown.contains(player.getUniqueId())) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{flag}", PlayerFlags.from(flag));
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 50, replacements);

                cooldown.add(player.getUniqueId());

                Homestead.getInstance().runAsyncTaskLater(() -> {
                    cooldown.remove(player.getUniqueId());
                }, 3);
            }

            return response;
        }

        return true;
    }

    public static boolean hasControlRegionPermissionFlag(UUID regionId, Player player, long flag) {
        Region region = RegionsManager.findRegion(regionId);

        if (region != null) {
            if (PlayerUtils.isOperator(player) || player.getUniqueId().equals(region.getOwnerId())) {
                return true;
            }

            boolean response = true;

            if (region.isPlayerMember(player)) {
                SerializableMember member = region.getMember(player);

                response = FlagsCalculator.isFlagSet(member.getRegionControlFlags(), flag);
            }

            if (!response && !cooldown.contains(player.getUniqueId())) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{flag}", RegionControlFlags.from(flag));
                replacements.put("{region}", region.getName());

                PlayerUtils.sendMessage(player, 70, replacements);

                cooldown.add(player.getUniqueId());

                Homestead.getInstance().runAsyncTaskLater(() -> {
                    cooldown.remove(player.getUniqueId());
                }, 3);
            }

            return response;
        }

        return true;
    }

    public static String getPlayerGroup(OfflinePlayer player) {
        if (PlayerLimits.getLimitsMethod() != LimitMethod.GROUPS) {
            return null;
        }

        try {
            if (player.isOnline()) {
                return Homestead.vault.getPermissions().getPrimaryGroup((Player) player);
            } else {
                return Homestead.vault.getPermissions().getPrimaryGroup(player.getLocation().getWorld().getName(),
                        player);
            }
        } catch (UnsupportedOperationException e) {
            Logger.error(
                    "Unable to find a service provider for permissions and groups, using the default group \"default\".");
            Logger.error(
                    "Please install a plugin that supports permissions and groups. We recommend installing the LuckPerms plugin.");
            Logger.error(
                    "To ignore this warning, change the limits method to \"static\" in this setting: limits.method");
        }

        return null;
    }
}
