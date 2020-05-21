package com.game.cache.source.mongodb;

import com.game.cache.InformationName;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.mapper.annotation.IndexField;
import com.game.cache.mapper.annotation.IndexType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作日志：
 * db.system.profile.find( { op: { $ne : 'command' }, 'appName':'cache' } ).limit(50).sort( { ts : -1 } ).pretty()
 *
 */
public class CacheMongoDBUtil {

    public static final UpdateOptions UPDATE_OPTIONS = new UpdateOptions().upsert(true);
    public static final String DB_NAME = "demo";

    public static void ensureIndexes(MongoCollection<Document> collection, int primarySharedId, CacheIndex cacheIndex) {
        createHashIndex(collection, cacheIndex);
        createUniqueIndex(collection, primarySharedId, cacheIndex);
    }

    private static void createUniqueIndex(MongoCollection<Document> collection, int primarySharedId, CacheIndex cacheIndex){
        Document primaryDocument = new Document();
        Document secondaryDocument = new Document();
        for (IndexField indexField : cacheIndex.fields()) {
            if (indexField.isPrimary()){
                /**
                 * 设置成哈希索引会报错~
                 * primaryDocument.append(indexField.name(), IndexType.HASHED.toIndexValue());
                 * The full response is {"raw": {"rs3/127.0.0.1:27029,127.0.0.1:27030,127.0.0.1:27031": {"ok": 0.0, "errmsg": "Caught exception during index builder initialization demo.material (fc2bfbd7-bc57-45cb-a1dd-dad9e51b1746): 1 provided. First index spec: { v: 2, key: { userId: \"hashed\", k1: 1, itemUniqueId: 1 }, name: \"userId_hashed_k1_1_itemUniqueId_1\", ns: \"demo.material\", unique: true, partialFilterExpression: { itemUniqueId: { $exists: true } } }", "code": 16763, "codeName": "Location16763"}}, "code": 16763, "codeName": "Location16763", "ok": 0.0, "errmsg": "Caught exception during index builder initialization demo.material (fc2bfbd7-bc57-45cb-a1dd-dad9e51b1746): 1 provided. First index spec: { v: 2, key: { userId: \"hashed\", k1: 1, itemUniqueId: 1 }, name: \"userId_hashed_k1_1_itemUniqueId_1\", ns: \"demo.material\", unique: true, partialFilterExpression: { itemUniqueId: { $exists: true } } }", "operationTime": {"$timestamp": {"t": 1589426882, "i": 1}}, "$clusterTime": {"clusterTime": {"$timestamp": {"t": 1589426883, "i": 3}}, "signature": {"hash": {"$binary": "AAAAAAAAAAAAAAAAAAAAAAAAAAA=", "$type": "00"}, "keyId": {"$numberLong": "0"}}}}
                 */
                primaryDocument.append(indexField.name(), indexField.type().toIndexValue());
            }
            else {
                secondaryDocument.append(indexField.name(), indexField.type().toIndexValue());
            }
        }
        IndexOptions indexOptions = new IndexOptions().unique(cacheIndex.options().unique());
        Document appendDocument = new Document();
        if (primarySharedId > 0){
            Document partialDocument = new Document();
            for (Map.Entry<String, Object> entry : secondaryDocument.entrySet()) {
                partialDocument.append(entry.getKey(), new Document("$exists", true));
            }
            indexOptions.partialFilterExpression(partialDocument);
            appendDocument.append(InformationName.CACHE_KEY.getKeyName(), IndexType.ASC.toIndexValue());
        }
        Document document = new Document(primaryDocument);
        document.putAll(appendDocument);
        document.putAll(secondaryDocument);
        collection.createIndex(document, indexOptions);
    }

    private static void createHashIndex(MongoCollection<Document> collection, CacheIndex cacheIndex){
        Document hashDocument = new Document();
        for (IndexField indexField : cacheIndex.fields()) {
            if (indexField.isPrimary()){
                hashDocument.append(indexField.name(), IndexType.HASHED.toIndexValue());
            }
        }
        collection.createIndex(hashDocument, new IndexOptions());
    }

    public static UpdateOneModel<Document> createUpdateOneModel(int primarySharedId, Map<String, Object> keyValue, Map<String, Object> cache2Values) {
        Document queryDocument = getQueryDocument(primarySharedId, keyValue);
        Document document = toDocument(cache2Values);
        return new UpdateOneModel<>(queryDocument, document, UPDATE_OPTIONS);
    }

    public static DeleteOneModel<Document> createDeleteOneModel(int primarySharedId, Map<String, Object> keyValue) {
        Document document = getQueryDocument(primarySharedId, keyValue);
        return new DeleteOneModel<>(document);
    }

    public static List<DeleteOneModel<Document>> createDeleteOneModelList(int primarySharedId, List<Map<String, Object>> key2ValuesList) {
        return key2ValuesList.stream().map(keyValue-> createDeleteOneModel(primarySharedId, keyValue)).collect(Collectors.toList());
    }

    public static Document getQueryDocument(int primarySharedId, Map<String, Object> keyValue){
        Document document = new Document(keyValue);
        if (primarySharedId > 0){
            document.append(InformationName.CACHE_KEY.getKeyName(),primarySharedId);
        }
        return document;
    }

    public static Document getQueryDocument(List<Integer> primarySharedIds, Map<String, Object> keyValue){
        Document document = new Document(keyValue);
        primarySharedIds = primarySharedIds.stream().filter( primarySharedId -> primarySharedId > 0).collect(Collectors.toList());
        if (primarySharedIds.size() == 1){
            document.append(InformationName.CACHE_KEY.getKeyName(), primarySharedIds.get(0));
        }
        else {
            document.append(InformationName.CACHE_KEY.getKeyName(), new Document("$in", primarySharedIds));
        }
        return document;
    }

    public static Document toDocument(Map<String, Object> cacheValue){
        Document document = new Document();
        for (Map.Entry<String, Object> entry : cacheValue.entrySet()) {
            if (InformationName.Names.contains(entry.getKey())) {
                continue;
            }
            document.put(entry.getKey(), entry.getValue());
        }
        return new Document("$set", document);
    }
}
