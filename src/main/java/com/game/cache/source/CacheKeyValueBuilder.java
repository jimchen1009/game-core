package com.game.cache.source;

import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassInformation;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CacheKeyValueBuilder<PK, K> implements ICacheKeyValueBuilder<PK, K> {

    private final ClassInformation information;
    private final IKeyValueBuilder<PK> primaryBuilder;
    private final IKeyValueBuilder<K> secondaryBuilder;

    public CacheKeyValueBuilder(ClassInformation information, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
        this.information = information;
        this.primaryBuilder = Objects.requireNonNull(primaryBuilder);
        this.secondaryBuilder = Objects.requireNonNull(secondaryBuilder);
    }

    @Override
    public ClassInformation getClassInformation() {
        return information;
    }

    @Override
    public List<Map.Entry<String, Object>> createPrimaryKeyValue(PK primaryKey) {
        return addKeyValue(new ArrayList<>(), information.getPrimaryKeys(), primaryBuilder.createValue(primaryKey));
    }

    @Override
    public List<Map.Entry<String, Object>> createAllKeyValue(PK primaryKey, K secondaryKey) {
        List<Map.Entry<String, Object>> entryList = addKeyValue(new ArrayList<>(), information.getPrimaryKeys(), primaryBuilder.createValue(primaryKey));
        return addKeyValue(entryList, information.getSecondaryKeys(), secondaryBuilder.createValue(secondaryKey));
    }

    private List<Map.Entry<String, Object>> addKeyValue(List<Map.Entry<String, Object>> keyValue, List<String> keyNames, Object[] objectValues){
        for (int i = 0; i < keyNames.size(); i++) {
            keyValue.add(new AbstractMap.SimpleEntry<>(keyNames.get(i), objectValues[i]));
        }
        return keyValue;
    }

    @Override
    public List<Map.Entry<String, Object>> createAllKeyValue(Map<String, Object> cacheValue){
        List<Map.Entry<String, Object>> keyValue = new ArrayList<>();
        for (String key : information.getPrimarySecondaryKeys()) {
            keyValue.add(new AbstractMap.SimpleEntry<>(key, cacheValue.get(key)));
        }
        return keyValue;
    }

    @Override
    public PK createPrimaryKey(Map<String, Object> cacheValue) {
        return primaryBuilder.createKey(addObjectValue(information.getPrimaryKeys(), cacheValue));
    }

    @Override
    public K createSecondaryKey(Map<String, Object> cacheValue) {
        return secondaryBuilder.createKey(addObjectValue(information.getSecondaryKeys(), cacheValue));
    }

    public IKeyValueBuilder<PK> getPrimaryBuilder() {
        return primaryBuilder;
    }

    public IKeyValueBuilder<K> getSecondaryBuilder() {
        return secondaryBuilder;
    }

    private Object[] addObjectValue(List<String> keyNameList, Map<String, Object> cacheValue){
        Object[] objectValues = new Object[keyNameList.size()];
        for (int i = 0; i < keyNameList.size(); i++) {
            objectValues[i] = cacheValue.get(keyNameList.get(i));
        }
        return objectValues;
    }
}
