package com.infamousgc.infamousCORE.Modules;

import com.infamousgc.infamousCORE.Main;
import com.infamousgc.infamousCORE.Tasks.CooldownManager;
import com.infamousgc.infamousCORE.Utils.Error;
import com.infamousgc.infamousCORE.Utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class HealModule implements CommandExecutor {
    private final CooldownManager cooldownManager;

    private static final String USAGE = "/heal [Player]";
    private static final String PERMISSION_HEAL = "core.heal";
    private static final String PERMISSION_HEAL_OTHERS = "core.heal.others";
    private static final String PERMISSION_COOLDOWN_BYPASS_HEAL = "core.cooldown.bypass.heal";

    public HealModule(Main plugin) {
        this.cooldownManager = plugin.getCooldownManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && args.length == 0) {
            Error.mustBePlayer(sender);
            return true;
        }

        if (!sender.hasPermission(PERMISSION_HEAL)) {
            Error.noPermission(sender, PERMISSION_HEAL);
            return true;
        }

        Player target;
        if (args.length == 1) {
            if (!sender.hasPermission(PERMISSION_HEAL_OTHERS)) {
                Error.noPermission(sender, PERMISSION_HEAL_OTHERS);
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

        // TODO: Implement combat check here to prevent healing during combat

        if (sender instanceof Player player) {
            if (!cooldownManager.check(player, CooldownManager.CooldownType.HEAL) && !player.hasPermission(PERMISSION_COOLDOWN_BYPASS_HEAL))
                return true;
            else cooldownManager.setCooldown(player, CooldownManager.CooldownType.HEAL);
        }

        healPlayer(target);

        if (sender != target)
            sender.sendMessage(format(PREFIX_GENERAL + "&7You have healed &d{0}", target.getName()));

        target.sendMessage(format(PREFIX_GENERAL + "&7You have been &dhealed"));

        return true;
    }

    private void healPlayer(Player player) {
        AttributeInstance maxHealthInstance = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthInstance == null) {
            Logger.severe("Player {0} does not have 'ATTRIBUTE.MAX_HEALTH', this is an error of the plugin, please contact the developer.");
            return;
        }
        player.setHealth(maxHealthInstance.getValue());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }
}
