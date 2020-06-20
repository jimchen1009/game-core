package com.game.cache.source.interact;

import com.game.cache.ICacheUniqueKey;

import java.util.function.BiConsumer;

public class CacheDBInteract extends CacheInteract<CacheDBCollection> implements ICacheDBInteract {

    public CacheDBInteract(ICacheLifeInteract cacheLifeInteract, BiConsumer<Long, ICacheUniqueKey> consumer) {
        super(cacheLifeInteract, consumer);
    }
}
