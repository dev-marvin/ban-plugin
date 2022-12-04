package me.tuskdev.ban;

import me.saiintbrisson.bukkit.command.BukkitFrame;
import me.tuskdev.ban.cache.BanCache;
import me.tuskdev.ban.command.BanCommand;
import me.tuskdev.ban.command.HistoryCommand;
import me.tuskdev.ban.command.UnBanCommand;
import me.tuskdev.ban.controller.BanController;
import me.tuskdev.ban.inventory.ViewFrame;
import me.tuskdev.ban.listener.PlayerConnectionListener;
import me.tuskdev.ban.view.BanHistoryView;
import org.bukkit.plugin.java.JavaPlugin;

public class BanPlugin extends JavaPlugin {

    private PooledConnection pooledConnection;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        saveResource("database.properties", false);

        pooledConnection = new PooledConnection(getDataFolder().getPath(), "database.properties");
    }

    @Override
    public void onEnable() {
        BanController banController = new BanController(pooledConnection);
        BanCache banCache = new BanCache(banController);

        ViewFrame viewFrame = new ViewFrame(this);
        viewFrame.register(new BanHistoryView(banCache, banController, getConfig().getConfigurationSection("history")));

        BukkitFrame bukkitFrame = new BukkitFrame(this);
        bukkitFrame.registerCommands(
                new BanCommand(banCache, banController, getConfig().getConfigurationSection("commands")),
                new HistoryCommand(viewFrame.getView(BanHistoryView.class), getConfig().getConfigurationSection("commands")),
                new UnBanCommand(banCache, banController, getConfig().getConfigurationSection("commands"))
        );

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(banCache, banController, getConfig().getString("kick-banned-message")), this);
    }

    @Override
    public void onDisable() {
        pooledConnection.close();
    }
}
