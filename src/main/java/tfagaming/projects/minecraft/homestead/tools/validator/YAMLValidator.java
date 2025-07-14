package tfagaming.projects.minecraft.homestead.tools.validator;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

public class YAMLValidator {
    private final FileConfiguration defaultConfig;
    private final FileConfiguration targetConfig;
    private final File targetFile;
    private final Set<String> filteredSubtrees;

    public YAMLValidator(String resourceFileName, File targetFile) {
        defaultConfig = loadResourceConfig(resourceFileName);

        this.targetFile = targetFile;

        targetConfig = YamlConfiguration.loadConfiguration(targetFile);
        this.filteredSubtrees = Collections.emptySet();
    }

    public YAMLValidator(String resourceFileName, File targetFile, Set<String> filteredSubtrees) {
        defaultConfig = loadResourceConfig(resourceFileName);

        this.targetFile = targetFile;

        targetConfig = YamlConfiguration.loadConfiguration(targetFile);
        this.filteredSubtrees = filteredSubtrees;
    }

    public YAMLValidator(String resourceFileName, String targetFileName, Set<String> filteredSubtrees) {
        defaultConfig = loadResourceConfig(resourceFileName);

        File targetFile = new File(targetFileName);
        this.targetFile = targetFile;

        targetConfig = YamlConfiguration.loadConfiguration(targetFile);
        this.filteredSubtrees = filteredSubtrees;
    }

    private FileConfiguration loadResourceConfig(String resourceFileName) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceFileName);

        if (inputStream == null) {
            throw new RuntimeException("Resource file '" + resourceFileName + "' is missing in plugin resources");
        }

        return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
    }

    public boolean validate() {
        return checkKeys(defaultConfig, targetConfig, "");
    }

    private boolean checkKeys(ConfigurationSection defaultSection, ConfigurationSection targetSection, String path) {
        if (defaultSection == null)
            return true;

        if (filteredSubtrees.contains(path)) {
            return true;
        }

        Set<String> keys = defaultSection.getKeys(false);

        boolean allKeysPresent = true;

        for (String key : keys) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (filteredSubtrees.contains(fullPath)) {
                continue;
            }

            if (targetSection == null || !targetSection.contains(key)) {
                allKeysPresent = false;
            }

            if (defaultSection.isConfigurationSection(key)) {
                ConfigurationSection defaultSubSection = defaultSection.getConfigurationSection(key);
                ConfigurationSection targetSubSection = (targetSection != null)
                        ? targetSection.getConfigurationSection(key)
                        : null;

                if (!checkKeys(defaultSubSection, targetSubSection, fullPath)) {
                    allKeysPresent = false;
                }
            }
        }

        return allKeysPresent;
    }

    public boolean fix() {
        fixKeys(defaultConfig, targetConfig, "");

        try {
            targetConfig.save(targetFile);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void fixKeys(ConfigurationSection defaultSection, ConfigurationSection targetSection, String path) {
        if (defaultSection == null)
            return;

        if (filteredSubtrees.contains(path)) {
            return;
        }

        Set<String> keys = defaultSection.getKeys(false);
        for (String key : keys) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (filteredSubtrees.contains(fullPath)) {
                continue;
            }

            if (!targetSection.contains(key)) {
                targetSection.set(key, defaultSection.get(key));
            }

            if (defaultSection.isConfigurationSection(key)) {
                ConfigurationSection defaultSubSection = defaultSection.getConfigurationSection(key);
                ConfigurationSection targetSubSection = targetSection.getConfigurationSection(key);

                if (targetSubSection == null) {
                    targetSubSection = targetSection.createSection(key);
                }

                fixKeys(defaultSubSection, targetSubSection, fullPath);
            }
        }
    }
}