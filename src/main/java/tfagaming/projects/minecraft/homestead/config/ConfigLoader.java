package tfagaming.projects.minecraft.homestead.config;

import org.bukkit.configuration.file.*;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.*;
import tfagaming.projects.minecraft.homestead.logs.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigLoader {
    private File configFile;
    private FileConfiguration config;

    public ConfigLoader(Homestead plugin) {        
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            Logger.error("Unable to find the configuration file, closing plugin's instance...");
            plugin.endInstance();
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);

        Logger.info("The configuration file is ready.");
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

    public long getDefaultPlayerFlags() {
        List<String> keys = getKeysUnderPath("default-players-flags");
        long flags = 0;

        for (String key : keys) {
            boolean value = get("default-players-flags." + key);

            if (value) {
                flags = FlagsCalculator.addFlag(flags, PlayerFlags.valueOf(key));
            }
        }

        return flags;
    }

    public long getDefaultWorldFlags() {
        List<String> keys = getKeysUnderPath("default-world-flags");
        long flags = 0;

        for (String key : keys) {
            boolean value = get("default-world-flags." + key);

            if (value) {
                flags = FlagsCalculator.addFlag(flags, WorldFlags.valueOf(key));
            }
        }

        return flags;
    }

    @SuppressWarnings("unchecked")
    public boolean isFlagDisabled(String flag) {
        return ((List<String>) get("disabled-flags")).contains(flag);
    }

    public boolean isWelcomeSignEnabled() {
        return get("welcome-signs.enabled");
    }

    public String getPrefix() {
        return Homestead.language.get("prefix");
    }

    public boolean isDebugEnabled() {
        return get("debug");
    }

}
