package com.game.core.cache.source;

import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.key.IKeyValueBuilder;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CacheKeyValueBuilder<K> implements ICacheKeyValueBuilder<K> {

    private final ICacheUniqueId cacheUniqueId;
    private final IKeyValueBuilder<K> secondaryBuilder;

    public CacheKeyValueBuilder(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder) {
        this.cacheUniqueId = cacheUniqueId;
        this.secondaryBuilder = Objects.requireNonNull(secondaryBuilder);
    }

    @Override
    public List<Map.Entry<String, Object>> createPrimaryKeyValue(long primaryKey) {
        return cacheUniqueId.createPrimaryUniqueKeys(primaryKey);
    }

    @Override
    public List<Map.Entry<String, Object>> createCombineUniqueKeyValue(long primaryKey, K secondaryKey) {
        List<Map.Entry<String, Object>> entryList = createPrimaryKeyValue(primaryKey);
        return addKeyValue(entryList, cacheUniqueId.getSecondaryKeyList(), secondaryBuilder.toKeyValue(secondaryKey));
    }

    private List<Map.Entry<String, Object>> addKeyValue(List<Map.Entry<String, Object>> keyValue, List<String> keyNames, Object[] objectValues){
        for (int i = 0; i < keyNames.size(); i++) {
            keyValue.add(new AbstractMap.SimpleEntry<>(keyNames.get(i), objectValues[i]));
        }
        return keyValue;
    }

    @Override
    public String toSecondaryKeyString(K secondaryKey) {
        return secondaryBuilder.toKeyString(secondaryKey);
    }
}
