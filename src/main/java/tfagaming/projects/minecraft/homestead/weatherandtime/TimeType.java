package tfagaming.projects.minecraft.homestead.weatherandtime;

import java.util.List;

public class TimeType {
    public final static int SERVER = 0;
    public final static int MORNING = 1;
    public final static int NOON = 6000;
    public final static int AFTERNOON = 9000;
    public final static int SUNSET = 12000;
    public final static int NIGHT = 18000;

    public static List<String> getAll() {
        return List.of("server", "morning", "noon", "afternoon", "sunset", "night");
    }

    public static int next(int current) {
        switch (current) {
            case SERVER:
                return MORNING;
            case MORNING:
                return NOON;
            case NOON:
                return AFTERNOON;
            case AFTERNOON:
                return SUNSET;
            case SUNSET:
                return NIGHT;
            case NIGHT:
                return SERVER;
            default:
                return SERVER;
        }
    }

    public static String from(int time) {
        switch (time) {
            case SERVER:
                return "Server";
            case MORNING:
                return "Morning";
            case NOON:
                return "Noon";
            case AFTERNOON:
                return "Afternoon";
            case SUNSET:
                return "Sunset";
            case NIGHT:
                return "Night";
            default:
                return "Server";
        }
    }
}
