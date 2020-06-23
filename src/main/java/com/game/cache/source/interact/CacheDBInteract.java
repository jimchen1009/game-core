package com.game.cache.source.interact;

import com.game.cache.ICacheUniqueId;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CacheDBInteract extends CacheInteract<CacheDBCollection> implements ICacheDBInteract {

    public CacheDBInteract(ICacheLifeInteract cacheLifeInteract, BiConsumer<Long, ICacheUniqueId> consumer, Supplier<Collection<ICacheUniqueId>> supplier) {
        super(cacheLifeInteract, consumer, supplier);
    }

    @Override
    protected List<ICacheUniqueId> getSharedCacheUniqueIdList(long primaryKey, ICacheUniqueId cacheDaoUnique, Collection<ICacheUniqueId> cacheUniqueIds) {
        return cacheUniqueIds.stream().filter( cacheUniqueId0 -> cacheUniqueId0.getCacheType().isDBType() && cacheUniqueId0.getName().equals(cacheDaoUnique.getName())).collect(Collectors.toList());
    }
}
