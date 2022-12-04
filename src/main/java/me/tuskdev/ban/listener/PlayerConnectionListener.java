package me.tuskdev.ban.listener;

import me.tuskdev.ban.cache.BanCache;
import me.tuskdev.ban.controller.BanController;
import me.tuskdev.ban.enums.BanState;
import me.tuskdev.ban.model.Ban;
import me.tuskdev.ban.util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerConnectionListener implements Listener {

    private final BanCache banCache;
    private final BanController banController;
    private final String message;

    public PlayerConnectionListener(BanCache banCache, BanController banController, String message) {
        this.banCache = banCache;
        this.banController = banController;
        this.message = ChatColor.translateAlternateColorCodes('&', message);
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        Ban ban = banCache.get(event.getUniqueId());
        if (ban != null && ban.getState() == BanState.ACTIVE && !ban.isExpired()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message.replace("{author}", ban.getAuthor().equals(BukkitUtil.CONSOLE_UUID) ? "Console" : Bukkit.getOfflinePlayer(ban.getAuthor()).getName()).replace("{reason}", ban.getReason()).replace("{start}", ban.getStart() + "").replace("{end}", ban.getEnd() + ""));
        }

        // INVALID AND DELETE EXPIRED BAN
        else if (ban != null && ban.getState() == BanState.ACTIVE) {
            ban.setState(BanState.EXPIRED);
            banCache.invalidate(event.getUniqueId());
            banController.update(ban);
        }
    }

}
