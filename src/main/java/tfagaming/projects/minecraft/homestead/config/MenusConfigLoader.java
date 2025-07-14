package tfagaming.projects.minecraft.homestead.config;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.*;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MenusConfigLoader {
    private File configFile;
    private FileConfiguration config;

    public MenusConfigLoader(Homestead plugin) {
        configFile = new File(plugin.getDataFolder(), "menus.yml");

        if (!configFile.exists()) {
            try {
                InputStream stream = plugin.getResource("menus.yml");
                FileUtils.copyInputStreamToFile(stream, configFile);
            } catch (IOException e) {
                Logger.error(
                        "Unable to copy the default menus configuration file (menus.yml), closing plugin's instance...");
                plugin.endInstance();

                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);

        Logger.info("The menus configuration file is ready.");
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path) {
        return (T) config.get(path);
    }

    public List<String> getKeysUnderPath(String path) {
        if (config.isConfigurationSection(path)) {
            Set<String> keys = config.getConfigurationSection(path).getKeys(false);

            return new ArrayList<String>(keys);
        }

        return new ArrayList<String>();
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
