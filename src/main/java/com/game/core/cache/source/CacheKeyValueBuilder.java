package com.game.core.cache.source;

import com.game.core.cache.CacheKeyValue;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.key.IKeyValueBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CacheKeyValueBuilder<K> implements ICacheKeyValueBuilder<K> {

    private final ICacheUniqueId cacheUniqueId;
    private final IKeyValueBuilder<K> secondaryBuilder;

    public CacheKeyValueBuilder(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder) {
        this.cacheUniqueId = cacheUniqueId;
        this.secondaryBuilder = Objects.requireNonNull(secondaryBuilder);
    }

    @Override
    public String toSecondaryKeyString(K secondaryKey) {
        return secondaryBuilder.toKeyString(secondaryKey);
    }

    @Override
    public List<Object> createSecondaryValueList(K secondaryKey) {
        return secondaryBuilder.toKeyValue(secondaryKey);
    }

    @Override
    public List<Object> createCombineValueList(long primaryKey, K secondaryKey) {
        List<Object> objectList = new ArrayList<>();
        objectList.add(primaryKey);
        objectList.addAll(secondaryBuilder.toKeyValue(secondaryKey));
        return objectList;
    }

    @Override
    public List<CacheKeyValue> createPrimaryKeyValue(Long primaryKey) {
        return cacheUniqueId.createPrimaryAndAdditionalKeys(primaryKey);
    }

    @Override
    public List<CacheKeyValue> createCombineKeyValue(Long primaryKey, K secondaryKey) {
        List<CacheKeyValue> keyValueList = createPrimaryKeyValue(primaryKey);
        List<String> secondaryKeyList = cacheUniqueId.getSecondaryKeyList();
        List<Object> objectList = secondaryBuilder.toKeyValue(secondaryKey);
        for (int i = 0; i < secondaryKeyList.size(); i++) {
            keyValueList.add(new CacheKeyValue(secondaryKeyList.get(i), objectList.get(i)));
        }
        return keyValueList;
    }
}
