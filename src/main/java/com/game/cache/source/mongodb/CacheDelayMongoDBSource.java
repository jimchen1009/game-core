package com.game.cache.source.mongodb;

import com.game.cache.CacheType;
import com.game.cache.data.IData;
import com.game.cache.source.CacheDelaySource;
import com.game.cache.source.ICacheKeyValueBuilder;
import com.game.cache.source.KeyDataValue;
import com.game.cache.source.PrimaryDelayCache;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.common.arg.Args;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.UpdateOneModel;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 目前缓存设置： 一表多用的情况，回写没有合并[按照实体类单独回写]
 * @param <K>
 * @param <V>
 */
public class CacheDelayMongoDBSource<K, V extends IData<K>> extends CacheDelaySource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheDelayMongoDBSource.class);


    public CacheDelayMongoDBSource(CacheMongoDBSource<K, V> dbSource, ICacheExecutor executor) {
        super(dbSource, executor);
    }


    private void addFailureKeyDataValue(List<Args.Two<Long, KeyDataValue<K, V>>> keyCacheValueList, Map<Long, PrimaryDelayCache<K, V>> failureKeyCacheValuesMap){
        for (Args.Two<Long, KeyDataValue<K, V>> arg : keyCacheValueList) {
            addFailureKeyDataValue(arg.arg0, arg.arg1, failureKeyCacheValuesMap);
        }
    }

    private void addFailureKeyDataValue(Long primaryKey, KeyDataValue<K, V> keyDataValue, Map<Long, PrimaryDelayCache<K, V>> failureKeyCacheValuesMap){
        PrimaryDelayCache<K, V> primaryCache = failureKeyCacheValuesMap.computeIfAbsent(primaryKey, PrimaryDelayCache::new);
        primaryCache.add(keyDataValue);
    }

    private CacheMongoDBSource<K, V> getMongoDBSource() {
        return (CacheMongoDBSource<K, V>)super.getCacheSource();
    }

    @Override
    public CacheType getCacheType() {
        return getMongoDBSource().getCacheType();
    }

    @Override
    protected Map<Long, PrimaryDelayCache<K, V>> executeWritePrimaryCache(Map<Long, PrimaryDelayCache<K, V>> pkPrimaryCacheMap) {

        List<DeleteOneModel<Document>> deleteOneModelList = new ArrayList<>();
        List<UpdateOneModel<Document>> updateOneModelList =  new ArrayList<>();

        List<Args.Two<Long, KeyDataValue<K, V>>> deleteKeyCacheValueList = new ArrayList<>();
        List<Args.Two<Long, KeyDataValue<K, V>>> updateKeyCacheValueList = new ArrayList<>();


        int primarySharedId = getCacheUniqueKey().getPrimarySharedId();
        ICacheKeyValueBuilder<K> keyValueBuilder = getKeyValueBuilder();
        for (Map.Entry<Long, PrimaryDelayCache<K, V>> entry : pkPrimaryCacheMap.entrySet()) {
            for (KeyDataValue<K, V> keyDataValue : entry.getValue().getAll()) {
                if (keyDataValue.isDeleted()) {
                    List<Map.Entry<String, Object>> entryList = keyValueBuilder.createCombineUniqueKeyValue(entry.getKey(), keyDataValue.getKey());
                    deleteOneModelList.add(CacheMongoDBUtil.createDeleteOneModel(primarySharedId, entryList));
                    deleteKeyCacheValueList.add(Args.create(entry.getKey(), keyDataValue));
                }
                else {
                    List<Map.Entry<String, Object>> entryList = keyValueBuilder.createCombineUniqueKeyValue(entry.getKey(), keyDataValue.getDataValue().secondaryKey());
                    Map<String, Object> cacheValue = getConverter().convert2Cache(keyDataValue.getDataValue());
                    updateOneModelList.add(CacheMongoDBUtil.createUpdateOneModel(primarySharedId, entryList, cacheValue.entrySet()));
                    updateKeyCacheValueList.add(Args.create(entry.getKey(), keyDataValue));
                }
            }
        }


        String name = getAClass().getName();
        deleteKeyCacheValueList = handleBatch(name, deleteOneModelList, deleteKeyCacheValueList, this::deleteDB);
        updateKeyCacheValueList = handleBatch(name, updateOneModelList, updateKeyCacheValueList, this::updateDB);

        Map<Long, PrimaryDelayCache<K, V>> failureKeyCacheValuesMap = new HashMap<>();
        addFailureKeyDataValue(deleteKeyCacheValueList, failureKeyCacheValuesMap);
        addFailureKeyDataValue(updateKeyCacheValueList, failureKeyCacheValuesMap);
        return failureKeyCacheValuesMap;
    }

    private boolean deleteDB(List<DeleteOneModel<Document>> modelList){
        if (modelList.isEmpty()){
            return true;
        }
        MongoCollection<Document> collection =  getMongoDBSource().getCollection();
        BulkWriteResult bulkDeleteResult = collection.bulkWrite(modelList);
        int deletedCount = bulkDeleteResult.getDeletedCount();
        if (deletedCount == modelList.size()){
            return true;
        }
        else {
            logger.error("class:{} deletedCount:{} != modelCount:{}", getAClass().getName(), deletedCount, modelList.size());
            return false;
        }
    }

    private boolean updateDB(List<UpdateOneModel<Document>> modelList){
        if (modelList.isEmpty()){
            return true;
        }
        MongoCollection<Document> collection =  getMongoDBSource().getCollection();
        BulkWriteResult bulkUpdateResult = collection.bulkWrite(modelList);
        int modifiedCount = bulkUpdateResult.getMatchedCount() + bulkUpdateResult.getUpserts().size();
        if (modifiedCount == modelList.size()){
            return true;
        }
        else {
            logger.error("class:{} updateCount:{} != modelCount:{}", getAClass().getName(), modifiedCount, modelList.size());
            return false;
        }
    }
}
