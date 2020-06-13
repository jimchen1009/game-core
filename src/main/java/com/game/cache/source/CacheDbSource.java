package com.game.cache.source;

import com.game.cache.CacheDaoUnique;
import com.game.cache.ICacheDaoUnique;
import com.game.cache.data.DataCollection;
import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassConfig;
import com.game.common.log.LogUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class CacheDbSource<K, V extends IData<K>> extends CacheSource<K, V> implements ICacheDbSource<K, V> {

    protected final ICacheSourceInteract sourceInteract;

    public CacheDbSource(CacheDaoUnique cacheDaoUnique, IKeyValueBuilder<K> secondaryBuilder, ICacheSourceInteract sourceInteract) {
        super(cacheDaoUnique, secondaryBuilder);
        this.sourceInteract = sourceInteract;
    }

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        CacheCollection cacheCollection = getCacheCollection(primaryKey);
        Collection<Map<String, Object>> cacheValuesList = cacheCollection.getCacheValuesList();
        List<V> valueList = converter.convert2ValueList(cacheValuesList);
        return new DataCollection<>(valueList, cacheCollection.getInformation());
    }

    public final CacheCollection getCacheCollection(long primaryKey) {
        int primarySharedId = getCacheDaoUnique().getPrimarySharedId();
        if (primarySharedId == 0){
            return getPrimaryCollection(primaryKey);
        }
        ICacheDaoUnique cacheDaoUnique = getCacheDaoUnique();
        String tableName = cacheDaoUnique.getTableName();
        CacheCollection cacheCollection = sourceInteract.removeCollection(primaryKey, cacheDaoUnique, primarySharedId);
        if (cacheCollection != null){
            return cacheCollection;
        }
        boolean loginSharedLoad = sourceInteract.loginSharedLoadTable(primaryKey, cacheDaoUnique);
        if (loginSharedLoad){
            //这个还有一个BUG，加载DB的时候，没有考虑Redis是否有数据了~
            List<Integer> primarySharedIds = ClassConfig.getPrimarySharedIdList(tableName);
            if (primarySharedIds.isEmpty()){
                throw new CacheException("%s, %s", LogUtil.toObjectString(primaryKey), getAClass().getSimpleName());
            }
            if (primarySharedIds.size() == 1 && primarySharedIds.get(0) == primarySharedId){
                return getPrimaryCollection(primaryKey);
            }
            else {
                Map<Integer, CacheCollection> sharedId2Collections = getSharedCollections(primaryKey, primarySharedIds);
                CacheCollection collection = sharedId2Collections.remove(primarySharedId);
                sourceInteract.addCollections(primaryKey, cacheDaoUnique, sharedId2Collections);
                return collection;
            }
        }
        else {
           return getPrimaryCollection(primaryKey);
        }
    }
}
