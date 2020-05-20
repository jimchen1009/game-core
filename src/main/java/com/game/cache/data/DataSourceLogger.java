package com.game.cache.data;

import com.game.common.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class DataSourceLogger<PK, K, V extends IData<K>> extends DataSourceDecorator<PK, K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSourceLogger.class);

    public DataSourceLogger(IDataSource<PK, K, V> dataSource) {
        super(dataSource);
    }

    @Override
    protected void onGet(PK primaryKey, K secondaryKey, V value) {
        Map<String, Object> convert2Cache = getConverter().convert2Cache(value);
        logger.trace("primaryKey:{} getAll:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(convert2Cache));
    }


    @Override
    protected void onGetAll(PK primaryKey, List<V> values) {
        List<Map<String, Object>> convert2CacheList = getConverter().convert2CacheList(values);
        logger.trace("primaryKey:{} getAll:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(convert2CacheList));
    }

    @Override
    protected void onReplaceOne(PK primaryKey, V value, boolean isSuccess) {
        Map<String, Object> convert2Cache = getConverter().convert2Cache(value);
        logger.trace("primaryKey:{} replaceOne:{} isSuccess:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(convert2Cache), isSuccess);
    }


    @Override
    protected void onReplaceBatch(PK primaryKey, Collection<V> values, boolean isSuccess) {
        List<Map<String, Object>> convert2CacheList = getConverter().convert2CacheList(values);
        logger.trace("primaryKey:{} replaceBatch:{} isSuccess:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(convert2CacheList), isSuccess);
    }

    @Override
    protected void onDeleteOne(PK primaryKey, K secondaryKey, boolean isSuccess) {
        logger.trace("primaryKey:{} onDeleteOne:{} isSuccess:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(secondaryKey), isSuccess);
    }

    @Override
    protected void onDeleteBatch(PK primaryKey, Collection<K> secondaryKeys, boolean isSuccess) {
        logger.trace("primaryKey:{} deleteBatch:{} isSuccess:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(secondaryKeys), isSuccess);
    }

    @Override
    protected void onGetCollection(PK primaryKey, DataCollection<K, V> collection) {
        logger.trace("primaryKey:{} getCollection:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(collection));
    }

    @Override
    protected boolean decoratorEnable() {
        return logger.isTraceEnabled();
    }
}
