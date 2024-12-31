package com.infamousgc.infamousCORE.Modules;

import com.infamousgc.infamousCORE.Utils.Error;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.infamousgc.infamousCORE.Utils.Formatter.PREFIX_GENERAL;
import static com.infamousgc.infamousCORE.Utils.Formatter.format;

public class NicknameModule implements CommandExecutor {
    private static final String USAGE = "/nickname <reset|[Nick]> [Player]";
    private static final String PERMISSION_NICKNAME = "core.nickname";
    private static final String PERMISSION_NICKNAME_OTHERS = "core.nickname.others";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION_NICKNAME)) {
            Error.noPermission(sender, PERMISSION_NICKNAME);
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            Error.invalidArguments(sender, USAGE);
            return true;
        }

        Player target;
        String nickname = args[0];

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                Error.mustSpecifyPlayer(sender, "whose nickname to change", USAGE);
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission(PERMISSION_NICKNAME_OTHERS)) {
                Error.noPermission(sender, PERMISSION_NICKNAME_OTHERS);
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                Error.playerNotOnline(sender, args[1]);
                return true;
            }
        }

        setDisplayName(target, sender, nickname);
        return true;
    }

    private void setDisplayName(Player target, CommandSender sender, String nickname) {
        if (nickname.equalsIgnoreCase("reset")) {
            target.setDisplayName(target.getName());
            target.setPlayerListName(target.getName());
            successReset(target, sender);
        } else {
            String formattedNickname = format(nickname);
            target.setDisplayName(formattedNickname);
            target.setPlayerListName(formattedNickname);
            successSet(target, sender, formattedNickname);
        }
    }

    private void successReset(Player target, CommandSender sender) {
        target.sendMessage(format(PREFIX_GENERAL + "&7Your nickname has been reset"));
        if (!target.equals(sender)) {
            sender.sendMessage(format(PREFIX_GENERAL + "&d{0}&7's nickname has been reset", target.getName()));
        }
    }

    private void successSet(Player target, CommandSender sender, String nickname) {
        target.sendMessage(format(PREFIX_GENERAL + "&7Your nickname has been changed to \"{0}&7\"", nickname));
        if (!target.equals(sender)) {
            sender.sendMessage(format(PREFIX_GENERAL + "&d{0}&7's nickname has been changed to \"{1}&7\"", target.getName(), nickname));
        }
    }
}