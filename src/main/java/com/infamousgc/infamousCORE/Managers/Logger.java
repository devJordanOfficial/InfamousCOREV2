package com.infamousgc.infamousCORE.Managers;

import com.infamousgc.infamousCORE.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d+)}");

    public static void log(String msg, Object... args) {
        String formatted = formatMessage(msg, args);
        formatted = ChatColor.translateAlternateColorCodes('&', "[" +
                Main.getPlugin(Main.class).getName() + "] " + formatted);

        Bukkit.getConsoleSender().sendMessage(formatted);
    }

    public static void severe(String msg, Object... args) {
        log("&c------------------------ &4SEVERE &c------------------------");
        log(msg, args);
        log("&c------------------------ &4SEVERE &c------------------------");
    }

    public static void warning(String msg, Object... args) {
        log("&e------------------------ &6WARNING &e------------------------");
        log(msg, args);
        log("&e------------------------ &6WARNING &e------------------------");
    }

    private static String formatMessage(String msg, Object... args) {
        if (args == null || args.length == 0) return msg;

        StringBuilder result = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(msg);

        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1)) -1;
            if (index >= 0 && index < args.length)
                matcher.appendReplacement(result, Matcher.quoteReplacement(args[index].toString()));
            else
                matcher.appendReplacement(result, matcher.group(0));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
