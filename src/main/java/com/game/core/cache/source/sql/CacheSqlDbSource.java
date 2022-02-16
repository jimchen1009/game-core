package com.game.core.cache.source.sql;

import com.game.core.cache.CacheKeyValue;
import com.game.core.cache.CacheType;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.CacheDbSource;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.core.db.sql.SqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CacheSqlDbSource<K, V extends IData<K>> extends CacheDbSource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheSqlDbSource.class);

    private final SqlProvider sqlProvider;

    public CacheSqlDbSource(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder, ICacheExecutor executor) {
        super(cacheUniqueId, secondaryBuilder, executor);
        this.sqlProvider = new SqlProvider(cacheUniqueId);
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        List<Object> entryList = getKeyValueBuilder().createCombineValueList(primaryKey, secondaryKey);
        SqlQuery<V> sqlQuery = new SqlQuery<>(sqlProvider.getSelectSecondaryCmd(), entryList.toArray(), rs -> converter.convert2Data(rs::getObject));
        SqlQuery.Result<V> result = SqlDbQueryUtil.getSQLDb().executeQueryObjectList(sqlQuery);
        return result.firstOne();
    }

    @Override
    public List<V> getAll(long primaryKey) {
        List<CacheKeyValue> entryList = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        Object[] params = entryList.stream().map(CacheKeyValue::getValue).toArray();
        SqlQuery<V> sqlQuery = new SqlQuery<>(sqlProvider.getSelectPrimaryCmd(), params, rs -> converter.convert2Data(rs::getObject));
        SqlQuery.Result<V> result = SqlDbQueryUtil.getSQLDb().executeQueryObjectList(sqlQuery);
        return result.getObjectList();
    }

    @Override
    public boolean replaceOne(long primaryKey, V data) {
        List<Object> objectList = new ArrayList<>();
        converter.convert2Cache(data, (name, object)-> objectList.add(object));
        SqlDbQueryUtil.getSQLDb().executeCommand(sqlProvider.getUpsertSecondaryCmd(), objectList.toArray());
        return true;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> dataList) {
        if (dataList.isEmpty()) {
            return true;
        }
        List<Object[]> paramsList = new ArrayList<>(dataList.size());
        for (V data : dataList) {
            List<Object> objectList = new ArrayList<>();
            converter.convert2Cache(data, (name, object)-> objectList.add(object));
            paramsList.add(objectList.toArray());
        }
        SqlDbQueryUtil.getSQLDb().executeBatchCommand(sqlProvider.getUpsertSecondaryCmd(), paramsList);
        return true;
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.MongoDb;
    }

    @Override
    public boolean deleteOne(long primaryKey, K secondaryKey) {
        List<Object> objectList = keyValueBuilder.createCombineValueList(primaryKey, secondaryKey);
        SqlDbQueryUtil.getSQLDb().executeCommand(sqlProvider.getDeleteSecondaryCmd(), objectList.toArray());
        return true;
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        if (secondaryKeys.isEmpty()) {
            return true;
        }
        List<Object[]> paramsList = new ArrayList<>(secondaryKeys.size());
        for (K secondaryKey : secondaryKeys) {
            List<Object> objectList = keyValueBuilder.createCombineValueList(primaryKey, secondaryKey);
            paramsList.add(objectList.toArray());
        }
        SqlDbQueryUtil.getSQLDb().executeBatchCommand(sqlProvider.getDeletePrimaryCmd(), paramsList);
        return true;
    }
}
