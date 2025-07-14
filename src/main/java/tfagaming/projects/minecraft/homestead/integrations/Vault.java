package tfagaming.projects.minecraft.homestead.integrations;


import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import tfagaming.projects.minecraft.homestead.Homestead;

public class Vault {
    private final Homestead plugin;
    public Chat chat;
    public Economy economy;
    public Permission permissions;

    public Vault(Homestead plugin) {
        this.plugin = plugin;
    }

    public boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = this.plugin.getServer().getServicesManager().getRegistration(Chat.class);

        if (rsp == null) {
            return false;
        }

        chat = rsp.getProvider();

        return chat != null;
    }

    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = this.plugin.getServer().getServicesManager()
                .getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();

        return economy != null;
    }

    public boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = this.plugin.getServer().getServicesManager()
                .getRegistration(Permission.class);

        if (rsp == null) {
            return false;
        }

        permissions = rsp.getProvider();

        return permissions != null;
    }

    public Chat getChat() {
        return chat;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public boolean isChatReady() {
        return chat != null;
    }

    public boolean isEconomyReady() {
        return economy != null;
    }

    public boolean isPermissionsReady() {
        return permissions != null;
    }
}
