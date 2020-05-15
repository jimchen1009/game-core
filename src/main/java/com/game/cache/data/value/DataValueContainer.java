package com.game.cache.data.value;

import com.game.cache.data.Data;
import com.game.cache.data.DataContainer;
import com.game.cache.data.IDataContainer;
import com.game.cache.data.IDataSource;
import com.game.cache.exception.CacheException;
import com.game.cache.mapper.JsonValueConverter;

public class DataValueContainer<K, V extends Data<K>> extends DataContainer<K,K,V> implements IDataValueContainer<K, V> {

    public DataValueContainer(IDataSource<K, K, V> dataSource) {
        super(dataSource);
    }

    @Override
    public V get(K primaryKey) {
        return get(primaryKey);
    }

    @Override
    public V set(K primaryKey, V value) {
        K dataKey = value.secondaryKey();
        if (primaryKey.equals(dataKey)){
            return replaceOne(primaryKey, value);
        }
        else {
            throw new CacheException("key:%s != primaryKey:%s", JsonValueConverter.toJSONString(dataKey), JsonValueConverter.toJSONString(primaryKey));
        }
    }
}
