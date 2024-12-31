package com.infamousgc.infamousCORE.Modules;

import com.infamousgc.infamousCORE.Main;
import com.infamousgc.infamousCORE.Tasks.CooldownManager;
import com.infamousgc.infamousCORE.Utils.Error;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class FeedModule implements CommandExecutor {
    private final CooldownManager cooldownManager;

    private static final String USAGE = "/feed [Player]";
    private static final String PERMISSION_FEED = "core.feed";
    private static final String PERMISSION_FEED_OTHERS = "core.feed.others";
    private static final String PERMISSION_COOLDOWN_BYPASS_FEED = "core.cooldown.bypass.feed";

    public FeedModule(Main plugin) {
        this.cooldownManager = plugin.getCooldownManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && args.length == 0) {
            Error.mustBePlayer(sender);
            return true;
        }

        if (!sender.hasPermission(PERMISSION_FEED)) {
            Error.noPermission(sender, PERMISSION_FEED);
            return true;
        }

        Player target;
        if (args.length == 1) {
            if (!sender.hasPermission(PERMISSION_FEED_OTHERS)) {
                Error.noPermission(sender, PERMISSION_FEED_OTHERS);
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                Error.playerNotOnline(sender, args[0]);
                return true;
            }
        } else
            target = (Player) sender;

        if (args.length > 1) {
            Error.invalidArguments(sender, USAGE);
            return true;
        }

        // TODO: Implement combat check here to prevent feeding during combat

        if (sender instanceof Player player) {
            if (!cooldownManager.check(player, CooldownManager.CooldownType.FEED) && !player.hasPermission(PERMISSION_COOLDOWN_BYPASS_FEED))
                return true;
            else cooldownManager.setCooldown(player, CooldownManager.CooldownType.FEED);
        }

        feedPlayer(target);

        if (sender != target)
            sender.sendMessage(format(PREFIX_GENERAL + "&7You have fed &d{0}", target.getName()));

        target.sendMessage(format(PREFIX_GENERAL + "&7You have been &dfed"));

        return true;
    }

    private void feedPlayer(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20);
    }
}
