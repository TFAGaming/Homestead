package tfagaming.projects.minecraft.homestead.integrations;

import org.bukkit.Bukkit;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.integrations.maps.BlueMapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.DynmapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.Pl3xMapAPI;
import tfagaming.projects.minecraft.homestead.integrations.maps.SquaremapAPI;
import tfagaming.projects.minecraft.homestead.logs.Logger;

public class DynamicMaps {
    public static DynmapAPI dynmap;
    public static Pl3xMapAPI pl3xmap;
    public static SquaremapAPI squaremap;
    public static BlueMapAPI bluemap;

    public DynamicMaps(Homestead plugin) {
        if (dynmap == null) {
            try {
                dynmap = new DynmapAPI(plugin);

                Logger.info("Successfully connected to dynmap's API.");

                dynmap.update();
            } catch (NoClassDefFoundError e) {

            }
        } else {
            dynmap.update();
        }

        if (pl3xmap == null) {
            try {
                pl3xmap = new Pl3xMapAPI(plugin);

                Logger.info("Successfully connected to Pl3xMap's API.");

                pl3xmap.update();
            } catch (NoClassDefFoundError e) {

            }
        } else {
            pl3xmap.update();
        }

        if (squaremap == null) {
            try {
                squaremap = new SquaremapAPI(plugin);

                Logger.info("Successfully connected to Squaremap's API.");

                squaremap.update();
            } catch (NoClassDefFoundError e) {

            }
        } else {
            squaremap.update();
        }

        if (bluemap == null) {
            try {
                de.bluecolored.bluemap.api.BlueMapAPI.onEnable((api) -> {
                    bluemap = new BlueMapAPI(plugin, api);

                    Logger.info("Successfully connected to BlueMap's API.");

                    bluemap.update();
                });
            } catch (NoClassDefFoundError e) {

            }
        } else {
            bluemap.update();
        }

        if (Homestead.config.isDebugEnabled()) {
            Logger.info("Updated dynamic map plugin markers.");
        }
    }

    public boolean isDynmapInstalled() {
        return Bukkit.getServer().getPluginManager().getPlugin("dynmap") != null
                && Bukkit.getServer().getPluginManager().getPlugin("dynmap").isEnabled();
    }

    public boolean isPl3xMapInstalled() {
        return Bukkit.getServer().getPluginManager().getPlugin("Pl3xMap") != null
                && Bukkit.getServer().getPluginManager().getPlugin("Pl3xMap").isEnabled();
    }

    public boolean isSquaremapInstalled() {
        return Bukkit.getServer().getPluginManager().getPlugin("squaremap") != null
                && Bukkit.getServer().getPluginManager().getPlugin("squaremap").isEnabled();
    }
}
