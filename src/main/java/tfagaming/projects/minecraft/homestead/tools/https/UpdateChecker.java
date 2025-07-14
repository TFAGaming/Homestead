package tfagaming.projects.minecraft.homestead.tools.https;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;

public class UpdateChecker {
    public static boolean foundUpdate = false;

    public UpdateChecker(Homestead plugin) {
        try {
            Logger.info("Looking for updates on GitHub...");

            URI uri = URI.create("https://raw.githubusercontent.com/TFAGaming/Homestead-Help/main/version.yml");
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = reader.readLine();

                if (!Homestead.getVersion().equalsIgnoreCase(response)) {
                    Logger.warning("There is an available update for Homestead.");
                    Logger.warning("Installed: " + Homestead.getVersion() + ", Updated: " + response);
                    Logger.warning(
                            "Download: https://www.spigotmc.org/resources/121873/, https://modrinth.com/plugin/homestead-plugin");

                    foundUpdate = true;
                } else {
                    Logger.info("You are running on the latest version of Homestead.");

                    foundUpdate = false;
                }
            }
        } catch (Exception e) {
            Logger.warning(
                    "Failed to fetch for updates, maybe GitHub is down or you are not connected to the internet.");
            Logger.warning(
                    "You can manually look for updates on SpigotMC or Modrinth: https://www.spigotmc.org/resources/121873/, https://modrinth.com/plugin/homestead-plugin");
            
            foundUpdate = false;
        }
    }
}
