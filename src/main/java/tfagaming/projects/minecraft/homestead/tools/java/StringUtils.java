package tfagaming.projects.minecraft.homestead.tools.java;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static String[] splitWithLimit(String input, String delimiter, int limit, boolean trim, boolean skipEmpty) {
        String[] parts = input.split(delimiter, limit);
        List<String> result = new ArrayList<>();

        for (String part : parts) {
            String processed = trim ? part.trim() : part;
            if (!skipEmpty || !processed.isEmpty()) {
                result.add(processed);
            }
        }

        return result.toArray(new String[0]);
    }

    public static String[] splitWithLimit(String input, String delimiter, int limit) {
        String trimmed = input.trim();

        String[] parts = trimmed.split(delimiter, limit);

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        return parts;
    }

    public static boolean isValidRegionName(String name) {
        if (name.length() < 2 || name.length() > 32) {
            return false;
        }

        String regex = "^[a-zA-Z0-9-_]+$";

        return name.matches(regex);
    }

    public static boolean isValidRegionDisplayName(String name) {
        if (name.length() < 2 || name.length() > 96) {
            return false;
        }

        return true;
    }

    public static boolean isValidSubAreaName(String name) {
        if (name.length() < 2 || name.length() > 16) {
            return false;
        }

        String regex = "^[a-zA-Z0-9-_]+$";

        return name.matches(regex);
    }

    public static boolean isValidRegionDescription(String description) {
        if (description.length() < 2 || description.length() > 128) {
            return false;
        }

        return true;
    }
}
