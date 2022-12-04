package me.tuskdev.ban.command;

import com.google.common.collect.ImmutableMap;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import me.tuskdev.ban.view.BanHistoryView;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class HistoryCommand {
    
    private final BanHistoryView banHistoryView;
    private final ConfigurationSection configurationSection;
    
    public HistoryCommand(BanHistoryView banHistoryView, ConfigurationSection configurationSection) {
        this.banHistoryView = banHistoryView;
        this.configurationSection = configurationSection;
    }
    
    @Command(
            name = "history",
            permission = "history.use",
            usage = "history <player>",
            description = "View the history of a player",
            target = CommandTarget.PLAYER
    )
    public void handleCommand(Context<Player> context) {
        if (context.argsCount() <= 0) {
            context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("history-usage")));
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(context.getArg(0));
        if (!offlinePlayer.hasPlayedBefore()) {
            context.sendMessage(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("player-not-found")));
            return;
        }
        
        banHistoryView.open(context.getSender(), ImmutableMap.of("target", offlinePlayer));
    }
    
}
