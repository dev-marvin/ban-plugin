package me.tuskdev.ban.view;

import com.google.common.collect.ImmutableMap;
import me.tuskdev.ban.cache.BanCache;
import me.tuskdev.ban.controller.BanController;
import me.tuskdev.ban.enums.BanState;
import me.tuskdev.ban.inventory.*;
import me.tuskdev.ban.model.Ban;
import me.tuskdev.ban.util.BukkitUtil;
import me.tuskdev.ban.util.ItemLoader;
import me.tuskdev.ban.util.TimeParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;

public class BanHistoryView extends PaginatedView<Ban> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private final BanCache banCache;
    private final BanController banController;
    private final ConfigurationSection configurationSection;

    public BanHistoryView(BanCache banCache, BanController banController, ConfigurationSection configurationSection) {
        super(configurationSection.getInt("rows", 6), "");

        setCancelOnClick(true);
        setLayout(configurationSection.getStringList("layout").toArray(new String[] {}));

        this.banCache = banCache;
        this.banController = banController;
        this.configurationSection = configurationSection;
    }

    @Override
    protected void onOpen(OpenViewContext context) {
        OfflinePlayer target = context.get("target");
        context.setInventoryTitle(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("title", "Ban History of {player}").replace("{player}", target.getName())));
        ((PaginatedView<Ban>)context.getView()).setSource(Arrays.asList(banController.selectAll(target.getUniqueId()).toArray(new Ban[] {})));
    }

    @Override
    protected void onItemRender(PaginatedViewSlotContext<Ban> render, ViewItem item, Ban value) {
        if (value == null) return;

        // INVALID AND DELETE EXPIRED BAN
        if (value.getState() == BanState.ACTIVE && value.isExpired()) {
            value.setState(BanState.EXPIRED);
            banCache.invalidate(value.getTarget());
            banController.update(value);
        }

        Map<String, Object> variables = ImmutableMap.of("player", Bukkit.getOfflinePlayer(value.getTarget()).getName(), "reason", value.getReason(), "banned-by", value.getAuthor().equals(BukkitUtil.CONSOLE_UUID) ? "Console" : Bukkit.getOfflinePlayer(value.getAuthor()).getName(), "banned-on", DATE_FORMAT.format(value.getStart()), "expires-on", value.getEnd() == -1 ? "Permanent" : DATE_FORMAT.format(value.getEnd()));
        switch (value.getState()) {
            case ACTIVE:
                item.withItem(ItemLoader.load(configurationSection.getConfigurationSection("items.active"), variables));
                break;
            case EXPIRED:
                item.withItem(ItemLoader.load(configurationSection.getConfigurationSection("items.expired"), variables));
                break;
            case SUSPENDED:
                item.withItem(ItemLoader.load(configurationSection.getConfigurationSection("items.suspended"), variables));
                break;
        }
    }

    @Override
    public ViewItem getPreviousPageItem(PaginatedViewContext<Ban> context) {
        if (context.getPage() == 0) return null;
        return new ViewItem(context.getPreviousPageItemSlot()).withItem(ItemLoader.load(configurationSection.getConfigurationSection("items.previous-page"), ImmutableMap.of()));
    }

    @Override
    public ViewItem getNextPageItem(PaginatedViewContext<Ban> context) {
        if (context.getPagesCount() <= (context.getPage()+1)) return null;
        return new ViewItem(context.getNextPageItemSlot()).withItem(ItemLoader.load(configurationSection.getConfigurationSection("items.next-page"), ImmutableMap.of()));
    }
}
