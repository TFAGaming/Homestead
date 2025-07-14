package tfagaming.projects.minecraft.homestead.tools.java;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;

public class Formatters {
    public static String replace(String string, Map<String, String> replacements) {
        if (string == null) {
            return "[Homestead: String is null]";
        }

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            string = string.replace(entry.getKey(), entry.getValue() == null ? "null" : entry.getValue());
        }

        return string;
    }

    public static String formatLocation(Location location) {
        HashMap<String, String> replacements = new HashMap<String, String>();

        replacements.put("{world}", location.getWorld().getName());
        replacements.put("{x}", String.valueOf(NumberUtils.truncateToTwoDecimalPlaces(location.getX())));
        replacements.put("{y}", String.valueOf(NumberUtils.truncateToTwoDecimalPlaces(location.getY())));
        replacements.put("{z}", String.valueOf(NumberUtils.truncateToTwoDecimalPlaces(location.getZ())));

        return ChatColorTranslator.translate(replace(Homestead.config.get("formatters.location"), replacements));
    }

    public static String formatChunk(Chunk chunk) {
        HashMap<String, String> replacements = new HashMap<String, String>();

        replacements.put("{world}", chunk.getWorld().getName());
        replacements.put("{x}", String.valueOf(NumberUtils.truncateToTwoDecimalPlaces(chunk.getX())));
        replacements.put("{z}", String.valueOf(NumberUtils.truncateToTwoDecimalPlaces(chunk.getZ())));

        return ChatColorTranslator.translate(replace(Homestead.config.get("formatters.location"), replacements));
    }

    public static String formatBalance(double amount) {
        String balance = NumberUtils.convertDoubleToBalance(amount);
        String format = Homestead.config.get("formatters.balance");

        return format.replace("{balance}", balance);
    }

    public static String formatDate(long date) {
        String pattern = Homestead.config.get("formatters.date-format");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String formatted = simpleDateFormat.format(date);
        String dateWithAgo = Homestead.config.get("formatters.date");

        Map<String, String> replacements = new HashMap<String, String>();
        
        replacements.put("{date}", formatted);
        replacements.put("{time-ago}", getAgo(date));

        return replace(dateWithAgo, replacements);
    }

    public static String formatRating(double rate) {
        return formatRating(Math.round((float) rate));
    }

    public static String formatRating(int rate) {
        String star = Homestead.language.get("default.star");

        switch (rate) {
            case 1:
                return ChatColorTranslator.translate("&#FF0000" + star.repeat(1));
            case 2:
                return ChatColorTranslator.translate("&#FF5900" + star.repeat(2));
            case 3:
                return ChatColorTranslator.translate("&#FFE600" + star.repeat(3));
            case 4:
                return ChatColorTranslator.translate("&#80FF00" + star.repeat(4));
            case 5:
                return ChatColorTranslator.translate("&#00FF08" + star.repeat(5));
            default:
                return getNone();
        }
    }

    public static String formatRemainingTime(long time) {
        long currentTime = System.currentTimeMillis();

        long differenceMillis = time - currentTime;
        long totalSeconds = differenceMillis / 1000;

        long days = totalSeconds / 86400;
        long remaining = totalSeconds % 86400;

        long hours = remaining / 3600;
        remaining = remaining % 3600;

        long minutes = remaining / 60;
        long seconds = remaining % 60;

        HashMap<String, String> replacements = new HashMap<String, String>();

        replacements.put("{d}", String.valueOf(days));
        replacements.put("{h}", String.valueOf(hours));
        replacements.put("{m}", String.valueOf(minutes));
        replacements.put("{s}", String.valueOf(seconds));

        return ChatColorTranslator
                .translate(replace(Homestead.config.get("formatters.remaining-time"), replacements));
    }

    public static String formatPaginationMenuTitle(String title, int currentPage, int totalPages) {
        HashMap<String, String> replacements = new HashMap<String, String>();

        replacements.put("{title}", title);
        replacements.put("{current-page}", String.valueOf(currentPage));
        replacements.put("{total-pages}", String.valueOf(totalPages));

        return ChatColorTranslator
                .translate(replace(Homestead.config.get("formatters.gui-pagination-title"), replacements));
    }

    public static String getPlayerOwnedRegions(OfflinePlayer player) {
        List<Region> regions = RegionsManager.getRegionsOwnedByPlayer(player);

        if (regions.size() == 0) {
            return getNone();
        }

        String format = Homestead.config.get("formatters.player-regions");

        return ChatColorTranslator.translate(regions.stream()
                .map((region) -> format.replace("{region}", region.getName()))
                .collect(Collectors.joining(Homestead.config.get("formatters.player-regions-joining"))));
    }

    public static String getPlayerTrustedRegions(OfflinePlayer player) {
        List<Region> regions = RegionsManager.getRegionsHasPlayerAsMember(player);

        if (regions.size() == 0) {
            return getNone();
        }

        String format = Homestead.config.get("formatters.player-regions");

        return ChatColorTranslator.translate(regions.stream()
                .map((region) -> format.replace("{region}", region.getName()))
                .collect(Collectors.joining(Homestead.config.get("formatters.player-regions-joining"))));
    }

    public static String getMembersOfRegion(Region region) {
        List<SerializableMember> members = region.getMembers();

        if (members.size() == 0) {
            return getNone();
        }

        String format = Homestead.config.get("formatters.region-members");

        return ChatColorTranslator.translate(members.stream()
                .map((member) -> format.replace("{playername}", member.getBukkitOfflinePlayer().getName()))
                .collect(Collectors.joining(Homestead.config.get("formatters.region-members-joining"))));
    }

    public static String getNone() {
        return ChatColorTranslator.translate(Homestead.language.get("default.none"));
    }

    public static String getBoolean(boolean value) {
        return ChatColorTranslator.translate(Homestead.language.get(value ? "default.isTrue" : "default.isFalse"));
    }

    public static String getEnabled(boolean value) {
        return ChatColorTranslator
                .translate(Homestead.language.get(value ? "default.isEnabled" : "default.isDisabled"));
    }

    public static String getFlag(boolean value) {
        return ChatColorTranslator.translate(Homestead.language.get(value ? "default.flagSet" : "default.flagUnset"));
    }

    public static String getPlayerStatus(OfflinePlayer player) {
        return ChatColorTranslator
                .translate(Bukkit.getBannedPlayers().contains(player) ? Homestead.language.get("default.banned")
                        : (player.isOnline() ? Homestead.language.get("default.online")
                                : Homestead.language.get("default.offline")));
    }

    public static String getAgo(long time) {
        long currentTime = System.currentTimeMillis();

        long differenceMillis = currentTime - time;
        long totalSeconds = differenceMillis / 1000;

        long days = totalSeconds / 86400;
        long remaining = totalSeconds % 86400;

        long hours = remaining / 3600;
        remaining = remaining % 3600;

        long minutes = remaining / 60;
        long seconds = remaining % 60;

        if (days != 0) {
            return ((String) Homestead.config.get("formatters.ago-days")).replace("{v}", String.valueOf(days));
        }

        if (hours != 0) {
            return ((String) Homestead.config.get("formatters.ago-hours")).replace("{v}", String.valueOf(hours));
        }

        if (minutes != 0) {
            return ((String) Homestead.config.get("formatters.ago-minutes")).replace("{v}", String.valueOf(minutes));
        }

        if (seconds != 0) {
            return ((String) Homestead.config.get("formatters.ago-seconds")).replace("{v}", String.valueOf(seconds));
        }

        return "0";
    }
}
