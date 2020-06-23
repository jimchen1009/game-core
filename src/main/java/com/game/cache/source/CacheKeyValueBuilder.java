package com.game.cache.source;

import com.game.cache.ICacheUniqueId;
import com.game.cache.key.IKeyValueBuilder;

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
    public List<Map.Entry<String, Object>> createCombineUniqueKeyValue(Map<String, Object> cacheValue){
        List<Map.Entry<String, Object>> entryList = cacheUniqueId.createPrimaryUniqueKeys((long) cacheValue.get(cacheUniqueId.getPrimaryKey()));
        for (String secondaryKey : cacheUniqueId.getSecondaryKeyList()) {
            entryList.add(new AbstractMap.SimpleEntry<>(secondaryKey, cacheValue.get(secondaryKey)));
        }
        return entryList;
    }

    @Override
    public K createSecondaryKey(Map<String, Object> cacheValue) {
        return secondaryBuilder.createKey(addKeyValue(cacheUniqueId.getSecondaryKeyList(), cacheValue));
    }

    @Override
    public String toSecondaryKeyString(K secondaryKey) {
        return secondaryBuilder.toKeyString(secondaryKey);
    }

    @Override
    public String toSecondaryKeyString(Map<String, Object> cacheValue) {
        Object[] objectValue = addKeyValue(cacheUniqueId.getSecondaryKeyList(), cacheValue);
        return secondaryBuilder.toKeyString(objectValue);
    }

    @Override
    public K createSecondaryKey(String string) {
        return secondaryBuilder.createKey(string);
    }

    private Object[] addKeyValue(List<String> keyNameList, Map<String, Object> cacheValue){
        Object[] objectValues = new Object[keyNameList.size()];
        for (int i = 0; i < keyNameList.size(); i++) {
            objectValues[i] = cacheValue.get(keyNameList.get(i));
        }
        return objectValues;
    }
}
