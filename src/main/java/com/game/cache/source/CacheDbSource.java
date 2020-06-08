package com.game.cache.source;

import com.game.cache.data.DataBitIndex;
import com.game.cache.data.DataCollection;
import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.common.log.LogUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class CacheDbSource<PK, K, V extends IData<K>> extends CacheSource<PK, K, V> implements ICacheDbSource<PK, K, V> {

    protected final ICacheSourceInteract<PK> sourceInteract;

    public CacheDbSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder, ICacheSourceInteract<PK> sourceInteract) {
        super(aClass, primaryBuilder, secondaryBuilder);
        this.sourceInteract = sourceInteract;
    }

    @Override
    public V get(PK primaryKey, K secondaryKey) {
        Map<String, Object> cacheValue = getCache(primaryKey, secondaryKey);
        return cacheValue == null ? null : converter.convert2Value(cacheValue);
    }

    @Override
    public List<V> getAll(PK primaryKey) {
        Collection<Map<String, Object>> cacheValuesList = getCacheAll(primaryKey);
        return converter.convert2ValueList(cacheValuesList);
    }

    @Override
    public DataCollection<K, V> getCollection(PK primaryKey) {
        CacheCollection cacheCollection = getCacheCollection(primaryKey);
        Collection<Map<String, Object>> cacheValuesList = cacheCollection.getCacheValuesList();
        List<V> valueList = converter.convert2ValueList(cacheValuesList);
        return new DataCollection<>(valueList, cacheCollection.getInformation());
    }

    @Override
    public boolean replaceOne(PK primaryKey, V value) {
        KeyCacheValue<K> keyCacheValue = KeyCacheValue.create(value.secondaryKey(), value.hasBitIndex(DataBitIndex.CacheCreated), converter.convert2Cache(value));
        boolean isSuccess = replaceOne(primaryKey, keyCacheValue);
        if (isSuccess){
        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(PK primaryKey, Collection<V> values) {
        List<KeyCacheValue<K>> cacheValueList = values.stream().map(value -> {
            Map<String, Object> cacheValue = converter.convert2Cache(value);
            return KeyCacheValue.create(value.secondaryKey(), value.hasBitIndex(DataBitIndex.CacheCreated), cacheValue);
        }).collect(Collectors.toList());
        boolean isSuccess = replaceBatch(primaryKey, cacheValueList);
        if (isSuccess){
        }
        return isSuccess;
    }

    public abstract Map<String, Object> getCache(PK primaryKey, K secondaryKey);

    public abstract Collection<Map<String, Object>> getCacheAll(PK primaryKey);

    public abstract boolean replaceOne(PK primaryKey, KeyCacheValue<K> keyCacheValue);

    public abstract boolean replaceBatch(PK primaryKey, List<KeyCacheValue<K>> keyCacheValueList);

    public final CacheCollection getCacheCollection(PK primaryKey) {
        int primarySharedId = getClassConfig().primarySharedId;
        if (primarySharedId == 0){
            return getPrimaryCollection(primaryKey);
        }
        String tableName = getClassConfig().tableName;
        CacheCollection cacheCollection = sourceInteract.removeCollection(primaryKey, tableName, primarySharedId);
        if (cacheCollection != null){
            return cacheCollection;
        }
        boolean loginSharedLoad = sourceInteract.loginSharedLoadTable(primaryKey, tableName);
        if (loginSharedLoad){
            //这个还有一个BUG，加载DB的时候，没有考虑Redis是否有数据了~
            List<Integer> primarySharedIds = sourceInteract.getPrimarySharedIds(primaryKey, tableName, primarySharedId);
            if (primarySharedIds.isEmpty()){
                throw new CacheException("%s, %s", LogUtil.toObjectString(primaryKey), getAClass().getSimpleName());
            }
            if (primarySharedIds.size() == 1 && primarySharedIds.get(0) == primarySharedId){
                return getPrimaryCollection(primaryKey);
            }
            else {
                Map<Integer, CacheCollection> sharedId2Collections = getSharedCollections(primaryKey, primarySharedIds);
                CacheCollection collection = sharedId2Collections.remove(primarySharedId);
                sourceInteract.addCollections(primaryKey, tableName, sharedId2Collections);
                return collection;
            }
        }
        else {
           return getPrimaryCollection(primaryKey);
        }
    }
}
