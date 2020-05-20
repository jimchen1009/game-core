package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class CacheDirectUpdateSource<PK, K, V extends IData<K>> extends CacheSource<PK, K, V> {

    public CacheDirectUpdateSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
        super(aClass, primaryBuilder, secondaryBuilder);
    }

    @Override
    public boolean replaceOne(PK primaryKey, KeyCacheValue<K> keyCacheValue) {
        Map<String, Object> key2Values = getKeyValueBuilder().createPrimarySecondaryKeyValue(keyCacheValue.getCacheValue());
        return replaceOne0(key2Values, keyCacheValue);
    }

    protected abstract boolean replaceOne0(Map<String, Object> key2Values, KeyCacheValue<K> keyCacheValue);

    @Override
    public boolean replaceBatch(PK primaryKey, List<KeyCacheValue<K>> keyCacheValueList) {
        if (keyCacheValueList.isEmpty()){
            return true;
        }
        Map<String, Object> key2Values =  getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        return replaceBatch0(key2Values, keyCacheValueList);
    }

    protected abstract boolean replaceBatch0(Map<String, Object> key2Values, List<KeyCacheValue<K>> keyCacheValueList);

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        Map<String, Object> key2Values = getKeyValueBuilder().createPrimarySecondaryKeyValue(primaryKey, secondaryKey);
        return deleteOne0(key2Values);
    }

    protected abstract boolean deleteOne0(Map<String, Object> key2Values);

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        if (secondaryKeys.isEmpty()){
            return true;
        }
        List<Map<String, Object>> key2ValuesList = new ArrayList<>(secondaryKeys.size());
        for (K secondaryKey : secondaryKeys) {
            Map<String, Object> keyValue = getKeyValueBuilder().createPrimarySecondaryKeyValue(primaryKey, secondaryKey);
            key2ValuesList.add(keyValue);
        }
        return deleteBatch0(key2ValuesList);
    }

    protected abstract boolean deleteBatch0(List<Map<String, Object>> key2ValuesList);
}
