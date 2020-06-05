package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.common.log.LogUtil;

import java.util.List;
import java.util.Map;

public abstract class CacheDbSource<PK, K, V extends IData<K>> extends CacheSource<PK, K, V> implements ICacheDbSource<PK, K, V> {

    protected final ICacheSourceInteract<PK> sourceInteract;

    public CacheDbSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder, ICacheSourceInteract<PK> sourceInteract) {
        super(aClass, primaryBuilder, secondaryBuilder);
        this.sourceInteract = sourceInteract;
    }

    @Override
    public final CacheCollection getCollection(PK primaryKey) {
        int primarySharedId = getClassConfig().primarySharedId;
        if (primarySharedId == 0){
            return getCollection(primaryKey);
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
                return getCollection(primaryKey);
            }
            else {
                Map<Integer, CacheCollection> sharedId2Collections = getSharedCollections(primaryKey, primarySharedIds);
                CacheCollection collection = sharedId2Collections.remove(primarySharedId);
                sourceInteract.addCollections(primaryKey, tableName, sharedId2Collections);
                return collection;
            }
        }
        else {
           return getCollection(primaryKey);
        }
    }
}
