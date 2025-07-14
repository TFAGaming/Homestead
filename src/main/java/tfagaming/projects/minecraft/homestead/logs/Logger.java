package tfagaming.projects.minecraft.homestead.logs;

import org.bukkit.Bukkit;

import tfagaming.projects.minecraft.homestead.Homestead;

public class Logger {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("Homestead");
    private static LogsFile logs;

    public Logger() {
        Logger.logs = new LogsFile();

        sendPluginBanner();
    }

    public void sendPluginBanner() {
        String lineSplitter = "";

        for (int i = 0; i < 54; i++) {
            lineSplitter += "-";
        }

        String banner = " _   _                           _                 _ \r\n" +
                "| | | | ___  _ __ ___   ___  ___| |_ ___  __ _  __| |\r\n" +
                "| |_| |/ _ \\| '_ ` _ \\ / _ \\/ __| __/ _ \\/ _` |/ _` |\r\n" +
                "|  _  | (_) | | | | | |  __/\\__ \\ ||  __/ (_| | (_| |\r\n" +
                "|_| |_|\\___/|_| |_| |_|\\___||___/\\__\\___|\\__,_|\\__,_|" +
                "\n\nVersion: " + Homestead.getVersion() + "\nRunning on " + Bukkit.getName() + ": "
                + Bukkit.getVersion();

        logger.info(lineSplitter);

        for (String line : banner.split("\n")) {
            logger.info(Colors.CYAN + line + Colors._RESET);
        }

        logger.info(lineSplitter);
    }

    public static void info(String message) {
        logger.info(message);
        logs.save("[INFO] " + message);
    }

    public static void info(String message, Object... noSave) {
        logger.info(message);
    }

    public static void info(String... args) {
        info(String.join(" ", args));
    }

    public static void notice(String message) {
        logger.info(Colors.CYAN + message + Colors._RESET);
        logs.save("[NOTICE] " + message);
    }

    public static void notice(String message, Object... noSave) {
        logger.info(Colors.CYAN + message + Colors._RESET);
    }

    public static void notice(String... args) {
        notice(String.join(" ", args));
    }

    public static void warning(String message) {
        logger.warning(Colors.YELLOW + message + Colors._RESET);
        logs.save("[WARNING] " + message);
    }

    public static void warning(String message, Object... noSave) {
        logger.warning(Colors.YELLOW + message + Colors._RESET);
    }

    public static void warning(String... args) {
        warning(String.join(" ", args));
    }

    public static void error(String message) {
        logger.severe(Colors.RED + message + Colors._RESET);
        logs.save("[ERROR] " + message);
    }

    public static void error(String message, Object... noSave) {
        logger.severe(Colors.RED + message + Colors._RESET);
    }

    public static void error(String... args) {
        error(String.join(" ", args));
    }

    public class Colors {
        public static final String RED = "\u001B[31m";
        public static final String YELLOW = "\u001B[33m";
        public static final String GREEN = "\u001B[32m";
        public static final String BLUE = "\u001B[34m";
        public static final String CYAN = "\u001B[36m";

        public static final String _RESET = "\u001B[0m";
    }
}
