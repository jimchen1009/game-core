package com.game.cache.source.mongodb;

import com.game.cache.data.Data;
import com.game.cache.source.CacheDelayUpdateSource;
import com.game.cache.source.ICacheKeyValueBuilder;
import com.game.cache.source.KeyCacheValue;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 目前缓存设置： 一表多用的情况，回写没有合并[按照实体类单独回写]
 * @param <PK>
 * @param <K>
 * @param <V>
 */
public class CacheDelayMongoDBSource<PK, K, V extends Data<K>> extends CacheDelayUpdateSource<PK, K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheDelayMongoDBSource.class);

    public CacheDelayMongoDBSource(CacheDirectMongoDBSource<PK, K, V> dbSource, ICacheExecutor executor) {
        super(dbSource, executor);
    }

    @Override
    protected String getScheduleName() {
        return getMongoDBSource().getCollectionName();
    }

    @Override
    protected Map<PK, List<KeyCacheValue<K>>> executeWriteBackKeyCacheValues0(Map<PK, Collection<KeyCacheValue<K>>> keyCacheValuesMap) {
        CacheDirectMongoDBSource<PK, K, V> mongoDBSource = getMongoDBSource();

        List<DeleteOneModel<Document>> deleteOneModelList = new ArrayList<>();
        List<UpdateOneModel<Document>> updateOneModelList =  new ArrayList<>();

        List<Args.Two<PK, KeyCacheValue<K>>> deleteKeyCacheValueList = new ArrayList<>();
        List<Args.Two<PK, KeyCacheValue<K>>> updateKeyCacheValueList = new ArrayList<>();

        int primaryKeyId = mongoDBSource.getPrimaryKeyId();
        ICacheKeyValueBuilder<PK, K> keyValueBuilder = mongoDBSource.getKeyValueBuilder();
        for (Map.Entry<PK, Collection<KeyCacheValue<K>>> entry : keyCacheValuesMap.entrySet()) {
            for (KeyCacheValue<K> keyCacheValue : entry.getValue()) {
                if (keyCacheValue.isDeleted()) {
                    Map<String, Object> keyValue = keyValueBuilder.createPrimarySecondaryKeyValue(entry.getKey(), keyCacheValue.getKey());
                    deleteOneModelList.add(CacheMongoDBUtil.createDeleteOneModel(primaryKeyId, keyValue));
                    deleteKeyCacheValueList.add(Args.create(entry.getKey(), keyCacheValue));
                }
                else {
                    Map<String, Object> keyValue = keyValueBuilder.createPrimarySecondaryKeyValue(keyCacheValue.getCacheValue());
                    updateOneModelList.add(CacheMongoDBUtil.createUpdateOneModel(primaryKeyId, keyValue, keyCacheValue.getCacheValue()));
                    updateKeyCacheValueList.add(Args.create(entry.getKey(), keyCacheValue));
                }
            }
        }
        Map<PK, List<KeyCacheValue<K>>> failureKeyCacheValuesMap = new HashMap<>();

        MongoCollection<Document> collection = mongoDBSource.getCollection();
        try {
            if (!deleteOneModelList.isEmpty()){
                BulkWriteResult bulkDeleteResult = collection.bulkWrite(deleteOneModelList);
                int deletedCount = bulkDeleteResult.getDeletedCount();
                if (deletedCount == deleteOneModelList.size()){
                    deleteKeyCacheValueList.clear();
                }
                else {
                    logger.error("class:{} deletedCount:{} != cacheCount:{}", getAClass().getName(), deletedCount, deleteKeyCacheValueList.size());
                }
            }
            if (!updateOneModelList.isEmpty()){
                BulkWriteResult bulkUpdateResult = collection.bulkWrite(updateOneModelList);
                int modifiedCount = bulkUpdateResult.getMatchedCount() + bulkUpdateResult.getUpserts().size();
                if (modifiedCount == updateOneModelList.size()){
                    updateKeyCacheValueList.clear();
                }
                else {
                    logger.error("class:{} updateCount:{} != cacheCount:{}", getAClass().getName(), modifiedCount, updateKeyCacheValueList.size());
                }
            }
        }
        catch (Throwable t){
            logger.error("", t);
        }
        finally {
            addFailureKeyCacheValues(deleteKeyCacheValueList, failureKeyCacheValuesMap);
            addFailureKeyCacheValues(updateKeyCacheValueList, failureKeyCacheValuesMap);
        }
        return failureKeyCacheValuesMap;
    }

    private void addFailureKeyCacheValues(List<Args.Two<PK, KeyCacheValue<K>>> keyCacheValueList, Map<PK, List<KeyCacheValue<K>>> failureKeyCacheValuesMap){
        for (Args.Two<PK, KeyCacheValue<K>> arg : keyCacheValueList) {
            List<KeyCacheValue<K>> keyCacheValues = failureKeyCacheValuesMap.computeIfAbsent(arg.arg0, key-> new ArrayList<>());
            keyCacheValues.add(arg.arg1);
        }
    }

    private CacheDirectMongoDBSource<PK, K, V> getMongoDBSource() {
        return (CacheDirectMongoDBSource<PK, K, V>)super.getCacheSource();
    }
}
