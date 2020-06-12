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
 * @param <PK>
 * @param <K>
 * @param <V>
 */
public class CacheDelayMongoDBSource<PK, K, V extends IData<K>> extends CacheDelaySource<PK, K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheDelayMongoDBSource.class);


    public CacheDelayMongoDBSource(CacheMongoDBSource<PK, K, V> dbSource, ICacheExecutor executor) {
        super(dbSource, executor);
    }


    private void addFailureKeyDataValue(List<Args.Two<PK, KeyDataValue<K, V>>> keyCacheValueList, Map<PK, PrimaryDelayCache<PK, K, V>> failureKeyCacheValuesMap){
        for (Args.Two<PK, KeyDataValue<K, V>> arg : keyCacheValueList) {
            addFailureKeyDataValue(arg.arg0, arg.arg1, failureKeyCacheValuesMap);
        }
    }

    private void addFailureKeyDataValue(PK primaryKey, KeyDataValue<K, V> keyDataValue, Map<PK, PrimaryDelayCache<PK, K, V>> failureKeyCacheValuesMap){
        PrimaryDelayCache<PK, K, V> primaryCache = failureKeyCacheValuesMap.computeIfAbsent(primaryKey, PrimaryDelayCache::new);
        primaryCache.add(keyDataValue);
    }

    private CacheMongoDBSource<PK, K, V> getMongoDBSource() {
        return (CacheMongoDBSource<PK, K, V>)super.getCacheSource();
    }

    @Override
    public CacheType getCacheType() {
        return getMongoDBSource().getCacheType();
    }

    @Override
    protected Map<PK, PrimaryDelayCache<PK, K, V>> executeWritePrimaryCache(Map<PK, PrimaryDelayCache<PK, K, V>> pkPrimaryCacheMap) {

        List<DeleteOneModel<Document>> deleteOneModelList = new ArrayList<>();
        List<UpdateOneModel<Document>> updateOneModelList =  new ArrayList<>();

        List<Args.Two<PK, KeyDataValue<K, V>>> deleteKeyCacheValueList = new ArrayList<>();
        List<Args.Two<PK, KeyDataValue<K, V>>> updateKeyCacheValueList = new ArrayList<>();


        int primarySharedId = getClassConfig().primarySharedId;
        ICacheKeyValueBuilder<PK, K> keyValueBuilder = getKeyValueBuilder();
        for (Map.Entry<PK, PrimaryDelayCache<PK, K, V>> entry : pkPrimaryCacheMap.entrySet()) {
            for (KeyDataValue<K, V> keyDataValue : entry.getValue().getAll()) {
                if (keyDataValue.isDeleted()) {
                    List<Map.Entry<String, Object>> entryList = keyValueBuilder.createAllKeyValue(entry.getKey(), keyDataValue.getKey());
                    deleteOneModelList.add(CacheMongoDBUtil.createDeleteOneModel(primarySharedId, entryList));
                    deleteKeyCacheValueList.add(Args.create(entry.getKey(), keyDataValue));
                }
                else {
                    List<Map.Entry<String, Object>> entryList = keyValueBuilder.createAllKeyValue(entry.getKey(), keyDataValue.getDataValue().secondaryKey());
                    Map<String, Object> cacheValue = getConverter().convert2Cache(keyDataValue.getDataValue());
                    updateOneModelList.add(CacheMongoDBUtil.createUpdateOneModel(primarySharedId, entryList, cacheValue.entrySet()));
                    updateKeyCacheValueList.add(Args.create(entry.getKey(), keyDataValue));
                }
            }
        }


        String name = getAClass().getName();
        deleteKeyCacheValueList = handleBatch(name, deleteOneModelList, deleteKeyCacheValueList, this::deleteDB);
        updateKeyCacheValueList = handleBatch(name, updateOneModelList, updateKeyCacheValueList, this::updateDB);

        Map<PK, PrimaryDelayCache<PK, K, V>> failureKeyCacheValuesMap = new HashMap<>();
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
