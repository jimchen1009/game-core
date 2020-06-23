package com.game.cache.source.interact;

import com.game.cache.ICacheUniqueId;
import com.game.common.util.ConcurrentWeakReferenceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class CacheInteract<T> implements ICacheInteract<T> {

    private static final Logger logger = LoggerFactory.getLogger(CacheInteract.class);

    private final ICacheLifeInteract redisLifeInteract;
    private BiConsumer<Long, ICacheUniqueId> consumer;
    private Supplier<Collection<ICacheUniqueId>> supplier;
    private final ConcurrentWeakReferenceMap<Long, Map<ICacheUniqueId, T>> weakReferenceMap;

    public CacheInteract(ICacheLifeInteract cacheLifeInteract, BiConsumer<Long, ICacheUniqueId> consumer, Supplier<Collection<ICacheUniqueId>> supplier) {
        this.redisLifeInteract = cacheLifeInteract;
        this.consumer = consumer;
        this.supplier = supplier;
        this.weakReferenceMap = new ConcurrentWeakReferenceMap<>();
    }

    @Override
    public boolean getAndSetSharedLoad(long primaryKey, ICacheUniqueId cacheDaoUnique) {
        return redisLifeInteract.getAndSetSharedLoad(primaryKey, cacheDaoUnique);
    }

    @Override
    public List<ICacheUniqueId> getSharedCacheUniqueIdList(long primaryKey, ICacheUniqueId cacheDaoUnique) {
        Collection<ICacheUniqueId> cacheUniqueIds = supplier.get().stream().filter(ICacheUniqueId::isCacheLoadAdvance).collect(Collectors.toList());
        return getSharedCacheUniqueIdList(primaryKey, cacheDaoUnique, cacheUniqueIds);
    }

    protected abstract List<ICacheUniqueId> getSharedCacheUniqueIdList(long primaryKey, ICacheUniqueId cacheDaoUnique, Collection<ICacheUniqueId> cacheUniqueIds);

    @Override
    public void addCollections(long primaryKey, ICacheUniqueId cacheDaoUnique, Map<ICacheUniqueId, T> cacheUniqueIdMap) {
        if (cacheUniqueIdMap == null || cacheUniqueIdMap.isEmpty()){
            return;
        }
        String format = String.format("%s_%s", primaryKey, cacheDaoUnique.getSourceUniqueId());
        weakReferenceMap.put(primaryKey, cacheUniqueIdMap);
        Set<ICacheUniqueId> cacheUniqueIds = cacheUniqueIdMap.keySet();
        for (ICacheUniqueId cacheUniqueId : cacheUniqueIds) {
            try {
                consumer.accept(primaryKey, cacheUniqueId);
            }
            catch (Throwable t){
                logger.error("");
            }
        }
    }

    @Override
    public T removeCollection(long primaryKey, ICacheUniqueId cacheDaoUnique) {
        Map<ICacheUniqueId, T> cacheUniqueIdMap = weakReferenceMap.get(primaryKey);
        return cacheUniqueIdMap == null ? null : cacheUniqueIdMap.remove(cacheDaoUnique);
    }
}
