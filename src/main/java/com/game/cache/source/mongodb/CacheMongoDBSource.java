package com.game.cache.source.mongodb;

import com.game.cache.CacheInformation;
import com.game.cache.CacheType;
import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.CacheDbSource;
import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.ICacheSourceInteract;
import com.game.cache.source.KeyCacheValue;
import com.game.cache.source.executor.ICacheExecutor;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CacheMongoDBSource<PK, K, V extends IData<K>> extends CacheDbSource<PK, K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheMongoDBSource.class);


    public CacheMongoDBSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder, ICacheSourceInteract<PK> sourceInteract) {
        super(aClass, primaryBuilder, secondaryBuilder, sourceInteract);
        MongoCollection<Document> collection = getCollection();
        CacheIndex cacheIndexes = getKeyValueBuilder().getClassInformation().getCacheIndexes();
        CacheMongoDBUtil.ensureIndexes(collection, getClassConfig().primarySharedId, cacheIndexes);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.MongoDb;
    }

    @Override
    public Map<String, Object> getCache(PK primaryKey, K secondaryKey) {
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createAllKeyValue(primaryKey, secondaryKey);
        return MongoDBQueryUtil.queryOne(getCollection(), getClassConfig().primarySharedId,  entryList);
    }

    @Override
    public Collection<Map<String, Object>> getCacheAll(PK primaryKey) {
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        return MongoDBQueryUtil.queryAll(getCollection(), getClassConfig().primarySharedId,  entryList);
    }

    @Override
    public CacheCollection getPrimaryCollection(PK primaryKey) {
        int primarySharedId = getClassConfig().primarySharedId;
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        Collection<Map<String, Object>> mapCollection = MongoDBQueryUtil.queryAll(getCollection(), primarySharedId, entryList);
        return new CacheCollection(primarySharedId, mapCollection, new CacheInformation());
    }

    @Override
    public Map<Integer, CacheCollection> getSharedCollections(PK primaryKey, List<Integer> primarySharedIds) {
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        Collection<Map<String, Object>> mapCollection = MongoDBQueryUtil.queryAll(getCollection(), primarySharedIds, entryList);
        Map<Integer, List<Map<String, Object>>> primarySharedId2CacheValues = CacheCollection.groupPrimarySharedId(mapCollection);
        Map<Integer, CacheCollection> mapCollectionMap = new HashMap<>(primarySharedId2CacheValues.size());
        for (Integer primarySharedId : primarySharedIds) {
            List<Map<String, Object>> cacheValueList = primarySharedId2CacheValues.get(primarySharedId);
            if (cacheValueList == null){
                cacheValueList = Collections.emptyList();
            }
            CacheCollection cacheCollection = new CacheCollection(primarySharedId, cacheValueList, new CacheInformation());
            mapCollectionMap.put(primarySharedId, cacheCollection);
        }
        return mapCollectionMap;
    }

    @Override
    public boolean replaceOne(PK primaryKey, KeyCacheValue<K> keyCacheValue) {
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createAllKeyValue(keyCacheValue.getCacheValue());
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(getClassConfig().primarySharedId,  entryList);
        Document document = CacheMongoDBUtil.toDocument(keyCacheValue.getCacheValue().entrySet());
        MongoCollection<Document> collection = getCollection();
        UpdateResult updateOne = collection.updateOne(queryDocument, document, CacheMongoDBUtil.UPDATE_OPTIONS);
        return updateOne.wasAcknowledged();
    }

    @Override
    public boolean replaceBatch(PK primaryKey, List<KeyCacheValue<K>> keyCacheValueList) {
        List<Map.Entry<String, Object>> keyValue = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);

        List<UpdateOneModel<Document>> updateOneModelList = keyCacheValueList.stream().map( keyCacheValue -> {
            List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createAllKeyValue(keyCacheValue.getCacheValue());
            Map<String, Object> key2Values = entryList.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return CacheMongoDBUtil.createUpdateOneModel(getClassConfig().primarySharedId,  keyValue, keyCacheValue.getCacheValue().entrySet());
        }).collect(Collectors.toList());
        MongoCollection<Document> collection = getCollection();
        BulkWriteResult writeResult = collection.bulkWrite(updateOneModelList);
        return writeResult.wasAcknowledged();
    }

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createAllKeyValue(primaryKey, secondaryKey);
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(getClassConfig().primarySharedId,  entryList);
        MongoCollection<Document> collection = getCollection();
        DeleteResult deleteOne = collection.deleteOne(queryDocument);
        return deleteOne.wasAcknowledged();
    }

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        List<List<Map.Entry<String, Object>>> key2ValuesList = new ArrayList<>();
        for (K secondaryKey : secondaryKeys) {
            List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createAllKeyValue(primaryKey, secondaryKey);
            key2ValuesList.add(entryList);
        }
        List<DeleteOneModel<Document>> deleteOneModelList = CacheMongoDBUtil.createDeleteOneModelList(getClassConfig().primarySharedId,  key2ValuesList);
        MongoCollection<Document> collection = getCollection();
        BulkWriteResult writeResult = collection.bulkWrite(deleteOneModelList);
        return writeResult.wasAcknowledged();
    }

    public MongoCollection<Document> getCollection(){
        return MongoDBQueryUtil.getCollection(getClassConfig().tableName);
    }

    @Override
    public ICacheDelaySource<PK, K, V> createDelayUpdateSource(ICacheExecutor executor) {
        return new CacheDelayMongoDBSource<>(this, executor);
    }
}