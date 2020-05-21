package com.game.cache.data;

import com.game.cache.exception.CacheException;
import com.game.cache.mapper.ClassConverter;
import com.game.cache.mapper.IClassConverter;
import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.KeyCacheValue;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;
import com.game.common.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class DataSource<PK, K, V extends IData<K>> implements IDataSource<PK, K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);

    private final IClassConverter<K, V> converter;
    private final ICacheSource<PK, K, V> cacheSource;

    DataSource(Class<V> aClass, ValueConvertMapper convertMapper, ICacheSource<PK, K, V> cacheSource) {
        this.converter = new ClassConverter<>(aClass, convertMapper);
        this.cacheSource = cacheSource;
    }

    @Override
    public LockKey getLockKey() {
        return cacheSource.getLockKey();
    }

    @Override
    public V get(PK primaryKey, K secondaryKey) {
        Map<String, Object> cacheValue = cacheSource.get(primaryKey, secondaryKey);
        return cacheValue == null ? null : markValueSource(converter.convert2Value(cacheValue));
    }

    @Override
    public List<V> getAll(PK primaryKey) {
        Collection<Map<String, Object>> cacheValuesList = cacheSource.getAll(primaryKey);
        return convertAndMarkValueSource(cacheValuesList);
    }

    @Override
    public boolean replaceOne(PK primaryKey, V value) {
        KeyCacheValue<K> keyCacheValue = KeyCacheValue.create(value.secondaryKey(), value.isCacheResource(), converter.convert2Cache(value));
        boolean isSuccess = cacheSource.replaceOne(primaryKey, keyCacheValue);
        if (isSuccess){
        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(PK primaryKey, Collection<V> values) {
        List<KeyCacheValue<K>> cacheValueList = values.stream().map(value -> {
            Map<String, Object> cacheValue = converter.convert2Cache(value);
            return KeyCacheValue.create(value.secondaryKey(), value.isCacheResource(), cacheValue);
        }).collect(Collectors.toList());
        boolean isSuccess = cacheSource.replaceBatch(primaryKey, cacheValueList);
        if (isSuccess){
        }
        return isSuccess;
    }

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        return cacheSource.deleteOne(primaryKey, secondaryKey);
    }

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        return cacheSource.deleteBatch(primaryKey, secondaryKeys);
    }

    @Override
    public DataCollection<K, V> getCollection(PK primaryKey) {
        CacheCollection cacheCollection = cacheSource.getCollection(primaryKey);
        Collection<Map<String, Object>> cacheValuesList = cacheCollection.getCacheValuesList();
        List<V> valueList = convertAndMarkValueSource(cacheValuesList);
        return new DataCollection<>(valueList, cacheCollection.getCacheInformation());
    }

    @Override
    @SuppressWarnings("unchecked")
    public V cloneValue(V value) {
        return (V)value.clone(()-> convertClone(value));
    }

    @Override
    public IClassConverter<K, V> getConverter() {
        return converter;
    }

    private V convertClone(V value){
        Map<String, Object> cacheValue = converter.convert2Cache(value);
        return converter.convert2Value(cacheValue);
    }

    private List<V> convertAndMarkValueSource(Collection<Map<String, Object>> cacheValuesList){
        List<V> dataList = converter.convert2ValueList(cacheValuesList);
        dataList.forEach(this::markValueSource);
        return dataList;
    }

    private V markValueSource(V dataValue){
        Field field = converter.getClsDescription().getCacheSourceFiled();
        try {
            field.set(dataValue, true);
        }
        catch (Throwable e) {
            throw new CacheException("%s", e, LogUtil.toJSONString(dataValue));
        }
        return dataValue;
    }
}
