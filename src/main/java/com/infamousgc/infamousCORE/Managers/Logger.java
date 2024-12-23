package com.infamousgc.infamousCORE.Managers;

import com.infamousgc.infamousCORE.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {
    private static final java.util.logging.Logger BUKKIT_LOGGER = Main.getPlugin(Main.class).getLogger();
    private static final String SEVERE_HEADER = format("&c------------------------ &4SEVERE &c------------------------");

    public static void info(String msg, Object... args) {
        BUKKIT_LOGGER.info(format(msg, args));
    }

    public static void warning(String msg, Object... args) {
        BUKKIT_LOGGER.warning(format(msg, args));
    }

    public static void severe(String msg, Object... args) {
        BUKKIT_LOGGER.severe(SEVERE_HEADER);
        BUKKIT_LOGGER.severe(format(msg, args));
        BUKKIT_LOGGER.severe(SEVERE_HEADER);
    }

    public static void log(Level level, String msg, Object... args) {
        BUKKIT_LOGGER.log(level, format(msg, args));
    }

    private static String format(String msg, Object... args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                msg = msg.replace("{" + i + "}", String.valueOf(args[i]));
            }
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
