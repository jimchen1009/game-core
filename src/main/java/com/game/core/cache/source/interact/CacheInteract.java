package com.game.core.cache.source.interact;

import com.game.core.cache.ICacheUniqueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class CacheInteract<T> implements ICacheInteract<T> {

    private static final Logger logger = LoggerFactory.getLogger(CacheInteract.class);

    private BiConsumer<Long, ICacheUniqueId> consumer;
    private Supplier<Collection<ICacheUniqueId>> supplier;
    private final Map<Long, WeakReference<Map<ICacheUniqueId, T>>> primary2CacheMap;

    public CacheInteract(BiConsumer<Long, ICacheUniqueId> consumer, Supplier<Collection<ICacheUniqueId>> supplier) {
        this.consumer = consumer;
        this.supplier = supplier;
        this.primary2CacheMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean getAndSetSharedLoad(long primaryKey, ICacheUniqueId cacheDaoUnique) {
        WeakReference<Map<ICacheUniqueId, T>> mapWeakReference = primary2CacheMap.get(primaryKey);
        if (mapWeakReference != null){
            return false;
        }
        mapWeakReference = new WeakReference<>(new HashMap<>());
        return primary2CacheMap.putIfAbsent(primaryKey, mapWeakReference) == null;
    }

    @Override
    public void removePrimary(long primaryKey) {
        primary2CacheMap.remove(primaryKey);
    }

    @Override
    public List<ICacheUniqueId> getSharedCacheUniqueIdList(long primaryKey, ICacheUniqueId iCacheUniqueId) {
        Collection<ICacheUniqueId> cacheUniqueIds = supplier.get().stream().filter(ICacheUniqueId::isCacheLoadAdvance)
                .filter(ICacheUniqueId::isAccountCache).filter( cacheUniqueId -> !cacheUniqueId.equals(iCacheUniqueId)).collect(Collectors.toList());
        return getSharedCacheUniqueIdList(primaryKey, iCacheUniqueId, cacheUniqueIds);
    }

    protected abstract List<ICacheUniqueId> getSharedCacheUniqueIdList(long primaryKey, ICacheUniqueId cacheDaoUnique, Collection<ICacheUniqueId> cacheUniqueIds);

    @Override
    public void addCollections(long primaryKey, ICacheUniqueId iCacheUniqueId, Map<ICacheUniqueId, T> cacheUniqueIdMap) {
        if (cacheUniqueIdMap == null || cacheUniqueIdMap.isEmpty()){
            return;
        }
        WeakReference<Map<ICacheUniqueId, T>> mapWeakReference = primary2CacheMap.get(primaryKey);
        if (mapWeakReference == null){
            logger.error("primaryKey:{} cacheDaoUnique:{} error.", primaryKey, iCacheUniqueId);
            return;
        }
        Map<ICacheUniqueId, T> currentCacheUniqueIdMap = mapWeakReference.get();
        if (currentCacheUniqueIdMap == null) {
            primary2CacheMap.put(primaryKey, new WeakReference<>(cacheUniqueIdMap));
        }
        else {
            currentCacheUniqueIdMap.putAll(cacheUniqueIdMap);
        }
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
        WeakReference<Map<ICacheUniqueId, T>> mapWeakReference = primary2CacheMap.get(primaryKey);
        if (mapWeakReference == null){
            return null;
        }
        Map<ICacheUniqueId, T> cacheUniqueIdMap = mapWeakReference.get();
        return cacheUniqueIdMap == null ? null : cacheUniqueIdMap.remove(cacheDaoUnique);
    }
}
