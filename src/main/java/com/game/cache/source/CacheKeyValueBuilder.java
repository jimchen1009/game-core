package com.game.cache.source;

import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassInformation;
import com.game.common.arg.Args;

import java.util.HashMap;
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

    public ClassInformation getClassInformation() {
        return information;
    }

    public Map<String, Object> createPrimaryKeyValue(PK primaryKey) {
        return addKeyValue(new HashMap<>(), information.getPrimaryKeys(), primaryBuilder.createValue(primaryKey));
    }

    public Map<String, Object> createPrimarySecondaryKeyValue(PK primaryKey, K secondaryKey) {
        Map<String, Object> key2Value = addKeyValue(new HashMap<>(), information.getPrimaryKeys(), primaryBuilder.createValue(primaryKey));
        return addKeyValue(key2Value, information.getSecondaryKeys(), secondaryBuilder.createValue(secondaryKey));
    }

    private Map<String, Object> addKeyValue(Map<String, Object> keyValue, List<String> keyNames, Object[] objectValues){
        for (int i = 0; i < keyNames.size(); i++) {
            keyValue.put(keyNames.get(i), objectValues[i]);
        }
        return keyValue;
    }

    public Map<String, Object> createPrimarySecondaryKeyValue(Map<String, Object> cacheValue){
        Map<String, Object> keyValue = new HashMap<>();
        for (String key : information.getPrimarySecondaryKeys()) {
            keyValue.put(key, cacheValue.get(key));
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

    public Args.Two<PK, K> createPrimarySecondaryKey(Map<String, Object> cacheValue){
        PK primaryKey = primaryBuilder.createKey(addObjectValue(information.getPrimaryKeys(), cacheValue));
        K secondaryKey = secondaryBuilder.createKey(addObjectValue(information.getSecondaryKeys(), cacheValue));
        return Args.create(primaryKey, secondaryKey);
    }

    private Object[] addObjectValue(List<String> keyNameList, Map<String, Object> cacheValue){
        Object[] objectValues = new Object[keyNameList.size()];
        for (int i = 0; i < keyNameList.size(); i++) {
            objectValues[i] = cacheValue.get(keyNameList.get(i));
        }
        return objectValues;
    }
}
