package com.infamousgc.infamousCORE.Utils;

import org.bukkit.command.CommandSender;

import static com.infamousgc.infamousCORE.Utils.Formatter.PREFIX_ERROR;
import static com.infamousgc.infamousCORE.Utils.Formatter.format;

public class Error {
    public static void noPermission(CommandSender sender, String node) {
        sender.sendMessage(format(PREFIX_ERROR + "&7Oops! You do not have the permission &c{0}&7.", node));
    }

    // MUST BE PLAYER
    public static void mustBePlayer(CommandSender sender) {
        sender.sendMessage(Formatter.format(PREFIX_ERROR + "&7Oops! This command must be run by a player!"));
    }

    public static void mustSpecifyPlayer(CommandSender sender, String input, String usage) {
        sender.sendMessage(format(PREFIX_ERROR + "&7Oops! You must specify a player {0} when running this command from the console. " +
                "(Usage: {2})", input, usage));
    }

    // PLAYER NOT ONLINE
    public static void playerNotOnline(CommandSender sender, String username) {
        sender.sendMessage(format(PREFIX_ERROR + "&7Oops! The player &7\"&c{0}&7\" is not online!", username));
    }

    // TOO MANY ARGUMENTS
    public static void invalidArguments(CommandSender sender, String usage) {
        sender.sendMessage(format(PREFIX_ERROR + "&7Oops! You provided an invalid amount of arguments (Usage: {0})", usage));
    }

    public static void pluginError(CommandSender sender, String log) {
        sender.sendMessage(format(PREFIX_ERROR + "&7Oops! There was an error in the plugin, please notify an admin."));
        Logger.severe(log);
    }
}
