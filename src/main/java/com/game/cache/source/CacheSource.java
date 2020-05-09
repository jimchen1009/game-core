package com.game.cache.source;

import com.game.cache.data.Data;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassDescription;
import com.game.common.arg.Args;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CacheSource<PK, K, V extends Data<K>> implements ICacheSource<PK, K> {

    protected final ClassDescription classDescription;
    private final IKeyValueBuilder<PK> primaryBuilder;
    private final IKeyValueBuilder<K> secondaryBuilder;

    public CacheSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
        this.classDescription = ClassDescription.get(aClass);
        this.primaryBuilder = primaryBuilder;
        this.secondaryBuilder = secondaryBuilder;
    }

    @Override
    public final Map<String, Object> get(PK primaryKey, K secondaryKey) {
        Map<String, Object> key2Values = addKeyValue(new HashMap<>(), classDescription.getPrimaryKeys(), primaryBuilder.buildValue(primaryKey));
        addKeyValue(key2Values, classDescription.getSecondaryKeys(), secondaryBuilder.buildValue(secondaryKey));
        return get0(key2Values);
    }

    protected abstract Map<String, Object> get0(Map<String, Object> key2Values);

    @Override
    public final Collection<Map<String, Object>> getAll(PK primaryKey) {
        Map<String, Object> key2Values = addKeyValue(new HashMap<>(), classDescription.getPrimaryKeys(), primaryBuilder.buildValue(primaryKey));
        return getAll0(key2Values);
    }

    protected abstract Collection<Map<String, Object>> getAll0(Map<String, Object> key2Values);

    @Override
    public final CacheCollection getCollection(PK primaryKey) {
        Object[] primaryValues = primaryBuilder.buildValue(primaryKey);
        Map<String, Object> key2Values = addKeyValue(new HashMap<>(), classDescription.getPrimaryKeys(), primaryBuilder.buildValue(primaryKey));
        return getCollection0(key2Values);
    }

    protected abstract CacheCollection getCollection0(Map<String, Object> key2Values);

    @Override
    public final boolean replaceOne(PK primaryKey, Map<String, Object> cacheValues) {
        Map<String, Object> key2Values = addKeyValue(cacheValues);
        return replaceOne0(key2Values, cacheValues);
    }

    protected abstract boolean replaceOne0(Map<String, Object> key2Values, Map<String, Object> cacheValues);

    @Override
    public final boolean replaceBatch(PK primaryKey, List<Map<String, Object>> cacheValuesList) {
        if (cacheValuesList.isEmpty()){
            return true;
        }
        Map<String, Object> key2Values = addKeyValue(new HashMap<>(), classDescription.getPrimaryKeys(), primaryBuilder.buildValue(primaryKey));
        List<Args.Two<Map<String, Object>, Map<String, Object>>> keyCacheValuesList = new ArrayList<>(cacheValuesList.size());
        for (Map<String, Object> cacheValues : cacheValuesList) {
            keyCacheValuesList.add(Args.create(addKeyValue(cacheValues), cacheValues));
        }
        return replaceBatch0(key2Values, keyCacheValuesList);
    }

    protected abstract boolean replaceBatch0(Map<String, Object> key2Values, List<Args.Two<Map<String, Object>, Map<String, Object>>> keyCacheValuesList);

    @Override
    public final boolean deleteOne(PK primaryKey, K secondaryKey) {
        Map<String, Object> key2Values = addKeyValue(new HashMap<>(), classDescription.getPrimaryKeys(), primaryBuilder.buildValue(primaryKey));
        addKeyValue(key2Values, classDescription.getSecondaryKeys(), secondaryBuilder.buildValue(secondaryKey));
        return deleteOne0(key2Values);
    }

    protected abstract boolean deleteOne0(Map<String, Object> key2Values);

    @Override
    public final boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        if (secondaryKeys.isEmpty()){
            return true;
        }
        List<Map<String, Object>> key2ValuesList = new ArrayList<>(secondaryKeys.size());
        for (K secondaryKey : secondaryKeys) {
            Map<String, Object> key2Values = addKeyValue(new HashMap<>(), classDescription.getPrimaryKeys(), primaryBuilder.buildValue(primaryKey));
            addKeyValue(key2Values, classDescription.getSecondaryKeys(), secondaryBuilder.buildValue(secondaryKey));
            key2ValuesList.add(key2Values);
        }
        return deleteBatch0(key2ValuesList);
    }

    protected abstract boolean deleteBatch0(List<Map<String, Object>> key2ValuesList);


    private Map<String, Object> addKeyValue(Map<String, Object> key2Values, List<String> keyNames, Object[] keyValues){
        for (int i = 0; i < keyNames.size(); i++) {
            key2Values.put(keyNames.get(i), keyValues[i]);
        }
        return key2Values;
    }

    private Map<String, Object> addKeyValue(Map<String, Object> cacheValues){
        Map<String, Object> key2Values = new HashMap<>();
        for (String key : classDescription.getPrimaryAndSecondaryKeys()) {
            key2Values.put(key, cacheValues.get(key));
        }
        return key2Values;
    }
}
