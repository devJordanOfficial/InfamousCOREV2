package com.infamousgc.infamousCORE.Commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommand {
    private final String usage = "/gamemode <Gamemode> [Player]";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Base Gamemode Command
        if (label.equalsIgnoreCase("gamemode") || label.equalsIgnoreCase("gm")) {

            if (!(sender.hasPermission("core.gamemode"))) {
                Error.noPermission(sender, "core.gamemode");
                return true;
            }

            // The player did not specify a gamemode where required
            if (args.length == 0) {
                errorNoGamemode(sender);
                return true;
            }
            // Console tried to set its own gamemode - must specify a player
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    Error.mustSpecifyPlayer(sender, "whose gamemode to set", usage);
                }
                setPlayerGamemode(args[0], sender.getName(), sender);
                return true;
            }
            // The sender specified a target player
            if (args.length == 2) {
                if (!(sender.hasPermission("core.gamemode.others"))) {
                    Error.noPermission(sender, "core.gamemode.others");
                    return true;
                }
                setPlayerGamemode(args[0], args[1], sender);
                return true;
            }
            Error.tooManyArgs(sender, usage);
            return true;
        }

        // Gamemode Survival Shorthand
        if (label.equalsIgnoreCase("gms")) {

            if (!(sender.hasPermission("core.gamemode"))) {
                Error.noPermission(sender, "core.gamemode");
                return true;
            }

            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    Error.mustSpecifyPlayer(sender, "whose gamemode to set", usage);
                    return true;
                }
                setPlayerGamemode("SURVIVAL", sender.getName(), sender);
                return true;
            }
            if (args.length == 1) {
                if (!playerExists(sender, args[0])) return true;
                setPlayerGamemode("SURVIVAL", args[0], sender);
                return true;
            }
            Error.tooManyArgs(sender, "/gms [Player]");
            return true;
        }

        // Gamemode Creative Shorthand
        if (label.equalsIgnoreCase("gmc")) {

            if (!(sender.hasPermission("core.gamemode"))) {
                Error.noPermission(sender, "core.gamemode");
                return true;
            }

            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    Error.mustSpecifyPlayer(sender, "whose gamemode to set", usage);
                    return true;
                }
                setPlayerGamemode("CREATIVE", sender.getName(), sender);
                return true;
            }
            if (args.length == 1) {
                if (!playerExists(sender, args[0])) return true;
                setPlayerGamemode("CREATIVE", args[0], sender);
                return true;
            }
            Error.tooManyArgs(sender, "/gmc [Player]");
            return true;
        }

        // Gamemode Adventure Shorthand
        if (label.equalsIgnoreCase("gma")) {

            if (!(sender.hasPermission("core.gamemode"))) {
                Error.noPermission(sender, "core.gamemode");
                return true;
            }

            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    Error.mustSpecifyPlayer(sender, "whose gamemode to set", usage);
                    return true;
                }
                setPlayerGamemode("ADVENTURE", sender.getName(), sender);
                return true;
            }
            if (args.length == 1) {
                if (!playerExists(sender, args[0])) return true;
                setPlayerGamemode("ADVENTURE", args[0], sender);
                return true;
            }
            Error.tooManyArgs(sender, "/gma [Player]");
            return true;
        }

        // Gamemode Spectator Shorthand
        if (label.equalsIgnoreCase("gmsp")) {

            if (!(sender.hasPermission("core.gamemode"))) {
                Error.noPermission(sender, "core.gamemode");
                return true;
            }

            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    Error.mustSpecifyPlayer(sender, "whose gamemode to set", usage);
                    return true;
                }
                setPlayerGamemode("SPECTATOR", sender.getName(), sender);
                return true;
            }
            if (args.length == 1) {
                if (!playerExists(sender, args[0])) return true;
                setPlayerGamemode("SPECTATOR", args[0], sender);
                return true;
            }
            Error.tooManyArgs(sender, "/gmsp [Player]");
            return true;
        }

        return false;
    }

    @SuppressWarnings("ConstantConditions") // Error is already being checked for
    private void setPlayerGamemode(String gamemode, String username, CommandSender sender) {

        if (Bukkit.getServer().getPlayer(username) == null) {
            Error.playerNotOnline(sender, username);
            return;
        }

        Player p = Bukkit.getServer().getPlayer(username);

        // Survival
        if (gamemode.equalsIgnoreCase("survival") ||
                gamemode.equalsIgnoreCase("s") ||
                gamemode.equalsIgnoreCase("0")) {
            p.setGameMode(GameMode.SURVIVAL);
            successTarget(p, "Survival");
            if (p != sender) {
                successSender(sender, username, "Survival");
            }
            return;
        }

        // Creative
        if (gamemode.equalsIgnoreCase("creative") ||
                gamemode.equalsIgnoreCase("c") ||
                gamemode.equalsIgnoreCase("1")) {
            p.setGameMode(GameMode.CREATIVE);
            successTarget(p, "Creative");
            if (p != sender) {
                successSender(sender, username, "Creative");
            }
            return;
        }

        // Adventure
        if (gamemode.equalsIgnoreCase("adventure") ||
                gamemode.equalsIgnoreCase("a") ||
                gamemode.equalsIgnoreCase("2")) {
            p.setGameMode(GameMode.ADVENTURE);
            successTarget(p, "Adventure");
            if (p != sender) {
                successSender(sender, username, "Adventure");
            }
            return;
        }

        // Spectator
        if (gamemode.equalsIgnoreCase("spectator") ||
                gamemode.equalsIgnoreCase("sp") ||
                gamemode.equalsIgnoreCase("3")) {
            p.setGameMode(GameMode.SPECTATOR);
            successTarget(p, "Spectator");
            if (p != sender) {
                successSender(sender, username, "Spectator");
            }
            return;
        }

        errorInvalidGamemode(sender, gamemode);
    }

    private boolean playerExists(CommandSender sender, String username) {
        if (!(sender.hasPermission("core.gamemode.others"))) {
            Error.noPermission(sender, "core.gamemode.others");
            return false;
        }
        if (Bukkit.getServer().getPlayer(username) == null) {
            Error.playerNotOnline(sender, username);
            return false;
        }
        return true;
    }

    private void successTarget(Player player, String gamemode) {
        player.sendMessage(parse(prefixGeneral + "&7Your gamemode has been changed to &d" + gamemode));
    }

    private void successSender(CommandSender sender, String username, String gamemode) {
        sender.sendMessage(parse(prefixGeneral + "&d" + username + "&7's gamemode was set to &d" + gamemode));
    }

    private void errorNoGamemode(CommandSender sender) {
        sender.sendMessage(parse(prefixError + "&7Oops! You must specify a gamemode. (Usage: " + usage + ")"));
    }

    private void errorInvalidGamemode(CommandSender sender, String gamemode) {
        sender.sendMessage(parse(prefixError + "&7Oops! The gamemode \"&c" + gamemode + "&7\" does not exist. (Usage: " + usage + ")"));
    }
}
