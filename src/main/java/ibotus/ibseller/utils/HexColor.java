package ibotus.ibseller.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Pattern;

public class HexColor {

    public static String color(String input) {
        if (input == null) {
            return null;
        }
        String text = Pattern.compile("#[a-fA-F0-9]{6}")
                .matcher(input)
                .replaceAll(match -> ChatColor.of(match.group()).toString());
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}

