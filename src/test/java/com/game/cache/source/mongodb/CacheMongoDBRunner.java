package com.game.cache.source.mongodb;

import com.game.cache.data.DataSource;
import com.game.cache.key.KeyValueHelper;
import com.game.cache.mapper.mongodb.MongoDBConvertMapper;
import com.game.db.mongodb.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class CacheMongoDBRunner {

    @Test
    public void test(){

        MongoDatabase database = MongoDBManager.getInstance().getDb("demo");
        MongoCollection<Document> collection = database.getCollection("user");
//        collection.drop();
        long userId = 1L;
//        Document queryDocument = new Document("userId", new Document("$exists", false));
//        Document document = new Document("$set", new Document("item", Collections.emptyList()));
//        collection.updateMany(queryDocument, document);

//        collection.insertOne(new Document("userId", userId).append("item", Collections.emptyList()));

        CacheMongoDBSource<Long, Long, UserItem> cacheSource = new CacheMongoDBSource<>(UserItem.class, "item", KeyValueHelper.LongBuilder, KeyValueHelper.LongBuilder);
        MongoDBConvertMapper convertMapper = new MongoDBConvertMapper();
        DataSource<Long, Long, UserItem> dataSource = new DataSource<>(UserItem.class, convertMapper, cacheSource);
        UserItems userItems = new UserItems(userId, dataSource);
        Collection<UserItem> currentItemList = userItems.getAll();
        userItems.replaceBatch(currentItemList);
        ArrayList<UserItem> updateItemList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            UserItem userItem = new UserItem(userId, i, 10);
            updateItemList.add(userItem);
        }
        userItems.replaceBatch(updateItemList);
        for (UserItem userItem : updateItemList) {
            userItem.decCount(1);
        }
        userItems.replaceBatch(updateItemList);
        userItems.removeBatch(updateItemList.stream().map(UserItem::secondaryKey).collect(Collectors.toList()));
    }
}
