package tfagaming.projects.minecraft.homestead.tools.minecraft.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapColor {
    // Most used colors
    public static final int RED = 0xFF0000;
    public static final int GREEN = 0x00FF00;
    public static final int BLUE = 0x0000FF;
    public static final int YELLOW = 0xFFFF00;
    public static final int CYAN = 0x00FFFF;
    public static final int MAGENTA = 0xFF00FF;
    public static final int WHITE = 0xFFFFFF;
    public static final int GRAY = 0x808080;
    public static final int LIGHT_GRAY = 0xC0C0C0;
    public static final int DARK_GRAY = 0x404040;
    public static final int ORANGE = 0xFFA500;
    public static final int PINK = 0xFFC0CB;
    public static final int PURPLE = 0x800080;
    public static final int BROWN = 0xA52A2A;
    public static final int GOLD = 0xFFD700;
    public static final int SILVER = 0xC0C0C0;
    public static final int NAVY = 0x000080;
    public static final int TEAL = 0x008080;
    public static final int LIME = 0x00FF00;
    public static final int MAROON = 0x800000;
    public static final int OLIVE = 0x808000;

    // Extended colors
    public static final int CRIMSON = 0xDC143C;
    public static final int CORAL = 0xFF7F50;
    public static final int SALMON = 0xFA8072;
    public static final int TURQUOISE = 0x40E0D0;
    public static final int VIOLET = 0xEE82EE;
    public static final int INDIGO = 0x4B0082;
    public static final int LAVENDER = 0xE6E6FA;
    public static final int PLUM = 0xDDA0DD;
    public static final int TAN = 0xD2B48C;
    public static final int BEIGE = 0xF5F5DC;
    public static final int MINT = 0x98FF98;
    public static final int SKY_BLUE = 0x87CEEB;
    public static final int SLATE_GRAY = 0x708090;
    public static final int DARK_SLATE_GRAY = 0x2F4F4F;
    public static final int CHOCOLATE = 0xD2691E;
    public static final int PERU = 0xCD853F;
    public static final int KHAKI = 0xF0E68C;
    public static final int THISTLE = 0xD8BFD8;

    // Neon/bright colors
    public static final int NEON_GREEN = 0x39FF14;
    public static final int ELECTRIC_BLUE = 0x7DF9FF;
    public static final int HOT_PINK = 0xFF69B4;
    public static final int BRIGHT_ORANGE = 0xFFA500;
    public static final int NEON_YELLOW = 0xFFFF33;
    public static final int NEON_PURPLE = 0x9D00FF;

    // Dark/Light variants
    public static final int DARK_RED = 0x8B0000;
    public static final int DARK_GREEN = 0x006400;
    public static final int DARK_BLUE = 0x00008B;
    public static final int LIGHT_BLUE = 0xADD8E6;
    public static final int LIGHT_GREEN = 0x90EE90;
    public static final int LIGHT_PINK = 0xFFB6C1;
    public static final int LIGHT_YELLOW = 0xFFFFE0;
    public static final int LIGHT_CYAN = 0xE0FFFF;

    public static final int DEFAULT = 0;

    private static final Map<String, Integer> COLOR_MAP = new HashMap<>();
    private static final Map<Integer, String> COLOR_CODE_TO_NAME = new HashMap<>();

    static {
        COLOR_MAP.put("RED", RED);
        COLOR_MAP.put("GREEN", GREEN);
        COLOR_MAP.put("BLUE", BLUE);
        COLOR_MAP.put("YELLOW", YELLOW);
        COLOR_MAP.put("CYAN", CYAN);
        COLOR_MAP.put("MAGENTA", MAGENTA);
        COLOR_MAP.put("WHITE", WHITE);
        COLOR_MAP.put("GRAY", GRAY);
        COLOR_MAP.put("LIGHT_GRAY", LIGHT_GRAY);
        COLOR_MAP.put("DARK_GRAY", DARK_GRAY);
        COLOR_MAP.put("ORANGE", ORANGE);
        COLOR_MAP.put("PINK", PINK);
        COLOR_MAP.put("PURPLE", PURPLE);
        COLOR_MAP.put("BROWN", BROWN);
        COLOR_MAP.put("GOLD", GOLD);
        COLOR_MAP.put("SILVER", SILVER);
        COLOR_MAP.put("DEFAULT", DEFAULT);
        COLOR_MAP.put("NAVY", NAVY);
        COLOR_MAP.put("TEAL", TEAL);
        COLOR_MAP.put("LIME", LIME);
        COLOR_MAP.put("MAROON", MAROON);
        COLOR_MAP.put("OLIVE", OLIVE);
        COLOR_MAP.put("CRIMSON", CRIMSON);
        COLOR_MAP.put("CORAL", CORAL);
        COLOR_MAP.put("SALMON", SALMON);
        COLOR_MAP.put("TURQUOISE", TURQUOISE);
        COLOR_MAP.put("VIOLET", VIOLET);
        COLOR_MAP.put("INDIGO", INDIGO);
        COLOR_MAP.put("LAVENDER", LAVENDER);
        COLOR_MAP.put("PLUM", PLUM);
        COLOR_MAP.put("TAN", TAN);
        COLOR_MAP.put("BEIGE", BEIGE);
        COLOR_MAP.put("MINT", MINT);
        COLOR_MAP.put("SKY_BLUE", SKY_BLUE);
        COLOR_MAP.put("SLATE_GRAY", SLATE_GRAY);
        COLOR_MAP.put("DARK_SLATE_GRAY", DARK_SLATE_GRAY);
        COLOR_MAP.put("CHOCOLATE", CHOCOLATE);
        COLOR_MAP.put("PERU", PERU);
        COLOR_MAP.put("KHAKI", KHAKI);
        COLOR_MAP.put("THISTLE", THISTLE);
        COLOR_MAP.put("NEON_GREEN", NEON_GREEN);
        COLOR_MAP.put("ELECTRIC_BLUE", ELECTRIC_BLUE);
        COLOR_MAP.put("HOT_PINK", HOT_PINK);
        COLOR_MAP.put("BRIGHT_ORANGE", BRIGHT_ORANGE);
        COLOR_MAP.put("NEON_YELLOW", NEON_YELLOW);
        COLOR_MAP.put("NEON_PURPLE", NEON_PURPLE);
        COLOR_MAP.put("DARK_RED", DARK_RED);
        COLOR_MAP.put("DARK_GREEN", DARK_GREEN);
        COLOR_MAP.put("DARK_BLUE", DARK_BLUE);
        COLOR_MAP.put("LIGHT_BLUE", LIGHT_BLUE);
        COLOR_MAP.put("LIGHT_GREEN", LIGHT_GREEN);
        COLOR_MAP.put("LIGHT_PINK", LIGHT_PINK);
        COLOR_MAP.put("LIGHT_YELLOW", LIGHT_YELLOW);
        COLOR_MAP.put("LIGHT_CYAN", LIGHT_CYAN);
    }

    static {
        COLOR_MAP.forEach((name, code) -> COLOR_CODE_TO_NAME.put(code, name));
    }

    public static List<String> getAll() {
        List<String> colors = new ArrayList<String>();

        for (Map.Entry<String, Integer> entry : COLOR_MAP.entrySet()) {
            colors.add(entry.getKey().toLowerCase().replace("_", "-"));
        }

        return colors;
    }

    public static String convertToColoredStringWithColorName(int color) {
        return "&#" + String.format("%06X", color & 0x00FFFFFF) + fromInt(color);
    }

    public static String fromInt(int color) {
        String name = COLOR_CODE_TO_NAME.get(color);

        if (name == null) {
            return "Unknown";
        }

        return formatColorName(name);
    }

    private static String formatColorName(String name) {
        String[] words = name.split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(word.substring(0, 1).toUpperCase())
                         .append(word.substring(1).toLowerCase())
                         .append(" ");
            }
        }
        
        return formatted.toString().trim();
    }

    public static int parseFromString(String colorString) {
        colorString = colorString.toUpperCase().replace("-", "_");

        if (colorString == null || colorString.isEmpty()) {
            return DEFAULT;
        }

        Integer color = COLOR_MAP.get(colorString);

        return color == null ? DEFAULT : color;
    }
}
