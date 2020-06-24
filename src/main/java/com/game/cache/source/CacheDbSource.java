package com.game.cache.source;

import com.game.cache.CacheInformation;
import com.game.cache.ICacheUniqueId;
import com.game.cache.data.DataCollection;
import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.source.interact.CacheDBCollection;
import com.game.cache.source.interact.ICacheDBInteract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class CacheDbSource<K, V extends IData<K>> extends CacheSource<K, V> implements ICacheDbSource<K, V> {

    protected final ICacheDBInteract sourceInteract;

    public CacheDbSource(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder, ICacheDBInteract sourceInteract) {
        super(cacheUniqueId, secondaryBuilder);
        this.sourceInteract = sourceInteract;
    }

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        CacheDBCollection cacheCollection = getCacheCollection(primaryKey);
        Collection<Map<String, Object>> cacheValuesList = cacheCollection.getCacheValuesList();
        List<V> valueList = converter.convert2ValueList(cacheValuesList);
        return new DataCollection<>(valueList, new CacheInformation());
    }

    public final CacheDBCollection getCacheCollection(long primaryKey) {
        ICacheUniqueId currentCacheUniqueId = getCacheUniqueId();
        if (currentCacheUniqueId.getPrimarySharedId() == 0){
            return getPrimaryCollection(primaryKey);
        }
        String name = currentCacheUniqueId.getName();
        CacheDBCollection cacheCollection = sourceInteract.removeCollection(primaryKey, currentCacheUniqueId);
        if (cacheCollection != null){
            return cacheCollection;
        }
        boolean loginSharedLoad = sourceInteract.getAndSetSharedLoad(primaryKey, currentCacheUniqueId);
        if (loginSharedLoad){
            Map<Integer, ICacheUniqueId> cacheUniqueIdMap = sourceInteract.getSharedCacheUniqueIdList(primaryKey, currentCacheUniqueId).stream()
                    .collect(Collectors.toMap(ICacheUniqueId::getPrimarySharedId, cacheUniqueId -> cacheUniqueId));
            if (cacheUniqueIdMap.isEmpty()){
                return getPrimaryCollection(primaryKey);
            }
            else {
                List<Integer> primarySharedIds = new ArrayList<>(cacheUniqueIdMap.keySet());
                primarySharedIds.add(currentCacheUniqueId.getPrimarySharedId());
                Map<Integer, CacheDBCollection> sharedId2Collections = getSharedCollections(primaryKey, primarySharedIds);
                CacheDBCollection collection = sharedId2Collections.remove(currentCacheUniqueId.getPrimarySharedId());
                Map<ICacheUniqueId, CacheDBCollection> uniqueId2Collections = new HashMap<>();
                for (Map.Entry<Integer, CacheDBCollection> entry : sharedId2Collections.entrySet()) {
                    uniqueId2Collections.put(cacheUniqueIdMap.get(entry.getKey()), entry.getValue());
                }
                sourceInteract.addCollections(primaryKey, currentCacheUniqueId, uniqueId2Collections);
                return collection;
            }
        }
        else {
           return getPrimaryCollection(primaryKey);
        }
    }
}
