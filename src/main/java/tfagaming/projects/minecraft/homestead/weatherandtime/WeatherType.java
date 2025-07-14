package tfagaming.projects.minecraft.homestead.weatherandtime;

import java.util.List;

public class WeatherType {
    public final static int SERVER = 0;
    public final static int CLEAR = 1;
    public final static int RAIN = 2;

    public static List<String> getAll() {
        return List.of("server", "clear", "rain");
    }

    public static int next(int current) {
        switch (current) {
            case SERVER:
                return CLEAR;
            case CLEAR:
                return RAIN;
            case RAIN:
                return SERVER;
            default:
                return SERVER;
        }
    }

    public static String from(int weather) {
        switch (weather) {
            case SERVER:
                return "Server";
            case CLEAR:
                return "Clear";
            case RAIN:
                return "Rain";
            default:
                return "Server";
        }
    }
}
