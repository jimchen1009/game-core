package com.game.core.cache.source.mongodb;

import com.game.common.config.EvnCoreConfigs;
import com.game.common.config.EvnCoreType;
import com.game.common.config.IEvnConfig;
import com.game.core.db.mongodb.MongoDbManager;
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

    public static Map<String, Object> queryOne(MongoCollection<Document> collection, int primaryKeyId, List<Map.Entry<String, Object>> keyValue) {
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(primaryKeyId, keyValue);
        return collection.find(queryDocument).first();
    }

    public static Collection<Map<String, Object>> queryAll(MongoCollection<Document> collection, int primarySharedId, List<Map.Entry<String, Object>> keyValue) {
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(primarySharedId, keyValue);
        return queryAll(collection, queryDocument);
    }

    public static Collection<Map<String, Object>> queryAll(MongoCollection<Document> collection, List<Integer> primarySharedIds, List<Map.Entry<String, Object>> keyValue) {
        Document queryDocument = CacheMongoDBUtil.getQueryDocument(primarySharedIds, keyValue);
        return queryAll(collection, queryDocument);
    }

    private static Collection<Map<String, Object>> queryAll(MongoCollection<Document> collection,  Document queryDocument) {
        FindIterable<Document> iterable = collection.find(queryDocument);
        List<Map<String, Object>> documentList = new ArrayList<>();
        for (Document document : iterable) {
            documentList.add(document);
        }
        return documentList;
    }

    public static MongoDatabase getDbDatabase(){
        IEvnConfig config = EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getConfig("mongodb");
        String name = config.getString("name");
        String dbName = config.getString("db");
        MongoDbManager dbManager = MongoDbManager.get(name);
        return dbManager.getDb(dbName);
    }

    public static MongoCollection<Document> getCollection(String name){
        MongoDatabase dbDatabase = getDbDatabase();
        return dbDatabase.getCollection(name);
    }
}
