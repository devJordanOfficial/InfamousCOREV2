package com.infamousgc.infamousCORE.Modules;

import com.infamousgc.infamousCORE.Utils.Error;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class GamemodeModule implements CommandExecutor {
    private static final String USAGE = "/gamemode <Gamemode> [Player]";
    private static final String PERMISSION_GAMEMODE = "core.gamemode";
    private static final String PERMISSION_GAMEMODE_OTHERS = "core.gamemode.others";

    private enum GamemodeAlias {
        SURVIVAL(GameMode.SURVIVAL, "survival", "s", "0"),
        CREATIVE(GameMode.CREATIVE, "creative", "c", "1"),
        ADVENTURE(GameMode.ADVENTURE, "adventure", "a", "2"),
        SPECTATOR(GameMode.SPECTATOR, "spectator", "sp", "3");

        private final GameMode gamemode;
        private final String[] aliases;

        GamemodeAlias(GameMode gamemode, String... aliases) {
            this.gamemode = gamemode;
            this.aliases = aliases;
        }

        private static final Map<String, GameMode> ALIAS_MAP = new HashMap<>();

        static {
            for (GamemodeAlias gamemodeAlias : values()) {
                for (String alias : gamemodeAlias.aliases) {
                    ALIAS_MAP.put(alias.toLowerCase(), gamemodeAlias.gamemode);
                }
            }
        }

        public static GameMode getByAlias(String alias) {
            return ALIAS_MAP.get(alias.toLowerCase());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION_GAMEMODE)) {
            Error.noPermission(sender, PERMISSION_GAMEMODE);
            return true;
        }

        String gamemode = null;
        String target = null;

        switch (label.toLowerCase()) {
            case "gamemode":
            case "gm":
                if (args.length < 1 || args.length > 2) {
                    Error.invalidArguments(sender, USAGE);
                    return true;
                }
                gamemode = args[0];
                target = args.length == 2 ? args[1] : sender.getName();
                break;
            case "gms":
                gamemode = "survival";
                target = args.length == 1 ? args[0] : sender.getName();
                break;
            case "gmc":
                gamemode = "creative";
                target = args.length == 1 ? args[0] : sender.getName();
                break;
            case "gma":
                gamemode = "adventure";
                target = args.length == 1 ? args[0] : sender.getName();
                break;
            case "gmsp":
                gamemode = "spectator";
                target = args.length == 1 ? args[0] : sender.getName();
                break;
            default:
                return false;
        }

        if (!sender.getName().equalsIgnoreCase(target) && !sender.hasPermission(PERMISSION_GAMEMODE_OTHERS)) {
            Error.noPermission(sender, PERMISSION_GAMEMODE_OTHERS);
            return true;
        }

        setPlayerGamemode(gamemode, target, sender);
        return true;
    }

    private void setPlayerGamemode(String gamemode, String username, CommandSender sender) {
        Player target = Bukkit.getPlayer(username);
        if (target == null) {
            Error.playerNotOnline(sender, username);
            return;
        }

        GameMode newGamemode = GamemodeAlias.getByAlias(gamemode);
        if (newGamemode == null) {
            invalidGamemode(sender, gamemode);
            return;
        }

        target.setGameMode(newGamemode);
        successTarget(target, newGamemode.name());
        if (!target.equals(sender)) {
            successSender(sender, username, newGamemode.name());
        }
    }

    private void successTarget(Player player, String gamemode) {
        player.sendMessage(format(PREFIX_GENERAL + "&7Your gamemode has been changed to &d{0}", gamemode));
    }

    private void successSender(CommandSender sender, String username, String gamemode) {
        sender.sendMessage(format(PREFIX_GENERAL + "&d{0}&7's gamemode was set to &d {1}", username, gamemode));
    }

    private void invalidGamemode(CommandSender sender, String gamemode) {
        sender.sendMessage(format(PREFIX_ERROR + "&7Oops! The gamemode \"&c{0}&7\" does not exist. Usage: {1}", gamemode, USAGE));
    }
}
