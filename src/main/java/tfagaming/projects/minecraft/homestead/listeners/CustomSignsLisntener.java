package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLocation;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CustomSignsLisntener implements Listener {
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();

        if (lines.length < 1)
            return;

        String firstLine = lines[0].trim();

        boolean breakBlock = false;

        switch (firstLine.toLowerCase()) {
            case "[welcome]":
                breakBlock = handleWelcomeSign(event, player, lines);
                break;
            case "[rent]":
                breakBlock = handleRentSign(event, player, lines);
                break;
            case "[sell]":
                breakBlock = handleSellSign(event, player, lines);
                break;
            default:
                return;
        }

        if (breakBlock) {
            event.getBlock().breakNaturally();
        }
    }

    @EventHandler
    public void onPlayerRightClickSign(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        Block clickedBlock = event.getClickedBlock();

        if (!(clickedBlock.getState() instanceof Sign)) {
            return;
        }

        Sign sign = (Sign) clickedBlock.getState();
        String[] lines = sign.getSide(Side.FRONT).getLines();

        if (lines.length == 0)
            return;

        String firstLine = lines[0].trim();

        String strippedFirstLine = ChatColor.stripColor(firstLine);

        switch (strippedFirstLine.toLowerCase()) {
            case "[welcome]": {
                event.setCancelled(true);

                PlayerUtils.sendMessage(player, 123);

                break;
            }
            case "[rent]": {
                event.setCancelled(true);

                handleRentSignInteraction(player, lines, event.getClickedBlock());

                break;
            }
            case "[sell]": {
                event.setCancelled(true);

                handleSellSignInteraction(player, lines, event.getClickedBlock());

                break;
            }
            default:
                break;
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Chunk chunk = event.getBlock().getChunk();

        if (ChunksManager.isChunkClaimed(chunk) && event.getBlock() != null && event.getBlock().getType().toString().toLowerCase().contains("sign")) {
            Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

            BlockState state = event.getBlock().getState();
            Sign sign = (Sign) state;

            String[] lines = sign.getSide(Side.FRONT).getLines();
            List<String> cleanLines = new ArrayList<>();

            for (String line : lines) {
                cleanLines.add(ChatColor.stripColor(line));
            }

            if (lines.length > 0 && ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Welcome]")) {
                region.setWelcomeSign(null);
            }
        }
    }

    private boolean handleWelcomeSign(SignChangeEvent event, Player player, String[] lines) {
        if (lines.length < 4) {
            PlayerUtils.sendMessage(player, 120);
            return true;
        }

        Region region = validateRegion(player, event.getBlock().getChunk());

        if (region == null) {
            return true;
        }

        if (!lines[2].trim().isEmpty() || !lines[3].trim().isEmpty()) {
            PlayerUtils.sendMessage(player, 121);
            return true;
        }

        event.setLine(0, ChatColor.GREEN + "[Welcome]");
        event.setLine(1, ChatColor.DARK_GREEN + region.getName());
        event.setLine(2, "");
        event.setLine(3, "");

        region.setWelcomeSign(new SerializableLocation(event.getBlock().getLocation()));

        return false;
    }

    private boolean handleRentSign(SignChangeEvent event, Player player, String[] lines) {
        boolean isEnabled = Homestead.config.get("renting.enabled");

        if (!isEnabled) {
            PlayerUtils.sendMessage(player, 105);
            return true;
        }

        if (lines.length < 4) {
            PlayerUtils.sendMessage(player, 120);
            return true;
        }

        Region region = validateRegion(player, event.getBlock().getChunk());

        if (region == null) {
            return true;
        }

        String priceStr = lines[2].trim();

        if (!NumberUtils.isValidDouble(priceStr)) {
            PlayerUtils.sendMessage(player, 122);
            return true;
        }

        double price = Double.parseDouble(priceStr);

        double minRent = Homestead.config.get("renting.min-rent");
        double maxRent = Homestead.config.get("renting.max-rent");

        if (price < minRent || price > maxRent) {
            PlayerUtils.sendMessage(player, 122);
            return true;
        }

        String durationStr = lines[3].trim();
        long durationMs = parseDurationToMillis(durationStr);

        if (durationMs <= 0 || durationMs > 6048000000L) {
            PlayerUtils.sendMessage(player, 129);
            return true;
        }

        event.setLine(0, ChatColor.GREEN + "[Rent]");
        event.setLine(1, ChatColor.DARK_GREEN + region.getName());
        event.setLine(2, ChatColor.RED + Formatters.formatBalance(price));
        event.setLine(3, ChatColor.GOLD + formatMillisToReadable(durationMs));

        return false;
    }

    private boolean handleSellSign(SignChangeEvent event, Player player, String[] lines) {
        boolean isEnabled = Homestead.config.get("selling.enabled");

        if (!isEnabled) {
            PlayerUtils.sendMessage(player, 105);
            return true;
        }

        if (lines.length < 4) {
            PlayerUtils.sendMessage(player, 120);
            return true;
        }

        Region region = validateRegion(player, event.getBlock().getChunk());

        if (region == null) {
            return true;
        }

        String priceStr = lines[2].trim();
        if (!NumberUtils.isValidDouble(priceStr)) {
            PlayerUtils.sendMessage(player, 122);
            return true;
        }

        double price = Double.parseDouble(priceStr);

        double minSell = Homestead.config.get("selling.min-sell");
        double maxSell = Homestead.config.get("selling.max-sell");

        if (price < minSell || price > maxSell) {
            PlayerUtils.sendMessage(player, 122);
            return true;
        }

        if (!lines[3].trim().isEmpty()) {
            PlayerUtils.sendMessage(player, 121);
            return true;
        }

        event.setLine(0, ChatColor.GREEN + "[Sell]");
        event.setLine(1, ChatColor.DARK_GREEN + region.getName());
        event.setLine(2, ChatColor.RED + Formatters.formatBalance(price));
        event.setLine(3, "");

        return false;
    }

    private Region validateRegion(Player player, Chunk chunk) {
        Region region = ChunksManager.getRegionOwnsTheChunk(chunk);

        if (region == null || (region != null && !region.getOwnerId().equals(player.getUniqueId()))) {
            PlayerUtils.sendMessage(player, 119);
            return null;
        }

        return region;
    }

    private long parseDurationToMillis(String duration) {
        if (duration == null || duration.isEmpty()) {
            return 0;
        }

        try {
            String numStr = duration.replaceAll("[^0-9]", "");
            if (numStr.isEmpty()) {
                return 0;
            }

            long num = Long.parseLong(numStr);

            char unit = duration.replaceAll("[0-9]", "").toLowerCase().charAt(0);

            switch (unit) {
                case 's':
                    return num * 1000;
                case 'm':
                    return num * 60 * 1000;
                case 'h':
                    return num * 60 * 60 * 1000;
                case 'd':
                    return num * 24 * 60 * 60 * 1000;
                case 'w':
                    return num * 7 * 24 * 60 * 60 * 1000;
                default:
                    return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatMillisToReadable(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        }

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long days = TimeUnit.MILLISECONDS.toDays(millis) % 7;
        long weeks = TimeUnit.MILLISECONDS.toDays(millis) / 7;

        StringBuilder sb = new StringBuilder();

        if (weeks > 0) {
            sb.append(weeks).append(weeks == 1 ? " Week " : " Weeks ");
        }
        if (days > 0) {
            sb.append(days).append(days == 1 ? " Day " : " Days ");
        }
        if (hours > 0) {
            sb.append(hours).append(hours == 1 ? " Hour " : " Hours ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(minutes == 1 ? " Minute " : " Minutes ");
        }
        if (seconds > 0) {
            sb.append(seconds).append(seconds == 1 ? " Second" : " Seconds");
        }

        return sb.toString().trim();
    }

    private void handleRentSignInteraction(Player player, String[] lines, Block sign) {
        try {
            String regionName = ChatColor.stripColor(lines[1].trim());

            // Parse price (line 2)
            String priceStr = ChatColor.stripColor(lines[2].trim());
            double price = parseFormattedPrice(priceStr);

            String durationStr = ChatColor.stripColor(lines[3].trim());
            long durationMs = parseFormattedDuration(durationStr);

            Region region = RegionsManager.findRegion(regionName);

            if (region == null) {
                PlayerUtils.sendMessage(player, 9);
                return;
            }

            if (region.getOwnerId().equals(player.getUniqueId()) || region.isPlayerBanned(player)) {
                PlayerUtils.sendMessage(player, 30);
                return;
            }

            if (price > PlayerUtils.getBalance(player)) {
                PlayerUtils.sendMessage(player, 125);
                return;
            }

            long rentEnd = System.currentTimeMillis() + durationMs;

            PlayerUtils.removeBalance(player, price);
            PlayerUtils.addBalance(region.getOwner(), price);

            SerializableRent rent = new SerializableRent(player, price, rentEnd);

            region.setRent(rent);

            sign.breakNaturally();

            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{region}", region.getName());
            replacements.put("{rent-end}", Formatters.formatRemainingTime(rentEnd));

            PlayerUtils.sendMessage(player, 126, replacements);

        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Error: This rent sign has invalid formatting!");
        }
    }

    private void handleSellSignInteraction(Player player, String[] lines, Block sign) {
        try {
            String regionName = ChatColor.stripColor(lines[1].trim());

            String priceStr = ChatColor.stripColor(lines[2].trim());
            double price = parseFormattedPrice(priceStr);

            Region region = RegionsManager.findRegion(regionName);

            if (region == null) {
                PlayerUtils.sendMessage(player, 9);
                return;
            }

            if (region.getOwnerId().equals(player.getUniqueId()) || region.isPlayerBanned(player)) {
                PlayerUtils.sendMessage(player, 30);
                return;
            }

            if (price > PlayerUtils.getBalance(player)) {
                PlayerUtils.sendMessage(player, 125);
                return;
            }

            PlayerUtils.removeBalance(player, price);
            PlayerUtils.addBalance(region.getOwner(), price);

            region.setOwner(player);

            sign.breakNaturally();

            if (region.isPlayerMember(player)) {
                region.removeMember(player);
            }

            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("{region}", region.getName());
            replacements.put("{price}", Formatters.formatBalance(price));

            PlayerUtils.sendMessage(player, 124, replacements);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Error: This sell sign has invalid formatting!");
        }
    }

    // From DeepSeek (Thanks bro)
    private double parseFormattedPrice(String input) throws NumberFormatException {
        // Remove all currency symbols and whitespace
        String cleanInput = input.replaceAll("[^0-9.,]", "").trim();

        // Handle cases where comma is used as decimal separator (e.g., "1,00")
        if (cleanInput.matches(".*,\\d{2}$")) {
            // Replace comma with dot for decimal parsing
            cleanInput = cleanInput.replace(".", "").replace(",", ".");
        }
        // Handle cases where dot is used as decimal separator (e.g., "1.00")
        else if (cleanInput.matches(".*\\.\\d{2}$")) {
            // Remove thousand separators if they exist (e.g., "1.000,00" -> "1000.00")
            cleanInput = cleanInput.replace(",", "");
        }
        // Handle ambiguous cases (e.g., "1,000" could be 1.000 or 1000)
        else {
            // If there's only one comma or dot, assume it's a thousand separator
            if (cleanInput.chars().filter(c -> c == ',' || c == '.').count() == 1) {
                cleanInput = cleanInput.replace(",", "").replace(".", "");
            }
        }

        // Handle k/m/b suffixes
        if (input.toLowerCase().contains("k")) {
            return Double.parseDouble(cleanInput) * 1000;
        } else if (input.toLowerCase().contains("m")) {
            return Double.parseDouble(cleanInput) * 1000000;
        } else if (input.toLowerCase().contains("b")) {
            return Double.parseDouble(cleanInput) * 1000000000;
        }

        return Double.parseDouble(cleanInput);
    }

    private long parseFormattedDuration(String input) throws NumberFormatException {
        input = input.toLowerCase();

        if (input.contains("second")) {
            double seconds = Double.parseDouble(input.replaceAll("[^0-9.]", ""));
            return (long) (seconds * 1000);
        } else if (input.contains("minute")) {
            double minutes = Double.parseDouble(input.replaceAll("[^0-9.]", ""));
            return (long) (minutes * 60 * 1000);
        } else if (input.contains("hour")) {
            double hours = Double.parseDouble(input.replaceAll("[^0-9.]", ""));
            return (long) (hours * 60 * 60 * 1000);
        } else if (input.contains("day")) {
            double days = Double.parseDouble(input.replaceAll("[^0-9.]", ""));
            return (long) (days * 24 * 60 * 60 * 1000);
        } else if (input.contains("week")) {
            double weeks = Double.parseDouble(input.replaceAll("[^0-9.]", ""));
            return (long) (weeks * 7 * 24 * 60 * 60 * 1000);
        }

        return parseDurationToMillis(input);
    }
}
