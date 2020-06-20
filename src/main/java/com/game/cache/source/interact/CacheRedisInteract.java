package com.game.cache.source.interact;

import com.game.cache.ICacheUniqueKey;

import java.util.function.BiConsumer;

public class CacheRedisInteract extends CacheInteract<CacheRedisCollection> implements ICacheRedisInteract {

    public CacheRedisInteract(ICacheLifeInteract cacheLifeInteract, BiConsumer<Long, ICacheUniqueKey> consumer) {
        super(cacheLifeInteract, consumer);
    }
}
