package com.game.core.cache.source.mongodb;

import com.game.core.cache.CacheKeyValue;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作日志：
 * db.system.profile.find( { op: { $ne : 'command' }, 'appName':'cache' } ).limit(50).sort( { ts : -1 } ).pretty()
 *
 */
public class CacheMongoDbUtil {

    public static final UpdateOptions UPDATE_OPTIONS = new UpdateOptions().upsert(true);
    public static final String DB_NAME = "demo";

    public static UpdateOneModel<Document> createUpdateOneModel(Collection<CacheKeyValue> keyValue, Collection<Map.Entry<String, Object>> cache2Values) {
        Document queryDocument = getQueryDocument(keyValue);
        Document document = toDocument(cache2Values);
        return new UpdateOneModel<>(queryDocument, document, UPDATE_OPTIONS);
    }

    public static DeleteOneModel<Document> createDeleteOneModel(List<CacheKeyValue> keyValue) {
        Document document = getQueryDocument(keyValue);
        return new DeleteOneModel<>(document);
    }

    public static List<DeleteOneModel<Document>> createDeleteOneModelList(Collection<List<CacheKeyValue>> key2ValuesList) {
        return key2ValuesList.stream().map(CacheMongoDbUtil::createDeleteOneModel).collect(Collectors.toList());
    }

    public static Document getQueryDocument(Collection<CacheKeyValue>  keyValue){
        Document document = new Document();
        for (CacheKeyValue entry : keyValue) {
            document.append(entry.getKey(), entry.getValue());
        }
        return document;
    }

    public static Document toDocument(Collection<Map.Entry<String, Object>> cacheValue){
        Document document = new Document();
        for (Map.Entry<String, Object> entry : cacheValue) {
            document.put(entry.getKey(), entry.getValue());
        }
        return new Document("$set", document);
    }
}
