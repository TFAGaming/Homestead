package tfagaming.projects.minecraft.homestead.tools.minecraft.items;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils.ButtonData;

public class ItemUtils {
    public static ItemStack getItem(ButtonData data) {
        return getItem(data.getName(), data.getLore(), data.getType());
    }

    public static ItemStack getItem(ButtonData data, Map<String, String> replacements) {
        return getItem(data.getName(), data.getLore(), data.getType(), replacements);
    }

    public static ItemStack getItem(String displayname, List<String> lore, Material material) {
        List<String> loreCopy = (lore != null) ? new ArrayList<>(lore) : null;

        if (material == null) {
            material = Material.BARRIER;
        }

        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColorTranslator.translate(displayname));

        if (loreCopy != null) {
            ArrayList<String> lorelist = new ArrayList<>();
            for (String each : loreCopy) {
                String[] lines = each.split("\\n");
                for (String line : lines) {
                    if (!line.contains("&")) {
                        line = "&f" + line;
                    }
                    lorelist.add(ChatColorTranslator.translate(line));
                }
            }
            meta.setLore(lorelist);
        }

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack getItem(String displayname, List<String> lore, Material material,
            Map<String, String> replacements) {
        displayname = Formatters.replace(displayname, replacements);

        List<String> loreCopy = (lore != null) ? new ArrayList<>(lore) : null;

        if (loreCopy != null) {
            for (int i = 0; i < loreCopy.size(); i++) {
                String string = loreCopy.get(i);
                string = Formatters.replace(string, replacements);
                loreCopy.set(i, string);
            }
        }

        return getItem(displayname, loreCopy, material);
    }

    // Get player head based by texture
    public static ItemStack getPlayerHead(String displayname, List<String> lore, String texture) {
        List<String> loreCopy = (lore != null) ? new ArrayList<>(lore) : null;

        ItemStack item = getCustomHeadTexture(texture);
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            throw new IllegalStateException("Failed to create a valid Player Head!");
        }

        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColorTranslator.translate(displayname));

        if (loreCopy != null) {
            ArrayList<String> lorelist = new ArrayList<>();
            for (String each : loreCopy) {
                String[] lines = each.split("\\n");
                for (String line : lines) {
                    if (!line.contains("&")) {
                        line = "&f" + line;
                    }
                    lorelist.add(ChatColorTranslator.translate(line));
                }
            }
            meta.setLore(lorelist);
        }

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack getPlayerHead(String displayname, List<String> lore, String texture,
            Map<String, String> replacements) {
        displayname = Formatters.replace(displayname, replacements);

        List<String> loreCopy = (lore != null) ? new ArrayList<>(lore) : null;

        if (loreCopy != null) {
            for (int i = 0; i < loreCopy.size(); i++) {
                String string = loreCopy.get(i);
                string = Formatters.replace(string, replacements);
                loreCopy.set(i, string);
            }
        }

        return getPlayerHead(displayname, loreCopy, texture);
    }

    private static ItemStack getCustomHeadTexture(String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        meta.setOwnerProfile(
                getProfile("https://textures.minecraft.net/texture/" + texture));
        head.setItemMeta(meta);

        return head;
    }

    private static PlayerProfile getProfile(String url) {
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        URL urlobject;

        try {
            urlobject = URI.create(url).toURL();
        } catch (IllegalArgumentException | MalformedURLException exception) {
            throw new RuntimeException("Invalid URL", exception);
        }

        textures.setSkin(urlobject);
        profile.setTextures(textures);

        return profile;
    }

    // Get player head based by UUID
    public static ItemStack getPlayerHead(String displayname, List<String> lore, UUID playerId) {
        List<String> loreCopy = (lore != null) ? new ArrayList<>(lore) : null;

        ItemStack item = getPlayerHeadById(playerId);
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            throw new IllegalStateException("Failed to create a valid Player Head!");
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColorTranslator.translate(displayname));

        if (loreCopy != null) {
            ArrayList<String> lorelist = new ArrayList<>();
            for (String each : loreCopy) {
                if (!each.contains("&")) {
                    each = "&f" + each;
                }
                lorelist.add(ChatColorTranslator.translate(each));
            }
            meta.setLore(lorelist);
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getPlayerHead(String displayname, List<String> lore, UUID playerId,
            Map<String, String> replacements) {
        displayname = Formatters.replace(displayname, replacements);

        List<String> loreCopy = (lore != null) ? new ArrayList<>(lore) : null;

        if (loreCopy != null) {
            for (int i = 0; i < loreCopy.size(); i++) {
                String string = loreCopy.get(i);
                string = Formatters.replace(string, replacements);
                loreCopy.set(i, string);
            }
        }

        return getPlayerHead(displayname, loreCopy, playerId);
    }

    private static ItemStack getPlayerHeadById(UUID playerId) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        OfflinePlayer player = Homestead.getInstance().getOfflinePlayerSync(playerId);

        if (player == null) {
            return new ItemStack(Material.PLAYER_HEAD);
        } else {
            meta.setOwningPlayer(player);
        }

        head.setItemMeta(meta);

        return head;
    }
}
