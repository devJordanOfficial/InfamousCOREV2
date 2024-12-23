package com.infamousgc.infamousCORE.Managers;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formatter {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d+)}");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    private static final Pattern FORMAT_CODE_PATTERN = Pattern.compile("&[klmno]");

    public static final String PREFIX_GENERAL = "&8[&5»&8] ";
    public static final String PREFIX_ERROR = "&8[&4»&8] ";
    public static final String PREFIX_INFO = "&8[&e»&8] ";

    public static String format(String msg, Object... args) {
        String parsed = parseVariables(msg, args);
        parsed = replaceHexColors(parsed);
        return ChatColor.translateAlternateColorCodes('&', parsed);
    }

    public static String gradient(String startHex, String endHex, String msg) {
        if (!isValidHex(startHex) ||!isValidHex(endHex))
            throw new IllegalArgumentException("Invalid hex color code: " + (isValidHex(startHex) ? endHex : startHex));

        int[] startRGB = hexToRGB(startHex);
        int[] endRGB = hexToRGB(endHex);
        String[] messageChars = msg.split("(?<=.)");

        StringBuilder result = new StringBuilder();
        String formatCode = "";
        int messageIndex = 0;

        for (int i = 0; i < messageChars.length; i++) {
            if (msg.charAt(i) == '&') {
                char nextChar = msg.charAt(i + 1);
                if (FORMAT_CODE_PATTERN.matcher(String.valueOf(nextChar)).matches()) {
                    formatCode = "&" + nextChar;
                    i++;
                    continue;
                } else if (nextChar == 'r') {
                    formatCode = "";
                    i++;
                    continue;
                }
            }
            int[] currentRGB = interpolateColor(startRGB, endRGB, messageIndex, messageChars.length -1);
            String hex = String.format("%02X%02X%02X", currentRGB[0], currentRGB[1], currentRGB[2]);
            result.append(String.format("#%s%s%s", hex, formatCode, messageChars[i]));
        }

        return format(result.toString());
    }

    private static String parseVariables(String msg, Object... args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                msg = msg.replace("{" + i + "}", String.valueOf(args[i]));
            }
        }
        return msg;
    }

    private static String replaceHexColors(String input) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = HEX_COLOR_PATTERN.matcher(input);
        while (matcher.find()) {
            matcher.appendReplacement(result, ChatColor.of(matcher.group()) + "");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static boolean isValidHex(String hex) {
        return hex.matches("[0-9a-fA-F]{6}");
    }

    private static int[] hexToRGB(String hex) {
        return new int[] {
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    private static int[] interpolateColor(int[] start, int[] end, int step, int maxSteps) {
        return new int[] {
                interpolate(start[0], end[0], step, maxSteps),
                interpolate(start[1], end[1], step, maxSteps),
                interpolate(start[2], end[2], step, maxSteps)
        };
    }

    private static int interpolate(int start, int end, int step, int maxSteps) {
        return start + (end - start) * step / maxSteps;
    }
}
