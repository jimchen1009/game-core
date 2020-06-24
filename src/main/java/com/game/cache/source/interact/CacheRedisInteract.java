package com.game.cache.source.interact;

import com.game.cache.ICacheUniqueId;
import com.game.cache.IClassConfig;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CacheRedisInteract extends CacheInteract<CacheRedisCollection> implements ICacheRedisInteract {

    public CacheRedisInteract(BiConsumer<Long, ICacheUniqueId> consumer, Supplier<Collection<ICacheUniqueId>> supplier) {
        super(consumer, supplier);
    }

    @Override
    protected List<ICacheUniqueId> getSharedCacheUniqueIdList(long primaryKey, ICacheUniqueId cacheDaoUnique, Collection<ICacheUniqueId> cacheUniqueIds) {
        return cacheUniqueIds.stream().filter(IClassConfig::isRedisSupport).collect(Collectors.toList());
    }
}
