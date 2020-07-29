package com.game.core.cache.data;

import com.game.common.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class DataSourceLogger<K, V extends IData<K>> extends DataSourceDecorator<K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSourceLogger.class);

    public DataSourceLogger(IDataSource<K, V> dataSource) {
        super(dataSource);
    }

    @Override
    protected void onGet(long primaryKey, K secondaryKey, V value) {
        Map<String, Object> convert2Cache = getConverter().convert2Cache(value);
        logger.trace("primaryKey:{} getCacheAll:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(convert2Cache));
    }


    @Override
    protected void onGetAll(long primaryKey, List<V> values) {
        List<Map<String, Object>> convert2CacheList = getConverter().convert2CacheList(values);
        logger.trace("primaryKey:{} getCacheAll:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(convert2CacheList));
    }

    @Override
    protected void onReplaceOne(long primaryKey, V value, boolean isSuccess) {
        Map<String, Object> convert2Cache = getConverter().convert2Cache(value);
        logger.trace("primaryKey:{} replaceOne:{} isSuccess:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(convert2Cache), isSuccess);
    }


    @Override
    protected void onReplaceBatch(long primaryKey, Collection<V> values, boolean isSuccess) {
        List<Map<String, Object>> convert2CacheList = getConverter().convert2CacheList(values);
        logger.trace("primaryKey:{} replaceBatch:{} isSuccess:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(convert2CacheList), isSuccess);
    }

    @Override
    protected void onDeleteOne(long primaryKey, K secondaryKey, boolean isSuccess) {
        logger.trace("primaryKey:{} onDeleteOne:{} isSuccess:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(secondaryKey), isSuccess);
    }

    @Override
    protected void onDeleteBatch(long primaryKey, Collection<K> secondaryKeys, boolean isSuccess) {
        logger.trace("primaryKey:{} deleteBatch:{} isSuccess:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(secondaryKeys), isSuccess);
    }

    @Override
    protected void onGetCollection(long primaryKey, DataCollection<K, V> collection) {
        logger.trace("primaryKey:{} getCacheCollection:{}", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(collection));
    }

    @Override
    protected boolean decoratorEnable() {
        return logger.isTraceEnabled();
    }
}
