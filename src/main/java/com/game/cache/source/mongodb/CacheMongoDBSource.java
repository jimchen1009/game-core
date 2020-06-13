package com.game.cache.source.mongodb;

import com.game.cache.CacheDaoUnique;
import com.game.cache.CacheInformation;
import com.game.cache.CacheType;
import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.annotation.CacheIndexes;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.CacheDbSource;
import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.ICacheSourceInteract;
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

public class CacheMongoDBSource<K, V extends IData<K>> extends CacheDbSource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheMongoDBSource.class);


    public CacheMongoDBSource(CacheDaoUnique cacheDaoUnique, IKeyValueBuilder<K> secondaryBuilder, ICacheSourceInteract cacheInteract) {
        super(cacheDaoUnique, secondaryBuilder, cacheInteract);
        MongoCollection<Document> collection = getCollection();
        CacheIndexes cacheIndexes = cacheDaoUnique.getInformation().getCacheIndexes();
        CacheMongoDBUtil.ensureIndexes(collection, getCacheDaoUnique().getPrimarySharedId(), cacheIndexes);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.MongoDb;
    }


    @Override
    public V get(long primaryKey, K secondaryKey) {
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createCombineUniqueKeyValue(primaryKey, secondaryKey);
        Map<String, Object> cacheValue = MongoDBQueryUtil.queryOne(getCollection(), getCacheDaoUnique().getPrimarySharedId(), entryList);
        return cacheValue == null ? null : converter.convert2Value(cacheValue);
    }

    @Override
    public List<V> getAll(long primaryKey) {
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        Collection<Map<String, Object>> cacheValuesList = MongoDBQueryUtil.queryAll(getCollection(), getCacheDaoUnique().getPrimarySharedId(), entryList);
        return converter.convert2ValueList(cacheValuesList);
    }

    @Override
    public CacheCollection getPrimaryCollection(long primaryKey) {
        int primarySharedId = getCacheDaoUnique().getPrimarySharedId();
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        Collection<Map<String, Object>> mapCollection = MongoDBQueryUtil.queryAll(getCollection(), primarySharedId, entryList);
        return new CacheCollection(primarySharedId, mapCollection, new CacheInformation());
    }

    @Override
    public Map<Integer, CacheCollection> getSharedCollections(long primaryKey, List<Integer> primarySharedIds) {
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
    public boolean replaceOne(long primaryKey, V value) {
        Map<String, Object> cacheValue = getConverter().convert2Cache(value);
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createCombineUniqueKeyValue(primaryKey, value.secondaryKey());
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(getCacheDaoUnique().getPrimarySharedId(),  entryList);
        Document document = CacheMongoDBUtil.toDocument(cacheValue.entrySet());
        MongoCollection<Document> collection = getCollection();
        UpdateResult updateOne = collection.updateOne(queryDocument, document, CacheMongoDBUtil.UPDATE_OPTIONS);
        return updateOne.wasAcknowledged();
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> values) {
        List<UpdateOneModel<Document>> updateOneModelList = values.stream().map(value -> {
            Map<String, Object> cacheValue = getConverter().convert2Cache(value);
            List<Map.Entry<String, Object>> primaryKeyValue = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
            List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createCombineUniqueKeyValue(primaryKey, value.secondaryKey());
            return CacheMongoDBUtil.createUpdateOneModel(getCacheDaoUnique().getPrimarySharedId(), primaryKeyValue, cacheValue.entrySet());
        }).collect(Collectors.toList());
        MongoCollection<Document> collection = getCollection();
        BulkWriteResult writeResult = collection.bulkWrite(updateOneModelList);
        return writeResult.wasAcknowledged();
    }

    @Override
    public boolean deleteOne(long primaryKey, K secondaryKey) {
        List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createCombineUniqueKeyValue(primaryKey, secondaryKey);
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(getCacheDaoUnique().getPrimarySharedId(),  entryList);
        MongoCollection<Document> collection = getCollection();
        DeleteResult deleteOne = collection.deleteOne(queryDocument);
        return deleteOne.wasAcknowledged();
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        List<List<Map.Entry<String, Object>>> key2ValuesList = new ArrayList<>();
        for (K secondaryKey : secondaryKeys) {
            List<Map.Entry<String, Object>> entryList = getKeyValueBuilder().createCombineUniqueKeyValue(primaryKey, secondaryKey);
            key2ValuesList.add(entryList);
        }
        List<DeleteOneModel<Document>> deleteOneModelList = CacheMongoDBUtil.createDeleteOneModelList(getCacheDaoUnique().getPrimarySharedId(),  key2ValuesList);
        MongoCollection<Document> collection = getCollection();
        BulkWriteResult writeResult = collection.bulkWrite(deleteOneModelList);
        return writeResult.wasAcknowledged();
    }

    public MongoCollection<Document> getCollection(){
        return MongoDBQueryUtil.getCollection(getCacheDaoUnique().getTableName());
    }

    @Override
    public ICacheDelaySource<K, V> createDelayUpdateSource(ICacheExecutor executor) {
        return new CacheDelayMongoDBSource<>(this, executor);
    }
}
