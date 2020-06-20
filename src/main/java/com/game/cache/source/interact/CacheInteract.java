package com.game.cache.source.interact;

import com.game.cache.ICacheUniqueKey;
import com.game.common.util.ConcurrentWeakReferenceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CacheInteract<T> implements ICacheInteract<T> {

    private static final Logger logger = LoggerFactory.getLogger(CacheInteract.class);

    private final ICacheLifeInteract redisLifeInteract;
    private BiConsumer<Long, ICacheUniqueKey> consumer;
    private final ConcurrentWeakReferenceMap<String, Map<Integer, T>> weakReferenceMap;

    public CacheInteract(ICacheLifeInteract cacheLifeInteract, BiConsumer<Long, ICacheUniqueKey> consumer) {
        this.redisLifeInteract = cacheLifeInteract;
        this.consumer = consumer;
        this.weakReferenceMap = new ConcurrentWeakReferenceMap<>();
    }

    @Override
    public boolean getAndSetSharedLoad(long primaryKey, ICacheUniqueKey cacheDaoUnique) {
        return redisLifeInteract.getAndSetSharedLoad(primaryKey, cacheDaoUnique);
    }

    @Override
    public void addCollections(long primaryKey, ICacheUniqueKey cacheDaoUnique, Map<Integer, T> collections) {
        if (collections == null || collections.isEmpty()){
            return;
        }
        String format = String.format("%s_%s", cacheDaoUnique.getName(), primaryKey);
        weakReferenceMap.put(format, collections);
        List<ICacheUniqueKey> cacheDaoUniqueList = cacheDaoUnique.sharedCacheDaoUniqueList();
        for (ICacheUniqueKey daoUnique : cacheDaoUniqueList) {
            try {
                consumer.accept(primaryKey, daoUnique);
            }
            catch (Throwable t){
                logger.error("");
            }
        }
    }

    @Override
    public T removeCollection(long primaryKey, ICacheUniqueKey cacheDaoUnique) {
        String format = String.format("%s_%s", cacheDaoUnique.getName(), primaryKey);
        Map<Integer, T> collections = weakReferenceMap.get(format);
        return collections == null ? null : collections.remove(cacheDaoUnique.getPrimarySharedId());
    }
}
