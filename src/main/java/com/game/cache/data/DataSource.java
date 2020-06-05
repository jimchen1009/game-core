package com.game.cache.data;

import com.game.cache.exception.CacheException;
import com.game.cache.mapper.ClassConverter;
import com.game.cache.mapper.IClassConverter;
import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;
import com.game.common.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

class DataSource<PK, K, V extends IData<K>> implements IDataSource<PK, K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);

    private final IClassConverter<K, V> converter;
    private final ICacheSource<PK, K, V> cacheSource;

    DataSource(Class<V> aClass, ValueConvertMapper convertMapper, ICacheSource<PK, K, V> cacheSource) {
        this.converter = new ClassConverter<>(aClass, convertMapper);
        this.cacheSource = cacheSource;
    }

    @Override
    public LockKey getLockKey(PK primaryKey) {
        return cacheSource.getLockKey(primaryKey);
    }

    @Override
    public V get(PK primaryKey, K secondaryKey) {
        V data = cacheSource.get(primaryKey, secondaryKey);
        return data == null ? null : markValueSource(data);
    }

    @Override
    public List<V> getAll(PK primaryKey) {
        List<V> dataList = cacheSource.getAll(primaryKey);
        for (V data : dataList) {
            markValueSource(data);
        }
        return dataList;
    }

    @Override
    public boolean replaceOne(PK primaryKey, V value) {
        boolean isSuccess = cacheSource.replaceOne(primaryKey, value);
        if (isSuccess){
        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(PK primaryKey, Collection<V> values) {
        boolean isSuccess = cacheSource.replaceBatch(primaryKey, values);
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
        DataCollection<K, V> collection = cacheSource.getCollection(primaryKey);
        for (V data : collection.getDataList()) {
            markValueSource(data);
        }
        return collection;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V cloneValue(V value) {
        return cacheSource.cloneValue(value);
    }

    @Override
    public IClassConverter<K, V> getConverter() {
        return converter;
    }

    private V markValueSource(V dataValue){
        Field field = converter.getInformation().getCacheSourceFiled();
        try {
            field.set(dataValue, true);
        }
        catch (Throwable e) {
            throw new CacheException("%s", e, LogUtil.toJSONString(dataValue));
        }
        return dataValue;
    }
}
