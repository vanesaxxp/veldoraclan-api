package dev.eggsv31.veldora.veldoraClan.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([a-fA-F0-9]{6})>");
    private static final Pattern CUSTOM_HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final Pattern STYLE_PATTERN = Pattern.compile("<(bold|italic|underline|reset|strikethrough|obfuscated)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLOR_PATTERN = Pattern.compile("<(black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern RAINBOW_PATTERN = Pattern.compile("<rainbow>(.*?)</rainbow>");
    private static final Pattern RGB_PATTERN = Pattern.compile("rgb\\((\\d{1,3}),\\s*(\\d{1,3}),\\s*(\\d{1,3})\\)");
    private static final Pattern HSL_PATTERN = Pattern.compile("hsl\\((\\d{1,3}),\\s*(\\d{1,3}),\\s*(\\d{1,3})\\)");
    private static final Pattern NEX_PATTERN = Pattern.compile("<nex:([a-zA-Z]+)>");
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatUtils() {
    }

    public static String formatMessage(String message) {
        if (message == null) {
            return "";
        }
        message = formatHexColors(message);
        message = formatCustomHexColors(message);
        message = applyColorTags(message);
        message = applyStyles(message);
        message = applyRainbowEffect(message);
        message = formatRGB(message);
        message = formatHSL(message);
        message = formatNexColors(message);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String formatMessageWithOptionalPrefix(String message, String prefix) {
        return message.contains("%prefix%") ? formatMessage(message.replace("%prefix%", prefix)) : formatMessage(message);
    }

    public static Component formatMiniMessageWithLegacySupport(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        String processedMessage = convertLegacyColorsToMiniMessage(message);
        return miniMessage.deserialize(processedMessage);
    }

    public static String convertLegacyColorsToMiniMessage(String message) {
        return message
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&o", "<italic>")
                .replace("&n", "<underline>")
                .replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>")
                .replace("&r", "<reset>");
    }

    private static String formatHexColors(String message) {
        Matcher hexMatcher = HEX_PATTERN.matcher(message);
        while (hexMatcher.find()) {
            String hexColor = hexMatcher.group(1);
            message = message.replace("<#" + hexColor + ">", convertHexToBukkit(hexColor));
        }
        return message;
    }

    private static String formatCustomHexColors(String message) {
        Matcher customHexMatcher = CUSTOM_HEX_PATTERN.matcher(message);
        while (customHexMatcher.find()) {
            String hexColor = customHexMatcher.group(1);
            message = message.replace("&#" + hexColor, convertHexToBukkit(hexColor));
        }
        return message;
    }

    private static String applyColorTags(String message) {
        Matcher matcher = COLOR_PATTERN.matcher(message);
        while (matcher.find()) {
            String colorName = matcher.group(1).toLowerCase();
            ChatColor chatColor;
            try {
                chatColor = ChatColor.valueOf(colorName.toUpperCase());
            } catch (IllegalArgumentException e) {
                chatColor = ChatColor.RESET;
            }
            message = message.replace("<" + matcher.group(1) + ">", chatColor.toString());
        }
        return message;
    }

    private static String applyStyles(String message) {
        Matcher styleMatcher = STYLE_PATTERN.matcher(message);
        while (styleMatcher.find()) {
            String style = styleMatcher.group(1).toLowerCase();
            String replacement = switch (style) {
                case "bold" -> ChatColor.BOLD.toString();
                case "italic" -> ChatColor.ITALIC.toString();
                case "underline" -> ChatColor.UNDERLINE.toString();
                case "strikethrough" -> ChatColor.STRIKETHROUGH.toString();
                case "obfuscated" -> ChatColor.MAGIC.toString();
                default -> ChatColor.RESET.toString();
            };
            message = message.replace("<" + style + ">", replacement);
        }
        return message;
    }

    private static String applyRainbowEffect(String message) {
        Matcher rainbowMatcher = RAINBOW_PATTERN.matcher(message);
        while (rainbowMatcher.find()) {
            String text = rainbowMatcher.group(1);
            String rainbowText = generateRainbowText(text);
            message = message.replace(rainbowMatcher.group(0), rainbowText);
        }
        return message;
    }

    private static String formatRGB(String message) {
        Matcher rgbMatcher = RGB_PATTERN.matcher(message);
        while (rgbMatcher.find()) {
            int r = Integer.parseInt(rgbMatcher.group(1));
            int g = Integer.parseInt(rgbMatcher.group(2));
            int b = Integer.parseInt(rgbMatcher.group(3));
            String hexColor = String.format("%02x%02x%02x", r, g, b);
            message = message.replace(rgbMatcher.group(0), convertHexToBukkit(hexColor));
        }
        return message;
    }

    private static String formatHSL(String message) {
        Matcher hslMatcher = HSL_PATTERN.matcher(message);
        while (hslMatcher.find()) {
            int h = Integer.parseInt(hslMatcher.group(1));
            int s = Integer.parseInt(hslMatcher.group(2));
            int l = Integer.parseInt(hslMatcher.group(3));
            String hexColor = hslToHex(h, s, l);
            message = message.replace(hslMatcher.group(0), convertHexToBukkit(hexColor));
        }
        return message;
    }

    private static String formatNexColors(String message) {
        Matcher nexMatcher = NEX_PATTERN.matcher(message);
        while (nexMatcher.find()) {
            String colorName = nexMatcher.group(1).toLowerCase();
            String replacement = convertNexToChatColor(colorName);
            if (replacement != null) {
                message = message.replace("<nex:" + colorName + ">", replacement);
            }
        }
        return message;
    }

    private static String convertNexToChatColor(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "red" -> ChatColor.RED.toString();
            case "green" -> ChatColor.GREEN.toString();
            case "blue" -> ChatColor.BLUE.toString();
            case "yellow" -> ChatColor.YELLOW.toString();
            case "purple" -> ChatColor.LIGHT_PURPLE.toString();
            case "aqua" -> ChatColor.AQUA.toString();
            case "white" -> ChatColor.WHITE.toString();
            case "black" -> ChatColor.BLACK.toString();
            case "gray" -> ChatColor.GRAY.toString();
            case "dark_red" -> ChatColor.DARK_RED.toString();
            case "dark_green" -> ChatColor.DARK_GREEN.toString();
            case "dark_blue" -> ChatColor.DARK_BLUE.toString();
            case "dark_gray" -> ChatColor.DARK_GRAY.toString();
            case "gold" -> ChatColor.GOLD.toString();
            default -> null;
        };
    }

    public static String convertHexToBukkit(String hex) {
        StringBuilder bukkitColor = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            bukkitColor.append("§").append(c);
        }
        return bukkitColor.toString();
    }

    private static String generateRainbowText(String text) {
        ChatColor[] colors = {ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.AQUA, ChatColor.LIGHT_PURPLE};
        StringBuilder rainbowText = new StringBuilder();
        int colorIndex = 0;
        for (char c : text.toCharArray()) {
            rainbowText.append(colors[colorIndex]).append(c);
            colorIndex = (colorIndex + 1) % colors.length;
        }
        return rainbowText.toString();
    }

    private static String hslToHex(int h, int s, int l) {
        float sNorm = s / 100.0f;
        float lNorm = l / 100.0f;
        float c = (1 - Math.abs(2 * lNorm - 1)) * sNorm;
        float x = c * (1 - Math.abs((h / 60.0f) % 2 - 1));
        float m = lNorm - c / 2;
        float r = 0, g = 0, b = 0;
        if (0 <= h && h < 60) {
            r = c; g = x;
        } else if (60 <= h && h < 120) {
            r = x; g = c;
        } else if (120 <= h && h < 180) {
            g = c; b = x;
        } else if (180 <= h && h < 240) {
            g = x; b = c;
        } else if (240 <= h && h < 300) {
            r = x; b = c;
        } else if (300 <= h && h < 360) {
            r = c; b = x;
        }
        int rHex = Math.round((r + m) * 255);
        int gHex = Math.round((g + m) * 255);
        int bHex = Math.round((b + m) * 255);
        return String.format("%02x%02x%02x", rHex, gHex, bHex);
    }
}
