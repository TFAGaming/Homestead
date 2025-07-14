package tfagaming.projects.minecraft.homestead.config;

import java.io.*;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;

public class LanguageLoader {
    private final Homestead plugin;
    public FileConfiguration language;

    public LanguageLoader(Homestead plugin, String language) {
        this.plugin = plugin;

        File directory = new File(plugin.getDataFolder(), "languages/");
        File defaultPath = new File(plugin.getDataFolder(), "languages/en-US.yml");

        if (!directory.isDirectory()) {
            directory.mkdir();

            try {
                InputStream stream = plugin.getResource("en-US.yml");
                FileUtils.copyInputStreamToFile(stream, defaultPath);
            } catch (IOException e) {
                Logger.error("Unable to copy the default language file (en-US.yml), closing plugin's instance...");
                plugin.endInstance();

                e.printStackTrace();
            }
        }

        if (language != null) {
            File localefile = new File(plugin.getDataFolder(),
                    "languages/" + language + (language.endsWith(".yml") ? "" : ".yml"));

            FileConfiguration loaded = YamlConfiguration.loadConfiguration(localefile);

            this.language = loaded;
        } else {
            FileConfiguration loaded = YamlConfiguration.loadConfiguration(defaultPath);

            this.language = loaded;
        }

        Logger.info("The language file is ready.");
    }

    public File getLanguageFile(String language) {
        return new File(plugin.getDataFolder(),
                "languages/" + language + (language.endsWith(".yml") ? "" : ".yml"));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path) {
        return (T) language.get(path);
    }
}
