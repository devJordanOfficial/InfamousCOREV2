package com.infamousgc.infamousCORE.Modules;

import com.infamousgc.infamousCORE.Main;
import com.infamousgc.infamousCORE.Utils.Error;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class CondenseModule implements CommandExecutor {
    private final Main plugin;
    private final Map<Material, ConversionInfo> conversions = new EnumMap<>(Material.class);

    private static final String USAGE = "/condense [all]";
    private static final String PERMISSION_CONDENSE = "core.condense";

    public CondenseModule(Main plugin) {
        this.plugin = plugin;
        loadConversions();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Error.mustBePlayer(sender);
            return true;
        }

        if (!player.hasPermission(PERMISSION_CONDENSE)) {
            Error.noPermission(sender, PERMISSION_CONDENSE);
            return true;
        }

        if (args.length > 1) {
            Error.invalidArguments(sender, USAGE);
            return true;
        }

        boolean all = args.length == 1 && args[0].equalsIgnoreCase("all");
        condenseItems(player, all);

        return true;
    }

    private void loadConversions() {
        ConfigurationSection config = plugin.conversions().getConfig().getConfigurationSection("conversions");
        if (config == null) return;

        for (String key : config.getKeys(false)) {
            ConfigurationSection conversion = config.getConfigurationSection(key);
            if (conversion == null) continue;

            String fromStr = conversion.getString("from");
            String toStr = conversion.getString("to");
            String ratioStr = conversion.getString("ratio");

            if (fromStr ==null || toStr == null || ratioStr == null) continue;

            Material from = Material.valueOf(fromStr.toUpperCase());
            Material to = Material.valueOf(toStr.toUpperCase());
            int[] ratio = parseRatio(ratioStr);

            conversions.put(from, new ConversionInfo(to, ratio[0], ratio[1]));
        }
    }

    private void condenseItems(Player player, boolean all) {
        ItemStack held = player.getInventory().getItemInMainHand();

        if (held.getType().isAir()) {
            player.sendMessage(format(PREFIX_ERROR + "&7You must be holding an item to condense"));
            return;
        }

        Material heldMaterial = held.getType();
        ConversionInfo conversionInfo = conversions.get(heldMaterial);

        if (conversionInfo == null) {
            player.sendMessage(format(PREFIX_INFO + "&e{0} &7cannot be condensed", formatName(heldMaterial)));
            return;
        }

        int totalItems = 0;
        if (all) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || item.getType() != heldMaterial) continue;
                totalItems += item.getAmount();
                item.setAmount(0);
            }
        } else {
            totalItems = held.getAmount();
            held.setAmount(0);
        }

        ConversionResult result = convert(totalItems, conversionInfo.requiredAmount, conversionInfo.toAmount);
        int totalConverted = result.converted;
        int totalProduced = result.produced;
        int leftover = totalItems - totalConverted;

        if (leftover > 0)
            addItems(player, new ItemStack(heldMaterial, leftover));

        if (totalProduced > 0)
            addItems(player, new ItemStack(conversionInfo.newMaterial, totalProduced));

        if (totalConverted > 0) {
            player.sendMessage(format(PREFIX_GENERAL + "&7Condensed &d{0} {1} &7to &d{2} {3}",
                    totalConverted, formatName(heldMaterial, totalConverted != 1),
                    totalProduced, formatName(conversionInfo.newMaterial, totalProduced != 1)));
        } else
            player.sendMessage(format(PREFIX_INFO + "&7Not enough items to condense"));
    }

    private int[] parseRatio(String ratio) {
        String[] parts = ratio.split(":");
        return parts.length == 1 ? new int[]{Integer.parseInt(parts[0]), 1} : new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    private ConversionResult convert(int totalItems, int required, int toAmount) {
        int convertible = totalItems - (totalItems % required);
        int produced = (convertible / required) * toAmount;
        return new ConversionResult(convertible, produced);
    }

    private void addItems(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(leftoverItem -> player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem));
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    new TextComponent(format("&eSome items were dropped because your inventory is full!")));
        }
    }

    private String formatName(Material material) {
        return formatName(material, false);
    }

    private String formatName(Material material, boolean plural) {
        String name = Arrays.stream(material.name().toLowerCase().split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
        return plural ? name + "s" : name;
    }

    private static class ConversionInfo {
        final Material newMaterial;
        final int requiredAmount;
        final int toAmount;

        ConversionInfo(Material newMaterial, int requiredAmount, int toAmount) {
            this.newMaterial = newMaterial;
            this.requiredAmount = requiredAmount;
            this.toAmount = toAmount;
        }
    }

    private static class ConversionResult {
        final int converted;
        final int produced;

        ConversionResult(int converted, int produced) {
            this.converted = converted;
            this.produced = produced;
        }
    }
}