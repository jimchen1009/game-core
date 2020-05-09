package com.game.cache.source.mongodb;

import com.game.cache.CollectionInfo;
import com.game.cache.data.Data;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.mapper.annotation.IndexField;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.CacheSource;
import com.game.common.arg.Args;
import com.game.db.mongodb.MongoDBManager;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CacheMongoDBSource<PK, K, V extends Data<K>> extends CacheSource<PK, K, V> {

    private static final String DB_NAME = "demo";
    private static final UpdateOptions UPDATE_OPTIONS = new UpdateOptions().upsert(true);

    private static final String ObjectId = "_id";

    private final String collectionName;

    public CacheMongoDBSource(Class<V> aClass, String collectionName, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
        super(aClass, primaryBuilder, secondaryBuilder);
        this.collectionName = collectionName;
        CacheIndex cacheIndexes = classDescription.getCacheIndexes();
        Document document = new Document();
        for (IndexField indexField : cacheIndexes.fields()) {
            document.append(indexField.name(), indexField.type().toIndexValue());
        }
        boolean unique = cacheIndexes.options().unique();
        getCollection().createIndex(document, new IndexOptions().unique(unique));
    }

    @Override
    protected Map<String, Object> get0(Map<String, Object> key2Values) {
        Document queryDocument = getQueryDocument(key2Values);
        MongoCollection<Document> collection = getCollection();
        return collection.find(queryDocument).first();
    }

    @Override
    protected Collection<Map<String, Object>> getAll0(Map<String, Object> key2Values) {
        Document queryDocument = getQueryDocument(key2Values);
        MongoCollection<Document> collection = getCollection();
        FindIterable<Document> iterable = collection.find(queryDocument);
        List<Map<String, Object>> documentList = new ArrayList<>();
        for (Document document : iterable) {
            documentList.add(document);
        }
        return documentList;
    }

    @Override
    protected CacheCollection getCollection0(Map<String, Object> key2Values) {
        Collection<Map<String, Object>> mapCollection = getAll0(key2Values);
        return new CacheCollection(mapCollection, new CollectionInfo());
    }

    @Override
    protected boolean replaceOne0(Map<String, Object> key2Values, Map<String, Object> cacheValue) {
        Document queryDocument = getQueryDocument(key2Values);
        Document document = toDocument(cacheValue);
        MongoCollection<Document> collection = getCollection();
        UpdateResult updateResult = collection.updateOne(queryDocument, document, UPDATE_OPTIONS);
        return updateResult.wasAcknowledged();
    }

    @Override
    protected boolean replaceBatch0(Map<String, Object> key2Values, List<Args.Two<Map<String, Object>, Map<String, Object>>> keyCacheValuesList) {
        List<UpdateManyModel<Document>> updateManyModelList = keyCacheValuesList.stream().map(tuple -> {
            Document queryDocument = getQueryDocument(tuple.arg0);
            Document document = toDocument(tuple.arg1);
            return new UpdateManyModel<Document>(queryDocument, document, UPDATE_OPTIONS);
        }).collect(Collectors.toList());
        MongoCollection<Document> collection = getCollection();
        BulkWriteResult writeResult = collection.bulkWrite(updateManyModelList);
        return writeResult.wasAcknowledged();
    }

    @Override
    protected boolean deleteOne0(Map<String, Object> key2Values) {
        Document queryDocument = getQueryDocument(key2Values);
        MongoCollection<Document> collection = getCollection();
        DeleteResult deleteResult = collection.deleteOne(queryDocument);
        return deleteResult.wasAcknowledged();
    }

    @Override
    protected boolean deleteBatch0(List<Map<String, Object>> key2ValuesList) {
        List<DeleteOneModel<Document>> deleteOneModelList = key2ValuesList.stream().map(key2Values -> {
            Document document = getQueryDocument(key2Values);
            return new DeleteOneModel<Document>(document);
        }).collect(Collectors.toList());
        MongoCollection<Document> collection = getCollection();
        BulkWriteResult writeResult = collection.bulkWrite(deleteOneModelList);
        return writeResult.wasAcknowledged();
    }

    private MongoCollection<Document> getCollection(){
        MongoDatabase database = MongoDBManager.getInstance().getDb(DB_NAME);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection;
    }


    private Document getQueryDocument(Map<String, Object> key2Values){
        Document document = new Document(key2Values);
        return document;
    }

    private Document toDocument(Map<String, Object> cacheValue){
        Document document = new Document(cacheValue);
        return new Document("$set", document);
    }
}
