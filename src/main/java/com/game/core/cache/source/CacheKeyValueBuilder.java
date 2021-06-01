package com.game.core.cache.source;

import com.game.core.cache.CacheKeyValue;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.key.IKeyValueBuilder;

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
    public List<CacheKeyValue> createPrimaryKeyValue(long primaryKey) {
        return cacheUniqueId.createPrimaryAndAdditionalKeys(primaryKey);
    }

    @Override
    public List<CacheKeyValue> createCombineUniqueKeyValue(long primaryKey, K secondaryKey) {
        List<CacheKeyValue> entryList = createPrimaryKeyValue(primaryKey);
        return addKeyValue(entryList, cacheUniqueId.getSecondaryKeyList(), secondaryBuilder.toKeyValue(secondaryKey));
    }

    private List<CacheKeyValue> addKeyValue(List<CacheKeyValue> keyValue, List<String> keyNames, Object[] objectValues){
        for (int i = 0; i < keyNames.size(); i++) {
            keyValue.add(new CacheKeyValue(keyNames.get(i), objectValues[i]));
        }
        return keyValue;
    }

    @Override
    public String toSecondaryKeyString(K secondaryKey) {
        return secondaryBuilder.toKeyString(secondaryKey);
    }
}
