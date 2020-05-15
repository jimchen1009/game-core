package com.game.cache.source.mongodb;

import com.game.db.mongodb.MongoDBManager;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 操作日志：
 * db.system.profile.find( { op: { $ne : 'command' }, 'appName':'cache' } ).limit(50).sort( { ts : -1 } ).pretty()
 *
 */
public class MongoDBQueryUtil {

    public static final UpdateOptions UPDATE_OPTIONS = new UpdateOptions().upsert(true);
    public static final String DB_NAME = "demo";


    public static Map<String, Object> queryOne(MongoCollection<Document> collection, int primaryKeyId, Map<String, Object> keyValue) {
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(primaryKeyId, keyValue);
        return collection.find(queryDocument).first();
    }

    public static Collection<Map<String, Object>> queryAll(MongoCollection<Document> collection, int primaryKeyId, Map<String, Object> keyValue) {
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(primaryKeyId, keyValue);
        FindIterable<Document> iterable = collection.find(queryDocument);
        List<Map<String, Object>> documentList = new ArrayList<>();
        for (Document document : iterable) {
            documentList.add(document);
        }
        return documentList;
    }

    public static MongoCollection<Document> getCollection(String name){
        MongoDatabase database = MongoDBManager.getInstance().getDb(DB_NAME);
        return database.getCollection(name);
    }

}
