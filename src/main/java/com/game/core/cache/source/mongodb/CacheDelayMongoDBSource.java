package com.game.core.cache.source.mongodb;

import com.game.common.arg.Args;
import com.game.core.cache.CacheKeyValue;
import com.game.core.cache.CacheType;
import com.game.core.cache.data.IData;
import com.game.core.cache.source.CacheDelaySource;
import com.game.core.cache.source.ICacheKeyValueBuilder;
import com.game.core.cache.source.KeyDataCommand;
import com.game.core.cache.source.PrimaryDelayCache;
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
public class CacheDelayMongoDbSource<K, V extends IData<K>> extends CacheDelaySource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheDelayMongoDbSource.class);

    public CacheDelayMongoDbSource(CacheMongoDbSource<K, V> dbSource) {
        super(dbSource);
    }

    private void addSuccessDataCommand(List<Args.Two<Long, KeyDataCommand<K, V>>> keyCacheValueList, Map<Long, PrimaryDelayCache<K, V>> successPrimaryCacheMap){
        for (Args.Two<Long, KeyDataCommand<K, V>> arg : keyCacheValueList) {
            addSuccessDataCommand(arg.arg0, arg.arg1, successPrimaryCacheMap);
        }
    }

    private void addSuccessDataCommand(Long primaryKey, KeyDataCommand<K, V> dataCommand, Map<Long, PrimaryDelayCache<K, V>> successPrimaryCacheMap){
        PrimaryDelayCache<K, V> primaryCache = successPrimaryCacheMap.computeIfAbsent(primaryKey, this::cretePrimaryDelayCache);
        primaryCache.add(dataCommand);
    }

    private CacheMongoDbSource<K, V> getMongoDBSource() {
        return (CacheMongoDbSource<K, V>)super.getCacheSource();
    }


    @Override
    protected Map<Long, PrimaryDelayCache<K, V>> executeWritePrimaryCache(Map<Long, PrimaryDelayCache<K, V>> pkPrimaryCacheMap) {

        List<DeleteOneModel<Document>> deleteOneModelList = new ArrayList<>();
        List<UpdateOneModel<Document>> updateOneModelList =  new ArrayList<>();

        List<Args.Two<Long, KeyDataCommand<K, V>>> deleteDataCommandList = new ArrayList<>();
        List<Args.Two<Long, KeyDataCommand<K, V>>> updateDataCommandList = new ArrayList<>();


        ICacheKeyValueBuilder<K> keyValueBuilder = getKeyValueBuilder();
        for (Map.Entry<Long, PrimaryDelayCache<K, V>> entry : pkPrimaryCacheMap.entrySet()) {
            for (KeyDataCommand<K, V> dataCommand : entry.getValue().getAllDataCommands()) {
                if (dataCommand.isDeleted()) {
                    List<CacheKeyValue> entryList = keyValueBuilder.createCombineKeyValue(entry.getKey(), dataCommand.getKey());
                    deleteOneModelList.add(CacheMongoDbUtil.createDeleteOneModel(entryList));
                    deleteDataCommandList.add(Args.create(entry.getKey(), dataCommand));
                }
                else {
                    List<CacheKeyValue> entryList = keyValueBuilder.createCombineKeyValue(entry.getKey(), dataCommand.getKey());
                    Map<String, Object> cacheValue = getConverter().convert2Cache(dataCommand.getData());
                    updateOneModelList.add(CacheMongoDbUtil.createUpdateOneModel(entryList, cacheValue.entrySet()));
                    updateDataCommandList.add(Args.create(entry.getKey(), dataCommand));
                }
            }
        }


        String name = getAClass().getName();
        deleteDataCommandList = handleBatch(name, deleteOneModelList, deleteDataCommandList, this::deleteDB);
        updateDataCommandList = handleBatch(name, updateOneModelList, updateDataCommandList, this::updateDB);

        Map<Long, PrimaryDelayCache<K, V>> successPrimaryCacheMap = new HashMap<>();
        addSuccessDataCommand(deleteDataCommandList, successPrimaryCacheMap);
        addSuccessDataCommand(updateDataCommandList, successPrimaryCacheMap);
        return successPrimaryCacheMap;
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

    @Override
    public CacheType getCacheType() {
        return getCacheSource().getCacheType();
    }
}
