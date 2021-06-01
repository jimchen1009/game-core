package com.game.core.cache.source;

import com.game.core.cache.CacheInformation;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.interact.CacheDBCollection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class CacheDbSource<K, V extends IData<K>> extends CacheSource<K, V> implements ICacheDbSource<K, V> {

    public CacheDbSource(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder) {
        super(cacheUniqueId, secondaryBuilder);
    }

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        CacheDBCollection cacheCollection = getPrimaryCollection(primaryKey);
        Collection<Map<String, Object>> cacheValuesList = cacheCollection.getCacheValuesList();
        List<V> valueList = converter.convert2ValueList(cacheValuesList);
        return new DataCollection<>(valueList, new CacheInformation());
    }
}
