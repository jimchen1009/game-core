package com.game.cache.data;

import com.game.cache.mapper.ClassConverter;
import com.game.cache.mapper.IClassConverter;
import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.ICacheSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataSource<PK, K, V extends Data<K>> implements IDataSource<PK, K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);

    private final IClassConverter<K, V> converter;
    private final ICacheSource<PK, K> cacheSource;

    public DataSource(Class<V> aClass, ValueConvertMapper convertMapper, ICacheSource<PK, K> cacheSource) {
        this.converter = new ClassConverter<>(aClass, convertMapper);
        this.cacheSource = cacheSource;
    }

    @Override
    public V get(PK primaryKey, K key) {
        Map<String, Object> cacheValue = cacheSource.get(primaryKey, key);
        return cacheValue == null ? null : converter.convert(cacheValue);
    }

    @Override
    public List<V> getAll(PK primaryKey) {
        Collection<Map<String, Object>> cacheValues = cacheSource.getAll(primaryKey);
        return cacheValues.stream().map(converter::convert).collect(Collectors.toList());
    }

    @Override
    public boolean replaceOne(PK primaryKey, V value) {
        return cacheSource.replaceOne(primaryKey, converter.convert(value));
    }

    @Override
    public boolean replaceBatch(PK primaryKey, Collection<V> values) {
        List<Map<String, Object>> cacheValueList = values.stream().map(converter::convert).collect(Collectors.toList());
        return cacheSource.replaceBatch(primaryKey, cacheValueList);
    }

    @Override
    public boolean deleteOne(PK primaryKey, K key) {
        return cacheSource.deleteOne(primaryKey, key);
    }

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> keys) {
        return cacheSource.deleteBatch(primaryKey, keys);
    }

    @Override
    public DataCollection<K, V> getCollection(PK primaryKey) {
        CacheCollection cacheCollection = cacheSource.getCollection(primaryKey);
        List<V> valueList = cacheCollection.getCacheValueList().stream().map(converter::convert).collect(Collectors.toList());
        return new DataCollection<>(valueList, cacheCollection.getCollectionInfo());
    }

    @Override
    @SuppressWarnings("unchecked")
    public V cloneValue(V value) {
        return (V)value.clone(()-> convertClone(value));
    }

    private V convertClone(V value){
        Map<String, Object> cacheValue = converter.convert(value);
        return converter.convert(cacheValue);
    }
}
