package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.config.ConfigLoader;
import tfagaming.projects.minecraft.homestead.config.LanguageLoader;
import tfagaming.projects.minecraft.homestead.config.MenusConfigLoader;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.validator.YAMLValidator;

public class ReloadSubCmd extends SubCommandBuilder {
    public ReloadSubCmd() {
        super("reload");
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Homestead instance = Homestead.getInstance();

        try {
            Homestead.config = new ConfigLoader(instance);

            Homestead.language = new LanguageLoader(instance, Homestead.config.get("language"));

            Homestead.menusConfig = new MenusConfigLoader(instance);

            Set<String> skipKeys = new HashSet<>();

            YAMLValidator configValidator = new YAMLValidator("config.yml", new File(instance.getDataFolder(), "config.yml"),
                    skipKeys);

            if (!configValidator.validate()) {
                boolean fixed = configValidator.fix();

                if (fixed) {
                    Homestead.config = new ConfigLoader(instance);
                } else {
                    throw new Exception("Unable to fix the config.yml file.");
                }
            }

            YAMLValidator languageValidator = new YAMLValidator("en-US.yml",
                    Homestead.language.getLanguageFile(Homestead.config.get("language")));

            if (!languageValidator.validate()) {
                boolean fixed = languageValidator.fix();

                if (fixed) {
                    Homestead.language = new LanguageLoader(instance, Homestead.config.get("language"));
                } else {
                    throw new Exception("Unable to fix the config.yml file.");
                }
            }

            YAMLValidator menusConfigValidator = new YAMLValidator("menus.yml", new File(instance.getDataFolder(), "menus.yml"),
                    skipKeys);

            if (!menusConfigValidator.validate()) {
                boolean fixed = menusConfigValidator.fix();

                if (fixed) {
                    Homestead.menusConfig = new MenusConfigLoader(instance);
                } else {
                    throw new Exception("Unable to fix the config.yml file.");
                }
            }

            PlayerUtils.sendMessage(sender, 90);
        } catch (Exception e) {
            System.out.println(e);

            PlayerUtils.sendMessage(sender, 87);
        }

        return true;
    }
}
