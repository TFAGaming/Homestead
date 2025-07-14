package tfagaming.projects.minecraft.homestead.integrations.maps;

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;

public class RegionIconTools {
    private static BufferedImage defaultIcon;
    private static Map<String, BufferedImage> icons = new HashMap<>();

    public static List<String> getAllIcons() {
        return Homestead.config.getKeysUnderPath("dynamic-maps.icons.list");
    }

    public static boolean isValidIcon(String icon) {
        return getAllIcons().contains(icon);
    }

    public static BufferedImage getIconBufferedImage(String icon) {
        if (icon == null) {
            return defaultIcon;
        }

        return icons.getOrDefault(icon, defaultIcon);
    }

    public static void downloadAllIcons() {
        Logger.info("Downloaded icons status: 0%");

        List<String> allIcons = getAllIcons();
        int totalIcons = allIcons.size();
        int downloadedCount = 0;

        defaultIcon = downloadIcon(Homestead.config.get("dynamic-maps.icons.default"));

        for (String icon : getAllIcons()) {
            if (icons.containsKey(icon)) {
                continue;
            }

            String url = Homestead.config.get("dynamic-maps.icons.list." + icon);

            if (url != null) {
                BufferedImage downloaded = downloadIcon(url);

                icons.putIfAbsent(icon, downloaded);

                downloadedCount++;
                Logger.info("Downloaded icons status: " + (int) ((downloadedCount / (float) totalIcons) * 100) + "%");
            }
        }
    }

    public static BufferedImage downloadIcon(String imageUrl) {
        try {
            URL url = new URI(imageUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedImage bufferedImage = ImageIO.read(connection.getInputStream());

            return bufferedImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
