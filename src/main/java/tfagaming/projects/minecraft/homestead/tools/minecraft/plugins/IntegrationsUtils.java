package tfagaming.projects.minecraft.homestead.tools.minecraft.plugins;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class IntegrationsUtils {
    public static boolean isVaultInstalled() {
		return Bukkit.getServer().getPluginManager().getPlugin("Vault") != null
				&& Bukkit.getServer().getPluginManager().getPlugin("Vault").isEnabled();
	}

    public static Plugin getVaultInstance() {
        return Bukkit.getServer().getPluginManager().getPlugin("Vault");
    }

	public static boolean isPlaceholderAPIInstalled() {
		return Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null
				&& Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI").isEnabled();
	}

    public static Plugin getPlaceholderAPIInstance() {
        return Bukkit.getServer().getPluginManager().getPlugin("Vault");
    }
}
