package com.game.core.cache.source.mongodb;

import com.game.core.cache.CacheKeyValue;
import com.game.core.cache.CacheType;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.CacheDbSource;
import com.game.core.cache.source.ICacheDelaySource;
import com.game.core.cache.source.executor.ICacheExecutor;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CacheMongoDbSource<K, V extends IData<K>> extends CacheDbSource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheMongoDbSource.class);

    public CacheMongoDbSource(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder, ICacheExecutor executor) {
        super(cacheUniqueId, secondaryBuilder, executor);
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        List<CacheKeyValue> entryList = getKeyValueBuilder().createCombineKeyValue(primaryKey, secondaryKey);
        Map<String, Object> cacheValue = MongoDbQueryUtil.queryOne(getCollection(), entryList);
        return cacheValue == null ? null : converter.convert2Data(cacheValue);
    }

    @Override
    public List<V> getAll(long primaryKey) {
        List<CacheKeyValue> entryList = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        Collection<Map<String, Object>> cacheValuesList = MongoDbQueryUtil.queryAll(getCollection(), entryList);
        return cacheValuesList.stream().map(converter::convert2Data).collect(Collectors.toList());
    }

    @Override
    public boolean replaceOne(long primaryKey, V data) {
        Map<String, Object> cacheValue = getConverter().convert2Cache(data);
        List<CacheKeyValue> entryList = getKeyValueBuilder().createCombineKeyValue(primaryKey, data.secondaryKey());
        Document queryDocument = CacheMongoDbUtil.getQueryDocument(entryList);
        Document document = CacheMongoDbUtil.toDocument(cacheValue.entrySet());
        MongoCollection<Document> collection = getCollection();
        UpdateResult updateOne = collection.updateOne(queryDocument, document, CacheMongoDbUtil.UPDATE_OPTIONS);
        return updateOne.wasAcknowledged();
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> dataList) {
        List<UpdateOneModel<Document>> updateOneModelList = dataList.stream().map(value -> {
            Map<String, Object> cacheValue = getConverter().convert2Cache(value);
            List<CacheKeyValue> entryList = getKeyValueBuilder().createCombineKeyValue(primaryKey, value.secondaryKey());
            return CacheMongoDbUtil.createUpdateOneModel(entryList, cacheValue.entrySet());
        }).collect(Collectors.toList());
        MongoCollection<Document> collection = getCollection();
        BulkWriteResult writeResult = collection.bulkWrite(updateOneModelList);
        return writeResult.wasAcknowledged();
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.MongoDb;
    }

    @Override
    public boolean deleteOne(long primaryKey, K secondaryKey) {
        List<CacheKeyValue> entryList = getKeyValueBuilder().createCombineKeyValue(primaryKey, secondaryKey);
        Document queryDocument = CacheMongoDbUtil.getQueryDocument(entryList);
        MongoCollection<Document> collection = getCollection();
        DeleteResult deleteOne = collection.deleteOne(queryDocument);
        return deleteOne.wasAcknowledged();
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        List<List<CacheKeyValue>> key2ValuesList = new ArrayList<>();
        for (K secondaryKey : secondaryKeys) {
            List<CacheKeyValue> entryList = getKeyValueBuilder().createCombineKeyValue(primaryKey, secondaryKey);
            key2ValuesList.add(entryList);
        }
        List<DeleteOneModel<Document>> deleteOneModelList = CacheMongoDbUtil.createDeleteOneModelList(key2ValuesList);
        MongoCollection<Document> collection = getCollection();
        BulkWriteResult writeResult = collection.bulkWrite(deleteOneModelList);
        return writeResult.wasAcknowledged();
    }

    public MongoCollection<Document> getCollection(){
        return MongoDbQueryUtil.getCollection(getCacheUniqueId().getName());
    }

    @Override
    public ICacheDelaySource<K, V> createDelayUpdateSource() {
        return new CacheDelayMongoDbSource<>(this);
    }
}
