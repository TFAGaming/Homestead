package tfagaming.projects.minecraft.homestead.tools.minecraft.chat;

import org.bukkit.ChatColor;

public class ChatColorTranslator {
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public static String translate(String string) {
        String[] texts = string.split(String.format(WITH_DELIMITER, "&"));

        StringBuilder finalText = new StringBuilder();

        for (int i = 0; i < texts.length; i++) {
            if (texts[i].equalsIgnoreCase("&")) {
                i++;

                if (texts[i].charAt(0) == '#') {
                    finalText
                            .append(net.md_5.bungee.api.ChatColor.of(texts[i].substring(0, 7)) + texts[i].substring(7));
                } else {
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]));
                }
            } else {
                finalText.append(texts[i]);
            }
        }

        return finalText.toString();
    }

    public static String removeColor(String string, boolean neverBeenTranslated) {
        if (string == null) {
            return null;
        }

        if (neverBeenTranslated) {
            return string.replaceAll("&[a-fA-F0-9k-orK-OR]", "");
        } else {
            return string.replaceAll("§[a-fA-F0-9k-orK-OR]", "");
        }
    }
}
