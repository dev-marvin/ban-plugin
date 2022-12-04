package me.tuskdev.ban.command;

import me.saiintbrisson.bukkit.command.command.BukkitContext;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.tuskdev.ban.cache.BanCache;
import me.tuskdev.ban.controller.BanController;
import me.tuskdev.ban.enums.BanState;
import me.tuskdev.ban.model.Ban;
import me.tuskdev.ban.util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class UnBanCommand {

    private final BanCache banCache;
    private final BanController banController;
    private final ConfigurationSection configurationSection;

    public UnBanCommand(BanCache banCache, BanController banController, ConfigurationSection configurationSection) {
        this.banCache = banCache;
        this.banController = banController;
        this.configurationSection = configurationSection;
    }

    @Command(
            name = "unban",
            permission = "unban.use",
            usage = "unban <player>"
    )
    public void handleCommand(BukkitContext context) {
        if (context.argsCount() <= 0) {
            context.sendMessage(configurationSection.getString("unban-usage"));
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(context.getArg(0));
        if (!offlinePlayer.hasPlayedBefore()) {
            context.sendMessage(configurationSection.getString("player-not-found"));
            return;
        }

        Ban ban = banCache.get(offlinePlayer.getUniqueId());
        if (ban == null || ban.getState() != BanState.ACTIVE || ban.isExpired()) {
            // INVALID AND DELETE EXPIRED BAN
            if (ban != null && ban.getState() == BanState.ACTIVE) {
                ban.setState(BanState.EXPIRED);
                banCache.invalidate(offlinePlayer.getUniqueId());
                banController.update(ban);
            }

            context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("player-not-banned")));
            return;
        }

        banCache.invalidate(offlinePlayer.getUniqueId());

        ban.setState(BanState.SUSPENDED);
        ban.setSuspendedBy(context.getSender() instanceof Player ? ((Player)context.getSender()).getUniqueId() : BukkitUtil.CONSOLE_UUID);
        banController.update(ban);

        context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("unban-success").replace("{player}", offlinePlayer.getName())));
    }

}
