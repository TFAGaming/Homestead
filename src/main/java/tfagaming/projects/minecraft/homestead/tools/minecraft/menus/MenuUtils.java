package tfagaming.projects.minecraft.homestead.tools.minecraft.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.items.ItemUtils;

public class MenuUtils {
    public static String getTitle(int path) {
        return Homestead.menusConfig.get("titles." + String.valueOf(path));
    }

    public static ButtonData getButtonData(int path) {
        String name = Homestead.menusConfig.get("buttons." + String.valueOf(path) + ".name");
        List<String> lore = Homestead.menusConfig.get("buttons." + String.valueOf(path) + ".lore");
        String type = Homestead.menusConfig.get("buttons." + String.valueOf(path) + ".type");

        if (name == null)
            name = "NO NAME";
        if (lore == null)
            lore = new ArrayList<>();
        if (type == null)
            type = "BARRIER";

        return new ButtonData(name, lore, type);
    }

    public static ItemStack getButton(ButtonData data, Map<String, String> replacements) {
        if (data.getOriginalType().startsWith("PLAYERHEAD-")) {
            String texture = data.getOriginalType().split("-")[1];

            return ItemUtils.getPlayerHead(data.getName(), data.getLore(), texture, replacements);
        } else {
            return ItemUtils.getItem(data.getName(), data.getLore(), data.getType(), replacements);
        }
    }

    public static ItemStack getButton(int path, OfflinePlayer... playerHead) {
        ButtonData data = getButtonData(path);

        if (data.getOriginalType().startsWith("PLAYERHEAD-")) {
            String texture = data.getOriginalType().split("-")[1];

            if (texture.equalsIgnoreCase("this")) {
                if (playerHead.length == 0) {
                    return ItemUtils.getItem(data.getName(), data.getLore(), Material.BARRIER);
                } else {
                    return ItemUtils.getPlayerHead(data.getName(), data.getLore(), playerHead[0].getUniqueId());
                }
            } else {
                return ItemUtils.getPlayerHead(data.getName(), data.getLore(), texture);
            }
        } else {
            return ItemUtils.getItem(data.getName(), data.getLore(), data.getType());
        }
    }

    public static ItemStack getButton(int path, Map<String, String> replacements, OfflinePlayer... playerHead) {
        ButtonData data = getButtonData(path);

        if (data.getOriginalType().startsWith("PLAYERHEAD-")) {
            String texture = data.getOriginalType().split("-")[1];

            if (texture.equalsIgnoreCase("this")) {
                if (playerHead.length == 0) {
                    return ItemUtils.getItem(data.getName(), data.getLore(), Material.BARRIER, replacements);
                } else {
                    return ItemUtils.getPlayerHead(data.getName(), data.getLore(), playerHead[0].getUniqueId(),
                            replacements);
                }
            } else {
                return ItemUtils.getPlayerHead(data.getName(), data.getLore(), texture, replacements);
            }
        } else {
            return ItemUtils.getItem(data.getName(), data.getLore(), data.getType(), replacements);
        }
    }

    @SuppressWarnings("unchecked")
    public static ItemStack getFlagButton(String flag, boolean value) {
        HashMap<String, String> replacements = new HashMap<>();

        replacements.put("{flag}", flag);
        replacements.put("{flag-description}", Homestead.language.get("flags-info." + flag + ".description"));
        replacements.put("{state}", Formatters.getFlag(value));
        replacements.put("{flag-allowed}",
                Formatters.getBoolean(!((List<String>) Homestead.config.get("disabled-flags")).contains(flag)));

        ButtonData data = getButtonData(17);
        String type = Homestead.language.get("flags-info." + flag + ".type");

        if (type != null && type.startsWith("PLAYERHEAD-")) {
            String texture = type.split("-")[1];

            return ItemUtils.getPlayerHead(data.getName(), data.getLore(), texture, replacements);
        } else {
            return ItemUtils.getItem(data.getName(), data.getLore(),
                    Material.getMaterial(type == null ? "BARRIER" : type), replacements);
        }
    }

    public static ItemStack getBackButton() {
        return ItemUtils.getItem(getButtonData(0));
    }

    public static ItemStack getPreviousPageButton() {
        return ItemUtils.getItem(getButtonData(1));
    }

    public static ItemStack getNextPageButton() {
        return ItemUtils.getItem(getButtonData(2));
    }

    public static ItemStack getEmptySlot() {
        return ItemUtils.getItem(getButtonData(3));
    }

    public static class ButtonData {
        public String name;
        public List<String> lore;
        public Material type;
        public String originalType;

        public ButtonData(String name, List<String> lore, String type) {
            this.name = name;
            this.lore = lore;
            this.type = Material.getMaterial(type);
            this.originalType = type;
        }

        public String getName() {
            return name;
        }

        public List<String> getLore() {
            return lore;
        }

        public Material getType() {
            return type == null ? Material.BARRIER : type;
        }

        public String getOriginalType() {
            return originalType;
        }
    }
}
