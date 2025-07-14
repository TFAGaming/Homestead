package tfagaming.projects.minecraft.homestead.tools.java;

public class NumberUtils {
    public static double truncateToTwoDecimalPlaces(double number) {
        return Math.floor(number * 100) / 100;
    }

    public static String convertDoubleToBalance(double number) {
        String[] suffixes = { "", "k", "M", "B", "T" };
        int index = 0;

        while (number >= 1000 && index < suffixes.length - 1) {
            number /= 1000;
            index++;
        }

        return String.format("%.2f%s", number, suffixes[index]);
    }

    public static boolean isValidDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidLong(String string) {
        try {
            Long.parseLong(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
