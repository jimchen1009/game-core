package com.game.cache.data.value;

import com.game.cache.data.Data;
import com.game.cache.data.DataContainer;
import com.game.cache.data.IDataSource;
import com.game.cache.exception.CacheException;
import com.game.cache.mapper.JsonValueConverter;

public class DataValue<K, V extends Data<K>> extends DataContainer<K,K,V> implements IDataValue<K, V> {

    public DataValue(K primaryKey, IDataSource<K, K, V> dataSource) {
        super(primaryKey, dataSource);
    }

    @Override
    public V get() {
        return get(this.primaryKey());
    }

    @Override
    public V set(V value) {
        K dataKey = value.secondaryKey();
        K primaryKey = this.primaryKey();
        if (primaryKey.equals(dataKey)){
            return replaceOne(value);
        }
        else {
            throw new CacheException("key:%s != primaryKey:%s", JsonValueConverter.toJSONString(dataKey), JsonValueConverter.toJSONString(primaryKey));
        }
    }
}
