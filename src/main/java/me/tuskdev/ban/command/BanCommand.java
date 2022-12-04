package me.tuskdev.ban.command;

import me.saiintbrisson.bukkit.command.command.BukkitContext;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.tuskdev.ban.cache.BanCache;
import me.tuskdev.ban.controller.BanController;
import me.tuskdev.ban.enums.BanState;
import me.tuskdev.ban.model.Ban;
import me.tuskdev.ban.util.BukkitUtil;
import me.tuskdev.ban.util.TimeParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class BanCommand {

    private final BanCache banCache;
    private final BanController banController;
    private final ConfigurationSection configurationSection;

    public BanCommand(BanCache banCache, BanController banController, ConfigurationSection configurationSection) {
        this.banCache = banCache;
        this.banController = banController;
        this.configurationSection = configurationSection;
    }

    @Command(
            name = "ban",
            aliases = { "tempban" },
            permission = "ban.use",
            usage = "ban <player> <time> <reason>"
    )
    public void handleCommand(BukkitContext context) {
        if (context.argsCount() <= 1) {
            context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("ban-usage")));
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(context.getArg(0));
        if (!offlinePlayer.hasPlayedBefore()) {
            context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("player-not-found")));
            return;
        }

        if (offlinePlayer.getName().equals(context.getSender().getName())) {
            context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("cannot-ban-yourself")));
            return;
        }

        Ban ban = banCache.get(offlinePlayer.getUniqueId());
        if (ban != null && ban.getState() == BanState.ACTIVE && !ban.isExpired()) {
            context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("player-already-banned")));
            return;
        }

        // INVALID AND DELETE EXPIRED BAN
        else if (ban != null && ban.getState() == BanState.ACTIVE) {
            ban.setState(BanState.EXPIRED);
            banCache.invalidate(offlinePlayer.getUniqueId());
            banController.update(ban);
        }

        long time = (context.getArg(1).contains("-") ? TimeParser.convert(context.getArg(1)) : -1);
        if (context.argsCount() >= 2 && context.getArg(1).contains("-") && time == -1) {
            context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("invalid-time")));
            return;
        }

        String reason = arrayToString(context.getArgs());
        if (reason.length() > 255) {
            context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("reason-too-long")));
            return;
        }

        Ban newBan = new Ban(offlinePlayer.getUniqueId(), (context.getSender() instanceof Player ? ((Player)context.getSender()).getUniqueId() : BukkitUtil.CONSOLE_UUID), reason, System.currentTimeMillis(), (time == -1 ? -1 : System.currentTimeMillis() + time));
        banCache.put(newBan);
        banController.insert(newBan);

        context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("ban-success").replace("{player}", offlinePlayer.getName())));

        if (offlinePlayer.isOnline()) offlinePlayer.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("ban-kick").replace("{author}", context.getSender().getName()).replace("{reason}", reason).replace("{time}", (time == -1 ? "permanent" : TimeParser.format(time)))));
    }

    String arrayToString(String[] array) {
        StringBuilder builder = new StringBuilder();

        for (int i = 2; i < array.length; i++)
            builder.append(array[i]).append(" ");

        return builder.toString().trim();
    }

}
