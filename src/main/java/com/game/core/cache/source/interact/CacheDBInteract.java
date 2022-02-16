package com.game.core.cache.source.interact;

import com.game.core.cache.ICacheUniqueId;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CacheDBInteract extends CacheInteract<CacheDBCollection> implements ICacheDBInteract {

    public CacheDBInteract(BiConsumer<Long, ICacheUniqueId> consumer, Supplier<Collection<ICacheUniqueId>> supplier) {
        super(consumer, supplier);
    }

    @Override
    protected List<ICacheUniqueId> getSharedCacheUniqueIdList(long primaryKey, ICacheUniqueId iCacheUniqueId, Collection<ICacheUniqueId> cacheUniqueIds) {
        return cacheUniqueIds.stream().filter( cacheUniqueId0 -> cacheUniqueId0.getCacheType().isDB() && cacheUniqueId0.getName().equals(iCacheUniqueId.getName())).collect(Collectors.toList());
    }
}
