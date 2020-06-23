package com.game.cache.source;

import com.game.cache.CacheInformation;
import com.game.cache.ICacheUniqueId;
import com.game.cache.data.DataCollection;
import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.source.interact.CacheDBCollection;
import com.game.cache.source.interact.ICacheDBInteract;
import com.game.common.log.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
        ICacheUniqueId cacheDaoUnique = getCacheUniqueId();
        int primarySharedId = cacheDaoUnique.getPrimarySharedId();
        if (primarySharedId == 0){
            return getPrimaryCollection(primaryKey);
        }
        String name = cacheDaoUnique.getName();
        CacheDBCollection cacheCollection = sourceInteract.removeCollection(primaryKey, cacheDaoUnique);
        if (cacheCollection != null){
            return cacheCollection;
        }
        boolean loginSharedLoad = sourceInteract.getAndSetSharedLoad(primaryKey, cacheDaoUnique);
        if (loginSharedLoad){
            Map<Integer, ICacheUniqueId> cacheUniqueIdMap = sourceInteract.getSharedCacheUniqueIdList(primaryKey, cacheDaoUnique).stream()
                    .collect(Collectors.toMap(ICacheUniqueId::getPrimarySharedId, cacheUniqueId -> cacheUniqueId));
            if (cacheUniqueIdMap.isEmpty()){
                throw new CacheException("%s, %s", LogUtil.toObjectString(primaryKey), getAClass().getSimpleName());
            }
            List<Integer> primarySharedIds = new ArrayList<>(cacheUniqueIdMap.keySet());
            if (primarySharedIds.size() == 1 && primarySharedIds.get(0) == primarySharedId){
                return getPrimaryCollection(primaryKey);
            }
            else {
                Map<Integer, CacheDBCollection> sharedId2Collections = getSharedCollections(primaryKey, primarySharedIds);
                CacheDBCollection collection = sharedId2Collections.remove(primarySharedId);
                Map<ICacheUniqueId, CacheDBCollection> uniqueId2Collections = new HashMap<>();
                for (Map.Entry<Integer, CacheDBCollection> entry : sharedId2Collections.entrySet()) {
                    uniqueId2Collections.put(cacheUniqueIdMap.get(entry.getKey()), entry.getValue());
                }
                sourceInteract.addCollections(primaryKey, cacheDaoUnique, uniqueId2Collections);
                return collection;
            }
        }
        else {
           return getPrimaryCollection(primaryKey);
        }
    }
}
