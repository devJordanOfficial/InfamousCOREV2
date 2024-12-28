package com.infamousgc.infamousCORE.Tasks;

import com.infamousgc.infamousCORE.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class ConfirmationManager implements CommandExecutor {
    private final Main plugin;
    private final Map<UUID, ConfirmationRequest> pendingConfirmations = new HashMap<>();

    public ConfirmationManager(Main plugin) {
        this.plugin = plugin;
    }

    public void requestConfirmation(Player player, String text, Consumer<Player> onConfirm) {
        UUID uuid = player.getUniqueId();

        // Cancel any existing confirmation request
        cancelConfirmation(uuid);

        // Create a new confirmation request
        ConfirmationRequest request = new ConfirmationRequest(player, text, onConfirm);
        pendingConfirmations.put(uuid, request);

        // Schedule a task to remove the confirmation request after 60 seconds
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ConfirmationRequest expiredRequest = pendingConfirmations.remove(uuid);
            if (expiredRequest != null) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(format("&7Your confirmation request has &eexpired&7.")));
            }
        }, 20 * 60); // 20 ticks per second, 60 seconds

        request.setExpirationTask(task);

        // Send confirmation message to the player
        TextComponent message = new TextComponent(format(PREFIX_INFO + "&7{0}, type /confirm or ", text));

        TextComponent clickable = new TextComponent(format("&e&nclick here"));
        clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/confirm"));
        clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(format("&7Click to &eConfirm")).create()));

        message.addExtra(clickable);
        message.addExtra(format("&7. Your request will be cancelled in 60 seconds or type /cancel"));

        player.spigot().sendMessage(message);
    }

    public boolean confirmAction(Player player) {
        UUID uuid = player.getUniqueId();
        ConfirmationRequest request = pendingConfirmations.remove(uuid);

        if (request != null) {
            request.getExpirationTask().cancel();
            request.getOnConfirm().accept(player);
            return true;
        }

        return false;
    }

    public boolean cancelAction(Player player) {
        UUID uuid = player.getUniqueId();
        ConfirmationRequest request = pendingConfirmations.remove(uuid);

        if (request != null) {
            request.getExpirationTask().cancel();
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(format("&7Your confirmation request has been &ecancelled&7.")));
            return true;
        }

        return false;
    }

    private void cancelConfirmation(UUID uuid) {
        ConfirmationRequest request = pendingConfirmations.remove(uuid);
        if (request != null) {
            request.getExpirationTask().cancel();
        }
    }

    private static class ConfirmationRequest {
        private final Player player;
        private final String action;
        private final Consumer<Player> onConfirm;
        private BukkitTask expirationTask;

        public ConfirmationRequest(Player player, String action, Consumer<Player> onConfirm) {
            this.player = player;
            this.action = action;
            this.onConfirm = onConfirm;
        }

        public Player getPlayer() {
            return player;
        }

        public String getAction() {
            return action;
        }

        public Consumer<Player> getOnConfirm() {
            return onConfirm;
        }

        public BukkitTask getExpirationTask() {
            return expirationTask;
        }

        public void setExpirationTask(BukkitTask expirationTask) {
            this.expirationTask = expirationTask;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!pendingConfirmations.containsKey(player.getUniqueId())) return true;
        if (command.getName().equalsIgnoreCase("confirm"))
            confirmAction(player);
        if (command.getName().equalsIgnoreCase("cancel"))
            cancelAction(player);
        return true;
    }
}
