package com.game.cache.source.mongodb;

import com.game.cache.CacheInformation;
import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.CacheDirectUpdateSource;
import com.game.cache.source.ICacheDelayUpdateSource;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CacheDirectMongoDBSource<PK, K, V extends IData<K>> extends CacheDirectUpdateSource<PK, K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheDirectMongoDBSource.class);


    public CacheDirectMongoDBSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
        super(aClass, primaryBuilder, secondaryBuilder);
        MongoCollection<Document> collection = getCollection();
        CacheIndex cacheIndexes = getKeyValueBuilder().getClsDescription().getCacheIndexes();
        CacheMongoDBUtil.ensureIndexes(collection, getPrimarySharedId(), cacheIndexes);
    }

    @Override
    protected Map<String, Object> get0(Map<String, Object> key2Values) {
        return MongoDBQueryUtil.queryOne(getCollection(), getPrimarySharedId(), key2Values);
    }

    @Override
    protected Collection<Map<String, Object>> getAll0(Map<String, Object> key2Values) {
        return MongoDBQueryUtil.queryAll(getCollection(), getPrimarySharedId(), key2Values);
    }

    @Override
    protected CacheCollection getCollection0(Map<String, Object> key2Values) {
        Collection<Map<String, Object>> mapCollection = getAll0(key2Values);
        return new CacheCollection(mapCollection, new CacheInformation());
    }

    @Override
    protected boolean replaceOne0(Map<String, Object> key2Values, KeyCacheValue<K> keyCacheValue) {
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(getPrimarySharedId(), key2Values);
        Document document = CacheMongoDBUtil.toDocument(keyCacheValue.getCacheValue());
        MongoCollection<Document> collection = getCollection();
        UpdateResult updateOne = collection.updateOne(queryDocument, document, CacheMongoDBUtil.UPDATE_OPTIONS);
        return updateOne.wasAcknowledged();
    }

    @Override
    protected boolean replaceBatch0(Map<String, Object> key2Values, List<KeyCacheValue<K>> keyCacheValueList) {
        List<UpdateOneModel<Document>> updateOneModelList = keyCacheValueList.stream().map( keyCacheValue -> {
            Map<String, Object> keyValue = getKeyValueBuilder().createPrimarySecondaryKeyValue(keyCacheValue.getCacheValue());
            return CacheMongoDBUtil.createUpdateOneModel(getPrimarySharedId(), keyValue, keyCacheValue.getCacheValue());
        }).collect(Collectors.toList());
        MongoCollection<Document> collection = getCollection();
        BulkWriteResult writeResult = collection.bulkWrite(updateOneModelList);
        return writeResult.wasAcknowledged();
    }

    @Override
    protected boolean deleteOne0(Map<String, Object> key2Values) {
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(getPrimarySharedId(), key2Values);
        MongoCollection<Document> collection = getCollection();
        DeleteResult deleteOne = collection.deleteOne(queryDocument);
        return deleteOne.wasAcknowledged();
    }

    @Override
    protected boolean deleteBatch0(List<Map<String, Object>> key2ValuesList) {
        List<DeleteOneModel<Document>> deleteOneModelList = CacheMongoDBUtil.createDeleteOneModelList(getPrimarySharedId(), key2ValuesList);
        MongoCollection<Document> collection = getCollection();
        BulkWriteResult writeResult = collection.bulkWrite(deleteOneModelList);
        return writeResult.wasAcknowledged();
    }

    public MongoCollection<Document> getCollection(){
        return MongoDBQueryUtil.getCollection(getCacheName());
    }

    @Override
    public ICacheDelayUpdateSource<PK, K, V> createDelayUpdateSource(ICacheExecutor executor) {
        return new CacheDelayMongoDBSource<>(this, executor);
    }
}
