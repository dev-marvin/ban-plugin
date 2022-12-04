package me.tuskdev.ban.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.tuskdev.ban.controller.BanController;
import me.tuskdev.ban.model.Ban;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BanCache {

    // CHANGE VALUE TO "Optional<Ban>" TO ALLOW NULL VALUES
    private final LoadingCache<UUID, Optional<Ban>> CACHE;

    public BanCache(BanController banController) {
        this.CACHE = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build(CacheLoader.from(key -> Optional.ofNullable(banController.select(key))));
    }

    public Ban get(UUID target) {
        try {
            return CACHE.get(target).orElse(null);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void invalidate(UUID target) {
        CACHE.invalidate(target);
    }

    public void put(Ban ban) {
        CACHE.put(ban.getTarget(), Optional.of(ban));
    }

}
