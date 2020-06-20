package com.game.cache.source;

import com.game.cache.CacheUniqueKey;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassInformation;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CacheKeyValueBuilder<K> implements ICacheKeyValueBuilder<K> {

    private final CacheUniqueKey cacheUniqueKey;
    private final IKeyValueBuilder<K> secondaryBuilder;

    public CacheKeyValueBuilder(CacheUniqueKey cacheUniqueKey, IKeyValueBuilder<K> secondaryBuilder) {
        this.cacheUniqueKey = cacheUniqueKey;
        this.secondaryBuilder = Objects.requireNonNull(secondaryBuilder);
    }

    @Override
    public List<Map.Entry<String, Object>> createPrimaryKeyValue(long primaryKey) {
        return cacheUniqueKey.createPrimaryUniqueKeys(primaryKey);
    }

    @Override
    public List<Map.Entry<String, Object>> createCombineUniqueKeyValue(long primaryKey, K secondaryKey) {
        List<Map.Entry<String, Object>> entryList = createPrimaryKeyValue(primaryKey);
        return addKeyValue(entryList, cacheUniqueKey.getInformation().getSecondaryKeys(), secondaryBuilder.toKeyValue(secondaryKey));
    }

    private List<Map.Entry<String, Object>> addKeyValue(List<Map.Entry<String, Object>> keyValue, List<String> keyNames, Object[] objectValues){
        for (int i = 0; i < keyNames.size(); i++) {
            keyValue.add(new AbstractMap.SimpleEntry<>(keyNames.get(i), objectValues[i]));
        }
        return keyValue;
    }

    @Override
    public List<Map.Entry<String, Object>> createCombineUniqueKeyValue(Map<String, Object> cacheValue){
        ClassInformation information = cacheUniqueKey.getInformation();
        List<Map.Entry<String, Object>> entryList = cacheUniqueKey.createPrimaryUniqueKeys((long) cacheValue.get(information.getPrimaryKey()));
        for (String secondaryKey : information.getSecondaryKeys()) {
            entryList.add(new AbstractMap.SimpleEntry<>(secondaryKey, cacheValue.get(secondaryKey)));
        }
        return entryList;
    }

    @Override
    public K createSecondaryKey(Map<String, Object> cacheValue) {
        return secondaryBuilder.createKey(addKeyValue(cacheUniqueKey.getInformation().getSecondaryKeys(), cacheValue));
    }

    @Override
    public String toSecondaryKeyString(K secondaryKey) {
        return secondaryBuilder.toKeyString(secondaryKey);
    }

    @Override
    public String toSecondaryKeyString(Map<String, Object> cacheValue) {
        Object[] objectValue = addKeyValue(cacheUniqueKey.getInformation().getSecondaryKeys(), cacheValue);
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
